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
package org.craftercms.cstudio.alfresco.activityfeed;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface CStudioActivityFeedDaoService {

    public void initIndexes();

	/**
	 * delete feed entries generated before the date
	 * 
	 * @param keepDate
	 * @return
	 * @throws SQLException
	 */
	public int deleteFeedEntries(Date keepDate) throws SQLException;

	/**
	 * insert a new feed entry 
	 * 
	 * @param activityFeed
	 * @return
	 * @throws SQLException
	 */
	public long insertFeedEntry(CStudioActivityFeedDAO activityFeed) throws SQLException;

	/**
	 * post a feed entry 
	 * 
	 * @param activityFeed
	 * @return
	 * @throws SQLException
	 */
	public long postFeedEntry(CStudioActivityFeedDAO activityFeed) throws SQLException;


	/**
	 * update the old url to be the new url
	 * 
	 * @param oldUrl
	 * @param newUrl
	 * @param site
	 * @throws SQLException
	 */
	public void updateUrl(String oldUrl,String newUrl,String site) throws SQLException;

	/**
	 * update the summary of an activity
	 *  
	 * @param site
	 * @param Url
	 * @param newSummary
	 * @throws SQLException
	 */
	public void updateSummary(String site, String Url, String newSummary) throws SQLException;

	/**
	 * get user feed entries
	 * 
	 * @param feedUserId
	 * @param format
	 * @param siteId
	 * @param startPos
	 * @param feedSize
	 * @param contentType
	 * @return
	 * @throws SQLException
	 */
	public List<CStudioActivityFeedDAO> selectUserFeedEntries(String feedUserId, String format, String siteId,
			int startPos, int feedSize, String contentType, boolean hideLiveItems) throws SQLException;
	
	/**
	 * find the last user worked on the content by the given relativePath based
	 * on the activity type
	 * 
	 * @param site
	 * @param contentId
	 * @param activityType 
	 * 			optional
	 * @return
	 * @throws SQLException
	 */
	public String getLastActor(String site, String contentId, String activityType) throws SQLException;

    public void deleteActivitiesForSite(String site);

    public CStudioActivityFeedDAO getDeletedActivity(String site, String contentId) throws SQLException;
}
