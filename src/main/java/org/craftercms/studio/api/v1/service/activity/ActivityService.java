/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.service.activity;


import org.craftercms.studio.api.v1.dal.ActivityFeed;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.ContentItemTO;

import java.util.List;
import java.util.Map;

/**
 * Provides services for tracking user activities
 * 
 * @author hyanghee
 *
 */
public interface ActivityService {

	/** a prefix to attach to all activities **/
	public static final String ACTIVITY_TYPE_KEY_PREFIX = "org.craftercms.cstudio.";
	
	enum ActivityType {
		CREATED,
		UPDATED,
		DELETED
	}
	
	/**
	 * post an activity
	 * 
	 * @param site
	 * @param user
	 * @param key
	 * 			identifies the content that this activity is related to
	 * @param activity
	 */
	public void postActivity(String site, String user, String key, ActivityType activity, Map<String, String> extraInfo);
	
	/**
	 * get the last user worked on the given content 
	 * 
	 * @param site
	 * @param relativePath
	 * @param activity
	 * 			activity type filter
	 * @return
	 */
	//public String getLastActor(String site, String key, ActivityType activity);

	public void renameContentId(String site, String oldUrl, String newUrl);
	
	//public void updateContentSummary(String site, String url, String summary);

	/**
	 * get a list of activities by the given user
	 * @param site
	 * @param user
	 * @param num
	 * 			a number of records to return
	 * @param sort
	 * @param ascending
	 * @param excludeLive
	 * 			exclude live items?
	 * @return a list of activities
	 * @throws org.craftercms.studio.api.v1.exception.ServiceException
	 */
	public List<ContentItemTO> getActivities(String site, String user, int num, String sort, boolean ascending, boolean excludeLive, String filterType) throws ServiceException;

	/**
	 * Retrieve user feed
	 *
	 * @param userId - required
	 * @param format - required
	 * @param siteId - optional, if set then will filter by given siteId else return all sites
	 * @return list of JSON feed entries
	 */

	public ActivityFeed getDeletedActivity(String site, String path);

	void deleteActivitiesForSite(String site);
}
