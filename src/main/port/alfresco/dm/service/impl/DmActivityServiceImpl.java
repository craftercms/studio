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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.util.Date;
import java.util.List;

import javolution.util.FastList;
import net.sf.json.JSONObject;

import org.alfresco.model.ContentModel;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.filter.DmFilterWrapper;
import org.craftercms.cstudio.alfresco.dm.service.api.DmActivityService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.service.api.CStudioActivityService;
import org.craftercms.cstudio.alfresco.service.api.ProfileService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.service.impl.ActivityServiceImpl;
import org.craftercms.cstudio.alfresco.to.UserProfileTO;
import org.craftercms.cstudio.alfresco.util.api.ContentItemExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dejan Brkic
 */
public class DmActivityServiceImpl extends ActivityServiceImpl implements DmActivityService{
    
    private static final Logger logger = LoggerFactory.getLogger(DmActivityServiceImpl.class);

    @Override
    public void register() {
        getServicesManager().registerService(DmActivityService.class, this);
    }

    /**
     * content item extractors
     */
    protected ContentItemExtractor _contentItemExtractor = null;
    public ContentItemExtractor getContentItemExtractor() {
        return _contentItemExtractor;
    }
    public void setContentItemExtractor(ContentItemExtractor contentItemExtractor) {
        this._contentItemExtractor = contentItemExtractor;
    }

    protected DmFilterWrapper _dmFilterWrapper;
    public DmFilterWrapper getDmFilterWrapper() {
        return _dmFilterWrapper;
    }

    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
        this._dmFilterWrapper = dmFilterWrapper;
    }

    @Override
    public List<DmContentItemTO> getActivities(String site, String user, int num, String sort, boolean ascending, boolean excludeLive, String filterType) throws ServiceException {
        int startPos = 0;
        if (_contentItemExtractor != null) {
            List<DmContentItemTO> contentItems = new FastList<DmContentItemTO>();
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
        } else {
            throw new ServiceException("no content item extractor found");
        }
    }

    /**
     *
     * Returns all non-live items if hideLiveItems is true, else should return all feeds back
     *
     */
    protected boolean getActivityFeeds(String user, String site,int startPos, int size, String filterType,boolean hideLiveItems,List<DmContentItemTO> contentItems,int remainingItem){
        CStudioActivityService activityService = getService(CStudioActivityService.class);
        List<String> feeds = activityService.getUserFeedEntries(user, ACTIVITY_FEED_FORMAT, site,startPos,size,filterType, hideLiveItems);
        boolean hasMoreItems=true;

        //if number of items returned is less than size it means that table has no more records
        if(feeds.size()<size){
            hasMoreItems=false;
        }

        if (feeds != null && feeds.size() > 0) {
            for (int index = 0; index < feeds.size() && remainingItem!=0; index++) {
                JSONObject feedObject = JSONObject.fromObject(feeds.get(index));
                String id = (feedObject.containsKey(ACTIVITY_PROP_CONTENTID)) ? feedObject.getString(ACTIVITY_PROP_CONTENTID) : "";
                DmContentItemTO item = createActivityItem(site, feedObject, id, _contentItemExtractor);
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
     * @param extractor
     * @return activity
     */
    protected DmContentItemTO createActivityItem(String site, JSONObject feedObject, String id, ContentItemExtractor extractor) {
        try {
            DmContentItemTO item = (DmContentItemTO) extractor.extractContent(site, id);
            if(item == null) // Item was deleted.
            {
                DmContentService dmContentService = getService(DmContentService.class);
                item = dmContentService.createDummyDmContentItemForDeletedNode(site, id);
                String modifier = (feedObject.containsKey(ACTIVITY_PROP_FEEDUSER)) ? feedObject.getString(ACTIVITY_PROP_FEEDUSER) : "";
                if(modifier != null && !modifier.isEmpty())
                {
                    item.setUser(modifier);
                    ProfileService profileService = getService(ProfileService.class);
                    UserProfileTO profile = profileService.getUserProfile(modifier, site, false);
                    if (profile != null) {
                        item.setUserFirstName(profile.getProfile().get(ContentModel.PROP_FIRSTNAME.getLocalName()));
                        item.setUserLastName(profile.getProfile().get(ContentModel.PROP_LASTNAME.getLocalName()));
                    }
                }

                String activitySummary = (feedObject.containsKey(ACTIVITY_PROP_ACTIVITY_SUMMARY)) ? feedObject.getString(ACTIVITY_PROP_ACTIVITY_SUMMARY) : "";
                JSONObject summaryObject = JSONObject.fromObject(activitySummary);
                if (summaryObject.containsKey(CStudioConstants.CONTENT_TYPE)) {
                    String contentType = (String)summaryObject.get(CStudioConstants.CONTENT_TYPE);
                    item.setContentType(contentType);
                }
                if(summaryObject.containsKey(CStudioConstants.INTERNAL_NAME)) {
                    String internalName = (String)summaryObject.get(CStudioConstants.INTERNAL_NAME);
                    item.setInternalName(internalName);
                }
                if(summaryObject.containsKey(CStudioConstants.BROWSER_URI)) {
                    String browserUri = (String)summaryObject.get(CStudioConstants.BROWSER_URI);
                    item.setBrowserUri(browserUri);
                }
            }
            String postDate = (feedObject.containsKey(ACTIVITY_PROP_POST_DATE)) ? feedObject.getString(ACTIVITY_PROP_POST_DATE) : "";
            Date editedDate = getEditedDate(postDate);
            item.setEventDate(editedDate);

            return item;
        } catch (Exception e) {
            logger.error("Error fetching content item for [" + id + "]", e.getMessage());
            return null;
        }
    }

    /**
     * get the edited date from the given string
     *
     * @param str
     * @return date
     */
    protected Date getEditedDate(String str) {
        if (!StringUtils.isEmpty(str)) {
            return ISO8601DateFormat.parse(str);
        } else {
            logger.error("No activity post date provided.");
            return null;
        }
    }
}
