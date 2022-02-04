/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.KEYWORD;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public interface GroupDAO {

    /**
     * Get all groups for given organization
     *
     * @param orgId organization identifier
     * @param keyword keyword to filter groups
     * @param offset offset fpr pagination
     * @param limit limit number of groups per page
     * @param sort sort order
     * @return List of groups
     */
    List<Group> getAllGroupsForOrganization(@Param(ORG_ID) long orgId, @Param(KEYWORD) String keyword,
                                            @Param(OFFSET) int offset, @Param(LIMIT) int limit,
                                            @Param(SORT) String sort);

    /**
     * Get all groups for given organization
     *
     * @param orgId organization identifier
     * @param keyword keyword to filter groups
     * @return List of groups
     */
    int getAllGroupsForOrganizationTotal(@Param(ORG_ID) long orgId, @Param(KEYWORD) String keyword);

    /**
     * Create group
     *
     * @param orgId organization id
     * @param groupName group name
     * @param groupDescription  group description
     * @return Number of affected rows in DB
     */
    Integer createGroup(@Param(ORG_ID) long orgId, @Param(GROUP_NAME) String groupName,
                        @Param(GROUP_DESCRIPTION) String groupDescription);

    /**
     * Update group
     *
     * @param group group to update
     * @return Number of affected rows in DB
     */
    Integer updateGroup(Group group);

    /**
     * Delete group
     *
     * @param groupId group identifier
     * @return Number of affected rows in DB
     */
    Integer deleteGroup(@Param(GROUP_ID) long groupId);

    /**
     * Delete groups
     *
     * @param groupIds ids of the groups to be deleted
     * @return Number of affected rows in DB
     */
    Integer deleteGroups(@Param(GROUP_IDS) List<Long> groupIds);

    /**
     * Get group by group id
     *
     * @param groupId group identifier
     * @return Group or null if not found
     */
    Group getGroup(@Param(GROUP_ID) long groupId);

    /**
     * Get groups by group ids
     *
     * @param groupIds list of group identifiers
     * @return List of groups or null if not found
     */
    List<Group> getGroups(@Param(GROUP_IDS) List<Long> groupIds);

    /**
     * Get group by group name
     *
     * @param groupName group name
     * @return Group or null if not found
     */
    Group getGroupByName(@Param(GROUP_NAME) String groupName);

    /**
     * Get group members
     *
     * @param groupId group identifier
     * @param offset offset for pagination
     * @param limit limit number of members per page
     * @param sort sort order
     * @return List of users, group members
     */
    List<User> getGroupMembers(@Param(GROUP_ID) long groupId, @Param(OFFSET) int offset, @Param(LIMIT) int limit,
                               @Param(SORT) String sort);

    /**
     * Get total number of group members.
     *
     * @param groupId group identifier
     * @return Number of members
     */
    Integer getGroupMembersTotal(@Param(GROUP_ID) long groupId);

    /**
     * Add users to the group
     *
     * @param groupId group identifier
     * @param userIds list of user identifiers
     * @return Number of rows affected in DB
     */
    Integer addGroupMembers(@Param(GROUP_ID) long groupId, @Param(USER_IDS) List<Long> userIds);

    /**
     * Remove users from the group
     *
     * @param groupId group identifier
     * @param userIds list of user identifiers
     * @return Number of rows affected in DB
     */
    Integer removeGroupMembers(@Param(GROUP_ID) long groupId, @Param(USER_IDS) List<Long> userIds);

    /**
     * Check if group exists
     *
     * @param groupId group identifier
     * @param groupName group name
     * @return Number of groups
     */
    Integer groupExists(@Param(GROUP_ID) long groupId, @Param(GROUP_NAME) String groupName);
}
