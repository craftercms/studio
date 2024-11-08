/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public interface AuditDAO {

    List<AuditLog> getAuditLog(Map params);

    int getAuditLogTotal(Map params);

    AuditLog getAuditLogEntry(Map params);

    int insertAuditLog(AuditLog auditLog);

    void insertAuditLogParams(Map params);

    List<AuditLog> selectUserFeedEntriesHideLive(Map params);

    List<AuditLog> selectUserFeedEntries(Map params);

    /**
     * Delete audit log for site
     * @param siteId site id
     */
    void deleteAuditLogForSite(@Param(SITE_ID) long siteId);

    /**
     * Gets commit authors from a list of commit ids.
     * This will retrieve a {@link CommitAuthor} object from the database when
     * the commit was created by Studio, meaning the following conditions are met:
     * <ul>
     *     <li>There is an audit entry for the given commit id</li>
     *     <li>AND the audit entry origin is API</li>
     *     <li>AND the audit entry primary_target_value is the given path</li>
     * </ul>
     *
     * @param siteId site id
     * @param commitIds the commit ids
     * @param path the path (to match primary_target_value)
     * @return the List of {@link CommitAuthor} if found
     */
    List<CommitAuthor> getCommitAuthors(@Param(SITE_ID) long siteId, @Param(COMMIT_IDS) List<String> commitIds, @Param(PATH) String path);

    /**
     * Checks if a commit has been audited.
     *
     * @param siteId   site id
     * @param commitId commit id
     * @return true if an entry exists in audit table for the given commit id, false otherwise.
     */
    boolean isAudited(@Param(SITE_ID) long siteId, @Param(COMMIT_ID) String commitId);
}
