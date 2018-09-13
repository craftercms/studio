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
import org.apache.commons.collections4.MapUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.Site;
import org.craftercms.studio.model.User;

import java.util.*;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserDAO userDAO;
    private GroupService groupService;
    private ConfigurationService configurationService;
    private SecurityProvider securityProvider;
    private SiteService siteService;
    private EntitlementValidator entitlementValidator;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<User> getAllUsersForSite(long orgId, String siteId, int offset, int limit, String sort)
            throws ServiceLayerException {
        List<String> groupNames = groupService.getSiteGroups(siteId);
        return securityProvider.getAllUsersForSite(orgId, groupNames, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserTO> userTOs;
        try {
            userTOs = userDAO.getAllUsers(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        List<User> users = new ArrayList<User>();
        userTOs.forEach(userTO -> {
            User u = new User();
            u.setId(userTO.getId());
            u.setUsername(userTO.getUsername());
            u.setFirstName(userTO.getFirstName());
            u.setLastName(userTO.getLastName());
            u.setEmail(userTO.getEmail());
            u.setEnabled(userTO.isEnabled());
            u.setExternallyManaged(userTO.getExternallyManaged() != 0);
            users.add(u);
        });
        return users;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public int getAllUsersForSiteTotal(long orgId, String siteId) throws ServiceLayerException {
        List<String> groupNames = groupService.getSiteGroups(siteId);
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_NAMES, groupNames);
        try {
            return userDAO.getAllUsersForSiteTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public int getAllUsersTotal() throws ServiceLayerException {
        try {
            return userDAO.getAllUsersTotal();
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_users")
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException {
        try {
            long start = 0;
            if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                start = System.currentTimeMillis();
                logger.debug("Starting entitlement validation");
            }
            entitlementValidator.validateEntitlement(Module.STUDIO, EntitlementType.USER, getAllUsersTotal(), 1);
            if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                logger.debug("Validation completed, duration : {0} ms", System.currentTimeMillis() - start);
            }
        } catch (EntitlementException e) {
            throw new ServiceLayerException("Unable to complete request due to entitlement limits. Please contact " +
                                            "your system administrator.", e);
        }
        return securityProvider.createUser(user);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_users")
    public void updateUser(User user) throws ServiceLayerException {
        securityProvider.updateUser(user);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_users")
    public void deleteUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException {
        securityProvider.deleteUsers(userIds, usernames);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException,
                                                                           UserNotFoundException {
        return securityProvider.getUserByIdOrUsername(userId, username);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_users")
    public List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled)
            throws ServiceLayerException, UserNotFoundException {
        return securityProvider.enableUsers(userIds, usernames, enabled);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_users")
    public List<Site> getUserSites(long userId, String username) throws ServiceLayerException {
        List<Site> sites = new ArrayList<>();
        Set<String> allSites = siteService.getAllAvailableSites();
        List<Group> userGroups = getUserGroups(userId, username);

        // Iterate all sites. If the user has any of the site groups, it has access to the site
        for (String siteId : allSites) {
            List<String> siteGroups = groupService.getSiteGroups(siteId);
            if (userGroups.stream().anyMatch(userGroup -> siteGroups.contains(userGroup.getName()))) {
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
    public List<String> getUserSiteRoles(long userId, String username, String site) throws ServiceLayerException {
        List<Group> groups = getUserGroups(userId, username);

        if (CollectionUtils.isNotEmpty(groups)) {
            Map<String, List<String>> roleMappings = configurationService.getSiteRoleMappingsConfig(site);
            Set<String> userRoles = new LinkedHashSet<>();

            if (MapUtils.isNotEmpty(roleMappings)) {
                for (Group group : groups) {
                    String groupName = group.getName();
                    List<String> roles = roleMappings.get(groupName);
                    if (CollectionUtils.isNotEmpty(roles)) {
                        userRoles.addAll(roles);
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
        Authentication authentication = securityProvider.getAuthentication();
        if (authentication != null) {
            String username = authentication.getUsername();
            User user;
            try {
                user = securityProvider.getUserByIdOrUsername(0, username);
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
        Authentication authentication = securityProvider.getAuthentication();
        if (authentication != null) {
            return getUserSites(-1, authentication.getUsername());
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public List<String> getCurrentUserSiteRoles(String site) throws AuthenticationException, ServiceLayerException {
        Authentication authentication = securityProvider.getAuthentication();
        if (authentication != null) {
            return getUserSiteRoles(-1, authentication.getUsername(), site);
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public List<Group> getUserGroups(long userId, String username) throws ServiceLayerException {
        return securityProvider.getUserGroups(userId, username);
    }

    @Override
    public boolean isUserMemberOfGroup(String username, String groupName) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_NAME, groupName);
        params.put(USERNAME, username);
        try {
            int result = userDAO.isUserMemberOfGroup(params);
            return result > 0;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
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

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

}
