/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.api.v2.dal;

import java.util.List;
import java.util.Map;

public interface UserDAO {

    /**
     * Get all users for given site
     *
     * @param params SQL query parameters
     * @return List of users
     */
    List<User> getAllUsersForSite(Map params);

    /**
     * Get all users
     *
     * @param params SQL query parameters
     * @return List of users
     */
    List<User> getAllUsers(Map params);

    /**
     * Create user
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int createUser(Map params);

    /**
     * Update user
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int updateUser(Map params);

    /**
     * Get ids for users
     *
     * @param params SQL query parameters
     * @return List of user ids
     */
    List<Long> getUserIdsForUsernames(Map params);

    /**
     * Delete users
     *
     * @param params SQL query params
     * @return Number of rows affected in DB
     */
    int deleteUsers(Map params);

    /**
     * Get user by id or username
     *
     * @param params SQL query parameters
     * @return User or null if not found
     */
    User getUserByIdOrUsername(Map params);

    /**
     * Enable/disable users
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    int enableUsers(Map params);

    /**
     * Get user groups
     *
     * @param params SQL query parameters
     * @return List of groups
     */
    List<Group> getUserGroups(Map params);

    /**
     * Get total number of users
     *
     * @return total number of users for site
     */
    int getAllUsersForSiteTotal(Map params);

    /**
     * Get total number of users
     *
     * @return total number of all users
     */
    int getAllUsersTotal();

    /**
     * Set password for user
     *
     * @param params SQL query parameters
     * @return Number of rows affected
     */
    int setUserPassword(Map params);

    /**
     * Check if user exists
     *
     * @param params
     * @return positive number if user exists, otherwise 0
     */
    Integer userExists(Map params);

    /**
     * Check if user is member of given group
     * @param params SQL query parameters
     * @return if true result > 0
     */
    Integer isUserMemberOfGroup(Map params);
}
