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

package org.craftercms.studio.api.v2.dal;

import org.craftercms.studio.api.v1.dal.NavigationOrderSequence;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.model.security.PersistentAccessToken;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface RetryingDatabaseOperationFacade {

    // Dependency API v1

    /**
     * Delete all dependencies for source path
     * @param params SQL query parameters
     */
    void deleteAllSourceDependencies(Map params);

    /**
     * Insert list of dependencies
     * @param params SQL query parameters
     */
    void insertDependenciesList(Map params);

    /**
     * Delete all dependencies for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteDependenciesForSite(Map params);

    /**
     * Delete dependencies for site and path (delete content subtask)
     * @param params SQL query parameters
     */
    void deleteDependenciesForSiteAndPath(Map params);

    /**
     * Move dependencies (move content subtask)
     * @param params SQL query parameters
     */
    void moveDependency(Map params);

    // Navigation Order Sequence API v1
    /**
     * Insert new navigation order sequence
     * @param navigationOrderSequence Navigation order sequence object
     */
    void insertNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence);

    /**
     * Update navigation order sequence
     * @param navigationOrderSequence Navigation order sequence object
     */
    void updateNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence);

    /**
     * Delete navigation order sequences for site (delete site subtasks)
     * @param params SQL query parameters
     */
    void deleteNavigationOrderSequencesForSite(Map params);

    // Publish Request API v1
    /**
     * Insert new publish request into publishing queue
     * @param publishRequest
     */
    void insertItemForDeployment(PublishRequest publishRequest);

    /**
     * Cancel workflow for single item (cancel items from publishing queue)
     * @param params SQL query parameters
     */
    void cancelWorkflow(Map params);

    /**
     * Cancel workflow for multiple items (cancel items from publishing queue)
     * @param params
     */
    void cancelWorkflowBulk(Map params);

    /**
     * Update publishing queue item state
     * @param item Publish request item
     */
    void updateItemDeploymentState(PublishRequest item);

    /**
     * Mark publish request item as completed
     * @param item Publish request item
     */
    void markPublishRequestItemCompleted(PublishRequest item);

    /**
     * Delete publish request items for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteDeploymentDataForSite(Map params);

    /**
     * Reset publishing requests in processing state
     *
     * @param params SQL query parameters
     */
    void resetPublishRequestProcessingQueue(Map params);

    // Site API v1
    /**
     * Create new site
     * @param siteFeed Site object
     * @return true if success otherwise false
     */
    boolean createSite(SiteFeed siteFeed);

    /**
     * Delete site
     * @param siteId site identifier
     * @param state deleted state value
     * @return
     */
    boolean deleteSite(String siteId, String state);

    /**
     * Update last commit id for site
     * @param params SQL query parameters
     */
    void updateSiteLastCommitId(Map params);

    /**
     * Enable/disable publishing site
     * @param params SQL query parameters
     */
    void enableSitePublishing(Map params);

    /**
     * Update publishing status for site
     * @param siteId site identifier
     * @param status status value
     */
    void updateSitePublishingStatus(String siteId, String status);

    /**
     * Update last verified git log commit id for site
     * @param params SQL query parameters
     */
    void updateSiteLastVerifiedGitlogCommitId(Map params);

    /**
     * Update last synced git log commit id for site
     * @param params SQL query parameters
     */
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
    /**
     * Insert audit log record
     * @param auditLog Audit log
     * @return number of rows affected
     */
    int insertAuditLog(AuditLog auditLog);

    /**
     * Insert audit log parameters
     * @param params SQL query parameters
     */
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

    // GitLog API v2
    /**
     * Insert new gitlog table row
     * @param params SQL query parameters
     */
    void insertGitLog(Map params);

    /**
     * Insert list of new rows to gitlog table
     * @param params
     */
    void insertGitLogList(Map params);

    /**
     * Mark git log row as processed
     * @param params SQL query parameters
     */
    void markGitLogProcessed(Map params);

    /**
     * Mark commit id as processed for given site and list of
     * @param siteId site identifier
     * @param commitIds list of commit ids
     */
    void markGitLogProcessedBulk(String siteId, List<String> commitIds);

    /**
     * Delete gitlog rows for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteGitLogForSite(Map params);

    /**
     * Mark gitlog as audited for site
     * @param siteId site identifier
     * @param commitId commit id
     * @param audited audited flag value
     */
    void markGitLogAudited(String siteId, String commitId, int audited);

    /**
     * Insert list of gitlog rows with ignore option if it already exists
     * @param siteId site identifier
     * @param commitIds commit id
     */
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
     * @param orgId organization id
     * @param groupName group name
     * @param groupDescription  group description
     * @return Number of affected rows in DB
     */
    Integer createGroup(long orgId, String groupName, String groupDescription);

    /**
     * Update group
     *
     * @param group group to update
     * @return Number of affected rows in DB
     */
    Integer updateGroup(Group group);

    /**
     * Delete group
     *
     * @param groupId group identifier
     * @return Number of affected rows in DB
     */
    Integer deleteGroup(long groupId);

    /**
     * Delete groups
     *
     * @param groupIds ids of the groups to be deleted
     * @return Number of affected rows in DB
     */
    Integer deleteGroups(List<Long> groupIds);

    /**
     * Add users to the group
     *
     * @param groupId group identifier
     * @param userIds list of user identifiers
     * @return Number of rows affected in DB
     */
    Integer addGroupMembers(long groupId, List<Long> userIds);

    /**
     * Remove users from the group
     *
     * @param groupId group identifier
     * @param userIds list of user identifiers
     * @return Number of rows affected in DB
     */
    Integer removeGroupMembers(long groupId, List<Long> userIds);

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
    void updateStatesBySiteAndPathBulk(long siteId, Collection<String> paths, long onStatesBitMap, long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param itemIds list of item identifiers
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesByIdBulk(List<Long> itemIds, long onStatesBitMap, long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesForSite(long siteId, long onStatesBitMap, long offStatesBitMap);

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
     * Move items
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     * @param parentId new parent id, if null parent will not be changed
     * @param oldPreviewUrl old preview url
     * @param newPreviewUrl new preview url
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void moveItems(String siteId, String oldPath, String newPath, Long parentId, String oldPreviewUrl,
                   String newPreviewUrl, long onStatesBitMap, long offStatesBitMap);

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
     * @param systemTypeFolder value for system type folder
     */
    void lockItemByPath(String siteId, String path, long lockOwnerId, long lockedBitOn, String systemTypeFolder);

    /**
     * Lock items
     * @param siteId site identifier
     * @param paths list of item paths
     * @param lockOwnerId lock owner
     * @param lockedBitOn state bit mask with LOCKED bit on
     * @param systemTypeFolder value for system type folder
     */
    void lockItemsByPath(String siteId, List<String> paths, long lockOwnerId, long lockedBitOn,
                         String systemTypeFolder);
    /**
     * Lock item
     * @param siteId site identifier
     * @param path path of the item
     * @param lockedBitOff state bit mask with LOCKED bit off
     */
    void unlockItemByPath(String siteId, String path, long lockedBitOff);

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

    /**
     * Cancel scheduled items from publishing queue
     * @param siteId site identifier
     * @param paths list of paths of content items to be cancelled
     * @param now timestamp now
     * @param cancelledState cancelled state value
     * @param readyState ready for live state value
     */
    void cancelScheduledQueueItems(String siteId, List<String> paths, ZonedDateTime now, String cancelledState,
                                   String readyState);

    // Remote Repository API v2
    /**
     * Insert remote repository record
     * @param params SQL query parameters
     */
    void insertRemoteRepository(Map params);

    /**
     * Delete remote repositories for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteRemoteRepositoryForSite(Map params);

    /**
     * Delete remote repository record
     * @param params SQL query parameters
     */
    void deleteRemoteRepository(Map params);

    /**
     * Delete remote repositories for site (delete site subtask)
     * @param params SQL query parameters
     */
    void deleteRemoteRepositoriesForSite(Map params);

    // Security API v2

    /**
     * Insert or update refresh token
     * @param userId User identifier
     * @param token token
     */
    void upsertRefreshToken(long userId, String token);

    /**
     * Delete refresh token
     * @param userId user identifier
     */
    void deleteRefreshToken(long userId);

    /**
     * Create access token
     * @param userId user identifier
     * @param token token
     */
    void createAccessToken(long userId, PersistentAccessToken token);

    /**
     * Update access token
     * @param userId user identifier
     * @param tokenId token identifier
     * @param enabled enabled flag
     */
    void updateAccessToken(long userId, long tokenId, boolean enabled);

    /**
     * Delete access token
     * @param userId user identifier
     * @param tokenId token identifier
     */
    void deleteAccessToken(long userId, long tokenId);

    /**
     * Delete expired tokens
     * @param sessionTimeout the timeout in minutes for active users
     * @param inactiveUsers the list of user ids that are inactive
     */
    int deleteExpiredTokens(int sessionTimeout, List<Long> inactiveUsers);

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
     * Insert workflow entries
     * @param workflowEntries list of workflow entries
     */
    void insertWorkflowEntries(List<Workflow> workflowEntries);

    /**
     * Update workflow entry
     * @param workflow workflow entry
     */
    void updateWorkflowEntry(Workflow workflow);

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

    // Activity stream
    /**
     * Insert record into activity stream
     * @param siteId site identifier
     * @param userId user identifier
     * @param action action that was performed
     * @param actionTimestamp timestamp when action was performed
     * @param item item that was actioned upon
     * @param packageId package identifier that was actioned upon
     */
    void insertActivity(long siteId, long userId, String action, ZonedDateTime actionTimestamp, Item item,
                        String packageId);
}
