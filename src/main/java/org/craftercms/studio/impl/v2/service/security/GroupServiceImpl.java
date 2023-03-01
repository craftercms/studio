/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.OrganizationNotFoundException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.OrganizationServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class GroupServiceImpl implements GroupService {

    private final GroupServiceInternal groupServiceInternal;
    private final OrganizationServiceInternal organizationServiceInternal;
    private final UserServiceInternal userServiceInternal;
    private final GeneralLockService generalLockService;
    private final StudioConfiguration studioConfiguration;
    private final AuditServiceInternal auditServiceInternal;
    private final SiteService siteService;

    @ConstructorProperties({"groupServiceInternal", "organizationServiceInternal",
            "userServiceInternal", "generalLockService",
            "studioConfiguration", "auditServiceInternal",
            "siteService"})
    public GroupServiceImpl(final GroupServiceInternal groupServiceInternal, final OrganizationServiceInternal organizationServiceInternal,
                            final UserServiceInternal userServiceInternal, final GeneralLockService generalLockService,
                            final StudioConfiguration studioConfiguration, final AuditServiceInternal auditServiceInternal,
                            final SiteService siteService) {
        this.groupServiceInternal = groupServiceInternal;
        this.organizationServiceInternal = organizationServiceInternal;
        this.userServiceInternal = userServiceInternal;
        this.generalLockService = generalLockService;
        this.studioConfiguration = studioConfiguration;
        this.auditServiceInternal = auditServiceInternal;
        this.siteService = siteService;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_GROUPS)
    public List<Group> getAllGroups(long orgId, String keyword, int offset, int limit, String sort)
            throws ServiceLayerException, OrganizationNotFoundException {
        // Security check
        if (organizationServiceInternal.organizationExists(orgId)) {
            return groupServiceInternal.getAllGroups(orgId, keyword, offset, limit, sort);
        } else {
            throw new OrganizationNotFoundException();
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_GROUPS)
    public int getAllGroupsTotal(long orgId, String keyword)
            throws ServiceLayerException, OrganizationNotFoundException {
        if (organizationServiceInternal.organizationExists(orgId)) {
            return groupServiceInternal.getAllGroupsTotal(orgId, keyword);
        } else {
            throw new OrganizationNotFoundException();
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_GROUPS)
    public Group createGroup(long orgId, String groupName,
                             String groupDescription, boolean externallyManaged)
            throws GroupAlreadyExistsException, ServiceLayerException, AuthenticationException {
        Group toRet = groupServiceInternal.createGroup(orgId, groupName, groupDescription, externallyManaged);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(userServiceInternal.getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(groupName);
        auditLog.setPrimaryTargetType(TARGET_TYPE_GROUP);
        auditLog.setPrimaryTargetValue(groupName);
        auditServiceInternal.insertAuditLog(auditLog);

        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_GROUPS)
    public Group updateGroup(long orgId, Group group)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException, GroupExternallyManagedException {
        checkExternallyManagedGroup(Arrays.asList(group.getId()));

        Group toRet = groupServiceInternal.updateGroup(orgId, group);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_UPDATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(userServiceInternal.getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(toRet.getGroupName());
        auditLog.setPrimaryTargetType(TARGET_TYPE_GROUP);
        auditLog.setPrimaryTargetValue(toRet.getGroupName());
        auditServiceInternal.insertAuditLog(auditLog);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_DELETE_GROUPS)
    public void deleteGroup(List<Long> groupIds)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException, GroupExternallyManagedException {
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

        checkExternallyManagedGroup(groupIds);

        List<Group> groups = groupServiceInternal.getGroups(groupIds);
        groupServiceInternal.deleteGroup(groupIds);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_DELETE);
        auditLog.setActorId(userServiceInternal.getCurrentUser().getUsername());
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        auditLog.setPrimaryTargetType(TARGET_TYPE_GROUP);
        auditLog.setPrimaryTargetValue(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        List<AuditLogParameter> parameters = new ArrayList<>();
        for (Group g : groups) {
            AuditLogParameter parameter = new AuditLogParameter();
            parameter.setTargetId(Long.toString(g.getId()));
            parameter.setTargetType(TARGET_TYPE_GROUP);
            parameter.setTargetValue(g.getGroupName());
            parameters.add(parameter);
        }
        auditLog.setParameters(parameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_GROUPS)
    public Group getGroup(long groupId) throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroup(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_GROUPS)
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort)
            throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroupMembers(groupId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_GROUPS)
    public int getGroupMembersTotal(final long groupId) throws ServiceLayerException, GroupNotFoundException {
        return groupServiceInternal.getGroupMembersTotal(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_GROUPS)
    public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {
        List<User> users = groupServiceInternal.addGroupMembers(groupId, userIds, usernames);
        Group group = groupServiceInternal.getGroup(groupId);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        List<AuditLogParameter> parameters = new ArrayList<>();
        for (User user : users) {
            AuditLogParameter parameter = new AuditLogParameter();
            parameter.setTargetId(Long.toString(user.getId()));
            parameter.setTargetType(TARGET_TYPE_USER);
            parameter.setTargetValue(user.getUsername());
            parameters.add(parameter);
        }
        auditLog.setParameters(parameters);
        auditLog.setOperation(OPERATION_ADD_MEMBERS);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(userServiceInternal.getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(Long.toString(groupId));
        auditLog.setPrimaryTargetType(TARGET_TYPE_GROUP);
        auditLog.setPrimaryTargetValue(group.getGroupName());
        auditServiceInternal.insertAuditLog(auditLog);
        return users;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_GROUPS)
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {
        Group group = getGroup(groupId);
        generalLockService.lock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        try {
            if (group.getGroupName().equals(SYSTEM_ADMIN_GROUP)) {
                List<User> members = getGroupMembers(groupId, 0, Integer.MAX_VALUE, StringUtils.EMPTY);
                if (CollectionUtils.isNotEmpty(members)) {
                    List<User> membersAfterRemove = new ArrayList<>(members);
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
            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_REMOVE_MEMBERS);
            auditLog.setActorId(userServiceInternal.getCurrentUser().getUsername());
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setPrimaryTargetId(Long.toString(group.getId()));
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(group.getGroupName());
            List<AuditLogParameter> parameters = new ArrayList<>();
            for (User user : users) {
                AuditLogParameter parameter = new AuditLogParameter();
                parameter.setTargetId(Long.toString(user.getId()));
                parameter.setTargetType(TARGET_TYPE_USER);
                parameter.setTargetValue(user.getUsername());
                parameters.add(parameter);
            }
            auditLog.setParameters(parameters);
            auditServiceInternal.insertAuditLog(auditLog);
        } finally {
            generalLockService.unlock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        }
    }

    /**
     * Check if groups contain external managed group member
     * If matched, the operation must not be permitted.
     * @param groupIds list of group IDs
     * @throws ServiceLayerException
     * @throws GroupNotFoundException
     * @throws GroupExternallyManagedException
     */
    private void checkExternallyManagedGroup(List<Long> groupIds) throws ServiceLayerException, GroupNotFoundException, GroupExternallyManagedException {
        List<Group> groups = groupServiceInternal.getGroups(groupIds);
        if (groups.stream().anyMatch(group -> group.isExternallyManaged())) {
            throw new GroupExternallyManagedException("Cannot update externally managed groups.");
        }
    }
}
