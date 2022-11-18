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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.FilenameUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.PREVIOUS_COMMIT_SUFFIX;

public class StudioAuditLogProcessingTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioAuditLogProcessingTask.class);
    private AuditServiceInternal auditServiceInternal;
    private int batchSizeGitLog = 1000;
    private int batchSizeAudited = 100;
    private ContentService contentService;

    @Override
    protected void executeInternal(String site) {
        try {
            String siteState = siteService.getSiteState(site);
            if (StringUtils.equals(siteState, STATE_READY)) {
                processAuditLog(site);
            }
        } catch (Exception e) {
            logger.error("Failed to process the audit log in site '{}'", site, e);
        }
    }

    private void processAuditLog(String site) throws SiteNotFoundException {
        logger.trace("Get the last verified commit for site '{}'", site);
        SiteFeed siteFeed = siteService.getSite(site);
        if (checkSiteUuid(site, siteFeed.getSiteUuid())) {
            String lastSyncedCommit = siteService.getLastSyncedGitlogCommitId(site);
            if (StringUtils.isNotEmpty(lastSyncedCommit)) {
                logger.debug("Update the GitLog table for site '{}' starting from the last synced commit ID '{}'",
                        site, lastSyncedCommit);
                contentRepository.updateGitlog(site, lastSyncedCommit, batchSizeGitLog);
                processAuditLogFromRepo(site, batchSizeAudited);
            }
        }
    }

    private void processAuditLogFromRepo(String siteId, int batchSize) throws SiteNotFoundException {
        List<GitLog> unauditedGitlogs = contentRepository.getUnauditedCommits(siteId, batchSize);
        if (unauditedGitlogs != null) {
            SiteFeed siteFeed = siteService.getSite(siteId);
            for (GitLog gl : unauditedGitlogs) {
                if (contentRepository.commitIdExists(siteId, gl.getCommitId())) {
                    String prevCommitId = gl.getCommitId() + PREVIOUS_COMMIT_SUFFIX;
                    List<RepoOperation> operations = contentRepository.getOperationsFromDelta(siteId, prevCommitId,
                            gl.getCommitId());
                    for (RepoOperation repoOperation : operations) {
                        if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getMoveToPath())) ||
                                ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
                            continue;
                        }
                        Map<String, String> activityInfo = new HashMap<>();
                        String contentClass;
                        AuditLog auditLog;
                        switch (repoOperation.getAction()) {
                            case CREATE:
                            case COPY:
                                contentClass = contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath());
                                if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                                    activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                                }
                                logger.debug("Add an audit log entry in site '{}' for path '{}'",
                                        siteId, repoOperation.getPath());
                                auditLog = auditServiceInternal.createAuditLogEntry();
                                auditLog.setOperation(OPERATION_CREATE);
                                auditLog.setOperationTimestamp(repoOperation.getDateTime());
                                auditLog.setSiteId(siteFeed.getId());
                                auditLog.setActorId(repoOperation.getAuthor());
                                auditLog.setActorDetails(repoOperation.getAuthor());
                                auditLog.setPrimaryTargetId(siteId + ":" + repoOperation.getPath());
                                auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
                                auditLog.setPrimaryTargetValue(repoOperation.getPath());
                                auditLog.setPrimaryTargetSubtype(contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath()));
                                auditLog.setOrigin(ORIGIN_GIT);
                                auditServiceInternal.insertAuditLog(auditLog);

                                break;

                            case UPDATE:
                                contentClass = contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath());
                                if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                                    activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                                }
                                logger.debug("Add an audit log entry in site '{}' for path '{}'",
                                        siteId, repoOperation.getPath());
                                auditLog = auditServiceInternal.createAuditLogEntry();
                                auditLog.setOperation(OPERATION_UPDATE);
                                auditLog.setOperationTimestamp(repoOperation.getDateTime());
                                auditLog.setSiteId(siteFeed.getId());
                                auditLog.setActorId(repoOperation.getAuthor());
                                auditLog.setActorDetails(repoOperation.getAuthor());
                                auditLog.setOrigin(ORIGIN_GIT);
                                auditLog.setPrimaryTargetId(siteId + ":" + repoOperation.getPath());
                                auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
                                auditLog.setPrimaryTargetValue(repoOperation.getPath());
                                auditLog.setPrimaryTargetSubtype(contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath()));
                                auditServiceInternal.insertAuditLog(auditLog);
                                break;

                            case DELETE:
                                contentClass = contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath());
                                if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                                    activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                                }
                                logger.debug("Add an audit log entry in site '{}' for path '{}'",
                                        siteId, repoOperation.getPath());
                                auditLog = auditServiceInternal.createAuditLogEntry();
                                auditLog.setOperation(OPERATION_DELETE);
                                auditLog.setOperationTimestamp(repoOperation.getDateTime());
                                auditLog.setSiteId(siteFeed.getId());
                                auditLog.setOrigin(ORIGIN_GIT);
                                auditLog.setActorId(repoOperation.getAuthor());
                                auditLog.setActorDetails(repoOperation.getAuthor());
                                auditLog.setPrimaryTargetId(siteId + ":" + repoOperation.getPath());
                                auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
                                auditLog.setPrimaryTargetValue(repoOperation.getPath());
                                auditLog.setPrimaryTargetSubtype(contentService.getContentTypeClass(siteId,
                                        repoOperation.getPath()));
                                auditServiceInternal.insertAuditLog(auditLog);

                                break;

                            case MOVE:
                                contentClass = contentService.getContentTypeClass(siteId,
                                        repoOperation.getMoveToPath());
                                if (repoOperation.getMoveToPath().endsWith(DmConstants.XML_PATTERN)) {
                                    activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                                }
                                logger.debug("Add an audit log entry in site '{}' for path '{}'",
                                        siteId, repoOperation.getMoveToPath());
                                auditLog = auditServiceInternal.createAuditLogEntry();
                                auditLog.setOperation(OPERATION_MOVE);
                                auditLog.setOperationTimestamp(repoOperation.getDateTime());
                                auditLog.setSiteId(siteFeed.getId());
                                auditLog.setActorId(repoOperation.getAuthor());
                                auditLog.setActorDetails(repoOperation.getAuthor());
                                auditLog.setOrigin(ORIGIN_GIT);
                                auditLog.setPrimaryTargetId(siteId + ":" + repoOperation.getMoveToPath());
                                auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
                                auditLog.setPrimaryTargetValue(repoOperation.getMoveToPath());
                                auditLog.setPrimaryTargetSubtype(contentService.getContentTypeClass(siteId,
                                        repoOperation.getMoveToPath()));
                                auditServiceInternal.insertAuditLog(auditLog);

                                break;

                            default:
                                logger.error("Failed to add an audit entry in site '{}' for the unknown " +
                                                "repo operation '{}'",
                                        siteId, repoOperation.getAction());
                                break;
                        }
                    }
                }
                contentRepository.markGitLogAudited(siteId, gl.getCommitId());
            }
        }
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setBatchSizeGitLog(int batchSizeGitLog) {
        this.batchSizeGitLog = batchSizeGitLog;
    }

    public void setBatchSizeAudited(int batchSizeAudited) {
        this.batchSizeAudited = batchSizeAudited;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
