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

package org.craftercms.studio.api.v1.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCK_OWNER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TTL;

public interface SiteFeedMapper {

    List<SiteFeed> getSites();

    int countSites();

    SiteFeed getSite(Map params);

	boolean createSite(SiteFeed siteFeed);

    boolean deleteSite(String siteId);

    void updateLastCommitId(Map params);

    String getLastCommitId(Map params);

    Integer exists(String siteId);

    Integer existsById(String id);

    Integer existsByName(String name);

    int getSitesPerUserQueryTotal(Map params);

    List<String> getSitesPerUserQuery(Map params);

    List<SiteFeed> getSitesPerUserData(Map params);

    void enablePublishing(Map params);

    void updatePublishingStatusMessage(Map params);

    void updateLastVerifiedGitlogCommitId(Map params);

    List<SiteFeed> getDeletedSites();

    /**
     * Set published repo created flag
     * @param siteId site identifier
     */
    void setPublishedRepoCreated(@Param(SITE_ID) String siteId);

    /**
     * Lock publisher task for site
     * @param siteId site identifier
     * @param lockOwnerId lock owner identifier
     * @param ttl TTL for lock
     * @return 1 if publishing was locked, otherwise 0
     */
    int tryLockPublishingForSite(@Param(SITE_ID) String siteId, @Param(LOCK_OWNER_ID) String lockOwnerId,
                                 @Param(TTL) int ttl);

    /**
     * unlock publisher task for site
     * @param siteId site identifier
     * @param lockOwnerId lock owner identifier
     */
    void unlockPublishingForSite(@Param(SITE_ID) String siteId, @Param(LOCK_OWNER_ID) String lockOwnerId);

    /**
     * update publishing lock heartbeat for site
     * @param siteId site identifier
     */
    void updatePublishingLockHeartbeatForSite(@Param(SITE_ID) String siteId);
}
