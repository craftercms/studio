/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.event.site.RepoSyncEvent;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections.ListUtils.subtract;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;

public class StudioSyncRepositoryTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioSyncRepositoryTask.class);
    private static int threadCounter = 0;

    private final SitesService sitesService;
    private final GeneralLockService generalLockService;
    private final AuditServiceInternal auditServiceInternal;
    private final SiteDAO siteDao;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    public void init() {
        threadCounter++;
    }

    @ConstructorProperties({"sitesService", "generalLockService",
            "auditServiceInternal", "siteDao",
            "retryingDatabaseOperationFacade"})
    public StudioSyncRepositoryTask(SitesService sitesService, GeneralLockService generalLockService,
                                    AuditServiceInternal auditServiceInternal, SiteDAO siteDao,
                                    RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.sitesService = sitesService;
        this.generalLockService = generalLockService;
        this.auditServiceInternal = auditServiceInternal;
        this.siteDao = siteDao;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    @Override
    protected void executeInternal(String site) {
        try {
            logger.debug("Execute sync repository thread counter '{}' ID '{}'", threadCounter,
                    Thread.currentThread().getId());
            String siteState = siteService.getSiteState(site);
            if (StringUtils.equals(siteState, STATE_READY)) {
                applicationContext.publishEvent(new RepoSyncEvent(site));
            }
        } catch (Exception e) {
            logger.error("Failed to sync the database from the repository in site '{}'", site, e);
        }
    }

    @EventListener
    protected void syncRepoListener(RepoSyncEvent event) throws ServiceLayerException {
        syncRepository(event.getSiteId());
    }

    private void syncRepository(final String siteId) throws ServiceLayerException {
        logger.debug("Sync the database with the repository in site '{}'", siteId);

        Site site = siteDao.getSite(siteId);
        if (!sitesService.checkSiteUuid(siteId, site.getSiteUuid())) {
            return;
        }
        String gitLockKey = StudioUtils.getSandboxRepoLockKey(siteId);
        generalLockService.lock(gitLockKey);
        try {
            // Get the last commit to be used along the sync process (instead of 'HEAD',
            // commits added after this point will be processed in subsequent executions of this method)
            final String lastCommitInRepo = contentRepository.getRepoLastCommitId(siteId);
            final String lastProcessedCommit = siteDao.getLastCommitId(siteId);
            if (StringUtils.equals(lastCommitInRepo, lastProcessedCommit)) {
                return;
            }
            // Some of these (the ones created by Studio APIs) will already be in the audit table
            List<String> unprocessedCommits;
            try {
                unprocessedCommits = contentRepository.getCommitIdsBetween(siteId, lastProcessedCommit, lastCommitInRepo);
            } catch (IOException e) {
                throw new ServiceLayerException(format("Failed to get unprocessed commits to sync repository for site '%s'", siteId), e);
            }
            // Change getAuditedCommitsAfter to get take a list of commits and return the ones that are already audited
            List<String> auditedCommits = auditServiceInternal.getAuditedCommitsAfter(siteId, lastProcessedCommit);
            if (isEmpty(subtract(unprocessedCommits, auditedCommits))) {
                // This means all commits after lastProcessedCommit were created by Studio
                updateLastCommitId(siteId, lastCommitInRepo);
                return;
            }

            String currentLastProcessedCommit = lastProcessedCommit;
            String lastUnprocessedCommit = null;
            for (String commitId : unprocessedCommits) {
                if (auditedCommits.contains(commitId)) {
                    if (lastUnprocessedCommit != null) {
                        ingestChanges(siteId, currentLastProcessedCommit, lastUnprocessedCommit);
                        lastUnprocessedCommit = null;
                    }
                    updateLastCommitId(siteId, commitId);
                    currentLastProcessedCommit = commitId;
                } else {
                    lastUnprocessedCommit = commitId;
                }
            }
            if (lastUnprocessedCommit != null) {
                ingestChanges(siteId, currentLastProcessedCommit, lastUnprocessedCommit);
                updateLastCommitId(siteId, lastUnprocessedCommit);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    private void updateLastCommitId(String siteId, String commitId) {
        retryingDatabaseOperationFacade.retry(() -> siteDao.updateLastCommitId(siteId, commitId));
    }

    private void ingestChanges(final String siteId, final String commitFrom, final String commitTo) {
        // This should compare the given commit id to its parent (or empty tree if no parent)
        // Then ingest the changes into the item table
        List<RepoOperation> operationsFromDelta = contentRepository.getOperationsFromDelta(siteId, commitFrom, commitTo);
        // TODO: Process operations
        auditChangesFromGit(siteId, commitFrom, commitTo);
    }

    private void auditChangesFromGit(String siteId, String commitFrom, String commitTo) {
        // TODO: Audit changes
        // Store an audit entry for commitTo, indicating that the commit (and changes introduced by it)
        // was not created by studio
        // This method should check if commitTo is a merge, and if so, store the list of commits introduced by the merge
        // as the audit parameters
        // The list of commits should correspond to the list of commits between commitTo and the base of commitFrom & commitTo
        // following the path of the commitTo second parent
    }

}
