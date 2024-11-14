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

/**
 * Provides operations to processed_commits table
 */
public interface ProcessedCommitsDAO {
    String SITE_ID = "siteId";
    String COMMIT_ID = "commitId";

    /**
     * Check if the given commitId has been processed
     * <p>
     * Note that the commitId should be more recent than the last processed commit for the site. Otherwise it might
     * already have been purged from the table.
     *
     * @param siteId   the site id
     * @param commitId the commit id
     * @return true if the commit has been processed, false otherwise
     */
    boolean isProcessed(@Param(SITE_ID) long siteId, @Param(COMMIT_ID) String commitId);

    /**
     * Inserts a new processed_commits record
     *
     * @param siteId   the site id
     * @param commitId the commit id
     */
    void insertCommit(@Param(SITE_ID) long siteId, @Param(COMMIT_ID) String commitId);

    /**
     * Deletes all commits records inserted before the given commitId
     *
     * @param siteId   the site id
     * @param commitId the commit id
     */
    void deleteBefore(@Param(SITE_ID) long siteId, @Param(COMMIT_ID) String commitId);

}
