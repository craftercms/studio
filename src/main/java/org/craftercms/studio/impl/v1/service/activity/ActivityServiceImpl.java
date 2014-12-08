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
package org.craftercms.studio.impl.v1.service.activity;

import java.util.*;

import javolution.util.FastList;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.dal.ActivityFeed;
import org.craftercms.studio.api.v1.dal.ActivityFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivityServiceImpl extends AbstractRegistrableService implements ActivityService {

	private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);
	
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

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.AcitivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.craftercms.cstudio.alfresco.service.api.AcitivityService.ActivityType)
      *//*
	public void postActivity(String site, String user, String contentId, ActivityType activity, Map<String,String> extraInfo) {
		
		JSONObject activityPost = new JSONObject();
		activityPost.put(ACTIVITY_PROP_USER, user);
		activityPost.put(ACTIVITY_PROP_ID, contentId);
        if(extraInfo != null)
            activityPost.putAll(extraInfo);
		AuthenticationUtil.setFullyAuthenticatedUser(user);
		String contentType = null;
		if(extraInfo!=null)
			contentType = extraInfo.get(DmConstants.KEY_CONTENT_TYPE);
        CStudioActivityService activityService = getService(CStudioActivityService.class);
		activityService.postActivity(ContentUtils.generateActivityValue(activity), site, null, activityPost.toString(),contentId,contentType);

	}*/

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ActivityService#getLastActor(java.lang.String, java.lang.String, org.craftercms.cstudio.alfresco.service.api.ActivityService.ActivityType)
	 *//*
	public String getLastActor(String site, String key, ActivityType activity) {
		String activityType = (activity == null) ? null : ContentUtils.generateActivityValue(activity);
        CStudioActivityService activityService = getService(CStudioActivityService.class);
		return activityService.getLastActor(site, key, activityType);
	}*/

	/**
	 * @param postLookup the postLookup to set
	 *//*
	public void setPostLookup(PostLookup postLookup) {
		this._postLookup = postLookup;
	}*/

	/**
	 * @param feedGenerator the feedGenerator to set
	 *//*
	public void setFeedGenerator(FeedGenerator feedGenerator) {
		this._feedGenerator = feedGenerator;
	}*/
/*
	@Override
	public void renameContentId(String oldUrl, String newUrl, String site) {
        CStudioActivityService activityService = getService(CStudioActivityService.class);
		activityService.renameContent(oldUrl, newUrl,site);
	}
*//*
	@Override
	public void updateContentSummary(String site,String url,String summary) {
        CStudioActivityService activityService = getService(CStudioActivityService.class);
		activityService.updateSummary(site, url, summary);
	}
*/


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

		if (!userNamesAreCaseSensitive) {
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

		if(logger.isDebugEnabled()){
			logger.debug("Total Item post live filter : " + contentItems.size() + " hasMoreItems : "+hasMoreItems);
		}
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
			ContentItemTO item = contentService.getContentItem(site, id);
			if(item == null) // Item was deleted.
			{
				item = contentService.createDummyDmContentItemForDeletedNode(site, id);
				String modifier = (feedObject.containsKey(ACTIVITY_PROP_FEEDUSER)) ? feedObject.getString(ACTIVITY_PROP_FEEDUSER) : "";
				if(modifier != null && !modifier.isEmpty())
				{
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
				if (summaryObject.containsKey(CStudioConstants.CONTENT_TYPE)) {
					String contentType = (String)summaryObject.get(CStudioConstants.CONTENT_TYPE);
					item.contentType = contentType;
				}
				if(summaryObject.containsKey(CStudioConstants.INTERNAL_NAME)) {
					String internalName = (String)summaryObject.get(CStudioConstants.INTERNAL_NAME);
					item.internalName = internalName;
				}
				if(summaryObject.containsKey(CStudioConstants.BROWSER_URI)) {
					String browserUri = (String)summaryObject.get(CStudioConstants.BROWSER_URI);
					item.browserUri = browserUri;
				}
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

	@Autowired
	protected ActivityFeedMapper activityFeedMapper;
	protected boolean userNamesAreCaseSensitive = false;
	protected ContentService contentService;

	public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive) {
		this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
}
