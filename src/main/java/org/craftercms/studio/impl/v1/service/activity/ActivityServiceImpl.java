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
package org.craftercms.studio.impl.v1.service.activity;

import java.util.*;

import javolution.util.FastList;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ActivityFeed;
import org.craftercms.studio.api.v1.dal.ActivityFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.craftercms.studio.api.v1.service.security.SecurityService;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.ACTIVITY_USERNAME_CASE_SENSITIVE;

public class ActivityServiceImpl extends AbstractRegistrableService implements ActivityService {

	private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

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

	/** activity post properties **/
	protected static final String ACTIVITY_PROP_ACTIVITY_SUMMARY = "activitySummary";
	protected static final String ACTIVITY_PROP_ID = "id";
	protected static final String ACTIVITY_PROP_POST_DATE = "postDate";
	protected static final String ACTIVITY_PROP_USER = "user";
	protected static final String ACTIVITY_PROP_FEEDUSER = "feedUserId";
	protected static final String ACTIVITY_PROP_CONTENTID = "contentId";

	/** activity feed format **/
	protected static final String ACTIVITY_FEED_FORMAT = "json";

	/**
	 * activity post lookup
	 */
	//protected PostLookup _postLookup;

	/**
	 * activity feed generator
	 */
	//protected FeedGenerator _feedGenerator;

    @Override
    public void register() {
        getServicesManager().registerService(ActivityService.class, this);
    }

	public void postActivity(String site, String user, String contentId, ActivityType activity, Map<String,String> extraInfo) {

		JSONObject activityPost = new JSONObject();
		activityPost.put(ACTIVITY_PROP_USER, user);
		activityPost.put(ACTIVITY_PROP_ID, contentId);
        if(extraInfo != null)
            activityPost.putAll(extraInfo);
		//AuthenticationUtil.setFullyAuthenticatedUser(user);
		String contentType = null;
		if(extraInfo!=null)
			contentType = extraInfo.get(DmConstants.KEY_CONTENT_TYPE);
		postActivity(ContentUtils.generateActivityValue(activity), site, null, activityPost.toString(),contentId,contentType, user);

	}

	private void postActivity(String activityType, String siteNetwork, String appTool, String activityData,
							 String contentId, String contentType, String approver) {
		String currentUser = (StringUtils.isEmpty(approver)) ? securityService.getCurrentUser() : approver;
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
			if (StringUtils.isEmpty(activityType)) {
				throw new ServiceException("Invalid activity type - activity type is empty");
			} else if (activityType.length() > MAX_LEN_ACTIVITY_TYPE) {
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
			if (StringUtils.isEmpty(currentUser)) {
				throw new ServiceException("Invalid user - user is empty");
			} else if (currentUser.length() > MAX_LEN_USER_ID) {
				throw new ServiceException("Invalid user - exceeds " + MAX_LEN_USER_ID + " chars: " + currentUser);
			} else { //if ((!currentUser.equals(securityService.getAdministratorUser())) && (!userNamesAreCaseSensitive)) {
				// user names are not case-sensitive
				currentUser = currentUser.toLowerCase();
			}


			if (contentType == null) {
				contentType = DmConstants.CONTENT_TYPE_PAGE;
			}
		} catch (ServiceException e) {
			// log error and throw exception
			logger.error("Error in getting feeds", e);
		}

		try {
			Date postDate = new Date();
			ActivityFeed activityPost = new ActivityFeed();
			activityPost.setUserId(currentUser);
			activityPost.setSiteNetwork(siteNetwork);
			activityPost.setSummary(activityData);
			activityPost.setType(activityType);
			activityPost.setCreationDate(postDate);
			activityPost.setModifiedDate(postDate);
			activityPost.setSummaryFormat("json");
			activityPost.setContentId(contentId);
			activityPost.setContentType(contentType);
			//activityFeedMapper.updateActivityFeed(activityPost);
			try {
				long postId = postFeedEntry(activityPost);
				activityPost.setId(postId);
				logger.debug("Posted: " + activityPost);

			} catch (Exception e) {
				throw new ServiceException("Failed to post activity: " + e, e);
			}
		}

		catch (ServiceException e) {
			// log error, subsume exception (for post activity)
			logger.error("Error in posting feed", e);
		}

	}

	private long postFeedEntry(ActivityFeed activityFeed) {
		int count=getCountUserContentFeedEntries(activityFeed.getUserId(),activityFeed.getSiteNetwork(),activityFeed.getContentId());
		if(count==0)
		{
			activityFeed.setCreationDate(new Date());
			return insertFeedEntry(activityFeed);
		}
		else
		{
			updateFeedEntry(activityFeed);
			return -1;
		}
	}

	private int getCountUserContentFeedEntries(String feedUserId, String siteId,String contentId) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("userId",feedUserId);
		params.put("contentId",contentId);
		params.put("siteNetwork",siteId);

		// where feed user is me and post user is not me
		return activityFeedMapper.getCountUserContentFeedEntries(params);
	}

	private long insertFeedEntry(ActivityFeed activityFeed) {
		DebugUtils.addDebugStack(logger);
		logger.debug("Insert activity " + activityFeed.getContentId());
		Long id = activityFeedMapper.insertActivityFeed(activityFeed);
		return (id != null ? id : -1);
	}

	private void updateFeedEntry(ActivityFeed activityFeed) {
		DebugUtils.addDebugStack(logger);
		logger.debug("Update activity " + activityFeed.getContentId());
		activityFeedMapper.updateActivityFeed(activityFeed);

	}


	@Override
	public void renameContentId(String site, String oldUrl, String newUrl) {
		DebugUtils.addDebugStack(logger);
		logger.debug("Rename " + oldUrl + " to " + newUrl);
		Map<String, String> params = new HashMap<String, String>();
		params.put("newPath", newUrl);
		params.put("site", site);
		params.put("oldPath", oldUrl);
        activityFeedMapper.renameContent(params);
	}

	@Override
	public List<ContentItemTO> getActivities(String site, String user, int num, String sort, boolean ascending, boolean excludeLive, String filterType) throws ServiceException {
		int startPos = 0;
		List<ContentItemTO> contentItems = new FastList<ContentItemTO>();
		boolean hasMoreItems = true;
		while(contentItems.size() < num && hasMoreItems){
			int remainingItems = num - contentItems.size();
			hasMoreItems = getActivityFeeds(user, site, startPos, num , filterType, excludeLive,contentItems,remainingItems);
			startPos = startPos+num;
		}
		if(contentItems.size() > num){
			return contentItems.subList(0, num);
		}
		return contentItems;
	}

	/**
	 *
	 * Returns all non-live items if hideLiveItems is true, else should return all feeds back
	 *
	 */
	protected boolean getActivityFeeds(String user, String site,int startPos, int size, String filterType,boolean hideLiveItems,List<ContentItemTO> contentItems,int remainingItem){
		List<String> activityFeedEntries = new ArrayList<String>();

		if (!getUserNamesAreCaseSensitive()) {
			user = user.toLowerCase();
		}

		List<ActivityFeed> activityFeeds = null;
		activityFeeds = selectUserFeedEntries(user, ACTIVITY_FEED_FORMAT, site, startPos, size,
				filterType, hideLiveItems);
		for (ActivityFeed activityFeed : activityFeeds) {
			activityFeedEntries.add(activityFeed.getJSONString());
		}

		boolean hasMoreItems=true;

		//if number of items returned is less than size it means that table has no more records
		if(activityFeedEntries.size()<size){
			hasMoreItems=false;
		}

		if (activityFeedEntries != null && activityFeedEntries.size() > 0) {
			for (int index = 0; index < activityFeedEntries.size() && remainingItem!=0; index++) {
				JSONObject feedObject = JSONObject.fromObject(activityFeedEntries.get(index));
				String id = (feedObject.containsKey(ACTIVITY_PROP_CONTENTID)) ? feedObject.getString(ACTIVITY_PROP_CONTENTID) : "";
				ContentItemTO item = createActivityItem(site, feedObject, id);
				contentItems.add(item);
				remainingItem--;
			}
		}
		logger.debug("Total Item post live filter : " + contentItems.size() + " hasMoreItems : "+hasMoreItems);

		return hasMoreItems;
	}

	/**
	 * create an activity from the given feed
	 *
	 * @param site
	 * @param feedObject
	 * @return activity
	 */
	protected ContentItemTO createActivityItem(String site, JSONObject feedObject, String id) {
		try {
			ContentItemTO item = contentService.getContentItem(site, id, 0);
			if(item == null || item.isDeleted()) {
				item = contentService.createDummyDmContentItemForDeletedNode(site, id);
				String modifier = (feedObject.containsKey(ACTIVITY_PROP_FEEDUSER)) ? feedObject.getString(ACTIVITY_PROP_FEEDUSER) : "";
				if(modifier != null && !modifier.isEmpty()) {
					item.user = modifier;
                    /* TODO: extract user information
                    ProfileService profileService = getService(ProfileService.class);
                    UserProfileTO profile = profileService.getUserProfile(modifier, site, false);
                    if (profile != null) {
                        item.setUserFirstName(profile.getProfile().get(ContentModel.PROP_FIRSTNAME.getLocalName()));
                        item.setUserLastName(profile.getProfile().get(ContentModel.PROP_LASTNAME.getLocalName()));
                    }*/
				}

				String activitySummary = (feedObject.containsKey(ACTIVITY_PROP_ACTIVITY_SUMMARY)) ? feedObject.getString(ACTIVITY_PROP_ACTIVITY_SUMMARY) : "";
				JSONObject summaryObject = JSONObject.fromObject(activitySummary);
				if (summaryObject.containsKey(StudioConstants.CONTENT_TYPE)) {
					String contentType = (String)summaryObject.get(StudioConstants.CONTENT_TYPE);
					item.contentType = contentType;
				}
				if(summaryObject.containsKey(StudioConstants.INTERNAL_NAME)) {
					String internalName = (String)summaryObject.get(StudioConstants.INTERNAL_NAME);
					item.internalName = internalName;
				}
				if(summaryObject.containsKey(StudioConstants.BROWSER_URI)) {
					String browserUri = (String)summaryObject.get(StudioConstants.BROWSER_URI);
					item.browserUri = browserUri;
				}
                item.setLockOwner("");
			}
			String postDate = (feedObject.containsKey(ACTIVITY_PROP_POST_DATE)) ? feedObject.getString(ACTIVITY_PROP_POST_DATE) : "";
			Date editedDate = ContentUtils.getEditedDate(postDate);
			item.eventDate = editedDate;

			return item;
		} catch (Exception e) {
			logger.error("Error fetching content item for [" + id + "]", e.getMessage());
			return null;
		}
	}

	private List<ActivityFeed> selectUserFeedEntries(String feedUserId, String format, String siteId,int startPos, int feedSize,String contentType, boolean hideLiveItems) {
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("userId",feedUserId);
		params.put("summaryFormat",format);
		params.put("siteNetwork",siteId);
		params.put("startPos", startPos);
		params.put("feedSize", feedSize);
		if(StringUtils.isNotEmpty(contentType) && !contentType.toLowerCase().equals("all")){
			params.put("contentType",contentType.toLowerCase());
		}
		if (hideLiveItems) {
			List<String> statesValues = new ArrayList<String>();
			for (State state : State.LIVE_STATES) {
				statesValues.add(state.name());
			}
			params.put("states", statesValues);
			return activityFeedMapper.selectUserFeedEntriesHideLive(params);
		} else {
			return activityFeedMapper.selectUserFeedEntries(params);
		}
	}

	@Override
	public ActivityFeed getDeletedActivity(String site, String path) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("contentId", path);
		params.put("siteNetwork", site);
		String activityType = ContentUtils.generateActivityValue(ActivityType.DELETED);
		params.put("activityType", activityType);
		return activityFeedMapper.getDeletedActivity(params);
	}

	@Override
	public void deleteActivitiesForSite(String site) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("site", site);
		activityFeedMapper.deleteActivitiesForSite(params);
	}

    @Override
    public List<ActivityFeed> getAuditLogForSite(String site, int startPos, int feedSize) {
        Map<String, Object> params = new HashMap<String,Object>();
        params.put("site", site);
        params.put("startPos", startPos);
        params.put("feedSize", feedSize);
        return activityFeedMapper.getAuditLogForSite(params);
    }

    public boolean getUserNamesAreCaseSensitive() {
	    boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(ACTIVITY_USERNAME_CASE_SENSITIVE));
	    return toReturn;
    }

    @Autowired
	protected ActivityFeedMapper activityFeedMapper;
	protected ContentService contentService;

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

    protected SecurityService securityService;
    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    protected StudioConfiguration studioConfiguration;

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}
