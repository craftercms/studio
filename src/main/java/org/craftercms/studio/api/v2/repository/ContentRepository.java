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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RepoOperation;

import java.time.ZonedDateTime;
import java.util.List;

public interface ContentRepository {

    /**
     * List subtree items for give site and path
     *
     * @param site site identifier
     * @param path path for subtree root
     * @return list of item paths contained in the subtree
     */
    List<String> getSubtreeItems(String site, String path);

    /**
     * Get a list of operations since the commit ID provided (compare that commit to HEAD)
     *
     * @param site         site to use
     * @param commitIdFrom commit ID to start at
     * @param commitIdTo   commit ID to end at
     * @return commit ID of current HEAD, updated operationsSinceCommit
     */
    List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo);

    /**
     * Get a list of operations since the commit ID provided (compare that commit to HEAD)
     *
     * @param site         site to use
     * @param commitIdFrom commit ID to start at
     * @param commitIdTo   commit ID to end at
     * @return commit ID of current HEAD, updated operationsSinceCommit
     */
    List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo);

    /**
     * Get first id from repository for given site
     *
     * @param site site id
     * @return first commit id
     */
    String getRepoFirstCommitId(String site);

    /**
     * Get git log object from database
     *
     * @param siteId   site id
     * @param commitId commit ID
     * @return git log object
     */
    GitLog getGitLog(String siteId, String commitId);

    /**
     * Mark Git log as verified
     *
     * @param siteId   site identifier
     * @param commitId commit id
     */
    void markGitLogVerifiedProcessed(String siteId, String commitId);

    /**
     * Insert Git Log
     *
     * @param siteId    site
     * @param commitId  commit ID
     * @param processed processed
     */
    void insertGitLog(String siteId, String commitId, int processed);

    /**
     * Get publishing history
     *
     * @param siteId site identifier
     * @param environment environment
     * @param path path regular expression to use as filter
     * @param fromDate lower boundary for published date
     * @param toDate upper boundary for published date
     * @param limit number of records to return
     * @return
     */
    List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                     ZonedDateTime fromDate, ZonedDateTime toDate, int limit);
}
