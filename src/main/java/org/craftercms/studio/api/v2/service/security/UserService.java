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

import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsersForSite(int orgId, String site, int offset, int limit, String sort);

    void createUser(User user) throws UserAlreadyExistsException;

    void updateUser(User user);

    void deleteUsers(List<Integer> userIds, List<String> usernames);

    User getUserByIdOrUsername(int userId, String username);

    void enableUsers(List<Integer> userIds, List<String> usernames, boolean enabled);

    List<Group> getUserGroups(int userId, String username);
}
