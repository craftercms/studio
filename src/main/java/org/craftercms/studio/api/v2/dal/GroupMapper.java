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

public interface GroupMapper {

    /**
     * Get all groups for given organization
     *
     * @param params SQL query paramters
     * @return List of groups
     */
    List<GroupDAO> getAllGroupsForOrganization(Map params);

    /**
     * Get all groups for given organization
     *
     * @param params SQL query paramters
     * @return List of groups
     */
    int getAllGroupsForOrganizationTotal(Map params);

    /**
     * Create group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer createGroup(Map params);

    /**
     * Update group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer updateGroup(Map params);

    /**
     * Delete group
     *
     * @param params SQL query parameters
     * @return Number of affected rows in DB
     */
    Integer deleteGroup(Map params);

    /**
     * Get group by group id
     *
     * @param params SQL query parameters
     * @return Group or null if not found
     */
    GroupDAO getGroup(Map params);

    /**
     * Get group by group name
     *
     * @param params SQL query parameters
     * @return Group or null if not found
     */
    GroupDAO getGroupByName(Map params);

    /**
     * Get group members
     *
     * @param params SQL query parameters
     * @return List of users, group members
     */
    List<UserDAO> getGroupMembers(Map params);

    /**
     * Add users to the group
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    Integer addGroupMembers(Map params);

    /**
     * Get User ids for usernames
     *
     * @param params SQL query parameters
     * @return List of user ids
     */
    List<Long> getUserIdsForUsernames(Map params);

    /**
     * Remove users from the group
     *
     * @param params SQL query parameters
     * @return Number of rows affected in DB
     */
    Integer removeGroupMembers(Map params);

    /**
     * Check if group exists
     *
     * @param params SQL query parameters
     * @return Number of groups
     */
    Integer groupExists(Map params);
}
