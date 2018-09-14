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

package org.craftercms.studio.api.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.security.UserPermissions;
import org.craftercms.studio.model.User;

import java.util.List;
import java.util.Set;

public interface UserServiceInternal {

    Set<String> getUserPermissions(String username, UserPermissions.Scope scope, String parameter) throws ServiceLayerException;

    User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException, UserNotFoundException;

    List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort)
            throws ServiceLayerException;

    List<User> getAllUsers(int offset, int limit, String sort) throws ServiceLayerException;

    int getAllUsersForSiteTotal(long orgId, String siteId) throws ServiceLayerException;

    int getAllUsersTotal() throws ServiceLayerException;

    User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException;

    boolean userExists(String username) throws ServiceLayerException;

    void updateUser(User user) throws ServiceLayerException;

    void deleteUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException;

    List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) throws ServiceLayerException, UserNotFoundException;

    List<User> findUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException,
            UserNotFoundException;

    List<GroupTO> getUserGroups(long userId, String username) throws ServiceLayerException;

    boolean isUserMemberOfGroup(String username, String groupName) throws ServiceLayerException;
}
