/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal;

import org.craftercms.studio.api.v1.dal.PublishRequest;

import java.util.Map;

public interface RetryingOperationFacade {

    /**
     * Mark all git logs as processed if they are inserted before marker
     * @param siteId site identifier
     * @param marker marker git commit
     * @param processed value for processed
     * @param unprocessed value for unprocessed
     */
    void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed, int unprocessed);

    /**
     * Mark git log as audited
     *
     * @param siteId site identifier
     * @param commitId git log commit id
     * @param audited audited flag value
     */
    void markGitLogAudited(String siteId, String commitId, int audited);

    /**
     * Add given remote repository for given cluster node
     * @param clusterId cluster node identifier
     * @param remoteRepositoryId remote repository identifier
     */
    void addClusterRemoteRepository(long clusterId, long remoteRepositoryId);

    /**
     * Delete all dependencies for site (delete site sub tusk)
     * @param params SQL query parameters
     */
    void deleteDependenciesForSite(Map params);

    /**
     * Delete all deployment data for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteDeploymentDataForSite(Map params);

    /**
     * Delete all dependencies for source path
     * @param params SQL query parameters
     */
    void deleteAllSourceDependencies(Map params);

    /**
     * Insert dependencies list
     * @param params SQL query parameters
     */
    void insertDependenciesList(Map params);

    /**
     * Cancel workflow bulk
     * @param params SQL query parameters
     */
    void cancelWorkflowBulk(Map params);

    /**
     * Mark publish requests completed
     * @param publishRequest publish request item
     */
    void markPublishRequestCompleted(PublishRequest publishRequest);

    /**
     * Set system processing for items
     * @param params SQL query parameters
     */
    void setSystemProcessingBySiteAndPathBulk(Map params);

    /**
     * Update site last commit id
     * @param params SQL query parameters
     */
    void updateSiteLastCommitId(Map params);

    /**
     * Update local last git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateClusterNodeLastCommitId(long clusterNodeId, long siteId, String commitId);

    /**
     * Update site's last verified git log commit id
     * @param params SQL query parameters
     */
    void updateSiteLastVerifiedGitlogCommitId(Map params);

    /**
     * Update local last verified git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateClusterNodeLastVerifiedGitlogCommitId(long clusterNodeId, long siteId, String commitId);

    /**
     * Update site last synced Git log commit id
     * @param params SQL query parameters
     */
    void updateSiteLastSyncedGitlogCommitId(Map params);

    /**
     * Update cluster node last synced git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateClusterNodeLastSyncedGitlogCommitId(long clusterNodeId, long siteId, String commitId);

    /**
     * Mark git log processed
     * @param params SQL query parameters
     */
    void markGitLogProcessed(Map params);
}
