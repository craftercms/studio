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

package org.craftercms.studio.api.v1.service.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface SecurityService {

	/**
	 * Returns the username of the current user OR NULL if no user is authenticated
     *
     * @return  current user
	 */
	String getCurrentUser();

    /**
     * Returns the {@link Authentication} for the current user or null if not user is authenticated.
     *
     * @return authentication
     */
    Authentication getAuthentication();

    Set<String> getUserRoles(String site);

    Set<String> getUserRoles(String site, String user);

    Set<String> getUserRoles(String site, String user, boolean includeGlobal);

    Map<String, Object> getUserProfile(String user) throws ServiceLayerException, UserNotFoundException;

    /**
     * Get user by git name.
     * Special use case because git stores user as string of first and last name separated by ' '
     * @param gitName first and last name separated with ' '
     * @return user
     *
     * @throws ServiceLayerException general service error
     * @throws UserNotFoundException user not found
     */
    Map<String, Object> getUserProfileByGitName(String gitName)
            throws ServiceLayerException, UserNotFoundException;

    Set<String> getUserPermissions(String site, String path, List<String> groups);

    Set<String> getUserPermissions(String site, String path, String user, List<String> groups);

    /**
     * Check if user exists
     *
     * @param username username
     * @return true if user exists
     *
     * @throws ServiceLayerException general service error
     */
    boolean userExists(String username) throws ServiceLayerException;


    /**
     * Get all users
     *
     * @return number of all users
     *
     * @throws ServiceLayerException general service error
     */
    int getAllUsersTotal() throws ServiceLayerException;

    /**
     * Change password
     *
     * @param username username
     * @param current current password
     * @param newPassword new password
     * @return true if user's password is successfully changed
     *
     * @throws UserExternallyManagedException user is externally managed
     * @throws PasswordDoesNotMatchException password does not match stored password
     * @throws ServiceLayerException general service error
     */
    boolean changePassword(String username, String current, String newPassword) throws
            PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException;


    /**
     * Reset user password
     *
     * @param username username
     * @param newPassword new password
     * @return true if user's password is successfully reset
     *
     * @throws UserNotFoundException user not found
     * @throws UserExternallyManagedException user externally managed
     * @throws ServiceLayerException general service error
     */
    boolean resetPassword(String username, String newPassword) throws UserNotFoundException,
        UserExternallyManagedException, ServiceLayerException;

    /**
     * Check if given user is site admin
     * @param username user
     * @param site site identifier
     * @return true if user belongs to admin group
     */
    boolean isSiteAdmin(String username, String site);
}
