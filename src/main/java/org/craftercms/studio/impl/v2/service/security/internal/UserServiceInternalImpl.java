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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.security.UserPermissions;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.craftercms.studio.model.User;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SYSTEM_PERMISSIONS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EMAIL;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENABLED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.FIRST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LAST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCALE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PASSWORD;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TIMEZONE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class UserServiceInternalImpl implements UserServiceInternal {

    private static final String ROLE_CONFIG_KEY = "role";
    private static final String NAME_CONFIG_KEY = "name";
    private static final String ALLOWED_PERMISSIONS_CONFIG_KEY = "allowed-permissions";
    private static final String PERMISSION_CONFIG_KEY = "permission";

    private StudioConfiguration studioConfiguration;
    private UserDAO userDao;
    private ContentRepository contentRepository;
    private GroupServiceInternal groupServiceInternal;

    @Override
    public Set<String> getUserPermissions(String username, UserPermissions.Scope scope, String parameter) throws ServiceLayerException {
        Set<String> permissions = null;
        List<GroupTO> userGroups = getUserGroups(-1, username);
        Set<String> userGroupsNames = new HashSet<String>();
        userGroups.forEach(group -> {
            userGroupsNames.add(group.getGroupName());
        });
        switch (scope) {
            case SYSTEM:
                permissions = getSystemScopePermissions(userGroupsNames);
        }
        return permissions;
    }

    @Override
    public User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        UserTO userTO;
        try {
            userTO = userDao.getUserByIdOrUsername(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        if(userTO == null) {
            throw new UserNotFoundException("No user found for username '" + username + "' or id '" + userId + "'");
        }
        User user = new User();
        user.setId(userTO.getId());
        user.setUsername(userTO.getUsername());
        user.setFirstName(userTO.getFirstName());
        user.setLastName(userTO.getLastName());
        user.setEmail(userTO.getEmail());
        user.setEnabled(userTO.isEnabled());
        user.setExternallyManaged(userTO.getExternallyManaged() != 0);
        return user;
    }

    private Set<String> getSystemScopePermissions(Set<String> userGroups) throws ConfigurationException {
        Set<String> userPermissions = new HashSet<String>();
        String configPath = getSystemPermissionsConfigPath();

        try (InputStream is = contentRepository.getContent(StringUtils.EMPTY, configPath)) {
            HierarchicalConfiguration config = ConfigUtils.readXmlConfiguration(is);
            List<HierarchicalConfiguration> rolesConfig = config.configurationsAt(ROLE_CONFIG_KEY);
            if (CollectionUtils.isNotEmpty(rolesConfig)) {
                for (HierarchicalConfiguration roleConfig : rolesConfig) {
                    String role = roleConfig.getString(NAME_CONFIG_KEY);
                    if (userGroups.contains(role)) {
                        List<HierarchicalConfiguration<String>> perms =
                                roleConfig.childConfigurationsAt(ALLOWED_PERMISSIONS_CONFIG_KEY);
                        if (CollectionUtils.isNotEmpty(perms)) {
                            perms.forEach( perm -> {
                                userPermissions.add(perm.getString(PERMISSION_CONFIG_KEY));
                            });
                        }
                    }
                }
            } else {
                throw new ConfigurationException("No menu items found in global menu config");
            }
        } catch (Exception e) {
            throw new ConfigurationException("Unable to read system permissions config @ " + configPath, e);
        }
        return null;
    }

    protected String getRequiredStringProperty(Configuration config, String key) throws ConfigurationException {
        String property = config.getString(key);
        if (StringUtils.isEmpty(property)) {
            throw new ConfigurationException("Missing required property '" + key + "'");
        } else {
            return property;
        }
    }

    @Override
    public List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort)
            throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAMES, groupNames);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, StringUtils.EMPTY);
        List<UserTO> userTOS;
        try {
            userTOS = userDao.getAllUsersForSite(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        List<User> users = new ArrayList<User>();
        userTOS.forEach(userTO -> {
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
    public List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserTO> userTOs;
        try {
            userTOs = userDao.getAllUsers(params);
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
    public int getAllUsersForSiteTotal(long orgId, String siteId) throws ServiceLayerException {
        List<String> groupNames = groupServiceInternal.getSiteGroups(siteId);
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_NAMES, groupNames);
        try {
            return userDao.getAllUsersForSiteTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllUsersTotal() throws ServiceLayerException {
        try {
            return userDao.getAllUsersTotal();
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException {
        if (userExists(user.getUsername())) {
            throw new UserAlreadyExistsException();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(USERNAME, user.getUsername());
        String hashedPassword = CryptoUtils.hashPassword(user.getPassword());
        params.put(PASSWORD, hashedPassword);
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(EXTERNALLY_MANAGED, user.isExternallyManaged() ? 1 : 0);
        params.put(TIMEZONE, StringUtils.EMPTY);
        params.put(LOCALE, StringUtils.EMPTY);
        params.put(ENABLED, user.isEnabled() ? 1 : 0);
        try {
            userDao.createUser(params);

            user.setId((Long) params.get(ID));

            return user;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public boolean userExists(String username) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME, username);
        try {
            Integer result = userDao.userExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void updateUser(User user) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, user.getId());
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(TIMEZONE, StringUtils.EMPTY);
        params.put(LOCALE, StringUtils.EMPTY);
        try {
            userDao.updateUser(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException {
        List<Long> allUserIds = new ArrayList<Long>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        try {
            if (CollectionUtils.isNotEmpty(usernames)) {
                allUserIds.addAll(userDao.getUserIdsForUsernames(params));
            }
            if (CollectionUtils.isNotEmpty(allUserIds)) {
                params = new HashMap<String, Object>();
                params.put(USER_IDS, allUserIds);
                userDao.deleteUsers(params);
            }
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) throws ServiceLayerException, UserNotFoundException {
        List<User> users = findUsers(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(ENABLED, enabled? 1: 0);
        try {
            userDao.enableUsers(params);
            return findUsers(userIds, usernames);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> findUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException,
            UserNotFoundException {
        List<User> users = new LinkedList<>();
        for(long userId : userIds) {
            users.add(getUserByIdOrUsername(userId, Long.toString(userId)));
        }
        for(String username : usernames) {
            Optional<User> user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
            if(!user.isPresent()) {
                users.add(getUserByIdOrUsername(-1, username));
            }
        }
        return users;
    }

    @Override
    public List<GroupTO> getUserGroups(long userId, String username) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        List<GroupTO> gDAOs;
        try {
            gDAOs = userDao.getUserGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return gDAOs;
    }

    @Override
    public boolean isUserMemberOfGroup(String username, String groupName) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(GROUP_NAME, groupName);
        params.put(USERNAME, username);
        try {
            int result = userDao.isUserMemberOfGroup(params);
            return result > 0;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    protected String getSystemPermissionsConfigPath() {
        return UrlUtils.concat(getGlobalConfigPath(), getSystemPermissionsFileName());
    }

    protected String getGlobalConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    protected String getSystemPermissionsFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SYSTEM_PERMISSIONS_FILE_NAME);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public GroupServiceInternal getGroupServiceInternal() {
        return groupServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }
}
