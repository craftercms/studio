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

package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

public interface SiteFeedMapper {

    List<SiteFeed> getSites();

    int countSites();

    SiteFeed getSite(Map params);

	boolean createSite(SiteFeed siteFeed);

    boolean deleteSite(String siteId);

    void updateLastCommitId(Map params);

    String getLastCommitId(Map params);

    Integer exists(String siteId);

    int getSitesPerUserQueryTotal(Map params);

    List<String> getSitesPerUserQuery(Map params);

    List<SiteFeed> getSitesPerUserData(Map params);

    void enablePublishing(Map params);

    void updatePublishingStatusMessage(Map params);

    void updateLastVerifiedGitlogCommitId(Map params);

    List<SiteFeed> getDeletedSites();
}
