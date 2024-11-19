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

package org.craftercms.studio.api.v1.dal;

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.api.v2.dal.PublishStatus;

import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public interface SiteFeedMapper {

    List<SiteFeed> getSites();

    int countSites();

    SiteFeed getSite(Map params);

	boolean createSite(SiteFeed siteFeed);

    /**
     * Delete site
     * @param siteId site identifier
     * @param state deleted state value
     * @return
     */
    boolean deleteSite(@Param(SITE_ID) String siteId, @Param(STATE) String state);

    void updateLastCommitId(Map params);

    Integer exists(String siteId);

    Integer existsById(String id);

    Integer existsByName(String name);

    /**
     * Checks if there is a site, different than the siteId, using the given name
     *
     * @param siteId the id of the site
     * @param name the name of the site
     * @return true if the name is being used by another site
     */
    boolean isNameUsed(@Param(SITE_ID) String siteId, @Param(NAME) String name);

    int getSitesPerUserQueryTotal(Map params);

    List<String> getSitesPerUserQuery(Map params);

    List<SiteFeed> getSitesPerUserData(Map params);

    void enablePublishing(Map params);

    /**
     * Update publishing status
     * @param siteId site identifier
     * @param status publisher status
     */
    void updatePublishingStatus(@Param(SITE_ID) String siteId, @Param(PUBLISHING_STATUS) String status);

    List<SiteFeed> getDeletedSites();

    /**
     * Set published repo created flag
     * @param siteId site identifier
     */
    void setPublishedRepoCreated(@Param(SITE_ID) String siteId);

    /**
     * Updates the name and description for the given site
     *
     * @param siteId the id of the site
     * @param name the name of the site
     * @param description the description of the site
     * @return the number of changed rows
     */
    int updateSite(@Param(SITE_ID) String siteId, @Param(NAME) String name, @Param(DESC) String description);

    void setSiteState(@Param(SITE_ID) String siteId, @Param(STATE) String state);

    List<String> getAllCreatedSites(@Param(STATE) String state);

    String getSiteState(@Param(SITE_ID) String siteId);

    int getPublishedRepoCreated(@Param(SITE_ID) String siteId);

    /**
     * Get publishing status for site
     * @param siteId site identifier
     * @return Publishing status
     */
    PublishStatus getPublishingStatus(@Param(SITE_ID) String siteId);

    /**
     * Duplicate a site in the database.
     * Notice that populateItemParentId SP should be called after this method to populate the new site's item parent ids.
     *
     * @param sourceSiteId  the id of the site to duplicate
     * @param siteId        the id of the new site
     * @param name          the name of the new site
     * @param description   the description of the new site
     * @param sandboxBranch the sandbox branch of the new site
     * @param siteUuid      the uuid of the new site
     * @see org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal#updateParentId (String)
     */
    void duplicate(@Param(SOURCE_SITE_ID) String sourceSiteId, @Param(SITE_ID) String siteId,
                   @Param(NAME) String name, @Param(DESC) String description,
                   @Param(SANDBOX_BRANCH) String sandboxBranch, @Param(UUID) String siteUuid);
}
