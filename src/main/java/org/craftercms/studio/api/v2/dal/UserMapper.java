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

package org.craftercms.studio.api.v2.dal;

import java.util.List;
import java.util.Map;

public interface UserMapper {

    List<UserDAO> getAllUsersForSite(Map params);

    void createUser(Map params);

    void updateUser(Map params);

    List<Integer> getUserIdsForUsernames(Map params);

    void deleteUsers(Map params);

    UserDAO getUserByIdOrUsername(Map params);

    void enableUsers(Map params);

    List<GroupDAO> getUserGroups(Map params);

    int getAllUsersTotal();

    void setUserPassword(Map params);

    Integer userExists(Map params);
}
