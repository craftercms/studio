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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParamter;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DISABLE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ENABLE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserServiceInternal userServiceInternal;
    private ConfigurationService configurationService;
    private GroupServiceInternal groupServiceInternal;
    private SiteService siteService;
    private EntitlementValidator entitlementValidator;
    private GeneralLockService generalLockService;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<User> getAllUsersForSite(long orgId, String siteId, int offset, int limit, String sort)
            throws ServiceLayerException {
        List<String> groupNames = groupServiceInternal.getSiteGroups(siteId);
        return userServiceInternal.getAllUsersForSite(orgId, groupNames, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException {
        return userServiceInternal.getAllUsers(offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public int getAllUsersForSiteTotal(long orgId, String siteId) throws ServiceLayerException {
        return userServiceInternal.getAllUsersForSiteTotal(orgId, siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public int getAllUsersTotal() throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal();
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_users")
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException {
        try {
            entitlementValidator.validateEntitlement(EntitlementType.USER, 1);
        } catch (EntitlementException e) {
            throw new ServiceLayerException("Unable to complete request due to entitlement limits. Please contact " +
                                            "your system administrator.", e);
        }
        User toRet = userServiceInternal.createUser(user);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(user.getUsername());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(user.getUsername());
        auditServiceInternal.insertAuditLog(auditLog);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_users")
    public void updateUser(User user) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        userServiceInternal.updateUser(user);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_UPDATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(user.getUsername());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(user.getUsername());
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_users")
    public void deleteUsers(List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException {
        User currentUser = getCurrentUser();

        if (CollectionUtils.containsAny(userIds, Arrays.asList(currentUser.getId())) ||
                CollectionUtils.containsAny(usernames, Arrays.asList(currentUser.getUsername()))) {
            throw new ServiceLayerException("Cannot delete self.");
        }

        generalLockService.lock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        try {
            try {
                Group g = groupServiceInternal.getGroupByName(SYSTEM_ADMIN_GROUP);
                List<User> members =
                        groupServiceInternal.getGroupMembers(g.getId(), 0, Integer.MAX_VALUE, StringUtils.EMPTY);
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
            } catch (GroupNotFoundException e) {
                throw new ServiceLayerException("The System Admin group is not found.", e);
            }

            List<User> toDelete = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
            userServiceInternal.deleteUsers(userIds, usernames);
            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_DELETE);
            auditLog.setActorId(getCurrentUser().getUsername());
            auditLog.setPrimaryTargetId(siteFeed.getSiteId());
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(siteFeed.getName());
            List<AuditLogParamter> paramters = new ArrayList<AuditLogParamter>();
            for (User deletedUser : toDelete) {
                AuditLogParamter paramter = new AuditLogParamter();
                paramter.setTargetId(Long.toString(deletedUser.getId()));
                paramter.setTargetType(TARGET_TYPE_USER);
                paramter.setTargetValue(deletedUser.getUsername());
                paramters.add(paramter);
            }
            auditLog.setParameters(paramters);
            auditServiceInternal.insertAuditLog(auditLog);
        } finally {
            generalLockService.unlock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public User getUserByIdOrUsername(long userId, String username)
            throws ServiceLayerException, UserNotFoundException {
        return userServiceInternal.getUserByIdOrUsername(userId, username);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_users")
    public List<User> enableUsers(List<Long> userIds, List<String> usernames,
                                  boolean enabled) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        List<User> users = userServiceInternal.enableUsers(userIds, usernames, enabled);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setSiteId(siteFeed.getId());
        if (enabled) {
            auditLog.setOperation(OPERATION_ENABLE);
        } else {
            auditLog.setOperation(OPERATION_DISABLE);
        }
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(siteFeed.getSiteId());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(siteFeed.getName());
        List<AuditLogParamter> paramters = new ArrayList<AuditLogParamter>();
        for (User u : users) {
            AuditLogParamter paramter = new AuditLogParamter();
            paramter.setTargetId(Long.toString(u.getId()));
            paramter.setTargetType(TARGET_TYPE_USER);
            paramter.setTargetValue(u.getUsername());
            paramters.add(paramter);
        }
        auditLog.setParameters(paramters);
        auditServiceInternal.insertAuditLog(auditLog);
        return users;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException {
        List<Site> sites = new ArrayList<>();
        Set<String> allSites = siteService.getAllAvailableSites();
        List<Group> userGroups = userServiceInternal.getUserGroups(userId, username);
        boolean isSysAdmin = userGroups.stream().anyMatch(group -> group.getGroupName().equals(SYSTEM_ADMIN_GROUP));

        // Iterate all sites. If the user has any of the site groups, it has access to the site
        for (String siteId : allSites) {
            List<String> siteGroups = groupServiceInternal.getSiteGroups(siteId);
            if (isSysAdmin || userGroups.stream().anyMatch(userGroup -> siteGroups.contains(userGroup.getGroupName()))) {
                try {
                    SiteFeed siteFeed = siteService.getSite(siteId);
                    Site site = new Site();
                    site.setSiteId(siteFeed.getSiteId());
                    site.setDesc(siteFeed.getDescription());

                    sites.add(site);
                } catch (SiteNotFoundException e) {
                    logger.error("Site not found: {0}", e, siteId);
                }
            }
        }

        return sites;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<String> getUserSiteRoles(long userId, String username, String site)
            throws ServiceLayerException, UserNotFoundException {
        List<Group> groups = userServiceInternal.getUserGroups(userId, username);

        if (CollectionUtils.isNotEmpty(groups)) {
            Map<String, List<String>> roleMappings = configurationService.geRoleMappings(site);
            Set<String> userRoles = new LinkedHashSet<>();

            if (MapUtils.isNotEmpty(roleMappings)) {
                for (Group group : groups) {
                    String groupName = group.getGroupName();
                    if (groupName.equals(SYSTEM_ADMIN_GROUP)) {
                        // If sysadmin, return all roles
                        Collection<List<String>> roleSets = roleMappings.values();

                        for (List<String> roleSet : roleSets) {
                            userRoles.addAll(roleSet);
                        }

                        break;
                    } else {
                        List<String> roles = roleMappings.get(groupName);
                        if (CollectionUtils.isNotEmpty(roles)) {
                            userRoles.addAll(roles);
                        }
                    }
                }
            }

            return new ArrayList<>(userRoles);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public AuthenticatedUser getCurrentUser() throws AuthenticationException, ServiceLayerException {
        Authentication authentication = securityService.getAuthentication();
        if (authentication != null) {
            String username = authentication.getUsername();
            User user;
            try {
                user = userServiceInternal.getUserByIdOrUsername(0, username);
            } catch (UserNotFoundException e) {
                throw new ServiceLayerException("Current authenticated user '" + username +
                    "' wasn't found in repository", e);
            }

            if (user != null) {
                AuthenticatedUser authUser = new AuthenticatedUser(user);
                authUser.setAuthenticationType(authentication.getAuthenticationType());

                return authUser;
            } else {
                throw new ServiceLayerException("Current authenticated user '" + username +
                                                "' wasn't found in repository");
            }
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public List<Site> getCurrentUserSites() throws AuthenticationException, ServiceLayerException {
        Authentication authentication = securityService.getAuthentication();
        if (authentication != null) {
            try {
                return getUserSites(-1, authentication.getUsername());
            } catch (UserNotFoundException e) {
                // Shouldn't happen
                throw new IllegalStateException(e);
            }
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public List<String> getCurrentUserSiteRoles(String site) throws AuthenticationException, ServiceLayerException {
        Authentication authentication = securityService.getAuthentication();
        if (authentication != null) {
            try {
                return getUserSiteRoles(-1, authentication.getUsername(), site);
            } catch (UserNotFoundException e) {
                // Shouldn't happen
                throw new IllegalStateException(e);
            }
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public String getCurrentUserSsoLogoutUrl() throws AuthenticationException, ServiceLayerException {
        Authentication authentication = securityService.getAuthentication();
        if (authentication != null) {
            return authentication.getSsoLogoutUrl();
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public GroupServiceInternal getGroupServiceInternal() {
        return groupServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }
}
