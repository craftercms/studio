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
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GIT_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.KEYS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.KEYWORD;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PROPERTIES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public interface UserDAO {

    /**
     * Get all users for given site
     *
     * @param groupNames group names
     * @param keyword keyword to filter users
     * @param offset offset for pagination
     * @param limit limit number of users per page
     * @param sort sort order;
     * @return List of users
     */
    List<User> getAllUsersForSite(@Param(GROUP_NAMES) List<String> groupNames, @Param(KEYWORD) String keyword,
                                  @Param(OFFSET) int offset, @Param(LIMIT) int limit, @Param(SORT) String sort);

    /**
     * Get all users
     * @param keyword keyword to filter users
     * @param offset offset for pagination
     * @param limit limit number of users per page
     * @param sort sort order;
     * @return List of users
     */
    List<User> getAllUsers(@Param(KEYWORD) String keyword, @Param(OFFSET) int offset, @Param(LIMIT) int limit,
                           @Param(SORT) String sort);

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
     * @param groupNames group names
     * @param keyword keyword to filter users
     * @return total number of users for site
     */
    int getAllUsersForSiteTotal(@Param(GROUP_NAMES) List<String> groupNames, @Param(KEYWORD) String keyword);

    /**
     * Get total number of users
     * @param keyword keyword to filter users
     * @return total number of all users
     */
    int getAllUsersTotal(@Param(KEYWORD) String keyword);

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
     * @param params SQL query params
     * @return positive number if user exists, otherwise 0
     */
    Integer userExists(Map params);

    /**
     * Check if user is member of given group
     * @param params SQL query parameters
     * @return if true result greater than 0
     */
    Integer isUserMemberOfGroup(Map params);

    /**
     * Get user by git name
     *
     * @param gitName SQL query parameter
     * @return User or null if not found
     */
    User getUserByGitName(@Param(GIT_NAME) String gitName);

    /**
     * Returns the current user properties
     * @param userId the id of the user
     * @param siteId the id of the site
     * @return the properties
     */
    List<UserProperty> getUserProperties(@Param(USER_ID) long userId, @Param(SITE_ID) long siteId);

    /**
     * Deletes the given user properties
     * @param userId the id of the user
     * @param siteId the id of the site
     * @param keys the keys to delete
     */
    void deleteUserProperties(@Param(USER_ID) long userId, @Param(SITE_ID) long siteId,
                              @Param(KEYS) List<String> keys);

    /**
     * Updates the given user properties
     * @param userId the id of the user
     * @param siteId the id of the site
     * @param properties the properties to update or add
     */
    void updateUserProperties(@Param(USER_ID) long userId, @Param(SITE_ID) long siteId,
                              @Param(PROPERTIES) Map<String, String> properties);

    /**
     * Deletes all user properties for a given site
     * @param siteId the id of the site
     */
    void deleteUserPropertiesBySiteId(@Param(SITE_ID) long siteId);

    /**
     * Deletes all user properties for a given user
     * @param userIds the id of the user
     */
    void deleteUserPropertiesByUserIds(@Param(USER_IDS) List<Long> userIds);

}
