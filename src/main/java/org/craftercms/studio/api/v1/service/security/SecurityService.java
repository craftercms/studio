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

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface SecurityService {

    String STUDIO_SESSION_TOKEN_ATRIBUTE = "studioSessionToken";

	/**
	 * authenticate a user. returns ticket
	 * @param username
	 * @param password
	 */
	String authenticate(String username, String password) throws BadCredentialsException, AuthenticationSystemException;

	/**
	 * Returns the username of the current user OR NULL if no user is authenticated
	 */
	String getCurrentUser();

    String getCurrentToken();

    Set<String> getUserRoles(String site, String user);

    Map<String, Object> getUserProfile(String user);

    Set<String> getUserPermissions(String site, String path, String user, List<String> groups);

    boolean validateTicket(String token);

    void addUserGroup(String groupName);

    void addUserGroup(String parentGroup, String groupName);

    void reloadConfiguration(String site);

    void reloadGlobalConfiguration();

    boolean logout();

    /**
     * Check if user exists
     *
     * @param username username
     * @return true if user exists
     */
    boolean userExists(String username);

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
    boolean createUser(String username, String password, String firstName, String lastName, String email) throws UserAlreadyExistsException;

    /**
     * Delete user with given username
     *
     * @param username
     * @return true if user is successfully deleted
     */
    boolean deleteUser(String username) throws UserNotFoundException, DeleteUserNotAllowedException;

    /**
     * Update user details
     *
     * @param username
     * @param firstName
     * @param lastName
     * @param email
     * @return true if user is successfully updated
     */
    boolean updateUser(String username, String firstName, String lastName, String email) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Enable/disable user with given username
     *
     * @param username username
     * @param enabled true: enable user; false: disable user
     * @return true if user is successfully enabled/disabled
     */
    boolean enableUser(String username, boolean enabled) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Create group with given parameters
     *
     * @param groupName
     * @param description
     * @param siteId
     * @return true if group is successfully created
     */
    boolean createGroup(String groupName, String description, String siteId) throws GroupAlreadyExistsException, SiteNotFoundException;

    /**
     * Get status for given user
     *
     * @param username username
     * @return user status
     */
    Map<String, Object> getUserStatus(String username) throws UserNotFoundException;

    /**
     * Get all users
     *
     * @return list of all users
     */
    List<Map<String, Object>> getAllUsers(int start, int number);

    /**
     * Get all users
     *
     * @return number of all users
     */
    int getAllUsersTotal();

    /**
     * Get all users for given site
     *
     * @param site
     * @param start
     * @param number
     * @return get list of users per site, paginated
     */
    List<Map<String, Object>> getUsersPerSite(String site, int start, int number) throws SiteNotFoundException;
    /**
     * Get number of all users for given site
     *
     * @param site
     * @return total number of users for site
     */
    int getUsersPerSiteTotal(String site) throws SiteNotFoundException;


    /**
     * Get group for given site with given name
     *
     * @param site site id
     * @param group group name
     * @return group details
     */
    Map<String, Object> getGroup(String site, String group) throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Get all groups
     *
     * @param start start index
     * @param number Number of records to retrieve in the result set
     */
    List<Map<String, Object>> getAllGroups(int start, int number);

    /**
     * Get all groups for given site
     *
     * @param site site id
     * @param start start index
     * @param number number of records to retrieve in the result set
     * @return list of groups per site, paginated
     */
    List<Map<String, Object>> getGroupsPerSite(String site, int start, int number) throws SiteNotFoundException;

    /**
     * Get number of all groups for given site
     *
     * @param site site id
     * @return total number of groups for site
     */
    int getGroupsPerSiteTotal(String site) throws SiteNotFoundException;

    /**
     * Get all users for given site and group
     *
     * @param site site id
     * @param group group name
     * @param start start index
     * @param number number of records to retrieve in the result set
     * @return list of users
     */
    List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int number) throws
            GroupNotFoundException, SiteNotFoundException;

    /**
     * Get number of all users for given site and group
     *
     * @param site site id
     * @param group group name
     * @return list of users
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
    boolean updateGroup(String siteId, String groupName, String description) throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Delete group for given site with given name
     *
     * @param site site id
     * @param group group name
     * @return true if group is successfully deleted
     */
    boolean deleteGroup(String site, String group) throws GroupNotFoundException, SiteNotFoundException;

    /**
     * Add user to the group
     *
     * @param siteId site id
     * @param groupName group name
     * @param username username
     * @return true if user is successfully added to the group
     */
    boolean addUserToGroup(String siteId, String groupName, String username) throws UserAlreadyExistsException,
            UserNotFoundException, GroupNotFoundException, SiteNotFoundException;

    /**
     * Remove user from the group
     *
     * @param siteId site id
     * @param groupName group name
     * @param username username
     * @return true if user is successfully removed from the group
     */
    boolean removeUserFromGroup(String siteId, String groupName, String username) throws UserNotFoundException,
            GroupNotFoundException, SiteNotFoundException;

    /**
     * Forgot password for given user
     *
     * @param username username
     * @return forgot password
     */
    Map<String, Object> forgotPassword(String username) throws ServiceException, UserNotFoundException, UserExternallyManagedException;

    /**
     * Forgot password token to validate
     *
     * @param token token
     * @return true if given token is valid
     */
    boolean validateToken(String token) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Change password
     *
     * @param username username
     * @param current current password
     * @param newPassword new password
     * @return true if user's password is successfully changed
     */
    boolean changePassword(String username, String current, String newPassword) throws UserNotFoundException, PasswordDoesNotMatchException, UserExternallyManagedException;

    /**
     * Set user password - forgot password token
     *
     * @param token forgot password token
     * @param newPassword new password
     * @return true if uses's password is successfully set
     */
    Map<String, Object> setUserPassword(String token, String newPassword) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Reset user password
     *
     * @param username username
     * @param newPassword new password
     * @return true if user's password is successfully reset
     */
    boolean resetPassword(String username, String newPassword) throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Validate user's active session
     *
     * @param request
     * @return true if user session is valid
     */
    boolean validateSession(HttpServletRequest request);

    /**
     * Check if given user is site admin
     * @param username user
     * @return true if user belongs to admin group
     */
    boolean isSiteAdmin(String username);
}
