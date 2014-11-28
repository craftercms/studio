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

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javolution.util.FastList;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.activity.CStudioActivityService;
import org.craftercms.studio.api.v1.service.activity.DmActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmPathTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dejan Brkic
 */
public class DmActivityServiceImpl extends ActivityServiceImpl implements DmActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(DmActivityServiceImpl.class);

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
                item = createDummyDmContentItemForDeletedNode(site, id);
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
            Date editedDate = getEditedDate(postDate);
            item.eventDate = editedDate;

            return item;
        } catch (Exception e) {
            logger.error("Error fetching content item for [" + id + "]", e.getMessage());
            return null;
        }
    }

    private ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath){
        String absolutePath = expandRelativeSitePath(site, relativePath);
        DmPathTO path = new DmPathTO(absolutePath);
        ContentItemTO item = new ContentItemTO();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = path.getName();
        //String relativePath = path.getRelativePath();
        String fullPath = path.toString();
        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativePath.replace("/" + name, "") : relativePath;
        item.path = folderPath;
        /**
         * Internal name should be just folder name
         */
        String internalName = folderPath;
        int index = folderPath.lastIndexOf('/');
        if (index != -1)
            internalName = folderPath.substring(index + 1);

        item.internalName = internalName;
        //item.title = internalName;
        item.isDisabled = false;
        item.isNavigation = false;
        item.name = name;
        item.uri = relativePath;

        //item.defaultWebApp = path.getDmSitePath();
        //set content type based on the relative Path
        String contentType = getContentType(site, relativePath);
        item.contentType = contentType;
        if (contentType.equals(DmConstants.CONTENT_TYPE_COMPONENT)) {
            item.component = true;
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_DOCUMENT)) {
            item.document = true;
        }
        // set if the content is new
        item.isDeleted = true;
        item.isContainer = false;
        //item.isNewFile = false;
        item.isNew = false;
        item.isInProgress = false;
        item.timezone = servicesConfig.getDefaultTimezone(site);
        item.isPreviewable = false;
        item.browserUri = getBrowserUri(item);

        return item;
    }

    protected String expandRelativeSitePath(String site, String relativePath) {
        return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
    }

    protected String getBrowserUri(ContentItemTO item) {
        String replacePattern = "";
        //if (item.isLevelDescriptor) {
        //    replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        //} else if (item.isComponent()) {
        if (item.isComponent) {
            replacePattern = DmConstants.ROOT_PATTERN_COMPONENTS;
        } else if (item.isAsset) {
            replacePattern = DmConstants.ROOT_PATTERN_ASSETS;
        } else if (item.isDocument) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        }
        boolean isPage = !(item.isComponent || item.isAsset || item.isDocument);
        return getBrowserUri(item.uri, replacePattern, isPage);
    }

    protected static String getBrowserUri(String uri, String replacePattern, boolean isPage) {
        String browserUri = uri.replaceFirst(replacePattern, "");
        browserUri = browserUri.replaceFirst("/" + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = "/";
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
    }

    protected String getContentType(String site, String uri) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site)) || uri.endsWith("/" + servicesConfig.getLevelDescriptorName(site))) {
            return DmConstants.CONTENT_TYPE_COMPONENT;
        } else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
            return DmConstants.CONTENT_TYPE_DOCUMENT;
        } else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
            return DmConstants.CONTENT_TYPE_ASSET;

        } else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
            return DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
        }
        return DmConstants.CONTENT_TYPE_PAGE;
    }

    protected boolean matchesPatterns(String uri, List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (uri.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get the edited date from the given string
     *
     * @param str
     * @return date
     */
    protected Date getEditedDate(String str) {
        if (!StringUtils.isEmpty(str)) {
            try {
                return (new ISO8601DateFormat()).parse(str);
            } catch (ParseException e) {
                logger.error("No activity post date provided.");
                return null;
            }
        } else {
            logger.error("No activity post date provided.");
            return null;
        }
    }


    @Override
    public void register() {
        getServicesManager().registerService(DmActivityService.class, this);
    }

    protected ContentService contentService;

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
