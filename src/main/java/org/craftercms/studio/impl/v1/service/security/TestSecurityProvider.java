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

import java.util.*;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;

/**
 */
public class TestSecurityProvider implements SecurityProvider {

    public Set<String> getUserGroups(String user) {
    	HashSet<String> groups = new HashSet<String>();
    	groups.add("site_global1_SiteManager");
    	return groups;
    };

    public String getCurrentUser() {
    	return "admin";
    };

    public Map<String, String> getUserProfile(String user) {
    	Map<String, String> profile = new HashMap<String, String>();
        profile.put("username", "admin");
        profile.put("email", "joe.blogs@craftersoftware.com");
        profile.put("firstName", "Joe");
        profile.put("lastName", "Blogs");

    	return profile;
    }

    public boolean validateTicket(String ticket){
        return ("FAKETICKET".equals(ticket));
    }

    public String authenticate(String username, String password) {
    	return "FAKETICKET";
    }

    @Override
    public void addUserGroup(String groupName) {

    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {

    }
}
