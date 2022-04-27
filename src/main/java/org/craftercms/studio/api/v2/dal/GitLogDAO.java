/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.AUDITED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.COMMIT_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.COMMIT_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.MARKER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PROCESSED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.UNPROCESSED;

public interface GitLogDAO {

    GitLog getGitLog(Map params);

    void insertGitLog(Map params);

    void insertGitLogList(Map params);

    void markGitLogProcessed(Map params);

    /**
     * Mark commit id as processed for given site and list of
     * @param siteId site identifier
     * @param commitIds list of commit ids
     */
    void markGitLogProcessedBulk(@Param(SITE_ID) String siteId, @Param(COMMIT_IDS) List<String> commitIds);

    void deleteGitLogForSite(Map params);

    void markGitLogAudited(@Param(SITE_ID) String siteId, @Param(COMMIT_ID) String commitId,
                           @Param(AUDITED) int audited);

    void insertIgnoreGitLogList(@Param(SITE_ID) String siteId, @Param(COMMIT_IDS) List<String> commitIds);

    List<GitLog> getUnauditedCommits(@Param(SITE_ID) String siteId, @Param(LIMIT) int limit);

    List<GitLog> getUnprocessedCommitsSinceMarker(@Param(SITE_ID) String siteId, @Param(MARKER) long marker);

    int countUnprocessedCommitsSinceMarker(@Param(SITE_ID) String siteId, @Param(MARKER) long marker);

    /**
     * Mark all git logs as processed if they are inserted before marker
     * @param siteId site identifier
     * @param marker marker git commit
     * @param processed value for processed
     */
    void markGitLogProcessedBeforeMarker(@Param(SITE_ID) String siteId, @Param(MARKER) long marker,
                                         @Param(PROCESSED) int processed, @Param(UNPROCESSED) int unprocessed);

    /**
     * Upsert git logs as processed and audited
     * @param siteId site identifier
     * @param commitIds commit ids
     */
    void upsertGitLogList(@Param(SITE_ID) String siteId, @Param(COMMIT_IDS) List<String> commitIds,
                          @Param(PROCESSED) int processed, @Param(AUDITED) int audited);
}
