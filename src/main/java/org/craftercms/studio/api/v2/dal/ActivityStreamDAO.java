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
import org.craftercms.studio.model.rest.dashboard.Activity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTIONS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTION_TIMESTAMP;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_FROM;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_TO;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ITEM;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PACKAGE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

public interface ActivityStreamDAO {

    /**
     * Insert record into activity stream
     *
     * @param siteId site identifier
     * @param userId user identifier
     * @param action action that was performed
     * @param actionTimestamp timestamp when action was performed
     * @param item item that was actioned upon
     * @param packageId package identifier that was actioned upon
     */
    void insertActivity(@Param(SITE_ID) long siteId, @Param(USER_ID) long userId, @Param(ACTION) String action,
                        @Param(ACTION_TIMESTAMP) ZonedDateTime actionTimestamp, @Param(ITEM) Item item,
                        @Param(PACKAGE_ID) String packageId);

    /**
     * Get total number activities for users
     * @param siteId site identifier
     * @param usernames list of usernames
     * @param actions list of actions to filter
     * @param dateForm lower boundary for filtering by date range
     * @param dateTo upper boundary for filtering by date range
     * @return total number of activities for given users
     */
    int getActivitiesForUsersTotal(@Param(SITE_ID) long siteId,
                                   @Param(USERNAMES) List<String> usernames,
                                   @Param(ACTIONS) List<String> actions,
                                   @Param(DATE_FROM) ZonedDateTime dateForm,
                                   @Param(DATE_TO) ZonedDateTime dateTo);

    /**
     * Get activities for users
     * @param siteId site identifier
     * @param usernames list of usernames
     * @param actions list of actions to filter
     * @param dateForm lower boundary for filtering by date range
     * @param dateTo upper boundary for filtering by date range
     * @param offset offset of the first record in the result
     * @param limit limit the number of the results to return
     * @return list of activities for given users
     */
    List<Activity> getActivitiesForUsers(@Param(SITE_ID) long siteId,
                                         @Param(USERNAMES) List<String> usernames,
                                         @Param(ACTIONS) List<String> actions,
                                         @Param(DATE_FROM) ZonedDateTime dateForm,
                                         @Param(DATE_TO) ZonedDateTime dateTo,
                                         @Param(OFFSET) int offset,
                                         @Param(LIMIT) int limit);
}
