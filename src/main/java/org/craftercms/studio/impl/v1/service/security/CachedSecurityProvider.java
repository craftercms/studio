/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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

    public Map<String, String> getUserProfile(String user) {
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
    public void addUserToGroup(String groupName, String user) {
        provider.addUserToGroup(groupName, user);
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
}