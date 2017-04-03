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

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.*;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;

/**
 */
public class CachedSecurityProvider implements SecurityProvider {

    private static final Logger logger = LoggerFactory.getLogger(CachedSecurityProvider.class);

    SecurityProvider provider;
    HashMap<String, Object> cache;

    public SecurityProvider getProvider() {
        return provider;
    }

    public void setProvider(SecurityProvider provider) {
        this.provider = provider;
    }


    /**
     * default constructor
     */
    public CachedSecurityProvider() {
    }

    public Set<String> getUserGroups(String user) {
        Set<String> value = null;
        value = (Set<String>)getCachedObject("getUserGroups-"+user);

        if(value == null) {
            value = provider.getUserGroups(user);

            if(value != null) {
                cacheObject("getUserGroups-"+user, value);
            }
        }

        return value;
    };

    public String getCurrentUser() {
        return provider.getCurrentUser();
    };

    public Map<String, Object> getUserProfile(String user) {
        return provider.getUserProfile(user);
    }

    public String authenticate(String username, String password) {
        return provider.authenticate(username, password);
    }

    public boolean validateTicket(String ticket){
        return provider.validateTicket(ticket);
    }

    @Override
    public void addUserGroup(String groupName) {
        provider.addUserGroup(groupName);
    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {
        provider.addUserGroup(parentGroup, groupName);
    }

    @Override
    public String getCurrentToken() {
        return provider.getCurrentToken();
    }

    @Override
    public boolean groupExists(final String siteId, final String groupName) {
        return false;
    }

    @Override
    public boolean userExists(final String username) {
        return false;
    }

    @Override
    public boolean userExistsInGroup(final String siteId, final String groupName, final String username) {
        return false;
    }

    @Override
    public boolean addUserToGroup(String siteId, String groupName, String user) throws UserNotFoundException, UserAlreadyExistsException, GroupNotFoundException {
        return provider.addUserToGroup(siteId, groupName, user);
    }

    @Override
    public boolean logout() {
        return provider.logout();
    }



    protected void cacheObject(String key, Object value) {
        HashMap<String, Object> cache = this.getCache();
        cache.put(key, value);
    }

    protected Object getCachedObject(String key) {
        Object value = null;

        HashMap<String, Object> cache = this.getCache();
        value = cache.get(key);

        if(value != null) {
            logger.debug("HIT: "+ key);
        }

        return value;
    }

    long cacheAge = System.currentTimeMillis();
    HashMap<String, Object> globalCache = new HashMap<String, Object>();

    protected HashMap<String, Object> getCache() {
        HashMap<String, Object> cache = null;

        if(System.currentTimeMillis() - cacheAge > 10000){
            cacheAge = System.currentTimeMillis();
            globalCache = new HashMap<String, Object>();
        }

        return globalCache;
    }

    @Override
    public void addContentWritePermission(String path, String group) {
        provider.addContentWritePermission(path, group);
    }

    @Override
    public void addConfigWritePermission(String path, String group) {
        provider.addConfigWritePermission(path, group);
    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean deleteUser(String username) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean updateUser(String username, String firstName, String lastName, String email) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean enableUser(String username, boolean enabled) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public Map<String, Object> getUserStatus(String username) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public boolean createGroup(String groupName, String description, String siteId) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public List<Map<String, Object>> getAllUsers(int start, int number) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public int getAllUsersTotal() {
        // TODO: DB: Implement this ?
        return 0;
    }

    @Override
    public List<Map<String, Object>> getUsersPerSite(String site, int start, int number) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public int getUsersPerSiteTotal(String site) throws SiteNotFoundException {
        // TODO: DB: Implement this ?
        return 0;
    }

    @Override
    public Map<String, Object> getGroup(String site, String group) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllGroups(int start, int number) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public List<Map<String, Object>> getGroupsPerSite(String site, int start, int number) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public int getGroupsPerSiteTotal(String site) {
        // TODO: DB: Implement this ?
        return 0;
    }

    @Override
    public List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int number) {
        // TODO: DB: Implement this ?
        return null;
    }

    @Override
    public int getUsersPerGroupTotal(String site, String group) throws GroupNotFoundException {
        // TODO: DB: Implement this ?
        return 0;
    }

    @Override
    public boolean updateGroup(String siteId, String groupName, String description) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean deleteGroup(String siteId, String groupName) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean removeUserFromGroup(String siteId, String groupName, String user) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean setUserPassword(String username, String newPassword) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public boolean isSystemUser(String username) throws UserNotFoundException {
        // TODO: DB: Implement this ?
        return false;
    }
}
