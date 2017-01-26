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

    void addUserToGroup(String groupName, String user);

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
}
