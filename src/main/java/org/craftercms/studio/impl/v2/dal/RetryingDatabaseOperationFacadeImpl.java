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

package org.craftercms.studio.impl.v2.dal;

import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequence;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequenceMapper;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.annotation.RetryingDatabaseOperation;
import org.craftercms.studio.api.v2.dal.ActivityStreamDAO;
import org.craftercms.studio.api.v2.dal.AuditDAO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.SecurityDAO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.model.security.PersistentAccessToken;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RetryingDatabaseOperation
@SuppressWarnings("rawtypes")
public class RetryingDatabaseOperationFacadeImpl implements RetryingDatabaseOperationFacade {

    private DependencyMapper dependencyMapper;
    private NavigationOrderSequenceMapper navigationOrderSequenceMapper;
    private PublishRequestMapper publishRequestMapper;
    private SiteFeedMapper siteFeedMapper;
    private AuditDAO auditDao;
    private ClusterDAO clusterDao;
    private GitLogDAO gitLogDao;
    private GroupDAO groupDao;
    private ItemDAO itemDao;
    private PublishRequestDAO publishRequestDao;
    private RemoteRepositoryDAO remoteRepositoryDao;
    private SecurityDAO securityDao;
    private UserDAO userDao;
    private WorkflowDAO workflowDao;
    private ActivityStreamDAO activityStreamDAO;

    // Dependency API v1
    @Override
    public void deleteAllSourceDependencies(Map params) {
        dependencyMapper.deleteAllSourceDependencies(params);
    }

    @Override
    public void insertDependenciesList(Map params) {
        dependencyMapper.insertList(params);
    }

    @Override
    public void deleteDependenciesForSite(Map params) {
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public void deleteDependenciesForSiteAndPath(Map params) {
        dependencyMapper.deleteDependenciesForSiteAndPath(params);
    }

    @Override
    public void moveDependency(Map params) {
        dependencyMapper.moveDependency(params);
    }

    // Navigation Order Sequence API v1
    @Override
    public void insertNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence) {
        navigationOrderSequenceMapper.insert(navigationOrderSequence);
    }

    @Override
    public void updateNavigationOrderSequence(NavigationOrderSequence navigationOrderSequence) {
        navigationOrderSequenceMapper.update(navigationOrderSequence);
    }

    @Override
    public void deleteNavigationOrderSequencesForSite(Map params) {
        navigationOrderSequenceMapper.deleteSequencesForSite(params);
    }

    // Publish request API v1
    @Override
    public void insertItemForDeployment(PublishRequest copyToEnvironment) {
        publishRequestMapper.insertItemForDeployment(copyToEnvironment);
    }

    @Override
    public void cancelWorkflow(Map params) {
        publishRequestMapper.cancelWorkflow(params);
    }

    @Override
    public void cancelWorkflowBulk(Map params) {
        publishRequestMapper.cancelWorkflowBulk(params);
    }

    @Override
    public void updateItemDeploymentState(PublishRequest item) {
        publishRequestMapper.updateItemDeploymentState(item);
    }

    @Override
    public void markPublishRequestItemCompleted(PublishRequest item) {
        publishRequestMapper.markItemCompleted(item);
    }

    @Override
    public void deleteDeploymentDataForSite(Map params) {
        publishRequestMapper.deleteDeploymentDataForSite(params);
    }

    @Override
    public void resetPublishRequestProcessingQueue(Map params) {
        publishRequestMapper.resetProcessingQueue(params);
    }

    // Site API v1
    @Override
    public boolean createSite(SiteFeed siteFeed) {
        return siteFeedMapper.createSite(siteFeed);
    }

    @Override
    public boolean deleteSite(String siteId, String state) {
        return siteFeedMapper.deleteSite(siteId, state);
    }

    @Override
    public void updateSiteLastCommitId(Map params) {
        siteFeedMapper.updateLastCommitId(params);
    }

    @Override
    public void enableSitePublishing(Map params) {
        siteFeedMapper.enablePublishing(params);
    }

    @Override
    public void updateSitePublishingStatus(String siteId, String status) {
        siteFeedMapper.updatePublishingStatus(siteId, status);
    }

    @Override
    public void updateSiteLastVerifiedGitlogCommitId(Map params) {
        siteFeedMapper.updateLastVerifiedGitlogCommitId(params);
    }

    @Override
    public void updateSiteLastSyncedGitlogCommitId(Map params) {
        siteFeedMapper.updateLastSyncedGitlogCommitId(params);
    }

    @Override
    public void setSitePublishedRepoCreated(String siteId) {
        siteFeedMapper.setPublishedRepoCreated(siteId);
    }

    @Override
    public void unlockPublishingForSite(String siteId, String lockOwnerId) {
        siteFeedMapper.unlockPublishingForSite(siteId, lockOwnerId);
    }

    @Override
    public void updatePublishingLockHeartbeatForSite(String siteId) {
        siteFeedMapper.updatePublishingLockHeartbeatForSite(siteId);
    }

    @Override
    public int updateSite(String siteId, String name, String description) {
        return siteFeedMapper.updateSite(siteId, name, description);
    }

    @Override
    public void setSiteState(String siteId, String state) {
        siteFeedMapper.setSiteState(siteId, state);
    }

    @Override
    public void clearPublishingLockForSite(String siteId) {
        siteFeedMapper.clearPublishingLockForSite(siteId);
    }

    // Audit API v2
    @Override
    public int insertAuditLog(AuditLog auditLog) {
        return auditDao.insertAuditLog(auditLog);
    }

    @Override
    public void insertAuditLogParams(Map params) {
        auditDao.insertAuditLogParams(params);
    }

    @Override
    public void deleteAuditLogForSite(long siteId) {
        auditDao.deleteAuditLogForSite(siteId);
    }

    // Cluster API v2
    @Override
    public int updateClusterMember(ClusterMember member) {
        return clusterDao.updateMember(member);
    }

    @Override
    public int addClusterMember(ClusterMember member) {
        return clusterDao.addMember(member);
    }

    @Override
    public int removeClusterMembers(Map params) {
        return clusterDao.removeMembers(params);
    }

    @Override
    public int removeClusterMemberByLocalAddress(Map params) {
        return clusterDao.removeMemberByLocalAddress(params);
    }

    @Override
    public int updateClusterNodeHeartbeat(Map params) {
        return clusterDao.updateHeartbeat(params);
    }

    @Override
    public void addClusterRemoteRepository(long clusterId, long remoteRepositoryId) {
        clusterDao.addClusterRemoteRepository(clusterId, remoteRepositoryId);
    }

    // GitLog API v2
    @Override
    public void insertGitLog(Map params) {
        gitLogDao.insertGitLog(params);
    }

    @Override
    public void insertGitLogList(Map params) {
        gitLogDao.insertGitLogList(params);
    }

    @Override
    public void markGitLogProcessed(Map params) {
        gitLogDao.markGitLogProcessed(params);
    }

    @Override
    public void markGitLogProcessedBulk(String siteId, List<String> commitIds) {
        gitLogDao.markGitLogProcessedBulk(siteId, commitIds);
    }

    @Override
    public void deleteGitLogForSite(Map params) {
        gitLogDao.deleteGitLogForSite(params);
    }

    @Override
    public void markGitLogAudited(String siteId, String commitId, int audited) {
        gitLogDao.markGitLogAudited(siteId, commitId, audited);
    }

    @Override
    public void insertIgnoreGitLogList(String siteId, List<String> commitIds) {
        gitLogDao.insertIgnoreGitLogList(siteId, commitIds);
    }

    @Override
    public void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed, int unprocessed) {
        gitLogDao.markGitLogProcessedBeforeMarker(siteId, marker, processed, unprocessed);
    }

    @Override
    public void upsertGitLogList(String siteId, List<String> commitIds, int processed, int audited) {
        gitLogDao.upsertGitLogList(siteId, commitIds, processed, audited);
    }

    // Group API v2
    @Override
    public Integer createGroup(long orgId, String groupName, String groupDescription) {
        return groupDao.createGroup(orgId, groupName, groupDescription);
    }

    @Override
    public Integer updateGroup(Group group) {
        return groupDao.updateGroup(group);
    }

    @Override
    public Integer deleteGroup(long groupId) {
        return groupDao.deleteGroup(groupId);
    }

    @Override
    public Integer deleteGroups(List<Long> groupIds) {
        return groupDao.deleteGroups(groupIds);
    }

    @Override
    public Integer addGroupMembers(long groupId, List<Long> userIds) {
        return groupDao.addGroupMembers(groupId, userIds);
    }

    @Override
    public Integer removeGroupMembers(long groupId, List<Long> userIds) {
        return groupDao.removeGroupMembers(groupId, userIds);
    }

    // Item API v2
    @Override
    public void upsertEntry(Item item) {
        itemDao.upsertEntry(item);
    }

    @Override
    public void upsertEntries(List<Item> entries) {
        itemDao.upsertEntries(entries);
    }

    @Override
    public void updateItem(Item item) {
        itemDao.updateItem(item);
    }

    @Override
    public void deleteById(long id) {
        itemDao.deleteById(id);
    }

    @Override
    public void deleteBySiteAndPath(long siteId, String path) {
        itemDao.deleteBySiteAndPath(siteId, path);
    }

    @Override
    public void setStatesBySiteAndPathBulk(long siteId, List<String> paths, long statesBitMap) {
        itemDao.setStatesBySiteAndPathBulk(siteId, paths, statesBitMap);
    }

    @Override
    public void setStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        itemDao.setStatesByIdBulk(itemIds, statesBitMap);
    }

    @Override
    public void resetStatesBySiteAndPathBulk(long siteId, List<String> paths, long statesBitMap) {
        itemDao.resetStatesBySiteAndPathBulk(siteId, paths, statesBitMap);
    }

    @Override
    public void resetStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        itemDao.resetStatesByIdBulk(itemIds, statesBitMap);
    }

    @Override
    public void updateStatesBySiteAndPathBulk(long siteId, List<String> paths, long onStatesBitMap, long offStatesBitMap) {
        itemDao.updateStatesBySiteAndPathBulk(siteId, paths, onStatesBitMap, offStatesBitMap);
    }

    @Override
    public void updateStatesByIdBulk(List<Long> itemIds, long onStatesBitMap, long offStatesBitMap) {
        itemDao.updateStatesByIdBulk(itemIds, onStatesBitMap, offStatesBitMap);
    }

    @Override
    public void updateStatesForSite(long siteId, long onStatesBitMap,
                                    long offStatesBitMap) {
        itemDao.updateStatesForSite(siteId, onStatesBitMap, offStatesBitMap);
    }

    @Override
    public void deleteItemsForSite(long siteId) {
        itemDao.deleteItemsForSite(siteId);
    }

    @Override
    public void deleteItemsById(List<Long> itemIds) {
        itemDao.deleteItemsById(itemIds);
    }

    @Override
    public void deleteItemsForSiteAndPath(long siteId, List<String> paths) {
        itemDao.deleteItemsForSiteAndPath(siteId, paths);
    }

    @Override
    public void deleteBySiteAndPathForFolder(long siteId, String path) {
        itemDao.deleteBySiteAndPathForFolder(siteId, path);
    }

    @Override
    public void moveItem(String siteId, String oldPath, String newPath) {
        itemDao.moveItem(siteId, oldPath, newPath);
    }

    @Override
    public void moveItems(String siteId, String oldPath, String newPath, Long parentId, String oldPreviewUrl,
                          String newPreviewUrl, long onStatesBitMap, long offStatesBitMap) {
        itemDao.moveItems(siteId, oldPath, newPath, parentId, oldPreviewUrl, newPreviewUrl, onStatesBitMap,
                offStatesBitMap);
    }

    @Override
    public void clearPreviousPath(String siteId, String path) {
        itemDao.clearPreviousPath(siteId, path);
    }

    @Override
    public void updateCommitId(String siteId, String path, String commitId) {
        itemDao.updateCommitId(siteId, path, commitId);
    }

    @Override
    public void updateLastPublishedOn(String siteId, String path, ZonedDateTime lastPublishedOn) {
        itemDao.updateLastPublishedOn(siteId, path, lastPublishedOn);
    }

    @Override
    public void updateLastPublishedOnBulk(String siteId, List<String> paths, ZonedDateTime lastPublishedOn) {
        itemDao.updateLastPublishedOnBulk(siteId, paths, lastPublishedOn);
    }

    @Override
    public void lockItemByPath(String siteId, String path, long lockOwnerId, long lockedBitOn,
                               String systemTypeFolder) {
        itemDao.lockItemByPath(siteId, path, lockOwnerId, lockedBitOn, systemTypeFolder);
    }

    @Override
    public void lockItemsByPath(String siteId, List<String> paths, long lockOwnerId, long lockedBitOn,
                                String systemTypeFolder) {
        itemDao.lockItemsByPath(siteId, paths, lockOwnerId, lockedBitOn, systemTypeFolder);
    }

    @Override
    public void unlockItemByPath(String siteId, String path, long lockedBitOff) {
        itemDao.unlockItemByPath(siteId, path, lockedBitOff);
    }

    @Override
    public void lockItemById(Long itemId, long lockOwnerId, long lockedBitOn, String systemTypeFolder) {
        itemDao.lockItemById(itemId, lockOwnerId, lockedBitOn, systemTypeFolder);
    }

    @Override
    public void lockItemsById(List<Long> itemIds, long lockOwnerId, long lockedBitOn, String systemTypeFolder) {
        itemDao.lockItemsById(itemIds, lockOwnerId, lockedBitOn, systemTypeFolder);
    }

    @Override
    public void unlockItemById(Long itemId, long lockedBitOff) {
        itemDao.unlockItemById(itemId, lockedBitOff);
    }

    @Override
    public void updateStatesByQuery(String siteId, String path, Long states, long setStatesMask, long resetStatesMask) {
        itemDao.updateStatesByQuery(siteId, path, states, setStatesMask, resetStatesMask);
    }

    // Publish Request API v2
    @Override
    public void cancelPackages(String siteId, List<String> packageIds, String cancelledState) {
        publishRequestDao.cancelPackages(siteId, packageIds, cancelledState);
    }

    @Override
    public void cancelScheduledQueueItems(String siteId, List<String> paths, ZonedDateTime now, String cancelledState,
                                          String readyState) {
        publishRequestDao.cancelScheduledQueueItems(siteId, paths, now, cancelledState, readyState);
    }

    // Remote Repository API v2
    @Override
    public void insertRemoteRepository(Map params) {
        remoteRepositoryDao.insertRemoteRepository(params);
    }

    @Override
    public void deleteRemoteRepositoryForSite(Map params) {
        remoteRepositoryDao.deleteRemoteRepositoryForSite(params);
    }

    @Override
    public void deleteRemoteRepository(Map params) {
        remoteRepositoryDao.deleteRemoteRepository(params);
    }

    @Override
    public void deleteRemoteRepositoriesForSite(Map params) {
        remoteRepositoryDao.deleteRemoteRepositoriesForSite(params);
    }

    // Security API v2
    @Override
    public void upsertRefreshToken(long userId, String token) {
        securityDao.upsertRefreshToken(userId, token);
    }

    @Override
    public void deleteRefreshToken(long userId) {
        securityDao.deleteRefreshToken(userId);
    }

    @Override
    public void createAccessToken(long userId, PersistentAccessToken token) {
        securityDao.createAccessToken(userId, token);
    }

    @Override
    public void updateAccessToken(long userId, long tokenId, boolean enabled) {
        securityDao.updateAccessToken(userId, tokenId, enabled);
    }

    @Override
    public void deleteAccessToken(long userId, long tokenId) {
        securityDao.deleteAccessToken(userId, tokenId);
    }

    @Override
    public int deleteExpiredTokens(int sessionTimeout, List<Long> inactiveUsers) {
        return securityDao.deleteExpiredTokens(sessionTimeout, inactiveUsers);
    }

    // User API v2
    @Override
    public int createUser(Map params) {
        return userDao.createUser(params);
    }

    @Override
    public int updateUser(Map params) {
        return userDao.updateUser(params);
    }

    @Override
    public int deleteUsers(Map params) {
        return userDao.deleteUsers(params);
    }

    @Override
    public int enableUsers(Map params) {
        return userDao.enableUsers(params);
    }

    @Override
    public int setUserPassword(Map params) {
        return userDao.setUserPassword(params);
    }

    @Override
    public void deleteUserProperties(long userId, long siteId, List<String> keys) {
        userDao.deleteUserProperties(userId, siteId, keys);
    }

    @Override
    public void updateUserProperties(long userId, long siteId, Map<String, String> properties) {
        userDao.updateUserProperties(userId, siteId, properties);
    }

    @Override
    public void deleteUserPropertiesBySiteId(long siteId) {
        userDao.deleteUserPropertiesBySiteId(siteId);
    }

    @Override
    public void deleteUserPropertiesByUserIds(List<Long> userIds) {
        userDao.deleteUserPropertiesByUserIds(userIds);
    }

    // Workflow API v2
    @Override
    public void insertWorkflowEntry(Workflow workflow) {
        workflowDao.insertWorkflowEntry(workflow);
    }

    @Override
    public void insertWorkflowEntries(List<Workflow> workflowEntries) {
        workflowDao.insertWorkflowEntries(workflowEntries);
    }

    @Override
    public void updateWorkflowEntry(Workflow workflow) {
        workflowDao.updateWorkflowEntry(workflow);
    }

    @Override
    public void deleteWorkflowEntries(String siteId, List<String> paths) {
        workflowDao.deleteWorkflowEntries(siteId, paths);
    }

    @Override
    public void deleteWorkflowEntry(String siteId, String path) {
        workflowDao.deleteWorkflowEntry(siteId, path);
    }

    @Override
    public void deleteWorkflowEntriesForSite(long siteId) {
        workflowDao.deleteWorkflowEntriesForSite(siteId);
    }

    // Activity Stream
    @Override
    public void insertActivity(long siteId, long userId, String action, ZonedDateTime actionTimestamp, Item item,
                               String packageId) {
        activityStreamDAO.insertActivity(siteId, userId, action, actionTimestamp, item, packageId);
    }

    public DependencyMapper getDependencyMapper() {
        return dependencyMapper;
    }

    public void setDependencyMapper(DependencyMapper dependencyMapper) {
        this.dependencyMapper = dependencyMapper;
    }

    public NavigationOrderSequenceMapper getNavigationOrderSequenceMapper() {
        return navigationOrderSequenceMapper;
    }

    public void setNavigationOrderSequenceMapper(NavigationOrderSequenceMapper navigationOrderSequenceMapper) {
        this.navigationOrderSequenceMapper = navigationOrderSequenceMapper;
    }

    public PublishRequestMapper getPublishRequestMapper() {
        return publishRequestMapper;
    }

    public void setPublishRequestMapper(PublishRequestMapper publishRequestMapper) {
        this.publishRequestMapper = publishRequestMapper;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public AuditDAO getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(AuditDAO auditDao) {
        this.auditDao = auditDao;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public GitLogDAO getGitLogDao() {
        return gitLogDao;
    }

    public void setGitLogDao(GitLogDAO gitLogDao) {
        this.gitLogDao = gitLogDao;
    }

    public GroupDAO getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDAO groupDao) {
        this.groupDao = groupDao;
    }

    public ItemDAO getItemDao() {
        return itemDao;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public PublishRequestDAO getPublishRequestDao() {
        return publishRequestDao;
    }

    public void setPublishRequestDao(PublishRequestDAO publishRequestDao) {
        this.publishRequestDao = publishRequestDao;
    }

    public RemoteRepositoryDAO getRemoteRepositoryDao() {
        return remoteRepositoryDao;
    }

    public void setRemoteRepositoryDao(RemoteRepositoryDAO remoteRepositoryDao) {
        this.remoteRepositoryDao = remoteRepositoryDao;
    }

    public SecurityDAO getSecurityDao() {
        return securityDao;
    }

    public void setSecurityDao(SecurityDAO securityDao) {
        this.securityDao = securityDao;
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public WorkflowDAO getWorkflowDao() {
        return workflowDao;
    }

    public void setWorkflowDao(WorkflowDAO workflowDao) {
        this.workflowDao = workflowDao;
    }

    public ActivityStreamDAO getActivityStreamDAO() {
        return activityStreamDAO;
    }

    public void setActivityStreamDAO(ActivityStreamDAO activityStreamDAO) {
        this.activityStreamDAO = activityStreamDAO;
    }
}

