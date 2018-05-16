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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.AuditFeed;
import org.craftercms.studio.api.v1.dal.AuditFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.craftercms.studio.api.v1.service.security.SecurityService;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
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

    @Autowired
    protected AuditFeedMapper auditFeedMapper;

    protected SiteService siteService;
    protected ContentService contentService;
    protected SecurityService securityService;
    protected StudioConfiguration studioConfiguration;
    protected DeploymentService deploymentService;

    @Override
    public void register() {
        getServicesManager().registerService(ActivityService.class, this);
    }

    @Override
    @ValidateParams
    public void postActivity(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, @ValidateStringParam(name = "contentId") String contentId, ActivityType activity, ActivitySource source, Map<String,String> extraInfo) {

        JSONObject activityPost = new JSONObject();
        activityPost.put(ACTIVITY_PROP_USER, user);
        activityPost.put(ACTIVITY_PROP_ID, contentId);
        if (extraInfo != null) {
            activityPost.putAll(extraInfo);
        }
        String contentType = null;
        if (extraInfo != null) {
            contentType = extraInfo.get(DmConstants.KEY_CONTENT_TYPE);
        }
        postActivity(activity.toString(), source.toString(), site, null, activityPost.toString(),contentId,contentType, user);

    }

    private void postActivity(String activityType, String activitySource, String siteNetwork, String appTool, String activityData,
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
            } else {
                // user names are not case-sensitive
                currentUser = currentUser.toLowerCase();
            }


            if (contentType == null) {
                contentType = CONTENT_TYPE_PAGE;
            }
        } catch (ServiceException e) {
            // log error and throw exception
            logger.error("Error in getting feeds", e);
        }

        try {
            ZonedDateTime postDate = ZonedDateTime.now(ZoneOffset.UTC);
            AuditFeed activityPost = new AuditFeed();
            activityPost.setUserId(currentUser);
            activityPost.setSiteNetwork(siteNetwork);
            activityPost.setSummary(activityData);
            activityPost.setType(activityType);
            activityPost.setCreationDate(postDate);
            activityPost.setModifiedDate(postDate);
            activityPost.setSummaryFormat("json");
            activityPost.setContentId(contentId);
            activityPost.setContentType(contentType);
            activityPost.setSource(activitySource);
            try {
                activityPost.setCreationDate(ZonedDateTime.now(ZoneOffset.UTC));
                long postId = insertFeedEntry(activityPost);
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


    private long insertFeedEntry(AuditFeed activityFeed) {
        DebugUtils.addDebugStack(logger);
        logger.debug("Insert activity " + activityFeed.getContentId());
        Long id = auditFeedMapper.insertActivityFeed(activityFeed);
        return (id != null ? id : -1);
    }

    @Override
    @ValidateParams
    public void renameContentId(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "oldUrl") String oldUrl, @ValidateSecurePathParam(name = "newUrl") String newUrl) {
        DebugUtils.addDebugStack(logger);
        logger.debug("Rename " + oldUrl + " to " + newUrl);
        Map<String, String> params = new HashMap<String, String>();
        params.put("newPath", newUrl);
        params.put("site", site);
        params.put("oldPath", oldUrl);
        auditFeedMapper.renameContent(params);
    }

    @Override
    @ValidateParams
    public List<ContentItemTO> getActivities(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, @ValidateIntegerParam(name = "num") int num, @ValidateStringParam(name = "sort") String sort, boolean ascending, boolean excludeLive, @ValidateStringParam(name = "filterType") String filterType) throws ServiceException {
        int startPos = 0;
        List<ContentItemTO> contentItems = new ArrayList<ContentItemTO>();
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

        List<AuditFeed> activityFeeds = null;
        activityFeeds = selectUserFeedEntries(user, ACTIVITY_FEED_FORMAT, site, startPos, size,
                filterType, hideLiveItems);
        for (AuditFeed activityFeed : activityFeeds) {
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
                item.published = true;
                item.setPublished(true);
                ZonedDateTime pubDate = deploymentService.getLastDeploymentDate(site, id);
                item.publishedDate = pubDate;
                item.setPublishedDate(pubDate);
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
                }

                String activitySummary = (feedObject.containsKey(ACTIVITY_PROP_ACTIVITY_SUMMARY)) ? feedObject.getString(ACTIVITY_PROP_ACTIVITY_SUMMARY) : "";
                JSONObject summaryObject = JSONObject.fromObject(activitySummary);
                if (summaryObject.containsKey(DmConstants.KEY_CONTENT_TYPE)) {
                    String contentType = (String)summaryObject.get(DmConstants.KEY_CONTENT_TYPE);
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
            ZonedDateTime editedDate = ZonedDateTime.parse(postDate);
            if (editedDate != null) {
                item.eventDate = editedDate.withZoneSameInstant(ZoneOffset.UTC);
            } else {
                item.eventDate = editedDate;
            }

            return item;
        } catch (Exception e) {
            logger.error("Error fetching content item for [" + id + "]", e.getMessage());
            return null;
        }
    }

    private List<AuditFeed> selectUserFeedEntries(String feedUserId, String format, String siteId, int startPos, int feedSize, String contentType, boolean hideLiveItems) {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("userId",feedUserId);
        params.put("summaryFormat",format);
        params.put("siteNetwork",siteId);
        params.put("startPos", startPos);
        params.put("feedSize", feedSize);
        params.put("activities", Arrays.asList(ActivityType.CREATED, ActivityType.DELETED, ActivityType.UPDATED, ActivityType.MOVED));
        if(StringUtils.isNotEmpty(contentType) && !contentType.toLowerCase().equals("all")){
            params.put("contentType",contentType.toLowerCase());
        }
        if (hideLiveItems) {
            List<String> statesValues = new ArrayList<String>();
            for (State state : State.LIVE_STATES) {
                statesValues.add(state.name());
            }
            params.put("states", statesValues);
            return auditFeedMapper.selectUserFeedEntriesHideLive(params);
        } else {
            return auditFeedMapper.selectUserFeedEntries(params);
        }
    }

    @Override
    @ValidateParams
    public AuditFeed getDeletedActivity(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("contentId", path);
        params.put("siteNetwork", site);
        String activityType = ActivityType.DELETED.toString();
        params.put("activityType", activityType);
        return auditFeedMapper.getDeletedActivity(params);
    }

    @Override
    @ValidateParams
    public void deleteActivitiesForSite(@ValidateStringParam(name = "site") String site) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        auditFeedMapper.deleteActivitiesForSite(params);
    }

    @Override
    @ValidateParams
    public List<AuditFeed> getAuditLogForSite(@ValidateStringParam(name = "site") String site, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number, @ValidateStringParam(name = "user") String user, List<String> actions)
            throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("start", start);
            params.put("number", number);
            if (StringUtils.isNotEmpty(user)) {
                params.put("user", user);
            }
            if (CollectionUtils.isNotEmpty(actions)) {
                params.put("actions", actions);
            }
            return auditFeedMapper.getAuditLogForSite(params);
        }
    }

    @Override
    @ValidateParams
    public long getAuditLogForSiteTotal(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, List<String> actions)
            throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            if (StringUtils.isNotEmpty(user)) {
                params.put("user", user);
            }
            if (CollectionUtils.isNotEmpty(actions)) {
                params.put("actions", actions);
            }
            return auditFeedMapper.getAuditLogForSiteTotal(params);
        }
    }

    public boolean getUserNamesAreCaseSensitive() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(ACTIVITY_USERNAME_CASE_SENSITIVE));
        return toReturn;
    }



    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public DeploymentService getDeploymentService() { return deploymentService; }
    public void setDeploymentService(DeploymentService deploymentService) { this.deploymentService = deploymentService; }
}
