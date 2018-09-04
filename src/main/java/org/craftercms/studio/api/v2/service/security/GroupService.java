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

import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.List;

public interface GroupService {

    /**
     * Get all groups
     *
     * @param orgId Organization identifier
     * @param offset Result set offset
     * @param limit Result set limit
     * @param sort Sort order
     * @return List of groups
     */
    List<Group> getAllGroups(long orgId, int offset, int limit, String sort);

    /**
     * Create group
     *
     * @param orgId Organization identifier
     * @param groupName Group name
     * @param groupDescription Group description
     */
    void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException;

    /**
     * Update group
     *
     * @param orgId Organization identifier
     * @param group Group to update
     */
    void updateGroup(long orgId, Group group);

    /**
     * Delete group(s)
     *
     * @param groupIds Group identifiers
     */
    void deleteGroup(List<Long> groupIds);

    /**
     * Get group
     *
     * @param groupId Group identifier
     * @return Group
     */
    Group getGroup(long groupId);

    /**
     * Get group members
     *
     * @param groupId Group identifier
     * @param offset Result set offset
     * @param limit Result set limit
     * @param sort Sort order
     * @return List of users
     */
    List<User> getGroupMembers(long groupId, int offset, int limit, String sort);

    /**
     * Add users to the group
     *
     * @param groupId Group identifier
     * @param userIds List of user identifiers
     * @param usernames List of usernames
     */
    void addGroupMembers(long groupId, List<Long> userIds, List<String> usernames);

    /**
     * Remove users from the group
     *
     * @param groupId Group identifier
     * @param userIds List of user identifiers
     * @param usernames List of usernames
     */
    void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames);

    /**
     * Get groups for site
     *
     * @param siteId Site identifier
     * @return List of group names
     */
    List<String> getSiteGroups(String siteId);

    /**
     * Get global groups
     *
     * @return List of group names
     */
    List<String> getGlobalGroups();

    /**
     * Get group by name
     * @param groupName group name
     * @return group object
     */
    Group getGroupByName(String groupName) throws GroupNotFoundException;
}
