/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.*;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.util.StudioConfiguration;

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

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

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
            throw new java.lang.RuntimeException("provider type '"+key+"' not found.  Check server config.");
        }

        return provider;
    }

    /**
     * default constructor 
     */
    public MappedSecurityProvider() {
        providerMap = new HashMap<String, SecurityProvider>();
    }

    public Set<String> getUserGroups(String user) {
    	SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUserGroups(user); 
    };

    public String getCurrentUser() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getCurrentUser(); 
    };

    public Map<String, Object> getUserProfile(String user) {
    	SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUserProfile(user); 
    }

    public String authenticate(String username, String password) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.authenticate(username, password); 
    }

    public boolean validateTicket(String ticket){
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.validateTicket(ticket); 
    }

    @Override
    public void addUserGroup(String groupName) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.addUserGroup(groupName);
    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.addUserGroup(parentGroup, groupName);
    }

    @Override
    public String getCurrentToken() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getCurrentToken();
    }

    @Override
    public boolean addUserToGroup(String siteId, String groupName, String user) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.addUserToGroup(siteId, groupName, user);
    }

    @Override
    public boolean logout() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.logout();
    }

    @Override
    public void addContentWritePermission(String path, String group) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.addContentWritePermission(path, group);
    }

    @Override
    public void addConfigWritePermission(String path, String group) {
        SecurityProvider provider = lookupProvider(getProviderType());
        provider.addContentWritePermission(path, group);
    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email) throws UserAlreadyExistsException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.createUser(username, password, firstName, lastName, email);
    }

    @Override
    public boolean deleteUser(String username) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.deleteUser(username);
    }

    @Override
    public boolean updateUser(String username, String firstName, String lastName, String email) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.updateUser(username, firstName, lastName, email);
    }

    @Override
    public boolean enableUser(String username, boolean enabled) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.enableUser(username, enabled);
    }

    @Override
    public Map<String, Object> getUserStatus(String username) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUserStatus(username);
    }

    @Override
    public boolean createGroup(String groupName, String description, String siteId) throws GroupAlreadyExistsException {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.createGroup(groupName, description, siteId);
    }

    @Override
    public List<Map<String, Object>> getAllUsers() {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getAllUsers();
    }

    @Override
    public List<Map<String, Object>> getUsersPerSite(String site) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUsersPerSite(site);
    }

    @Override
    public Map<String, Object> getGroup(String site, String group) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getGroup(site, group);
    }

    @Override
    public List<Map<String, Object>> getAllGroups(int start, int end) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getAllGroups(start, end);
    }

    @Override
    public List<Map<String, Object>> getGroupsPerSite(String site) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getGroupsPerSite(site);
    }

    @Override
    public List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int end) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.getUsersPerGroup(site, group, start, end);
    }

    @Override
    public boolean updateGroup(String siteId, String groupName, String description) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.updateGroup(siteId, groupName, description);
    }

    @Override
    public boolean deleteGroup(String siteId, String groupName) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.deleteGroup(siteId, groupName);
    }

    @Override
    public boolean removeUserFromGroup(String siteId, String groupName, String user) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.removeUserFromGroup(siteId, groupName, user);
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.changePassword(username, current, newPassword);
    }

    @Override
    public boolean setUserPassword(String username, String newPassword) {
        SecurityProvider provider = lookupProvider(getProviderType());
        return provider.setUserPassword(username, newPassword);
    }
}
