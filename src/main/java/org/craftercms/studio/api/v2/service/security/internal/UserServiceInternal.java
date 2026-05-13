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

package org.craftercms.studio.api.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.security.NormalizedGroup;
import org.craftercms.studio.model.AuthenticatedUser;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

public interface UserServiceInternal {

    @NonNull
    User getUserByIdOrUsername(long userId, String username) throws UserNotFoundException, ServiceLayerException;

    List<User> getUsersByIdOrUsername(List<Long> userIds,
                                      List<String> usernames) throws ServiceLayerException, UserNotFoundException;

    /**
     * Get paginated list of all users for site filtered by keyword
     * @param groupNames group names for site
     * @param keyword keyword to filter users
     * @param offset pagination offset
     * @param limit limit number of users to return per page
     * @param sort sort order
     * @return requested page of list of users
     * @throws ServiceLayerException
     */
    List<User> getAllUsersForSite(List<NormalizedGroup> groupNames, String keyword, int offset, int limit,
                                  String sort) throws ServiceLayerException;

    /**
     * Get paginated list of all users filtered by keyword
     * @param keyword keyword to filter users
     * @param offset offset for pagination
     * @param limit limit number of users per page
     * @param sort sort order
     * @return requested page of list of users
     * @throws ServiceLayerException
     */
    List<User> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException;

    /**
     * Get total number of users for site filtered by keyword
     * @param siteId site identifier
     * @param keyword keyword to filter users
     * @return total number of users for site filtered by keyword
     * @throws ServiceLayerException
     */
    int getAllUsersForSiteTotal(String siteId, String keyword) throws ServiceLayerException;

    /**
     * Get total number of users filtered by keyword
     * @param keyword keyword to filter user
     * @return total number of users filtered by keyword
     * @throws ServiceLayerException
     */
    int getAllUsersTotal(String keyword) throws ServiceLayerException;

    User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException;

    boolean userExists(long userId, String username) throws ServiceLayerException;

    void updateUser(User user) throws UserNotFoundException, ServiceLayerException;

    void deleteUsers(List<Long> userIds, List<String> usernames) throws UserNotFoundException, ServiceLayerException;

    List<User> enableUsers(List<Long> userIds, List<String> usernames,
                           boolean enabled) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Retrieves the list of groups associated with the specified user.
	 *
	 * @param userId   the unique identifier of the user
	 * @param username the username of the user
	 * @return a list of {@link Group} objects associated with the user
	 * @throws UserNotFoundException  if no user is found with the given userId or username
	 * @throws ServiceLayerException  if an error occurs while accessing the service layer
	 */
    List<Group> getUserGroups(long userId, String username) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Retrieves the list of groups associated with the specified user, optionally filtering
	 * to include only externally managed groups.
	 *
	 * @param userId                        the unique identifier of the user
	 * @param username                      the username of the user
	 * @param filterExternallyManagedGroups if true, only externally managed groups are returned;
	 *                                      if false, all groups are returned
	 * @return a list of {@link Group} objects associated with the user, filtered based on the flag
	 * @throws UserNotFoundException        if no user is found with the given userId or username
	 * @throws ServiceLayerException        if an error occurs while accessing the service layer
	 */
	List<Group> getUserGroups(long userId, String username, boolean filterExternallyManagedGroups) throws UserNotFoundException, ServiceLayerException;

    boolean isUserMemberOfGroup(String username, String groupName) throws UserNotFoundException, ServiceLayerException;

    boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException;

    boolean setUserPassword(String username, String newPassword) throws UserNotFoundException,
            ServiceLayerException;

    /**
     * Get user by git name.
     * Special use case because git stores user as string of first and last name separated by ' '
     * @param gitName first and last name separated with ' '
     * @return user
     */
    User getUserByGitName(String gitName) throws ServiceLayerException, UserNotFoundException;

    /**
     * Get the properties for the given site &amp; the current user
     * @param siteId the id of the site
     * @return the current properties
     * @throws ServiceLayerException if there is any error fetching the properties
     */
    Map<String, Map<String, String>> getUserProperties(String siteId) throws ServiceLayerException;

    /**
     * Update or add properties for the given site &amp; the current user
     * @param siteId the id of the site
     * @param propertiesToUpdate the properties to update or add
     * @return the updated properties
     * @throws ServiceLayerException if there is any error updating or fetching the properties
     */
    Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
            throws ServiceLayerException;

    /**
     * Delete properties for the given site &amp; current user
     * @param siteId the id of the site
     * @param propertiesToDelete the list of keys to delete
     * @return the updated properties
     * @throws ServiceLayerException if there is any error deleting or fetching the properties
     */
    Map<String, String> deleteUserProperties(String siteId, List<String> propertiesToDelete)
            throws ServiceLayerException;

    /**
     * Returns the current authenticated user
     * @return the user if present
     * @throws AuthenticationException if there is no user authenticated
     */
    AuthenticatedUser getCurrentUser() throws AuthenticationException;

    /**
     * Check if given user has system_admin role
     * @param username user
     * @return true if user is system_admin, false otherwise
     */
    boolean isSystemAdmin(String username);
}
