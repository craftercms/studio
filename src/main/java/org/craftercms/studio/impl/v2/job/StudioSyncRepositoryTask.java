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
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.site.SyncFromRepoEvent;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;

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
                applicationContext.publishEvent(new SyncFromRepoEvent(site));
            }
        } catch (Exception e) {
            logger.error("Failed to sync the database from the repository in site '{}'", site, e);
        }
    }

    @EventListener
    protected void syncRepoListener(SyncFromRepoEvent event) throws ServiceLayerException {
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

            String currentLastProcessedCommit = lastProcessedCommit;
            String lastUnprocessedCommit = null;
            for (String commitId : unprocessedCommits) {
                if (auditServiceInternal.isAudited(site.getId(), commitId)) {
                    if (lastUnprocessedCommit != null) {
                        ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                        lastUnprocessedCommit = null;
                    }
                    updateLastCommitId(siteId, commitId);
                    currentLastProcessedCommit = commitId;
                } else {
                    lastUnprocessedCommit = commitId;
                }
            }
            if (lastUnprocessedCommit != null) {
                ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                updateLastCommitId(siteId, lastUnprocessedCommit);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    private void updateLastCommitId(String siteId, String commitId) {
        retryingDatabaseOperationFacade.retry(() -> siteDao.updateLastCommitId(siteId, commitId));
    }

    private void ingestChanges(final Site site, final String commitFrom, final String commitTo) {
        // This should compare the given commit id to its parent (or empty tree if no parent)
        // Then ingest the changes into the item table
        List<RepoOperation> operationsFromDelta = contentRepository.getOperationsFromDelta(site.getSiteId(), commitFrom, commitTo);
        // TODO: Process operations
        auditChangesFromGit(site, commitFrom, commitTo);;
    }

    /**
     * Creates an audit log entry indicating the sync of git history up from commitFrom up to commitTo <br/>
     * It will add as audit log parameters any commit between commitFrom and commitTo.
     *
     * @param site       The site being synced
     * @param commitFrom The last previously synced commit id
     * @param commitTo   The new synced commit id
     */
    private void auditChangesFromGit(final Site site, final String commitFrom, final String commitTo) {
        AuditLog auditLogEntry = auditServiceInternal.createAuditLogEntry();
        auditLogEntry.setSiteId(site.getId());
        auditLogEntry.setOperation(OPERATION_GIT_CHANGES);
        auditLogEntry.setOrigin(ORIGIN_GIT);
        auditLogEntry.setCommitId(commitTo);
        auditLogEntry.setActorId(ACTOR_ID_GIT);
        auditLogEntry.setActorDetails(ACTOR_ID_GIT);
        auditLogEntry.setPrimaryTargetId(site.getSiteId());
        auditLogEntry.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLogEntry.setPrimaryTargetValue(site.getName());

        try {
            List<String> commitIds = contentRepository.getIntroducedCommits(site.getSiteId(), commitFrom, commitTo);
            List<AuditLogParameter> auditParameters = new ArrayList<>();
            for (String commitId : commitIds) {
                AuditLogParameter auditParameter = new AuditLogParameter();
                auditParameter.setTargetId(commitId);
                auditParameter.setTargetType(TARGET_TYPE_SYNCED_COMMIT);
                auditParameter.setTargetValue(commitId);
                auditParameters.add(auditParameter);
            }
            auditLogEntry.setParameters(auditParameters);
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to calculate introduced commits for site '{}' from commit '{}' to commit '{}'",
                    site.getSiteId(), commitFrom, commitTo, e);
        }

        auditServiceInternal.insertAuditLog(auditLogEntry);
    }

}
