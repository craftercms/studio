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
package org.craftercms.studio.impl.v1.content.pipeline;


import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivityProcessor extends BaseContentProcessor {

    public static final String NAME = "PostActivityProcessor";


    /**
     * default constructor
     */
    public PostActivityProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public PostActivityProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
        String user = content.getProperty(DmConstants.KEY_USER);
        ActivityService.ActivityType activityType = (ActivityService.ActivityType.CREATED.toString().equals(type)) ? ActivityService.ActivityType.CREATED : ActivityService.ActivityType.UPDATED;
        String site = (String) content.getProperty(DmConstants.KEY_SITE);
        String folderPath = (String) content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = (String) content.getProperty(DmConstants.KEY_FILE_NAME);
        boolean isSystemAsset = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SYSTEM_ASSET));
        if (isSystemAsset) {
            ContentAssetInfoTO assetInfoTO = (ContentAssetInfoTO)result.getItem();
            fileName = assetInfoTO.getFileName();
        }
        //AuthenticationUtil.setFullyAuthenticatedUser(user);
        String uri = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        if (ContentUtils.matchesPatterns(uri, displayPatterns)) {
            Map<String,String> extraInfo = new HashMap<String,String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, contentService.getContentTypeClass(site, uri));
            activityService.postActivity(site, user, uri, activityType, ActivityService.ActivitySource.UI, extraInfo);
            // disabled due to a performance issue (CRAFTER-655)
            //updateDependenciesActivity(site, user, uri, activityType, extraInfo);
        }
    }


    
    protected void updateDependenciesActivity(String site, String user, String relativePath, ActivityService.ActivityType activityType, Map<String, String> extraInfo) {
        DmDependencyTO dependencyTO = dmDependencyService.getDependencies(site, relativePath, false, true);
        List<DmDependencyTO> dependencyList = new ArrayList<>();
        if (dependencyTO != null) {
            dependencyList = dependencyTO.flattenChildren();
        }
        for (DmDependencyTO dep : dependencyList) {
            List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
            if(ContentUtils.matchesPatterns(dep.getUri(), displayPatterns)){
                ContentItemTO item = contentService.getContentItem(site, dep.getUri(), 0);
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, contentService.getContentTypeClass(site, dep.getUri()));
                if (dep.getUri().startsWith(DmConstants.ROOT_PATTERN_SYSTEM_COMPONENTS)) {
                    activityService.postActivity(site, user, dep.getUri(), ActivityService.ActivityType.CREATED, ActivityService.ActivitySource.UI, extraInfo);
                } else {
                    activityService.postActivity(site, user, dep.getUri(), activityType, ActivityService.ActivitySource.UI, extraInfo);
                }
            }
        }
    }

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ActivityService activityService;
    protected DmDependencyService dmDependencyService;

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }
}
