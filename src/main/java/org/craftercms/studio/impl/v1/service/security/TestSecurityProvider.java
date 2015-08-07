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

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;

import javax.servlet.http.HttpSession;

/**
 */
public class TestSecurityProvider implements SecurityProvider {

    private final static Map<String, Set<String>> USER_GROUPS = new HashMap<String, Set<String>>() {{
        Set<String> adminGroups = new HashSet<String>();
        adminGroups.add("crafter-admin");
        adminGroups.add("crafter-create-sites");
        Set<String> authorGroups = new HashSet<String>();
        authorGroups.add("crafter-author");
        put("admin", adminGroups);
        put("author", authorGroups);
    }};

    private final static Map<String, Map<String, String>> USER_PROFILES = new HashMap<String, Map<String, String>>() {{
        Map<String, String> adminProfile = new HashMap<String, String>();
        adminProfile.put("username", "admin");
        adminProfile.put("email", "joe.admin@craftersoftware.com");
        adminProfile.put("firstName", "Joe");
        adminProfile.put("lastName", "Admin");
        
        Map<String, String> authorProfile = new HashMap<String, String>();
        authorProfile.put("username", "author");
        authorProfile.put("email", "joe.author@craftersoftware.com");
        authorProfile.put("firstName", "Joe");
        authorProfile.put("lastName", "Author");

        put("admin", adminProfile);
        put("author", authorProfile);
        put(null, new HashMap<String, String>());
    }};

    private final static List<String> USER_FAKETICKETS = new ArrayList<String>() {{
        add("admin_FAKETICKET");
        add("author_FAKETICKET");
    }};

    public Set<String> getUserGroups(String user) {
        return USER_GROUPS.get(user);
    }

    Map<String, String> activeUser = new HashMap<String, String>();
    Map<String, String> activeProcess = new HashMap<String, String>();

    public String getCurrentUser() {
        RequestContext context = RequestContext.getCurrent();
        String username = null;

        if(context!=null) {
            username = activeUser.get("username"); 
            //HttpSession httpSession = context.getRequest().getSession();
            //(String)httpSession.getValue("username");
        }
        else {
             username = activeProcess.get("username"); 
        }

        return username;
    }

    public Map<String, String> getUserProfile(String user) {
       
        return USER_PROFILES.get(user);
    }

    public boolean validateTicket(String ticket) {
        String theTicket = ticket;
        RequestContext context = RequestContext.getCurrent();
       
        if(theTicket == null) {
            if(context != null) {
                theTicket = activeUser.get("ticket");
                //HttpSession httpSession = context.getRequest().getSession();
                //if(httpSession != null) {
                    //theTicket = (String)httpSession.getValue("ticket");
                //}
                //}
            }
            else {
                theTicket = activeProcess.get("ticket");    
            }
        }

        return USER_FAKETICKETS.contains(theTicket);
    }

    public String authenticate(String username, String password) {
        RequestContext context = RequestContext.getCurrent();
        String ticket = null;
        
        if(getUserProfile(username) != null) {
            ticket = username + "_FAKETICKET";

            if(context != null) {
                //     HttpSession httpSession = context.getRequest().getSession();
                //     if(httpSession != null) {
                //         httpSession.putValue("username", username);
                //         httpSession.putValue("ticket", ticket);
                //     }
                activeUser.put("username", username);
                activeUser.put("ticket", ticket);
       
             }
             else {
                activeProcess.put("username", username);
                activeProcess.put("ticket", ticket);
             }
        }

    	return ticket;
    }

    @Override
    public void addUserGroup(String groupName) {

    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {

    }

    @Override
    public void addUserToGroup(String groupName, String user) {

    }

    @Override
    public String getCurrentToken() {
        return activeProcess.get("ticket");
    }

    @Override
    public boolean logout() {
        activeUser.remove("username");
        activeUser.remove("ticket");
        return true;
    }
}
