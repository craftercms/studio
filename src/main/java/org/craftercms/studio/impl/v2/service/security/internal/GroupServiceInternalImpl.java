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

package org.craftercms.studio.impl.v2.service.security.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

public class GroupServiceInternalImpl implements GroupServiceInternal {

    private GroupDAO groupDao;
    private UserServiceInternal userServiceInternal;
    private ConfigurationService configurationService;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public Group getGroup(long groupId) throws GroupNotFoundException, ServiceLayerException {
        Group group;
        try {
            group = groupDao.getGroup(groupId);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }

        if (group != null) {
            return group;
        } else {
            throw new GroupNotFoundException("No group found for id '" + groupId + "'");
        }
    }

    @Override
    public List<Group> getGroups(List<Long> groupIds) throws GroupNotFoundException, ServiceLayerException {
        List<Group> groups;
        try {
            groups = groupDao.getGroups(groupIds);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }

        if (groups != null) {
            return groups;
        } else {
            throw new GroupNotFoundException("No group found for id '" + groupIds + "'");
        }
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
        Group group;
        try {
            group = groupDao.getGroupByName(groupName);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }

        if (group != null) {
            return group;
        } else {
            throw new GroupNotFoundException("No group found for name '" + groupName + "'");
        }
    }

    @Override
    public boolean groupExists(long groupId, String groupName) throws ServiceLayerException {
        try {
            Integer result = groupDao.groupExists(groupId, groupName);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<Group> getAllGroups(long orgId, String keyword, int offset, int limit, String sort)
            throws ServiceLayerException {
        try {
            return groupDao.getAllGroupsForOrganization(orgId, keyword, offset, limit, sort);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllGroupsTotal(long orgId, String keyword) throws ServiceLayerException {
        try {
            return groupDao.getAllGroupsForOrganizationTotal(orgId, keyword);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public Group createGroup(long orgId, String groupName, String groupDescription)
            throws GroupAlreadyExistsException, ServiceLayerException {
        if (groupExists(-1, groupName)) {
            throw new GroupAlreadyExistsException("Group '" + groupName + "' already exists");
        }
        try {
            retryingDatabaseOperationFacade.createGroup(orgId, groupName, groupDescription);
            return groupDao.getGroupByName(groupName);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public Group updateGroup(long orgId, Group group) throws GroupNotFoundException, ServiceLayerException {
        if (!groupExists(group.getId(), StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + group.getId() + "'");
        }
        try {
            retryingDatabaseOperationFacade.updateGroup(group);

            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteGroup(List<Long> groupIds) throws GroupNotFoundException, ServiceLayerException {
        for (Long groupId : groupIds) {
            if (!groupExists(groupId, StringUtils.EMPTY)) {
                throw new GroupNotFoundException("No group found for id '" + groupId + "'");
            }
        }

        try {
            retryingDatabaseOperationFacade.deleteGroups(groupIds);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort)
            throws GroupNotFoundException, ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }

        try {
            return groupDao.getGroupMembers(groupId, offset, limit, sort);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getGroupMembersTotal(final long groupId) throws GroupNotFoundException, ServiceLayerException {
        if(!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }
        try {
            return groupDao.getGroupMembersTotal(groupId);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws GroupNotFoundException, UserNotFoundException, ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }

        List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
        try {
            retryingDatabaseOperationFacade.addGroupMembers(groupId,
                    users.stream().map(User::getId).collect(Collectors.toList()));

            return users;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws GroupNotFoundException, UserNotFoundException, ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }
        List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
        try {
            retryingDatabaseOperationFacade.removeGroupMembers(groupId,
                    users.stream().map(User::getId).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<String> getSiteGroups(String siteId) throws ServiceLayerException {
        Map<String, List<String>> groupRoleMapping;
        try {
            groupRoleMapping = configurationService.getRoleMappings(siteId);
        } catch (ConfigurationException e) {
            throw new ServiceLayerException("Unable to get role mappings config for site '" + siteId + "'", e);
        }

        List<String> groups = new ArrayList<>();
        groups.addAll(groupRoleMapping.keySet());

        return groups;
    }

    public GroupDAO getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDAO groupDao) {
        this.groupDao = groupDao;
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

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
