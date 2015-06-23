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
public class MappedSecurityProvider implements SecurityProvider {

    private static final Logger logger = LoggerFactory.getLogger(MappedSecurityProvider.class);

    private Map<String, SecurityProvider> providerMap;
    private String providerType;

    public Map<String, SecurityProvider> getProviderMap() {
        return providerMap;
    }

    public void setProviderMap(Map<String, SecurityProvider> map) {
        providerMap = map;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String type) {
        providerType = type;
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
        providerType = "default";
    }

    public Set<String> getUserGroups(String user) {
    	SecurityProvider provider = lookupProvider(providerType);
        return provider.getUserGroups(user); 
    };

    public String getCurrentUser() {
        SecurityProvider provider = lookupProvider(providerType);
        return provider.getCurrentUser(); 
    };

    public Map<String, String> getUserProfile(String user) {
    	SecurityProvider provider = lookupProvider(providerType);
        return provider.getUserProfile(user); 
    }

    public String authenticate(String username, String password) {
        SecurityProvider provider = lookupProvider(providerType);
        return provider.authenticate(username, password); 
    }

    public boolean validateTicket(String ticket){
        SecurityProvider provider = lookupProvider(providerType);
        return provider.validateTicket(ticket); 
    }

    @Override
    public void addUserGroup(String groupName) {
        SecurityProvider provider = lookupProvider(providerType);
        provider.addUserGroup(groupName);
    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {
        SecurityProvider provider = lookupProvider(providerType);
        provider.addUserGroup(parentGroup, groupName);
    }

    @Override
    public String getCurrentToken() {
        SecurityProvider provider = lookupProvider(providerType);
        return provider.getCurrentToken();
    }
}
