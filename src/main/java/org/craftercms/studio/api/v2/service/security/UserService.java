/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.api.v2.service.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.security.NormalizedRole;
import org.craftercms.studio.model.Site;
import org.jspecify.annotations.NonNull;

/**
 * Provides operations to manage users
 */
public interface UserService {

	/**
	 * Get paginated list of all users for site filtered by keyword
	 *
	 * @param site site identifier
	 * @param keyword keyword to filter users
	 * @param offset offset for pagination
	 * @param limit limit number of users to return per page
	 * @param sort sort order
	 * @param showDisabled if true, include disabled users
	 * @return requested page of list of users
	 * @throws ServiceLayerException if there is an error fetching the users
	 */
	Collection<User> getAllUsersForSite(String site, String keyword, int offset, int limit, String sort,
			boolean showDisabled)
			throws ServiceLayerException;

	/**
	 * Get paginated list of all users filtered by keyword
	 *
	 * @param keyword keyword to filter users
	 * @param offset offset for pagination
	 * @param limit limit number of users to return per page
	 * @param sort sort order
	 * @param showDisabled if true, include disabled users
	 * @return requested page of list of users
	 * @throws ServiceLayerException if there is an error fetching the users
	 */
	Collection<User> getAllUsers(String keyword, int offset, int limit, String sort, boolean showDisabled)
			throws ServiceLayerException;

	/**
	 * Get total number of users for site filtered by keyword
	 *
	 * @param site site identifier
	 * @param keyword keyword to filter users
	 * @param showDisabled if true, include disabled users
	 * @return total number of users for site filtered by keyword
	 * @throws ServiceLayerException if there is an error fetching the total
	 * number of users
	 */
	int getAllUsersForSiteTotal(String site, String keyword, boolean showDisabled) throws ServiceLayerException;

	/**
	 * Get total number of users filtered by keyword
	 *
	 * @param keyword keyword to filter user
	 * @param showDisabled if true, include disabled users
	 * @return total number of users filtered by keyword
	 * @throws ServiceLayerException if there is an error fetching the total
	 * number of users
	 */
	int getAllUsersTotal(String keyword, boolean showDisabled) throws ServiceLayerException;

	/**
	 * Get user by id or username
	 *
	 * @param userId user id
	 * @param username username
	 * @return user
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException if the user is not found
	 */
	@NonNull
	User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Indicates if a user exists with the given username
	 *
	 * @param username the username
	 * @return true if the user exists, false otherwise
	 * @throws ServiceLayerException general service error
	 */
	boolean userExists(String username) throws ServiceLayerException;

	/**
	 * Indicates if a user exists with the given username or user id
	 *
	 * @param userId the user id
	 * @param username the username
	 * @return true if the user exists, false otherwise
	 * @throws ServiceLayerException general service error
	 */
	boolean userExists(long userId, String username) throws ServiceLayerException;

	/**
	 * Create a new user
	 *
	 * @param user user to create
	 * @return created user
	 * @throws UserAlreadyExistsException if the user already exists
	 * @throws ServiceLayerException general service error
	 */
	User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException;

	/**
	 * Update a user
	 *
	 * @param user user to update
	 * @throws UserNotFoundException if the user is not found
	 * @throws ServiceLayerException general service error
	 * @throws UserExternallyManagedException if the user is externally managed
	 */
	void updateUser(User user) throws UserNotFoundException, ServiceLayerException, UserExternallyManagedException;

	/**
	 * Enable or disable users
	 *
	 * @param userIds list of user ids
	 * @param usernames list of usernames
	 * @param enabled true to enable, false to disable
	 * @return list of users that were enabled or disabled
	 * @throws UserNotFoundException if a user is not found
	 * @throws ServiceLayerException general service error
	 * @throws UserExternallyManagedException if trying to enable or disable an
	 * externally managed user
	 */
	List<User> enableUsers(List<Long> userIds, List<String> usernames,
			boolean enabled) throws UserNotFoundException, ServiceLayerException, UserExternallyManagedException;

	/**
	 * Get user by git name. Special use case because git stores user as string
	 * of first and last name separated by ' '
	 *
	 * @param gitName first and last name separated with ' '
	 * @return user
	 */
	User getUserByGitName(String gitName) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get the list of Sites the given user belongs to (meaning it belongs to at
	 * least one group mapped in the site role mappings)
	 *
	 * @param userId the user id
	 * @param username the username
	 * @return the list of sites the user belongs to
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException if the user is not found
	 */
	List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get the list of roles the given user has in the given site
	 *
	 * @param userId the user id
	 * @param username the username
	 * @param site the site
	 * @return the list of roles the user has in the site
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException if the user is not found
	 */
	List<NormalizedRole> getUserSiteRoles(long userId, String username, String site)
			throws ServiceLayerException, UserNotFoundException;

	/**
	 * Retrieves the list of groups associated with the specified user.
	 *
	 * @param userId the unique identifier of the user
	 * @param username the username of the user
	 * @return a list of {@link Group} objects associated with the user
	 * @throws UserNotFoundException if no user is found with the given userId
	 * or username
	 * @throws ServiceLayerException if an error occurs while accessing the
	 * service layer
	 */
	List<Group> getUserGroups(long userId, String username) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Check if given user has system_admin role
	 *
	 * @param username user
	 * @return true if user is system_admin, false otherwise
	 */
	boolean isSystemAdmin(String username);

	/**
	 * Get the list of sites the current authenticated user belongs to
	 *
	 * @return the list of sites the current authenticated user belongs to
	 * @throws AuthenticationException if there is no user authenticated
	 * @throws ServiceLayerException general service error
	 */
	List<Site> getCurrentUserSites() throws AuthenticationException, ServiceLayerException;

	/**
	 * Get the list of roles the current authenticated user has in the given
	 * site
	 *
	 * @param site the site
	 * @return the list of roles the current authenticated user has in the site
	 * @throws AuthenticationException if there is no user authenticated
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException if the user is not found
	 */
	List<String> getCurrentUserSiteRoles(String site) throws AuthenticationException, ServiceLayerException, UserNotFoundException;

	/**
	 * Get a list of users by ids or usernames
	 *
	 * @param userIds list of user ids
	 * @param usernames list of usernames
	 * @return list of users
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException if a user is not found
	 */
	List<User> getUsersByIdOrUsername(List<Long> userIds,
			List<String> usernames) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Retrieves the list of groups associated with the specified user,
	 * optionally filtering to include only externally managed groups.
	 *
	 * @param userId the unique identifier of the user
	 * @param username the username of the user
	 * @param filterExternallyManagedGroups if true, only externally managed
	 * groups are returned; if false, all groups are returned
	 * @return a list of {@link Group} objects associated with the user,
	 * filtered based on the flag
	 * @throws UserNotFoundException if no user is found with the given userId
	 * or username
	 * @throws ServiceLayerException if an error occurs while accessing the
	 * service layer
	 */
	List<Group> getUserGroups(long userId, String username, boolean filterExternallyManagedGroups) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Get the global roles for a user
	 *
	 * @param username the username
	 * @return the list global roles
	 */
	Collection<NormalizedRole> getUserGlobalRoles(String username) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Forgot password feature for given username
	 *
	 * @param username user that forgot password
	 * @throws ServiceLayerException general service error
	 */
	void forgotPassword(String username)
			throws ServiceLayerException;

	/**
	 * Get a forgot password token for given username
	 *
	 * @param username the username
	 * @return forgot password token
	 */
	String getForgotPasswordToken(String username) throws ServiceLayerException;

	/**
	 * User changes password
	 *
	 * @param username username
	 * @param current current password
	 * @param newPassword new password
	 * @return user whose password is successfully changed
	 * @throws PasswordDoesNotMatchException password does not match with stored
	 * @throws UserExternallyManagedException user is externally managed
	 * @throws ServiceLayerException general service error
	 * @throws AuthenticationException authentication error
	 * @throws UserNotFoundException user not found
	 */
	User changePassword(String username, String current, String newPassword)
			throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException,
			AuthenticationException, UserNotFoundException;

	/**
	 * Set user password - forgot password token
	 *
	 * @param token forgot password token
	 * @param newPassword new password
	 * @return uses whose password is successfully set
	 * @throws UserNotFoundException user not found
	 * @throws UserExternallyManagedException user is externally managed
	 * @throws ServiceLayerException general service error
	 */
	User setPassword(String token, String newPassword)
			throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException;

	/**
	 * Admin resets the user password
	 *
	 * @param username username
	 * @param newPassword new password
	 * @return true if user's password is successfully reset
	 * @throws UserNotFoundException user not found
	 * @throws UserExternallyManagedException user is externally managed
	 * @throws ServiceLayerException general service error
	 */
	boolean resetPassword(String username, String newPassword) throws UserNotFoundException,
			UserExternallyManagedException, ServiceLayerException;

	/**
	 * Validate forgot password token
	 *
	 * @param token forgot password token to validate
	 * @return true if token is valid otherwise false
	 * @throws UserNotFoundException user not found
	 * @throws UserExternallyManagedException user is externally managed
	 * @throws ServiceLayerException general service error
	 */
	boolean validateToken(String token) throws UserNotFoundException, UserExternallyManagedException,
			ServiceLayerException;

	/**
	 * Get the properties for the given site &amp; the current user
	 *
	 * @param siteId the id of the site
	 * @return the current properties
	 * @throws ServiceLayerException if there is any error fetching the
	 * properties
	 */
	Map<String, Map<String, String>> getUserProperties(String siteId) throws ServiceLayerException;

	/**
	 * Update or add properties for the given site &amp; the current user
	 *
	 * @param siteId the id of the site
	 * @param propertiesToUpdate the properties to update or add
	 * @return the updated properties
	 * @throws ServiceLayerException if there is any error updating or fetching
	 * the properties
	 */
	Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
			throws ServiceLayerException;

	/**
	 * Delete properties for the given site &amp; current user
	 *
	 * @param siteId the id of the site
	 * @param propertiesToDelete the list of keys to delete
	 * @return the updated properties
	 * @throws ServiceLayerException if there is any error deleting or fetching
	 * the properties
	 */
	Map<String, String> deleteUserProperties(String siteId, List<String> propertiesToDelete)
			throws ServiceLayerException;

	/**
	 * Get permissions of the current authenticated user for given site
	 *
	 * @param site site identifier
	 * @return list of permissions
	 */
	Set<String> getCurrentUserSitePermissions(String site)
			throws ServiceLayerException, UserNotFoundException, ExecutionException;

	/**
	 * Check if the current authenticated user has given permissions for given
	 * site
	 *
	 * @param site site identifier
	 * @param permissions list of permissions to check
	 * @return map with values true or false for each given permission
	 */
	Map<String, Boolean> hasCurrentUserSitePermissions(String site, Collection<String> permissions)
			throws ServiceLayerException, UserNotFoundException, ExecutionException;

	/**
	 * Get global permissions of the current authenticated user
	 *
	 * @return list of global permissions
	 */
	Set<String> getCurrentUserGlobalPermissions()
			throws ServiceLayerException, UserNotFoundException, ExecutionException;

	/**
	 * Check if the current authenticated user has given global permissions
	 *
	 * @param permissions list of permissions to check
	 * @return map with values true or false for each given permission
	 */
	Map<String, Boolean> hasCurrentUserGlobalPermissions(List<String> permissions)
			throws ServiceLayerException, UserNotFoundException, ExecutionException;

	/**
	 * Indicates if a user is a member of a site
	 *
	 * @param username the username
	 * @param siteId the site id
	 * @return true if the user is a member of the site, false otherwise
	 */
	boolean isSiteMember(String username, String siteId);

	/**
	 * Check if a user is a site admin for the given site
	 *
	 * @param username the username
	 * @param siteId the site id
	 * @return true if the user is a site admin, false otherwise
	 * @throws ServiceLayerException if there is an error checking if the user
	 * is a site admin
	 * @throws UserNotFoundException if the user is not found
	 */
	boolean isSiteAdmin(String username, String siteId) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get permissions of the given user for given site and path
	 *
	 * @param site site identifier
	 * @param path path
	 * @param user user
	 * @return list of permissions
	 * @throws ServiceLayerException if there is an error fetching the permissions
	 */
	Set<String> getUserPermissions(String site, String path, String user) throws ServiceLayerException, UserNotFoundException;

}
