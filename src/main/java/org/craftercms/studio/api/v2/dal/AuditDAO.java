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

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public interface AuditDAO {

    List<AuditLog> getAuditLogForSite(Map params);

    int getAuditLogForSiteTotal(Map params);

    List<AuditLog> getAuditLog(Map params);

    int getAuditLogTotal(Map params);

    AuditLog getAuditLogEntry(Map params);

    int insertAuditLog(AuditLog auditLog);

    void insertAuditLogParams(Map params);

    List<AuditLog> selectUserFeedEntriesHideLive(Map params);

    List<AuditLog> selectUserFeedEntries(Map params);

    /**
     * Get total number of records for audit dashboard
     * @param params SQL query parameters
     * @return total number of records
     */
    int getAuditDashboardTotal(Map params);

    /**
     * Get records for audit dashboard content
     * @param params SQL query parameters
     * @return records for audit dashboard
     */
    List<AuditLog> getAuditDashboard(Map params);

    /**
     * Delete audit log for site
     * @param siteId site id
     */
    void deleteAuditLogForSite(@Param(SITE_ID) long siteId);
}
