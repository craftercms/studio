/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.api.v2.service.audit.internal;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.AuditLog;

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
     * @return List of audit log entries
     */
    List<AuditLog> getAuditLog();

    /**
     * Get audit log entry by id
     *
     * @param auditLogId id of audit log entry to get
     * @return
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


}
