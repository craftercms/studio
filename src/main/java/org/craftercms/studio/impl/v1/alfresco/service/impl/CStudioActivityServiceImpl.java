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
package org.craftercms.studio.impl.v1.alfresco.service.impl;

import java.util.HashMap;
import org.craftercms.studio.api.domain.ActivityFeed;
import org.craftercms.studio.api.persistence.ActivityFeedMapper;
import org.craftercms.studio.impl.v1.alfresco.service.AbstractRegistrableService;
import org.craftercms.studio.impl.v1.alfresco.service.api.CStudioActivityService;
import org.craftercms.studio.impl.v1.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CStudioActivityServiceImpl extends AbstractRegistrableService implements CStudioActivityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CStudioActivityServiceImpl.class);
	protected static final int MAX_LEN_USER_ID = 255; // needs to match schema:
	// feed_user_id,
	// post_user_id
	protected static final int MAX_LEN_SITE_ID = 255; // needs to match schema:
	// site_network
	protected static final int MAX_LEN_ACTIVITY_TYPE = 255; // needs to match
	// schema:
	// activity_type
	protected static final int MAX_LEN_ACTIVITY_DATA = 4000; // needs to match
	// schema:
	// activity_data
	protected static final int MAX_LEN_APP_TOOL_ID = 36; // needs to match
	// schema: app_tool

	@Autowired
	protected ActivityFeedMapper activityFeedMapper;
	//protected boolean userNamesAreCaseSensitive = false;

	//public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive) {
	//	this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
	//}



	@Override
	public void register() {
		getServicesManager().registerService(CStudioActivityService.class, this);
	}

	/*
    public void postActivity(String activityType, String siteNetwork, String appTool, String activityData,
			String contentId, String contentType) {
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		try {
			// optional - default to empty string
			if (siteNetwork == null) {
				siteNetwork = "";
			} else if (siteNetwork.length() > MAX_LEN_SITE_ID) {
				throw new ServiceException("Invalid site network - exceeds " + MAX_LEN_SITE_ID + " chars: "
						+ siteNetwork);
			}

			// optional - default to empty string
			if (appTool == null) {
				appTool = "";
			} else if (appTool.length() > MAX_LEN_APP_TOOL_ID) {
				throw new ServiceException("Invalid app tool - exceeds " + MAX_LEN_APP_TOOL_ID + " chars: " + appTool);
			}

			// required
			ParameterCheck.mandatoryString("activityType", activityType);
			if (activityType.length() > MAX_LEN_ACTIVITY_TYPE) {
				throw new ServiceException("Invalid activity type - exceeds " + MAX_LEN_ACTIVITY_TYPE + " chars: "
						+ activityType);
			}

			// optional - default to empty string
			if (activityData == null) {
				activityData = "";
			} else if (activityType.length() > MAX_LEN_ACTIVITY_DATA) {
				throw new ServiceException("Invalid activity data - exceeds " + MAX_LEN_ACTIVITY_DATA + " chars: "
						+ activityData);
			}

			// required
			ParameterCheck.mandatoryString("currentUser", currentUser);
			if (currentUser.length() > MAX_LEN_USER_ID) {
				throw new ServiceException("Invalid user - exceeds " + MAX_LEN_USER_ID + " chars: " + currentUser);
			} else if ((!currentUser.equals(AuthenticationUtil.SYSTEM_USER_NAME)) && (!userNamesAreCaseSensitive)) {
				// user names are not case-sensitive
				currentUser = currentUser.toLowerCase();
			}
			if (contentType == null) {
				contentType = DmConstants.CONTENT_TYPE_PAGE;
			}
		} catch (ServiceException e) {
			// log error and throw exception
			LOGGER.error("Error in getting feeds", e);
		}

		try {
			Date postDate = new Date();
			CStudioActivityFeedDAO activityPost = new CStudioActivityFeedDAO();
			activityPost.setUserId(currentUser);
			activityPost.setSiteNetwork(siteNetwork);
			activityPost.setSummary(activityData);
			activityPost.setType(activityType);
			activityPost.setCreationDate(postDate);
			activityPost.setModifiedDate(postDate);
			activityPost.setSummaryFormat("json");
			activityPost.setContentId(contentId);
			activityPost.setContentType(contentType);
			try {
				long postId = feedDaoService.postFeedEntry(activityPost);
				if (LOGGER.isDebugEnabled()) {
					activityPost.setId(postId);
					LOGGER.debug("Posted: " + activityPost);
				}
			} catch (SQLException e) {
				throw new ServiceException("Failed to post activity: " + e, e);
			}
		}

		catch (ServiceException e) {
			// log error, subsume exception (for post activity)
			LOGGER.error("Error in posting feed", e);
		}

	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries
	 * (java.lang.String, java.lang.String, java.lang.String)
	 *//*
	public List<String> getUserFeedEntries(String feedUserId, String format, String siteId, int startPos, int feedSize,
			String contentType, boolean hideLiveItems) {
		// NOTE: siteId is optional
		ParameterCheck.mandatoryString("feedUserId", feedUserId);
		ParameterCheck.mandatoryString("format", format);
		List<String> activityFeedEntries = new ArrayList<String>();

		if (!userNamesAreCaseSensitive) {
			feedUserId = feedUserId.toLowerCase();
		}

		try {
			List<CStudioActivityFeedDAO> activityFeeds = null;
			activityFeeds = feedDaoService.selectUserFeedEntries(feedUserId, format, siteId, startPos, feedSize,
					contentType, hideLiveItems);
			for (CStudioActivityFeedDAO activityFeed : activityFeeds) {
				activityFeedEntries.add(activityFeed.getJSONString());
			}
		} catch (SQLException se) {
			LOGGER.error("Error in getting feeds", se);
		} catch (JSONException se) {
			LOGGER.error("Error in getting feeds", se);
		}
		return activityFeedEntries;
	}*/

	/*
	@Override
	public void renameContent(String oldUrl, String newUrl, String site) {
		try {
			feedDaoService.updateUrl(oldUrl, newUrl,site);
		} catch (SQLException se) {
			LOGGER.error("Error in getting feeds", se);
		}
	}*/

	/*
	@Override
	public void updateSummary(String site, String url, String newSummary) {
		try {
			feedDaoService.updateSummary(site, url, newSummary);
		} catch (SQLException se) {
			LOGGER.error("Error in updating summary", se);
		}

	}*/

	/*
	@Override
	public String getLastActor(String site, String key, String activityType) {
		try {
			return feedDaoService.getLastActor(site, key, activityType);
		} catch (SQLException e) {
			LOGGER.error("Error while getting the last actor of " + key, e);
			return null;

		}

	}*/

	@Override
	public ActivityFeed getDeletedActivity(String site, String path) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("contentId", path);
		params.put("siteNetwork", site);
		String activityType = ContentUtils.generateActivityValue(CStudioActivityService.ActivityType.DELETED);
		params.put("activityType", activityType);
		return activityFeedMapper.getDeletedActivity(params);
	}

	public void setActivityFeedMapper(ActivityFeedMapper activityFeedMapper) {
		this.activityFeedMapper = activityFeedMapper;
	}
}
