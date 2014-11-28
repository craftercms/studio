/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.service.activity;

import org.craftercms.studio.api.v1.dal.ActivityFeed;

public interface CStudioActivityService {

    public static final String ACTIVITY_TYPE_KEY_PREFIX = "org.craftercms.cstudio.";

    enum ActivityType {
        CREATED,
        UPDATED,
        DELETED
    }

	/*
     * Post Activity
     */

    /**
     * Post a custom activity type
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param jsonActivityData - required
     */
    //public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData,String contentId, String contentType);
    
    /*
     * Retrieve Feed Entries
     */

    /**
     * Retrieve user feed
     *
     * @param userId - required
     * @param format - required
     * @param siteId - optional, if set then will filter by given siteId else return all sites
     * @return list of JSON feed entries
     */
    //public List<String> getUserFeedEntries(String userId, String format, String siteId,int startPos, int feedSize,String contentType, boolean hideLiveItems);

    /**
     * get the last user worked on the given content 
     *
     * @param site
     * @param relativePath
     * @param activityType
     * 			activity type filter
     * @return
     */
    //public String getLastActor(String site, String key, String activityType);

    //public void renameContent(String oldUrl, String  newUrl,String site);

    //public void updateSummary(String site,String Url,String newSummary);

    public ActivityFeed getDeletedActivity(String site, String path);
}
