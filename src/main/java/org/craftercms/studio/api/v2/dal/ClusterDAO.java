/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_NODE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.COMMIT_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NODE_LAST_COMMIT_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NODE_LAST_VERIFIED_GITLOG_COMMIT_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.REMOTE_REPOSITORY_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATE;

public interface ClusterDAO {

    /**
     * Get all cluster members from database
     *
     * @return List of cluster members
     */
    List<ClusterMember> getAllMembers();

    /**
     * Get number of active cluster members from database
     *
     * @return number of active cluster members
     */
    int countActiveMembers(Map params);

    /**
     * Get other cluster members from database - different from member executing query
     *
     * @return List of cluster members
     */
    List<ClusterMember> getOtherMembers(Map params);

    /**
     * Update cluster member in the database
     *
     * @param member Cluster member to update
     *
     * @return number of affected rows
     */
    int updateMember(ClusterMember member);

    /**
     * Add member to cluster in the database
     *
     * @param member Member to add
     *
     * @return number of affected rows
     */
    int addMember(ClusterMember member);

    /**
     * Remove members from cluster in the database
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeMembers(Map params);

    /**
     * Get cluster member by id from database
     *
     * @param clusterMemberId Cluster member id
     *
     * @return Cluster member with given id
     */
    ClusterMember getMemberById(long clusterMemberId);

    /**
     * Check if cluster member exists with given url
     * @param memberUrl Member Url
     * @return 0 if member does not exist, if member exists returns value greater than 0
     */
    int memberExists(String memberUrl);

    /**
     * Count number of cluster member registrations
     *
     * @param params Parameters for SQL query
     *
     * @return Number of cluster members registered with given parameters
     */
    int countRegistrations(Map params);

    /**
     * Remove member from cluster in the database by local address
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeMemberByLocalAddress(Map params);

    /**
     * Update heartbeat for cluster node
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int updateHeartbeat(Map params);

    /**
     * Get all members not being updating heartbeat for specified limit
     *
     * @param params Parameters for SQL query
     * @return List of stale members
     */
    List<ClusterMember> getMembersWithStaleHeartbeat(Map params);

    /**
     * Get all inactive members not being updating heartbeat for specified limit
     *
     * @param params Parameters for SQL query
     * @return List of inactive members
     */
    List<ClusterMember> getInactiveMembersWithStaleHeartbeat(Map params);

    /**
     * Get member by git remote repository name
     *
     * @param params Parameters for SQL query
     * @return List members
     */
    List<ClusterMember> getMemberByRemoteName(Map params);

    /**
     * Get remote repositories that are missing on given cluster node
     *
     * @param localAddress cluster node address
     * @param siteId site identifier
     * @return list of remote repositories
     */
    List<RemoteRepository> getMissingClusterNodeRemoteRepositories(@Param(CLUSTER_LOCAL_ADDRESS) String localAddress,
                                                                   @Param(SITE_ID) String siteId);

    /**
     * Add given remote repository for given cluster node
     * @param clusterId cluster node identifier
     * @param remoteRepositoryId remote repository identifier
     */
    void addClusterRemoteRepository(@Param(CLUSTER_ID) long clusterId,
                                    @Param(REMOTE_REPOSITORY_ID) long remoteRepositoryId);

    /**
     * Get member by local address
     * @param localAddress local address
     * @return cluster member
     */
    ClusterMember getMemberByLocalAddress(@Param(CLUSTER_LOCAL_ADDRESS) String localAddress);

    /**
     * Insert cluster node site sync repo record
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param nodeLastCommitId last commit id of local sandbox repository
     * @param nodeLastVerifiedGitlogCommitId last verified git log commit id in local repo
     */
    void insertClusterSiteSyncRepo(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId,
                                   @Param(NODE_LAST_COMMIT_ID) String nodeLastCommitId,
                                   @Param(NODE_LAST_VERIFIED_GITLOG_COMMIT_ID) String nodeLastVerifiedGitlogCommitId);

    /**
     * Update local last verified git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateNodeLastVerifiedGitlogCommitId(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId,
                                              @Param(COMMIT_ID) String commitId);

    /**
     * Update local last git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateNodeLastCommitId(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId,
                                @Param(COMMIT_ID) String commitId);

    /**
     * get last commit id for node
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @return last commit id for local studio node
     */
    String getNodeLastCommitId(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId);

    /**
     * get last verified git log commit id for site
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @return last verified git log commit id for local studio node
     */
    String getNodeLastVerifiedGitlogCommitId(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId);

    /**
     * Check if sync markers exists in DB
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @return
     */
    int existsClusterSiteSyncRepo(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId);

    void setSiteState(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId,
                      @Param(STATE) String state);

    List<ClusterSiteRecord> getSiteStateAcrossCluster(@Param(SITE_ID) String siteId);

    ClusterSiteRecord getClusterSiteRecord(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId);

    void setPublishedRepoCreated(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId);

    void updateNodeLastSyncedGitlogCommitId(@Param(CLUSTER_NODE_ID) long clusterNodeId, @Param(SITE_ID) long siteId,
                                             @Param(COMMIT_ID) String commitId);
}
