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

import java.util.Map;

import net.sf.json.JSONObject;

import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityServiceImpl extends AbstractRegistrableService implements ActivityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityServiceImpl.class);
	
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


}
