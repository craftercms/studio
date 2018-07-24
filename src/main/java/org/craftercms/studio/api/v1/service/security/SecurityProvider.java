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

package org.craftercms.studio.api.v1.service.security;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface SecurityProvider {

    Set<String> getUserGroups(String user);

    Set<String> getUserGroupsPerSite(String user, String site);

    String getCurrentUser();

    Map<String, Object> getUserProfile(String user);

    String authenticate(String username, String password) throws BadCredentialsException, AuthenticationSystemException;

    boolean validateTicket(String ticket);

    boolean logout();

    void addUserGroup(String groupName);

    void addUserGroup(String parentGroup, String groupName);

    String getCurrentToken();

    /**
     * Check if a group exists
     *
     * @param siteId site Id
     * @param groupName group name
     * @return true if group exists, false otherwise
     */
    boolean groupExists(String siteId, String groupName);

    /**
     * Check if a user exists
     *
     * @param username username
     * @return true if user exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * Check if a user is in a group or not
     *
     * @param siteId site
     * @param groupName group name
     * @param username username
     * @return true if user exists in that group, false otherwise
     */
    boolean userExistsInGroup(String siteId, String groupName, String username);

    /**
     * Add user to the group
     * @param siteId site id
     * @param groupName group name
     * @param user username
     */
    boolean addUserToGroup(String siteId, String groupName, String user) throws UserAlreadyExistsException,
            UserNotFoundException, GroupNotFoundException, SiteNotFoundException;

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
     * @param externallyManaged true if externally managed, otherwise false
     * @return true if success, otherwise false
     */
    boolean createUser(String username, String password, String firstName, String lastName, String email,
                       boolean externallyManaged) throws UserAlreadyExistsException;

    /**
     * Delete user with given username
     *
     * @param username
     * @return true if user is deleted
     */
    boolean deleteUser(String username) throws UserNotFoundException;

    /**
     * Update user details
     *
     * @param username
     * @param firstName
     * @param lastName
     * @param email
     * @return true if user details are successfully updated
     */
    boolean updateUser(String username, String firstName, String lastName, String email)
            throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Enable/disable user with given username
     *
     * @param username username
     * @param enabled true: enable user; false: disable user
     * @return true if user is successfully enabled
     */
    boolean enableUser(String username, boolean enabled) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Get status for given user
     *
     * @param username
     * @return user status
     */
    Map<String, Object> getUserStatus(String username) throws UserNotFoundException;

    /**
     * Create group with given parameters
     *
     * @param groupName
     * @param description
     * @param siteId
     * @param externallyManaged true if externally managed, otherwise false
     * @return true if group is successfully created
     */
    boolean createGroup(String groupName, String description, String siteId, boolean externallyManaged)
            throws GroupAlreadyExistsException, SiteNotFoundException;

    /**
     * Get all users
     *
     * @return List of all users
     */
    List<Map<String, Object>> getAllUsers(int start, int number);

    /**
     * Get all users
     *
     * @return List of all users
     */
    int getAllUsersTotal();

    /**
     * Get all users for given site
     *
     * @param site
     * @param start
     * @param number @return
     */
    List<Map<String, Object>> getUsersPerSite(String site, int start, int number) throws SiteNotFoundException;

    /**
     * Get number of all users for given site
     *
     * @param site
     * @return total number of users per site
     */
    int getUsersPerSiteTotal(String site) throws SiteNotFoundException;

    /**
     * Get group for given site id with given group name
     *
     * @param site site id
     * @param group group name
     * @return group details
     */
    Map<String, Object> getGroup(String site, String group) throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Get all groups
     * @param start start index
     * @param number Number of records to retrieve in the result set
     * @return list of all groups and its details
     */
    List<Map<String, Object>> getAllGroups(int start, int number);

    /**
     * Get all groups for given site
     *
     * @param site site id
     * @param start
     * @param number
     * @return list of groups per site
     */
    List<Map<String, Object>> getGroupsPerSite(String site, int start, int number) throws SiteNotFoundException;

    /**
     * Get number of all groups for given site
     *
     * @param site site id
     * @return total number of groups
     */
    int getGroupsPerSiteTotal(String site) throws SiteNotFoundException;

    /**
     * Get all users for given site and group
     * @param site site id
     * @param group group name
     * @param start start index
     * @param number number of records to retrieve in the result set
     * @return list of users of the group paginated
     */
    List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int number)
            throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Get number of all users for given site and group
     * @param site site id
     * @param group group name
     * @return total number of users for given group
     */
    int getUsersPerGroupTotal(String site, String group) throws
            GroupNotFoundException, SiteNotFoundException;

    /**
     * Update group with given parameters
     *
     * @param groupName
     * @param description
     * @param siteId
     * @return true if group is successfully updated
     */
    boolean updateGroup(String siteId, String groupName, String description)
            throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Delete group with given site id and group name
     *
     * @param groupName
     * @param siteId
     * @return true if group is successfully deleted
     */
    boolean deleteGroup(String siteId, String groupName) throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Remove user from the group
     * @param siteId site id
     * @param groupName group name
     * @param user username
     */
    boolean removeUserFromGroup(String siteId, String groupName, String user)
            throws UserNotFoundException, GroupNotFoundException, SiteNotFoundException;

    /**
     * Change password
     * @param username username
     * @param current current password
     * @param newPassword new password
     * @return true if password is succcessfully changed
     */
    boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException;

    /**
     * Set user password
     * @param username username
     * @param newPassword new password
     * @return true if password is successfully set
     */
    boolean setUserPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Check if given user is a system user
     *
     * @param username username
     * @return true if user is system user
     */
    boolean isSystemUser(String username) throws UserNotFoundException;
}
