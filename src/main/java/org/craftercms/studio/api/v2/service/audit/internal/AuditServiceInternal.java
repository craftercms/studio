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

package org.craftercms.studio.api.v2.service.audit.internal;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.AuditLog;

import java.time.ZonedDateTime;
import java.util.List;

public interface AuditServiceInternal {

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
     * Get audit log filtered by parameters
     *
     * @param siteId  site identifier
     * @param siteName site name
     * @param offset offset of first record in result set
     * @param limit number of records to return as result set
     * @param user filter by user
     * @param operations filter by list of operations
     * @param includeParameters include audit log parameters in result set
     * @param dateFrom filter results by lower border for date
     * @param dateTo filter results by upper border for date
     * @param target filter results by target
     * @param origin filter results by origin
     * @param clusterNodeId filter results by cluster node
     * @param sort sort strategy
     * @param order order strategy
     * @return List of audit log entries
     */
    List<AuditLog> getAuditLog(String siteId, String siteName, int offset, int limit, String user,
                               List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                               ZonedDateTime dateTo, String target, String origin, String clusterNodeId, String sort,
                               String order);

    int getAuditLogTotal(String siteId, String siteName, String user, List<String> operations,
                                    boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                    String target, String origin, String clusterNodeId);

    /**
     * Get total number of records for audit dashboard filtered by parameters
     *
     * @param siteId site identifier
     * @param user filter logs by user
     * @param operations filter logs by action
     * @param dateFrom lower boundary for operation timestamp
     * @param dateTo upper boundary for operation timestamp
     * @param target filter logs by target
     * @return total number of records
     */
    int getAuditDashboardTotal(String siteId,String user, List<String> operations, ZonedDateTime dateFrom,
                               ZonedDateTime dateTo, String target);

    /**
     * Get audit dashboard content filtered by parameters
     *
     * @param siteId site identifier
     * @param offset offset of the first record
     * @param limit number of records to return
     * @param user filter logs by user
     * @param operations filter logs by actions
     * @param dateFrom lower boundary for operation timestamp
     * @param dateTo upper boundary for operation timestamp
     * @param target filter logs by target
     * @param sort sort for records
     * @param order order for records
     * @return list of records for audit dashboard
     */
    List<AuditLog> getAuditDashboard(String siteId, int offset, int limit, String user, List<String> operations,
                                     ZonedDateTime dateFrom, ZonedDateTime dateTo, String target, String sort,
                                     String order);

    /**
     * Get audit log entry by id
     *
     * @param auditLogId id of audit log entry to get
     * @return Audit log entry
     */
    AuditLog getAuditLogEntry(long auditLogId);

    /**
     * Insert log audit entry
     *
     * @param auditLog Audit log to insert
     * @return true if successful, otherwise false
     */
    boolean insertAuditLog(AuditLog auditLog);

    /**
     * Create Audit log entry and populate common properties
     *
     * @return Audit log entry
     */
    AuditLog createAuditLogEntry();

    List<AuditLog> selectUserFeedEntries(String user, String siteId, int offset,
                                                int limit, String contentType, boolean hideLiveItems);

    /**
     * Delete audit log for site
     * @param siteId site id
     */
    void deleteAuditLogForSite(long siteId);
}
