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

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.impl.v2.service.security.Authentication;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.List;

public interface SecurityProvider {
    List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort)
        throws ServiceLayerException;

    boolean createUser(User user) throws UserAlreadyExistsException, ServiceLayerException;

    void updateUser(User user) throws ServiceLayerException;

    void deleteUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException;

    User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException;

    void enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) throws ServiceLayerException;

    List<Group> getUserGroups(long userId, String username) throws ServiceLayerException;

    List<Group> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException;

    void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException,
        ServiceLayerException;

    void updateGroup(long orgId, Group group) throws ServiceLayerException;

    void deleteGroup(List<Long> groupIds) throws ServiceLayerException;

    Group getGroup(long groupId) throws ServiceLayerException;

    List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException;

    boolean addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException;

    void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException;

    int getAllUsersTotal() throws ServiceLayerException;

    String getCurrentUser();

    String authenticate(String username, String password) throws BadCredentialsException,
        AuthenticationSystemException, EntitlementException;

    boolean validateTicket(String ticket);

    String getCurrentToken();

    Authentication getAuthentication();

    boolean logout();

    boolean changePassword(String username, String current, String newPassword) throws PasswordDoesNotMatchException,
        UserExternallyManagedException, ServiceLayerException;

    boolean setUserPassword(String username, String newPassword) throws UserNotFoundException,
        UserExternallyManagedException, ServiceLayerException;

    boolean userExists(String username) throws ServiceLayerException;

    boolean groupExists(String groupName) throws ServiceLayerException;
}
