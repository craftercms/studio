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
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.security.UserPermissions;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.craftercms.studio.model.User;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SYSTEM_PERMISSIONS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

public class UserServiceInternalImpl implements UserServiceInternal {

    private static final String ROLE_CONFIG_KEY = "role";
    private static final String NAME_CONFIG_KEY = "name";
    private static final String ALLOWED_PERMISSIONS_CONFIG_KEY = "allowed-permissions";
    private static final String PERMISSION_CONFIG_KEY = "permission";

    private StudioConfiguration studioConfiguration;
    private UserDAO userDao;
    private ContentRepository contentRepository;

    @Override
    public Set<String> getUserPermissions(String username, UserPermissions.Scope scope, String parameter) throws ConfigurationException {
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

    public List<GroupTO> getUserGroups(long userId, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        List<GroupTO> userGroups = userDao.getUserGroups(params);
        return userGroups;
    }

    protected String getRequiredStringProperty(Configuration config, String key) throws ConfigurationException {
        String property = config.getString(key);
        if (StringUtils.isEmpty(property)) {
            throw new ConfigurationException("Missing required property '" + key + "'");
        } else {
            return property;
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
}
