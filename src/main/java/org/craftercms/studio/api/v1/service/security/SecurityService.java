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

import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;

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
	String authenticate(String username, String password) throws BadCredentialsException,
        AuthenticationSystemException, EntitlementException;

	/**
	 * Returns the username of the current user OR NULL if no user is authenticated
	 */
	String getCurrentUser();

    String getCurrentToken();

    Set<String> getUserRoles(String site, String user);

    Map<String, Object> getUserProfile(String user);

    Set<String> getUserPermissions(String site, String path, String user, List<String> groups);

    boolean validateTicket(String token);

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
     * Get all users
     *
     * @return number of all users
     */
    int getAllUsersTotal();


    /**
     * Forgot password for given user
     *
     * @param username username
     * @return forgot password
     */
    Map<String, Object> forgotPassword(String username)
            throws ServiceException, UserNotFoundException, UserExternallyManagedException;

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
    boolean changePassword(String username, String current, String newPassword)
            throws UserNotFoundException, PasswordDoesNotMatchException, UserExternallyManagedException;

    /**
     * Set user password - forgot password token
     *
     * @param token forgot password token
     * @param newPassword new password
     * @return true if uses's password is successfully set
     */
    Map<String, Object> setUserPassword(String token, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException;

    /**
     * Reset user password
     *
     * @param username username
     * @param newPassword new password
     * @return true if user's password is successfully reset
     */
    boolean resetPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException;

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
     * @param site site identifier
     * @return true if user belongs to admin group
     */
    boolean isSiteAdmin(String username, String site);
}
