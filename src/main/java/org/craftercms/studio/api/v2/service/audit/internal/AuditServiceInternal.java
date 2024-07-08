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

import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.model.rest.Person;

import java.time.ZonedDateTime;
import java.util.List;

// TODO: JM: Merge this to AuditService
public interface AuditServiceInternal {

    /**
     * Get audit log filtered by parameters
     *
     * @param siteId  site identifier
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
    List<AuditLog> getAuditLog(String siteId, int offset, int limit, String user,
                               List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                               ZonedDateTime dateTo, String target, String origin, String clusterNodeId, String sort,
                               String order);

    /**
     * Get the audit log entry count given the provided filter parameters
     *
     * @param siteId            site ID
     * @param user            filter by user
     * @param operations        filter by list of operations
     * @param includeParameters include audit log parameters in result set
     * @param dateFrom          filter results by lower border for date
     * @param dateTo            filter results by upper border for date
     * @param target            filter results by target
     * @param origin            filter results by origin
     * @param clusterNodeId     filter results by cluster node
     * @return total number of records matching the filter
     */
    int getAuditLogTotal(String siteId, String user, List<String> operations,
                                    boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                    String target, String origin, String clusterNodeId);

    /**
     * Get audit log entry by id
     *
     * @param auditLogId id of audit log entry to get
     * @param siteId the site ID. When null or empty, it will retrieve
     *               entries for all sites and include admin activities.
     * @return Audit log entry
     */
    AuditLog getAuditLogEntry(String siteId, long auditLogId);

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
     * Get author of the commit.
     * This will look in the audit data and retrieve a Person when:
     * <ul>
     *     <li>There is an audit entry for the given commit id</li>
     *     <li>AND the audit entry origin is API</li>
     * </ul>
     *
     * @param commitId commit id
     * @return author of the commit, if found, otherwise null
     */
    Person getAuthor(String commitId);

    /**
     * Check if a commit has been audited.
     * @param siteId site id
     * @param commitId commit id
     * @return true if there is an audit entry for the given commit id, otherwise false
     */
    boolean isAudited(long siteId, String commitId);
}
