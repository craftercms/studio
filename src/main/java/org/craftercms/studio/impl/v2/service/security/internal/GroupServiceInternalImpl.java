/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.security.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class GroupServiceInternalImpl implements GroupServiceInternal {

    private GroupDAO groupDao;
    private UserServiceInternal userServiceInternal;
    private ConfigurationService configurationService;

    @Override
    public Group getGroup(long groupId) throws GroupNotFoundException, ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID, groupId);

        Group group;
        try {
            group = groupDao.getGroup(params);
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
    public Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_NAME, groupName);

        Group group;
        try {
            group = groupDao.getGroupByName(params);
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
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID, groupId);
        params.put(GROUP_NAME, groupName);

        try {
            Integer result = groupDao.groupExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException {
        // Prepare parameters
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);

        try {
            return groupDao.getAllGroupsForOrganization(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllGroupsTotal(long orgId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, orgId);

        try {
            return groupDao.getAllGroupsForOrganizationTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public Group createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException,
                                                                                           ServiceLayerException {
        if (groupExists(-1, groupName)) {
            throw new GroupAlreadyExistsException("Group '" + groupName + "' already exists");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, groupName);
        params.put(GROUP_DESCRIPTION, groupDescription);

        try {
            groupDao.createGroup(params);

            Group group = new Group();
            group.setId((Long) params.get(ID));
            group.setGroupName(groupName);
            group.setGroupDescription(groupDescription);

            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public Group updateGroup(long orgId, Group group) throws GroupNotFoundException, ServiceLayerException {
        if (!groupExists(group.getId(), StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + group.getId() + "'");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ID, group.getId());
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, group.getGroupName());
        params.put(GROUP_DESCRIPTION, group.getGroupDescription());

        try {
            groupDao.updateGroup(params);

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

        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_IDS, groupIds);

        try {
            groupDao.deleteGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws GroupNotFoundException,
                                                                                               ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID, groupId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);

        try {
            return groupDao.getGroupMembers(params);
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
            return groupDao.getGroupMembersTotal(Collections.singletonMap(GROUP_ID, groupId));
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> addGroupMembers(long groupId, List<Long> userIds,
                                      List<String> usernames) throws GroupNotFoundException, UserNotFoundException,
                                                                     ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }

        List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(GROUP_ID, groupId);

        try {
            groupDao.addGroupMembers(params);

            return users;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds,
                                   List<String> usernames) throws GroupNotFoundException, UserNotFoundException,
                                                                  ServiceLayerException {
        if (!groupExists(groupId, StringUtils.EMPTY)) {
            throw new GroupNotFoundException("No group found for id '" + groupId+ "'");
        }

        List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(GROUP_ID, groupId);

        try {
            groupDao.removeGroupMembers(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<String> getSiteGroups(String siteId) throws ServiceLayerException {
        Map<String, List<String>> groupRoleMapping;
        try {
            groupRoleMapping = configurationService.geRoleMappings(siteId);
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

}
