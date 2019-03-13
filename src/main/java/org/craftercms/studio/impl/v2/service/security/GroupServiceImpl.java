/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.exception.OrganizationNotFoundException;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.OrganizationServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.KEY_CONTENT_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class GroupServiceImpl implements GroupService {

    private ConfigurationService configurationService;
    private GroupServiceInternal groupServiceInternal;
    private OrganizationServiceInternal organizationServiceInternal;
    private UserServiceInternal userServiceInternal;
    private GeneralLockService generalLockService;
    private ActivityService activityService;
    private StudioConfiguration studioConfiguration;
    private UserService userService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort)
            throws ServiceLayerException, OrganizationNotFoundException {
        // Security check
        if (organizationServiceInternal.organizationExists(orgId)) {
            return groupServiceInternal.getAllGroups(orgId, offset, limit, sort);
        } else {
            throw new OrganizationNotFoundException();
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public int getAllGroupsTotal(long orgId) throws ServiceLayerException, OrganizationNotFoundException {
        if (organizationServiceInternal.organizationExists(orgId)) {
            return groupServiceInternal.getAllGroupsTotal(orgId);
        } else {
            throw new OrganizationNotFoundException();
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_groups")
    public Group createGroup(long orgId, String groupName, String groupDescription)
            throws GroupAlreadyExistsException, ServiceLayerException, AuthenticationException {
        Group toRet = groupServiceInternal.createGroup(orgId, groupName, groupDescription);
        ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_GROUP);
        activityService.postActivity(
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                userService.getCurrentUser().getUsername(), groupName,
                activityType, ActivityService.ActivitySource.API, extraInfo);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public Group updateGroup(long orgId, Group group)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException {
        Group toRet = groupServiceInternal.updateGroup(orgId, group);
        ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_GROUP);
        activityService.postActivity(
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                userService.getCurrentUser().getUsername(), group.getGroupName(),
                activityType, ActivityService.ActivitySource.API, extraInfo);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_groups")
    public void deleteGroup(List<Long> groupIds)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException {
        Group sysAdminGroup;
        try {
            sysAdminGroup = groupServiceInternal.getGroupByName(SYSTEM_ADMIN_GROUP);
        } catch (GroupNotFoundException e) {
            throw new ServiceLayerException("The System Admin group is not found", e);
        }

        if (CollectionUtils.isNotEmpty(groupIds)) {
            if (groupIds.contains(sysAdminGroup.getId())) {
                throw new ServiceLayerException("Deleting the System Admin group is not allowed.");
            }
        }
        List<Group> groups = groupServiceInternal.getGroups(groupIds);
        groupServiceInternal.deleteGroup(groupIds);
        for (Group g : groups) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.DELETED;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_GROUP);
            activityService.postActivity(
                    studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                    userService.getCurrentUser().getUsername(), g.getGroupName(),
                    activityType, ActivityService.ActivitySource.API, extraInfo);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public Group getGroup(long groupId) throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroup(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort)
            throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroupMembers(groupId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public int getGroupMembersTotal(final long groupId) throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroupMembersTotal(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {
        List<User> users = groupServiceInternal.addGroupMembers(groupId, userIds, usernames);
        for (User user : users) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.ADD_USER_TO_GROUP;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(
                    studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                    userService.getCurrentUser().getUsername(), user.getUsername(),
                    activityType, ActivityService.ActivitySource.API, extraInfo);
        }
        return users;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {
        Group group = getGroup(groupId);
        generalLockService.lock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        try {
            if (group.getGroupName().equals(SYSTEM_ADMIN_GROUP)) {
                List<User> members = getGroupMembers(groupId, 0, Integer.MAX_VALUE, StringUtils.EMPTY);
                if (CollectionUtils.isNotEmpty(members)) {
                    List<User> membersAfterRemove = new ArrayList<User>();
                    membersAfterRemove.addAll(members);
                    members.forEach(m -> {
                        if (CollectionUtils.isNotEmpty(userIds)) {
                            if (userIds.contains(m.getId())) {
                                membersAfterRemove.remove(m);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(usernames)) {
                            if (usernames.contains(m.getUsername())) {
                                membersAfterRemove.remove(m);
                            }
                        }
                    });
                    if (CollectionUtils.isEmpty(membersAfterRemove)) {
                        throw new ServiceLayerException("Removing all members of the System Admin group is not allowed." +
                                " We must have at least one system administrator.");
                    }
                }
            }
            List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);

            groupServiceInternal.removeGroupMembers(groupId, userIds, usernames);

            for (User user : users) {
                ActivityService.ActivityType activityType = ActivityService.ActivityType.REMOVE_USER_FROM_GROUP;
                Map<String, String> extraInfo = new HashMap<String, String>();
                extraInfo.put(KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                activityService.postActivity(
                        studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                        userService.getCurrentUser().getUsername(), user.getUsername(),
                        activityType, ActivityService.ActivitySource.API, extraInfo);
            }
        } finally {
            generalLockService.unlock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        }
    }

    public GroupServiceInternal getGroupServiceInternal() {
        return groupServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public OrganizationServiceInternal getOrganizationServiceInternal() {
        return organizationServiceInternal;
    }

    public void setOrganizationServiceInternal(OrganizationServiceInternal organizationServiceInternal) {
        this.organizationServiceInternal = organizationServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public ActivityService getActivityService() {
        return activityService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
