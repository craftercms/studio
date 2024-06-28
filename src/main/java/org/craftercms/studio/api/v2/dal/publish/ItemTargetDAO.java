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

package org.craftercms.studio.api.v2.dal.publish;

import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Collection;

/**
 * Provide access to the item_target data.
 */
public interface ItemTargetDAO {

    String SITE_ID = "siteId";
    String PACKAGE_ID = "packageId";
    String COMMIT_ID = "commitId";
    String PATH = "path";
    String TARGET = "target";
    String LIVE_TARGET = "liveTarget";
    String STAGING_TARGET = "stagingTarget";
    String TARGETS = "targets";
    String TIMESTAMP = "timestamp";

    /**
     * Get the item target records by item path
     *
     * @param siteId the site id
     * @param path   the item path
     * @return the item target record
     */
    Collection<ItemTarget> getByItemPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

    /**
     * Update for successful publish items in the package.
     * For each successful PublishItem:
     * - Clear the oldPath
     * - Set the commitId
     * - Set the lastPublishedOn date to now
     *
     * @param packageId        the package id
     * @param commitId         the target published commit id
     * @param target           the target
     * @param itemSuccessState the state of the successful items to filter
     */
    void updateForCompletePackage(@Param(PACKAGE_ID) long packageId,
                                  @Param(COMMIT_ID) String commitId,
                                  @Param(TARGET) String target,
                                  @Param(TIMESTAMP) Instant timestamp,
                                  @Param(PublishDAO.ITEM_SUCCESS_STATE) long itemSuccessState);

    /**
     * Populate the item_target table for the initial publish.
     *
     * @param siteId    the site id
     * @param targets   the publishing targets
     * @param commitId  the commit id of published repository
     * @param timestamp the timestamp for the published_on date
     */
    void insertForInitialPublish(@Param(SITE_ID) long siteId,
                                 @Param(TARGETS) Collection<String> targets,
                                 @Param(COMMIT_ID) String commitId,
                                 @Param(TIMESTAMP) Instant timestamp);

    /**
     * Initialize staging target data using the current live target items
     *
     * @param siteId        the site id
     * @param stagingTarget the staging target
     * @param liveTarget    the live target
     */
    void initStaging(@Param(SITE_ID) long siteId,
                     @Param(STAGING_TARGET) String stagingTarget,
                     @Param(LIVE_TARGET) String liveTarget);
}
