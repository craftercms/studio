/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Site;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface UserService {

    /**
     * Get paginated list of all users for site filtered by keyword
     * @param orgId organization identifier
     * @param site site identifier
     * @param keyword keyword to filter users
     * @param offset offset for pagination
     * @param limit limit number of users to return per page
     * @param sort sort order
     * @return requested page of list of users
     * @throws ServiceLayerException
     */
    List<User> getAllUsersForSite(long orgId, String site, String keyword, int offset, int limit, String sort)
            throws ServiceLayerException;

    /**
     * Get paginated list of all users filtered by keyword
     * @param keyword keyword to filter users
     * @param offset offset for pagination
     * @param limit limit number of users to return per page
     * @param sort sort order
     * @return requested page of list of users
     * @throws ServiceLayerException
     */
    List<User> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException;

    /**
     * Get total number of users for site filtered by keyword
     * @param orgId organization identifier
     * @param site site identifier
     * @param keyword keyword to filter users
     * @return total number of users for site filtered by keyword
     * @throws ServiceLayerException
     */
    int getAllUsersForSiteTotal(long orgId, String site, String keyword) throws ServiceLayerException;

    /**
     * Get total number of users filtered by keyword
     * @param keyword keyword to filter user
     * @return total number of users filtered by keyword
     * @throws ServiceLayerException
     */
    int getAllUsersTotal(String keyword) throws ServiceLayerException;

    User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException;

    void updateUser(User user) throws ServiceLayerException, UserNotFoundException, AuthenticationException;

    void deleteUsers(List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException;

    User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException, UserNotFoundException;

    List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException;

    List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException;

    List<String> getUserSiteRoles(long userId, String username, String site)
            throws ServiceLayerException, UserNotFoundException;

    AuthenticatedUser getCurrentUser() throws AuthenticationException, ServiceLayerException;

    List<Site> getCurrentUserSites() throws AuthenticationException, ServiceLayerException;

    List<String> getCurrentUserSiteRoles(String site) throws AuthenticationException, ServiceLayerException;

    /**
     * Forgot password feature for given username
     *
     * @param username user that forgot password
     * @return true if success
     *
     * @throws ServiceLayerException general service error
     * @throws UserNotFoundException user not found
     * @throws UserExternallyManagedException user is externally managed
     */
    boolean forgotPassword(String username)
            throws ServiceLayerException, UserNotFoundException, UserExternallyManagedException;

    /**
     * User changes password
     *
     * @param username username
     * @param current current password
     * @param newPassword new password
     * @return user whose password is successfully changed
     *
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
     *
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
     *
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
     *
     * @throws UserNotFoundException user not found
     * @throws UserExternallyManagedException user is externally managed
     * @throws ServiceLayerException general service error
     */
    boolean validateToken(String token) throws UserNotFoundException, UserExternallyManagedException,
            ServiceLayerException;

    /**
     * Get the properties for the given site & the current user
     * @param siteId the id of the site
     * @return the current properties
     * @throws ServiceLayerException if there is any error fetching the properties
     */
    Map<String, Map<String, String>> getUserProperties(String siteId) throws ServiceLayerException;

    /**
     * Update or add properties for the given site & the current user
     * @param siteId the id of the site
     * @param propertiesToUpdate the properties to update or add
     * @return the updated properties
     * @throws ServiceLayerException if there is any error updating or fetching the properties
     */
    Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
            throws ServiceLayerException;

    /**
     * Delete properties for the given site & current user
     * @param siteId the id of the site
     * @param propertiesToDelete the list of keys to delete
     * @return the updated properties
     * @throws ServiceLayerException if there is any error deleting or fetching the properties
     */
    Map<String, String> deleteUserProperties(String siteId, List<String> propertiesToDelete)
            throws ServiceLayerException;

    /**
     * Get permissions of the current authenticated user for given site
     * @param site site identifier
     * @return
     */
    List<String> getCurrentUserSitePermissions(String site)
            throws ServiceLayerException, UserNotFoundException, ExecutionException;

    /** Check if the current authenticated user has given permissions for given site
     *
     * @param site site identifier
     * @param permissions list of permissions to check
     * @return map with values true or false for each given permission
     */
    Map<String, Boolean> hasCurrentUserSitePermissions(String site, List<String> permissions)
            throws ServiceLayerException, UserNotFoundException, ExecutionException;

    /**
     * Get global permissions of the current authenticated user
     * @return
     */
    List<String> getCurrentUserGlobalPermissions()
            throws ServiceLayerException, UserNotFoundException, ExecutionException;

    /** Check if the current authenticated user has given global permissions
     *
     * @param permissions list of permissions to check
     * @return map with values true or false for each given permission
     */
    Map<String, Boolean> hasCurrentUserGlobalPermissions(List<String> permissions)
            throws ServiceLayerException, UserNotFoundException, ExecutionException;
}
