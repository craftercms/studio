/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public interface SiteDAO {

    /**
     * Delete site
     *
     * @param siteId site identifier
     */
    void deleteSiteRelatedItems(@Param(SITE_ID) String siteId);

    /**
     * Mark the site as DELETING
     *
     * @param siteId the site id
     */
    void startSiteDelete(@Param(SITE_ID) String siteId);

    /**
     * Marks the site as DELETED
     *
     * @param siteId the site id
     */
    void completeSiteDelete(@Param(SITE_ID) String siteId);

    /**
     * Checks if a non-deleted site exists with the given site id
     *
     * @param siteId the site id
     * @return true if the site exists, false otherwise
     */
    boolean exists(@Param(SITE_ID) String siteId);

    /**
     * Enables/disables publishing for the given site
     *
     * @param siteId  the site id
     * @param enabled true to enable publishing, false to disable
     */
    void enablePublishing(@Param(SITE_ID) String siteId, @Param(ENABLED) boolean enabled);

    /**
     * Gets the site with the given site id
     *
     * @param siteId the site id
     * @return the {@link Site} object
     */
    Site getSite(@Param(SITE_ID) String siteId);

    /**
     * update last commit id
     *
     * @param siteId       site identifier
     * @param lastCommitId last commit id
     */
    void updateLastCommitId(@Param(SITE_ID) String siteId, @Param(LAST_COMMIT_ID) String lastCommitId);
}