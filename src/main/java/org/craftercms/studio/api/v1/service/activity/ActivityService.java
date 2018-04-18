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


import org.craftercms.studio.api.v1.dal.AuditFeed;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
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

	enum ActivityType {
		CREATED,
		UPDATED,
		DELETED,
        MOVED,
        ADD_USER_TO_GROUP,
        REMOVE_USER_FROM_GROUP,
        LOGIN,
        LOGOUT,
        CREATE_SITE,
        DELETE_SITE,
        PUSH_TO_REMOTE,
        PULL_FROM_REMOTE
	}

	enum ActivitySource {
	    UI,
        REPOSITORY
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
	void postActivity(String site, String user, String key, ActivityType activity, ActivitySource source, Map<String, String> extraInfo);

	void renameContentId(String site, String oldUrl, String newUrl);

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
	List<ContentItemTO> getActivities(String site, String user, int num, String sort, boolean ascending, boolean excludeLive, String filterType) throws ServiceException;

	/**
	 * Get activity for deleted content
	 *
	 * @param site - site id
	 * @param path - path of the content
	 * @return list of JSON feed entries
	 */

	AuditFeed getDeletedActivity(String site, String path);

	void deleteActivitiesForSite(String site);

    /**
     * Get audit log for site
     *
     * @param site site
     * @param start
     * @param number
     * @param user
     * @param actions
     * @return audit list
     * @throws SiteNotFoundException
     */
    List<AuditFeed> getAuditLogForSite(String site, int start, int number, String user, List<String> actions) throws SiteNotFoundException;

    long getAuditLogForSiteTotal(String site, String user, List<String> actions) throws SiteNotFoundException;
}
