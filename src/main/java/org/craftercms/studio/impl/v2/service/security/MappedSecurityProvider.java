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

import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_TYPE;

/**
 */
public class MappedSecurityProvider implements SecurityProvider {

    private static final Logger logger = LoggerFactory.getLogger(MappedSecurityProvider.class);

    private Map<String, SecurityProvider> providerMap;
    protected StudioConfiguration studioConfiguration;

    public Map<String, SecurityProvider> getProviderMap() {
        return providerMap;
    }

    public void setProviderMap(Map<String, SecurityProvider> map) {
        providerMap = map;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public String getProviderType() {
        return studioConfiguration.getProperty(SECURITY_TYPE);
    }

    public void registerSecurityProvider(String type, SecurityProvider provider) {
        logger.debug("Registering Security Provider: ({0}) with class: '{1}'", type, (""+provider));
        providerMap.put(type, provider);
    }

    protected SecurityProvider lookupProvider(String key) {
        SecurityProvider provider = providerMap.get(key);

        if(provider == null) {
            throw new RuntimeException("provider type '"+key+"' not found.  Check server config.");
        }

        return provider;
    }

    /**
     * default constructor
     */
    public MappedSecurityProvider() {
        providerMap = new HashMap<String, SecurityProvider>();
    }

    @Override
    public boolean createUser(User user) throws UserAlreadyExistsException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.createUser(user);
    }

    @Override
    public void updateUser(User user) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.updateUser(user);
    }

    @Override
    public void deleteUsers(List<Long> userIds, List<String> usernames) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.deleteUsers(userIds, usernames);
    }

    @Override
    public User getUserByIdOrUsername(long userId, String username) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUserByIdOrUsername(userId, username);
    }

    @Override
    public void enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.enableUsers(userIds, usernames, enabled);
    }

    @Override
    public List<Group> getUserGroups(long userId, String username) {
        SecurityProvider provider =  lookupProvider(getProviderType());
        return provider.getUserGroups(userId, username);
    }

    @Override
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getAllGroups(orgId, offset, limit, sort);
    }

    @Override
    public void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.createGroup(orgId, groupName, groupDescription);
    }

    @Override
    public void updateGroup(long orgId, Group group) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.updateGroup(orgId, group);
    }

    @Override
    public void deleteGroup(List<Long> groupIds) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.deleteGroup(groupIds);
    }

    @Override
    public Group getGroup(long groupId) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getGroup(groupId);
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getGroupMembers(groupId, offset, limit, sort);
    }

    @Override
    public boolean addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.addGroupMembers(groupId, userIds, usernames);
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.removeGroupMembers(groupId, userIds, usernames);
    }

    @Override
    public List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getAllUsersForSite(orgId, groupNames, offset, limit, sort);
    }

    @Override
    public int getAllUsersTotal() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getAllUsersTotal();
    }

    @Override
    public String getCurrentUser() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getCurrentUser();
    }

    @Override
    public String authenticate(String username, String password)
            throws BadCredentialsException, AuthenticationSystemException, EntitlementException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.authenticate(username, password);
    }

    @Override
    public boolean validateTicket(String ticket) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.validateTicket(ticket);
    }

    @Override
    public String getCurrentToken() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getCurrentToken();
    }

    @Override
    public boolean logout() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.logout();
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.changePassword(username, current, newPassword);
    }

    @Override
    public boolean setUserPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.setUserPassword(username, newPassword);
    }

    @Override
    public boolean userExists(String username) {
        SecurityProvider securityProvider = lookupProvider(getProviderType());
        return securityProvider.userExists(username);
    }

    @Override
    public boolean groupExists(String groupName) {
        SecurityProvider securityProvider = lookupProvider(getProviderType());
        return securityProvider.groupExists(groupName);
    }
}
