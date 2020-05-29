/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

public interface UserService {

    List<User> getAllUsersForSite(long orgId, String site, int offset, int limit, String sort)
            throws ServiceLayerException;

    List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException;

    int getAllUsersForSiteTotal(long orgId, String site) throws ServiceLayerException;

    int getAllUsersTotal() throws ServiceLayerException;

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

    String getCurrentUserSsoLogoutUrl() throws AuthenticationException, ServiceLayerException;

    /**
     * Forgot password feature for given username
     *
     * @param username user that forgot password
     * @return true if success
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
     */
    User setPassword(String token, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException;

    /**
     * Admin resets the user password
     *
     * @param username username
     * @param newPassword new password
     * @return true if user's password is successfully reset
     */
    boolean resetPassword(String username, String newPassword) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException;

    /**
     * Validate forgot password token
     *
     * @param token forgot password token to validate
     * @return true if token is valid otherwise false
     */
    boolean validateToken(String token) throws UserNotFoundException, UserExternallyManagedException,
            ServiceLayerException;
}
