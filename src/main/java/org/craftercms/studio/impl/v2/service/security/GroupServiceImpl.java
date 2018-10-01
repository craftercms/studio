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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;

public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupDAO groupDAO;
    private ConfigurationService configurationService;
    private SecurityProvider securityProvider;
    private GeneralLockService generalLockService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException {
        return securityProvider.getAllGroups(orgId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public int getAllGroupsTotal(long orgId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        try {
            return groupDAO.getAllGroupsForOrganizationTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_groups")
    public Group createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException,
        ServiceLayerException {
        return securityProvider.createGroup(orgId, groupName, groupDescription);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public Group updateGroup(long orgId, Group group) throws ServiceLayerException {
        return securityProvider.updateGroup(orgId, group);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_groups")
    public void deleteGroup(List<Long> groupIds) throws ServiceLayerException {
        try {
            Group g = getGroupByName(SYSTEM_ADMIN_GROUP);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                if (groupIds.contains(g.getId())) {
                    throw new ServiceLayerException("Deleting System Admin group is not allowed.");
                }
            }
        } catch (GroupNotFoundException e) {
            throw new ServiceLayerException("Unexpected error. System Admin group not found", e);
        }
        securityProvider.deleteGroup(groupIds);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public Group getGroup(long groupId) throws ServiceLayerException {
        return securityProvider.getGroup(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException {
        return securityProvider.getGroupMembers(groupId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException {
        return securityProvider.addGroupMembers(groupId, userIds, usernames);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException {
        Group g = getGroup(groupId);
        generalLockService.lock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        try {
            if (g.getName().equals(SYSTEM_ADMIN_GROUP)) {
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
                        throw new ServiceLayerException("Removing all users from System Admin group is not allowed");
                    }
                }
            }
            securityProvider.removeGroupMembers(groupId, userIds, usernames);
        } finally {
            generalLockService.unlock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        }
    }

    @Override
    public List<String> getSiteGroups(String siteId) throws ServiceLayerException {
        Map<String, List<String>> roleMappingsConfig = configurationService.geRoleMappings(siteId);

        return new ArrayList<>(roleMappingsConfig.keySet());
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        GroupTO groupTO;
        try {
            groupTO = groupDAO.getGroupByName(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        if (groupTO != null) {
            Group g = new Group();
            g.setId(groupTO.getId());
            g.setName(groupTO.getGroupName());
            g.setDesc(groupTO.getGroupDescription());
            return g;
        } else {
            throw new GroupNotFoundException();
        }
    }

    public GroupDAO getGroupDAO() {
        return groupDAO;
    }

    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }
}
