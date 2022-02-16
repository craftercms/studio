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

package org.craftercms.studio.api.v2.service.audit;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Audit Service
 */
public interface AuditService {

    /**
     * Get audit log for site
     *
     * @param site site
     * @param offset offset of the first record
     * @param limit number of records to return
     * @param user filter logs by user
     * @param actions filter logs by actions
     * @return audit list
     * @throws SiteNotFoundException thrown if site does not exist
     */
    List<AuditLog> getAuditLogForSite(String site, int offset, int limit, String user, List<String> actions)
            throws SiteNotFoundException;

    /**
     * Get total number of audit log entries for site
     *
     * @param site site
     * @param user filter logs by user
     * @param actions filter logs by actions
     * @return number of audit log entries
     * @throws SiteNotFoundException thrown if site does not exist
     */
    int getAuditLogForSiteTotal(String site, String user, List<String> actions) throws SiteNotFoundException;

    /**
     * Get audit log
     *
     * @param siteId filter logs by given site Id
     * @param siteName filter logs by given site name
     * @param offset offset of the first record
     * @param limit number of records to return
     * @param user filter logs by given user
     * @param operations filter logs by given operations
     * @param includeParameters include audit log parameters into result set
     * @param dateFrom filter logs by date starting from given date
     * @param dateTo filter logs by date until given date
     * @param target filter logs by given operation target
     * @param origin filter logs by origin
     * @param clusterNodeId filter logs by given cluster node id
     * @param sort sort logs by given sort type
     * @param order order logs
     * @return audit log result set
     */
    List<AuditLog> getAuditLog(String siteId, String siteName, int offset, int limit, String user,
                               List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                               ZonedDateTime dateTo, String target, String origin, String clusterNodeId, String sort,
                               String order);

    /**
     * Get total number of audit log entries for given filters
     *
     * @param siteId filter logs by given site Id
     * @param siteName filter logs by given site name
     * @param user filter logs by given user
     * @param operations filter logs by given operations
     * @param includeParameters include audit log parameters into result set
     * @param dateFrom filter logs by date starting from given date
     * @param dateTo filter logs by date until given date
     * @param target filter logs by given operation target
     * @param origin filter logs by origin
     * @param clusterNodeId filter logs by given cluster node id
     * @return number of audit log entries
     */
    int getAuditLogTotal(String siteId, String siteName, String user, List<String> operations,
                                    boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                    String target, String origin, String clusterNodeId);

    /**
     * Get audit log entry by id
     *
     * @param auditLogId audit log id
     * @return audit log entry
     */
    AuditLog getAuditLogEntry(long auditLogId);

    /**
     * Get user activities
     *
     * @param site site
     * @param limit limit
     * @param sort sort by
     * @param ascending true if ascending order, otherwise false
     * @param excludeLive exclude live items
     * @param filterType filter type
     * @return list of content items
     *
     * @throws ServiceLayerException general service error
     */
    List<ContentItemTO> getUserActivities(String site, int limit, String sort, boolean ascending,
                                      boolean excludeLive, String filterType) throws ServiceLayerException;
}
