/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.dashboard;

import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service that process requests for Dashboard API
 */
public interface DashboardService {

    /**
     * Get total number of audit log entries for audit dashboard
     *
     * @param siteId filter logs by given site id
     * @param user filter logs by given user
     * @param operations filter logs by given operations
     * @param dateFrom filter logs by date starting from given date
     * @param dateTo filter logs by date until given date
     * @param target filter logs by given operation target
     * @return number of audit log entries
     */
    int getAuditDashboardTotal(String siteId, String user, List<String> operations, ZonedDateTime dateFrom,
                         ZonedDateTime dateTo, String target);

    /**
     * Get content for audit dashboard
     *
     * @param siteId filter logs by given site id
     * @param offset offset of the first record
     * @param limit number of records to return
     * @param user filter logs by given user
     * @param operations filter logs by given operations
     * @param dateFrom filter logs by date starting from given date
     * @param dateTo filter logs by date until given date
     * @param target filter logs by given operation target
     * @param sort sort logs by given sort type
     * @param order order logs
     *
     * @return audit log result set
     */
    List<AuditLog> getAuditDashboard(String siteId, int offset, int limit, String user, List<String> operations,
                               ZonedDateTime dateFrom, ZonedDateTime dateTo, String target, String sort, String order);

    /**
     * Get total number of records for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter
     * @param modifier filter by user (modifier)
     * @param contentType filter by content type
     * @param state filter by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @return total number of records
     */
    int getContentDashboardTotal(String siteId, String path, String modifier, String contentType,
                                                   long state, ZonedDateTime dateFrom, ZonedDateTime dateTo);

    /**
     * Get result set for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter
     * @param modifier filter by user (modifier)
     * @param contentType filter by content type
     * @param state filter by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @param sortBy sort results by column
     * @param order order results
     * @param offset offset of the first record in result set
     * @param limit number of records to return as result set
     * @return list of items for content dashboard
     */
    List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier, String contentType,
                                                   long state, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                   String sortBy, String order, int offset, int limit);

    /**
     * Get total number of publishing history items for given search parameters
     *
     * @param siteId site identifier
     * @param environment environment to get publishing history
     * @param path regular expression to filter paths
     * @param publisher filter publishing history for specified user
     * @param dateFrom lower boundary for date range
     * @param dateTo upper boundary for date range
     * @param contentType publishing history for specified content type
     * @param state filter items by their state
     *
     * @return total number of deployment history items
     */
    int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                  ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state);

    /**
     * Get deployment history items for given search parameters
     *
     * @param siteId site identifier
     * @param environment environment to get publishing history
     * @param path regular expression to filter paths
     * @param publisher filter publishing history for specified user
     * @param dateFrom lower boundary for date range
     * @param dateTo upper boundary for date range
     * @param contentType publishing history for specified content type
     * @param state filter items by their state
     * @param sortBy sort publishing history
     * @param order apply order to publishing history
     * @param offset offset of the first item in the result set
     * @param limit number of items to return
     *
     * @return total number of publishing packages
     */
    List<PublishingDashboardItem> getPublishingHistory(String siteId, String environment, String path, String publisher,
                                                       ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType,
                                                       long state, String sortBy, String order, int offset, int limit);
}
