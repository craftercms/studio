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

import org.craftercms.studio.api.v1.dal.NavigationOrderSequence;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.model.security.PersistentAccessToken;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface RetryingDatabaseOperationFacade {

    // Dependency API v1
    void deleteAllSourceDependencies(Map params);

    void insertDependenciesList(Map params);

    void deleteDependenciesForSite(Map params);

    void deleteDependenciesForSiteAndPath(Map params);

    void moveDependency(Map params);

    // Navigation Order Sequence API v1
    void insertNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence);

    void updateNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence);

    void deleteNavigationOrderSequencesForSite(Map params);

    // Publish Request API v1
    void insertItemForDeployment(PublishRequest copyToEnvironment);

    void cancelWorkflow(Map params);

    void cancelWorkflowBulk(Map params);

    void updateItemDeploymentState(PublishRequest item);

    void markPublishRequestItemCompleted(PublishRequest item);

    void deleteDeploymentDataForSite(Map params);

    /**
     * Reset publishing requests in processing state
     *
     * @param params SQL query parameters
     */
    void resetPublishRequestProcessingQueue(Map params);

    // Site API v1
    boolean createSite(SiteFeed siteFeed);

    /**
     * Delete site
     * @param siteId site identifier
     * @param state deleted state value
     * @return
     */
    boolean deleteSite(String siteId, String state);

    void updateSiteLastCommitId(Map params);

    void enableSitePublishing(Map params);

    void updateSitePublishingStatusMessage(String siteId, String status, String message);

    void updateSiteLastVerifiedGitlogCommitId(Map params);

    void updateSiteLastSyncedGitlogCommitId(Map params);

    /**
     * Set published repo created flag
     * @param siteId site identifier
     */
    void setSitePublishedRepoCreated(String siteId);

    /**
     * unlock publisher task for site
     * @param siteId site identifier
     * @param lockOwnerId lock owner identifier
     */
    void unlockPublishingForSite(String siteId, String lockOwnerId);

    /**
     * update publishing lock heartbeat for site
     * @param siteId site identifier
     */
    void updatePublishingLockHeartbeatForSite(String siteId);

    /**
     * Updates the name and description for the given site
     *
     * @param siteId the id of the site
     * @param name the name of the site
     * @param description the description of the site
     * @return the number of changed rows
     */
    int updateSite(String siteId, String name, String description);

    void setSiteState(String siteId, String state);

    /**
     * Clear publishing lock for site
     * @param siteId site identifier
     */
    void clearPublishingLockForSite(String siteId);

    // Audit API v2
    int insertAuditLog(AuditLog auditLog);

    void insertAuditLogParams(Map params);

    /**
     * Delete audit log for site
     * @param siteId site id
     */
    void deleteAuditLogForSite(long siteId);

    // Cluster API v2
    /**
     * Update cluster member in the database
     *
     * @param member Cluster member to update
     *
     * @return number of affected rows
     */
    int updateClusterMember(ClusterMember member);

    /**
     * Add member to cluster in the database
     *
     * @param member Member to add
     *
     * @return number of affected rows
     */
    int addClusterMember(ClusterMember member);

    /**
     * Remove members from cluster in the database
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeClusterMembers(Map params);

    /**
     * Remove member from cluster in the database by local address
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int removeClusterMemberByLocalAddress(Map params);

    /**
     * Update heartbeat for cluster node
     *
     * @param params Parameters for SQL query
     *
     * @return number of affected rows
     */
    int updateClusterNodeHeartbeat(Map params);
    /**
     * Add given remote repository for given cluster node
     * @param clusterId cluster node identifier
     * @param remoteRepositoryId remote repository identifier
     */
    void addClusterRemoteRepository(long clusterId, long remoteRepositoryId);

    /**
     * Insert cluster node site sync repo record
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param nodeLastCommitId last commit id of local sandbox repository
     * @param nodeLastVerifiedGitlogCommitId last verified git log commit id in local repo
     */
    void insertClusterSiteSyncRepo(long clusterNodeId, long siteId, String nodeLastCommitId,
                                   String nodeLastVerifiedGitlogCommitId, String nodeLastSyncedGitlogCommitId);

    /**
     * Update local last verified git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateClusterNodeLastVerifiedGitlogCommitId(long clusterNodeId, long siteId, String commitId);

    /**
     * Update local last git log commit id
     * @param clusterNodeId cluster node identifier
     * @param siteId site identifier
     * @param commitId commit id
     */
    void updateClusterNodeLastCommitId(long clusterNodeId, long siteId, String commitId);

    void setClusterNodeSiteState(long clusterNodeId, long siteId, String state);

    void setClusterNodePublishedRepoCreated(long clusterNodeId, long siteId);

    void updateClusterNodeLastSyncedGitlogCommitId(long clusterNodeId, long siteId, String commitId);

    // GitLog API v2
    void insertGitLog(Map params);

    void insertGitLogList(Map params);

    void markGitLogProcessed(Map params);

    /**
     * Mark commit id as processed for given site and list of
     * @param siteId site identifier
     * @param commitIds list of commit ids
     */
    void markGitLogProcessedBulk(String siteId, List<String> commitIds);

    void deleteGitLogForSite(Map params);

    void markGitLogAudited(String siteId, String commitId, int audited);

    void insertIgnoreGitLogList(String siteId, List<String> commitIds);

    /**
     * Mark all git logs as processed if they are inserted before marker
     * @param siteId site identifier
     * @param marker marker git commit
     * @param processed value for processed
     */
    void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed, int unprocessed);

    /**
     * Upsert git logs as processed and audited
     * @param siteId site identifier
     * @param commitIds commit ids
     */
    void upsertGitLogList(String siteId, List<String> commitIds, int processed, int audited);

    // Group API v2
    /**
     * Create group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer createGroup(Map params);

    /**
     * Update group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer updateGroup(Map params);

    /**
     * Delete group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer deleteGroup(Map params);

    /**
     * Delete group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer deleteGroups(Map params);

    /**
     * Add users to the group
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    Integer addGroupMembers(Map params);

    /**
     * Remove users from the group
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    Integer removeGroupMembers(Map params);

    // Item API v2
    /**
     * ItemDAO
     * insert or update item
     *
     * @param item item to insert/update
     */
    void upsertEntry(Item item);

    /**
     * Insert or update items
     *
     * @param entries list of items to insert
     */
    void upsertEntries(List<Item> entries);

    /**
     * Update item
     * @param item item to update
     */
    void updateItem(Item item);

    /**
     * Delete item
     * @param id id of the item to delete
     */
    void deleteById(long id);

    /**
     * Delete item
     * @param siteId site identifier
     * @param path path of item to delete
     */
    void deleteBySiteAndPath(long siteId, String path);

    /**
     * Set items state
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMap states bit map to be set
     */
    void setStatesBySiteAndPathBulk(long siteId, List<String> paths, long statesBitMap);

    /**
     * Set items state
     * @param itemIds ids of items
     * @param statesBitMap states bit map to be set
     */
    void setStatesByIdBulk(List<Long> itemIds, long statesBitMap);

    /**
     * Reset items state
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMap states bit map to be reset
     */
    void resetStatesBySiteAndPathBulk(long siteId, List<String> paths, long statesBitMap);

    /**
     * Reset items state
     * @param itemIds ids of items
     * @param statesBitMap states bit map to be reset
     */
    void resetStatesByIdBulk(List<Long> itemIds, long statesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param paths list of paths to update states for
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesBySiteAndPathBulk(long siteId, List<String> paths, long onStatesBitMap, long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param itemIds list of item identifiers
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesByIdBulk(List<Long> itemIds, long onStatesBitMap, long offStatesBitMap);

    /**
     * Delete all items for site
     * @param siteId site id
     */
    void deleteItemsForSite(long siteId);

    /**
     * Delete items by id
     * @param itemIds item ids
     */
    void deleteItemsById(List<Long> itemIds);

    /**
     * Delete items for site and paths
     * @param siteId site id
     * @param paths paths of the items
     */
    void deleteItemsForSiteAndPath(long siteId, List<String> paths);

    /**
     * Delete items for site and folder path
     * @param siteId site id
     * @param path path of the folder
     */
    void deleteBySiteAndPathForFolder(long siteId, String path);

    /**
     * Move item
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     */
    void moveItem(String siteId, String oldPath, String newPath);

    /**
     * Move item
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void moveItems(String siteId, String oldPath, String newPath, String oldPreviewUrl, String newPreviewUrl,
                   long onStatesBitMap, long offStatesBitMap);

    /**
     * Clear previous path of the content
     * @param siteId site identifier
     * @param path path of the content
     */
    void clearPreviousPath(String siteId, String path);

    /**
     * Update commit id for item
     * @param siteId site identifier
     * @param path path of the item
     * @param commitId commit id
     */
    void updateCommitId(String siteId, String path, String commitId);

    /**
     * Update last published date for item
     * @param siteId site identifier
     * @param path path of the item
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOn(String siteId, String path, ZonedDateTime lastPublishedOn);

    /**
     * Update last published date for item
     * @param siteId site identifier
     * @param paths list of paths
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOnBulk(String siteId, List<String> paths, ZonedDateTime lastPublishedOn);

    /**
     * Lock item
     * @param siteId site identifier
     * @param path path of the item
     * @param lockOwnerId lock owner
     * @param lockedBitOn state bit mask with LOCKED bit on
     */
    void lockItemByPath(String siteId, String path, long lockOwnerId, long lockedBitOn);

    /**
     * Lock item
     * @param siteId site identifier
     * @param path path of the item
     * @param lockedBitOff state bit mask with LOCKED bit off
     */
    void unlockItemByPath(String siteId, String path, long lockedBitOff);

    /**
     * Lock item
     * @param itemId item identifier
     * @param lockOwnerId lock owner
     * @param lockedBitOn state bit mask with LOCKED bit on
     */
    void lockItemById(Long itemId, long lockOwnerId, long lockedBitOn);

    /**
     * Lock item
     * @param itemId item identifier
     * @param lockedBitOff state bit mask with LOCKED bit off
     */
    void unlockItemById(Long itemId, long lockedBitOff);

    /**
     * Update item state by query
     * @param siteId site identifier
     * @param path path regex to filter items to be updated
     * @param states states bitmap mask to filter items to be updated
     * @param setStatesMask states bitmap mask to set states on
     * @param resetStatesMask states bitmap mask to set states off
     */
    void updateStatesByQuery(String siteId, String path, Long states, long setStatesMask, long resetStatesMask);

    // Publish Request API v2
    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     * @param cancelledState cancelled state
     */
    void cancelPackages(String siteId, List<String> packageIds, String cancelledState);

    // Remote Repository API v2
    void insertRemoteRepository(Map params);

    void deleteRemoteRepositoryForSite(Map params);

    void deleteRemoteRepository(Map params);

    void deleteRemoteRepositoriesForSite(Map params);

    // Security API v2
    void upsertRefreshToken(long userId, String token);

    void deleteRefreshToken(long userId);

    void createAccessToken(long userId, PersistentAccessToken token);

    void updateAccessToken(long userId, long tokenId, boolean enabled);

    void deleteAccessToken(long userId, long tokenId);

    void deleteExpiredTokens(int maxAge);

    // User API v2
    /**
     * Create user
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int createUser(Map params);

    /**
     * Update user
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int updateUser(Map params);

    /**
     * Delete users
     *
     * @param params SQL query params
     * @return Number of rows affected in DB
     */
    int deleteUsers(Map params);

    /**
     * Enable/disable users
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int enableUsers(Map params);

    /**
     * Set password for user
     *
     * @param params SQL query parameters
     * @return Number of rows affected
     */
    int setUserPassword(Map params);

    /**
     * Deletes the given user properties
     * @param userId the id of the user
     * @param siteId the id of the site
     * @param keys the keys to delete
     */
    void deleteUserProperties(long userId, long siteId, List<String> keys);

    /**
     * Updates the given user properties
     * @param userId the id of the user
     * @param siteId the id of the site
     * @param properties the properties to update or add
     */
    void updateUserProperties(long userId, long siteId, Map<String, String> properties);

    /**
     * Deletes all user properties for a given site
     * @param siteId the id of the site
     */
    void deleteUserPropertiesBySiteId(long siteId);

    /**
     * Deletes all user properties for a given user
     * @param userIds the id of the user
     */
    void deleteUserPropertiesByUserIds(List<Long> userIds);

    // Workflow API v2
    /**
     * Insert workflow entry
     * @param workflow workflow entry
     */
    void insertWorkflowEntry(Workflow workflow);

    /**
     * Delete workflow entries
     * @param siteId site identifier
     * @param paths list of paths
     */
    void deleteWorkflowEntries(String siteId, List<String> paths);

    /**
     * Delete workflow entry
     * @param siteId site identifier
     * @param path path
     */
    void deleteWorkflowEntry(String siteId, String path);

    /**
     * Delete workflow entries for site
     * @param siteId site id
     */
    void deleteWorkflowEntriesForSite(long siteId);
}
