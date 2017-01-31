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

package org.craftercms.studio.api.v1.service.security;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface SecurityProvider {

    Set<String> getUserGroups(String user);

    String getCurrentUser();

    Map<String, Object> getUserProfile(String user);

    String authenticate(String username, String password);

    boolean validateTicket(String ticket);

    void addUserGroup(String groupName);

    void addUserGroup(String parentGroup, String groupName);

    String getCurrentToken();

    /**
     * Add user to the group
     * @param siteId site id
     * @param groupName group name
     * @param user username
     */
    boolean addUserToGroup(String siteId, String groupName, String user);

    boolean logout();

    void addContentWritePermission(String path, String group);

    void addConfigWritePermission(String path, String group);

    /**
     * Create new user with given parameters
     *
     * @param username username
     * @param password password
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email address
     * @return true if success, otherwise false
     */
    boolean createUser(String username, String password, String firstName, String lastName, String email);

    /**
     * Delete user with given username
     *
     * @param username
     * @return
     */
    boolean deleteUser(String username);

    /**
     * Update user details
     *
     * @param username
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    boolean updateUser(String username, String firstName, String lastName, String email);

    /**
     * Enable/disable user with given username
     *
     * @param username username
     * @param enabled true: enable user; false: disable user
     * @return
     */
    boolean enableUser(String username, boolean enabled);

    /**
     * Get status for given user
     *
     * @param username
     * @return
     */
    Map<String, Object> getUserStatus(String username);

    /**
     * Create group with given parameters
     *
     * @param groupName
     * @param description
     * @param siteId
     * @return
     */
    boolean createGroup(String groupName, String description, long siteId);

    /**
     * Get all users
     *
     * @return List of all users
     */
    List<Map<String, Object>> getAllUsers();

    /**
     * Get all users for given site
     *
     * @param site
     * @return
     */
    List<Map<String, Object>> getUsersPerSite(String site);

    /**
     * Get group for given site id with given group name
     *
     * @param site site id
     * @param group group name
     * @return
     */
    Map<String, Object> getGroup(String site, String group);

    /**
     * Get all groups
     * @param start start index
     * @param end end index
     * @return
     */
    List<Map<String, Object>> getAllGroups(int start, int end);

    /**
     * Get all groups for given site
     *
     * @param site site id
     * @return
     */
    List<Map<String, Object>> getGroupsPerSite(String site);

    /**
     * Get all users for given site and group
     * @param site site id
     * @param group group name
     * @param start start index
     * @param end end index
     * @return
     */
    List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int end);

    /**
     * Update group with given parameters
     *
     * @param groupName
     * @param description
     * @param siteId
     * @return
     */
    boolean updateGroup(String siteId, String groupName, String description);

    /**
     * Delete group with given site id and group name
     *
     * @param groupName
     * @param description
     * @param siteId
     * @return
     */
    boolean deleteGroup(String siteId, String groupName);
}
