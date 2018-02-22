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
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

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
        if (result.getCommitId() != null) {
            String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
            String user = content.getProperty(DmConstants.KEY_USER);
            ActivityService.ActivityType activityType = (ActivityService.ActivityType.CREATED.toString().equals(type)) ? ActivityService.ActivityType.CREATED : ActivityService.ActivityType.UPDATED;
            String site = (String) content.getProperty(DmConstants.KEY_SITE);
            String folderPath = (String) content.getProperty(DmConstants.KEY_FOLDER_PATH);
            String fileName = (String) content.getProperty(DmConstants.KEY_FILE_NAME);
            boolean isSystemAsset = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SYSTEM_ASSET));
            if (isSystemAsset) {
                ContentAssetInfoTO assetInfoTO = (ContentAssetInfoTO) result.getItem();
                fileName = assetInfoTO.getFileName();
            }
            String uri = (folderPath.endsWith(FILE_SEPARATOR)) ? folderPath + fileName : folderPath + FILE_SEPARATOR + fileName;
            List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
            if (ContentUtils.matchesPatterns(uri, displayPatterns)) {
                Map<String, String> extraInfo = new HashMap<String, String>();
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, contentService.getContentTypeClass(site, uri));
                activityService.postActivity(site, user, uri, activityType, ActivityService.ActivitySource.UI, extraInfo);
            }
        }
    }

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ActivityService activityService;

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }
}
