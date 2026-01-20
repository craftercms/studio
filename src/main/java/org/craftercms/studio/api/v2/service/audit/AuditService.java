/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.CommitAuthor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Audit Service
 */
public interface AuditService {

	/**
	 * Get audit log
	 *
	 * @param siteId            filter logs by given site ID. It can be null or empty when user
	 *                          is system admin, it will then retrieve entries for all sites and include admin activities.
	 * @param offset            offset of the first record
	 * @param limit             number of records to return
	 * @param user              filter logs by given user
	 * @param operations        filter logs by given operations
	 * @param includeParameters include audit log parameters into result set
	 * @param dateFrom          filter logs by date starting from given date
	 * @param dateTo            filter logs by date until given date
	 * @param target            filter logs by given operation target
	 * @param origin            filter logs by origin
	 * @param clusterNodeId     filter logs by given cluster node id
	 * @param sort              sort logs by given sort type
	 * @param order             order logs
	 * @return audit log result set
	 */
	List<AuditLog> getAuditLog(String siteId, int offset, int limit, String user,
				   List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
				   ZonedDateTime dateTo, String target, String origin, String clusterNodeId, String sort,
				   String order) throws SiteNotFoundException;

	/**
	 * Get total number of audit log entries for given filters
	 *
	 * @param siteId            filter logs by given site ID. It can be null or empty when user
	 *                          is system admin, it will then retrieve entries for all sites and include admin activities.
	 * @param user              filter logs by given user
	 * @param operations        filter logs by given operations
	 * @param includeParameters include audit log parameters into result set
	 * @param dateFrom          filter logs by date starting from given date
	 * @param dateTo            filter logs by date until given date
	 * @param target            filter logs by given operation target
	 * @param origin            filter logs by origin
	 * @param clusterNodeId     filter logs by given cluster node id
	 * @return number of audit log entries
	 */
	int getAuditLogTotal(String siteId, String user, List<String> operations,
			     boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
			     String target, String origin, String clusterNodeId) throws SiteNotFoundException;

	/**
	 * Get audit log entry by id
	 *
	 * @param siteId     site ID. It can be null or empty when user is system admin
	 * @param auditLogId audit log id
	 * @return audit log entry
	 */
	AuditLog getAuditLogEntry(String siteId, long auditLogId) throws SiteNotFoundException;

	/**
	 * Insert log audit entry
	 *
	 * @param auditLog Audit log to insert
	 * @return true if successful, otherwise false
	 */
	boolean insertAuditLog(AuditLog auditLog);

	/**
	 * Get commit authors from a list of commit ids
	 * This will look in the audit data and retrieve a CommitAuthor when:
	 * <ul>
	 *     <li>There is an audit entry for the given commit id</li>
	 *     <li>AND the audit entry origin is API</li>
	 *     <li>AND the audit entry primary_target_value correspond to the given path</li>
	 * </ul>
	 *
	 * @param siteId    site id
	 * @param commitIds commit ids
	 * @param path      path of the file
	 * @return list {@link CommitAuthor} of the commit
	 */
	List<CommitAuthor> getCommitAuthors(long siteId, List<String> commitIds, String path);
}
