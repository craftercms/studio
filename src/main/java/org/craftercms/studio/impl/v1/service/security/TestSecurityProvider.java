/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_FIRSTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_LASTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_USERNAME;

/**
 */
public class TestSecurityProvider implements SecurityProvider {

    private final static Map<String, Set<String>> USER_GROUPS = new HashMap<String, Set<String>>() {{
        Set<String> adminGroups = new HashSet<String>();
        adminGroups.add("crafter-admin");
        adminGroups.add("crafter-create-sites");
        Set<String> authorGroups = new HashSet<String>();
        authorGroups.add("crafter-author");
        Set<String> approverGroups = new HashSet<String>();
        approverGroups.add("crafter-approver");
        put("admin", adminGroups);
        put("author", authorGroups);
        put("approver", approverGroups);
    }};

    private final static Map<String, Map<String, Object>> USER_PROFILES = new HashMap<String, Map<String, Object>>() {{
        Map<String, Object> adminProfile = new HashMap<String, Object>();
        adminProfile.put(KEY_USERNAME, "admin");
        adminProfile.put(KEY_EMAIL, "evaladmin@example.com");
        adminProfile.put(KEY_FIRSTNAME, "Joe");
        adminProfile.put(KEY_LASTNAME, "Admin");

        Map<String, Object> authorProfile = new HashMap<String, Object>();
        authorProfile.put(KEY_USERNAME, "author");
        authorProfile.put(KEY_EMAIL, "evalauthor@example.com");
        authorProfile.put(KEY_FIRSTNAME, "Joe");
        authorProfile.put(KEY_LASTNAME, "Author");

        Map<String, Object> approverProfile = new HashMap<String, Object>();
        authorProfile.put(KEY_USERNAME, "approver");
        authorProfile.put(KEY_EMAIL, "evalapprover@example.com");
        authorProfile.put(KEY_FIRSTNAME, "Joe");
        authorProfile.put(KEY_LASTNAME, "Approver");

        put("admin", adminProfile);
        put("author", authorProfile);
        put("approver", approverProfile);
        put(null, new HashMap<String, Object>());
    }};

    private final static List<String> USER_FAKETICKETS = new ArrayList<String>() {{
        add("admin_FAKETICKET");
        add("author_FAKETICKET");
        add("approver_FAKETICKET");
    }};

    public Set<String> getUserGroups(String user) {
        return USER_GROUPS.get(user);
    }

    public Set<String> getUserGroupsPerSite(String user, String site) {
        return USER_GROUPS.get(user);
    }

    Map<String, String> activeUser = new HashMap<String, String>();
    Map<String, String> activeProcess = new HashMap<String, String>();

    public String getCurrentUser() {
        RequestContext context = RequestContext.getCurrent();
        String username = null;

        if(context!=null) {
            username = activeUser.get(KEY_USERNAME);
        }
        else {
             username = activeProcess.get(KEY_USERNAME);
        }

        return username;
    }

    public Map<String, Object> getUserProfile(String user) {

        return USER_PROFILES.get(user);
    }

    public boolean validateTicket(String ticket) {
        String theTicket = ticket;
        RequestContext context = RequestContext.getCurrent();

        if(theTicket == null) {
            if(context != null) {
                theTicket = activeUser.get("ticket");
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
                activeUser.put(KEY_USERNAME, username);
                activeUser.put("ticket", ticket);

             }
             else {
                activeProcess.put(KEY_USERNAME, username);
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
    public boolean addUserToGroup(String siteId, String groupName, String user) {
        // TODO: DB: Implement this ?
        return false;
    }

    @Override
    public String getCurrentToken() {
        return activeProcess.get("ticket");
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
    public boolean logout() {
        activeUser.remove(KEY_USERNAME);
        activeUser.remove("ticket");
        return true;
    }

    @Override
    public void addContentWritePermission(String path, String group) {
        // do nothing
    }

    @Override
    public void addConfigWritePermission(String path, String group) {
        // do nothing
    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email,
                              boolean externallyManaged) {
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
    public boolean createGroup(String groupName, String description, String siteId, boolean externallyManaged) {
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
