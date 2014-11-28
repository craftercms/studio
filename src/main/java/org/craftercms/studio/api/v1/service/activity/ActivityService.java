/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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
	//public void postActivity(String site, String user, String key, ActivityType activity, Map<String, String> extraInfo);
	
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

	//public void renameContentId(String oldUrl, String newUrl, String site);
	
	//public void updateContentSummary(String site, String url, String summary);
}
