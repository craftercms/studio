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

package org.craftercms.studio.api.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;

import java.util.List;

public interface UserServiceInternal {

    User getUserByIdOrUsername(long userId, String username) throws UserNotFoundException, ServiceLayerException;

    List<User> getUsersByIdOrUsername(List<Long> userIds,
                                      List<String> usernames) throws ServiceLayerException, UserNotFoundException;

    List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit,
                                  String sort) throws ServiceLayerException;

    List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException;

    int getAllUsersForSiteTotal(long orgId, String siteId) throws ServiceLayerException;

    int getAllUsersTotal() throws ServiceLayerException;

    User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException;

    boolean userExists(long userId, String username) throws ServiceLayerException;

    void updateUser(User user) throws UserNotFoundException, ServiceLayerException;

    void deleteUsers(List<Long> userIds, List<String> usernames) throws UserNotFoundException, ServiceLayerException;

    List<User> enableUsers(List<Long> userIds, List<String> usernames,
                           boolean enabled) throws UserNotFoundException, ServiceLayerException;

    List<Group> getUserGroups(long userId, String username) throws UserNotFoundException, ServiceLayerException;

    boolean isUserMemberOfGroup(String username, String groupName) throws UserNotFoundException, ServiceLayerException;

    boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException;

    boolean setUserPassword(String username, String newPassword) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException;

    /**
     * Get user by git name.
     * Special use case because git stores user as string of first and last name separated by ' '
     * @param gitName first and last name separated with ' '
     * @return user
     */
    User getUserByGitName(String gitName);
}
