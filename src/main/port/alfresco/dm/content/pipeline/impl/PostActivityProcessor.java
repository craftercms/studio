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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import javolution.util.FastList;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ActivityService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ContentAssetInfoTO;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivityProcessor extends BaseContentProcessor {

    public static final String NAME = "PostActivityProcessor";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

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
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        String uri = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
        DmContentService dmContentService = getServicesManager().getService(DmContentService.class);
        ActivityService activityService = getServicesManager().getService(ActivityService.class);
        if(dmContentService.matchesDisplayPattern(site, uri)){
            Map<String,String> extraInfo = new HashMap<String,String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, dmContentService.getContentType(site, uri));
            activityService.postActivity(site, user, uri, activityType,extraInfo);
            // disabled due to a performance issue (CRAFTER-655)
            //updateDependenciesActivity(site, user, uri, activityType, extraInfo);
        }
    }
    
    protected void updateDependenciesActivity(String site, String user, String relativePath, ActivityService.ActivityType activityType, Map<String, String> extraInfo) {
        DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
        DmContentService dmContentService = getServicesManager().getService(DmContentService.class);
        ActivityService activityService = getServicesManager().getService(ActivityService.class);
        DmDependencyTO dependencyTO = dmDependencyService.getDependencies(site, null, relativePath, false, true);
        List<DmDependencyTO> dependencyList = new FastList<DmDependencyTO>();
        if (dependencyTO != null) {
            dependencyList = dependencyTO.flattenChildren();
        }
        for (DmDependencyTO dep : dependencyList) {
            if(dmContentService.matchesDisplayPattern(site, dep.getUri())){
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, dmContentService.getContentType(site, dep.getUri()));
                if (dep.getUri().startsWith(DmConstants.ROOT_PATTERN_SYSTEM_COMPONENTS)) {
                    activityService.postActivity(site, user, dep.getUri(), ActivityService.ActivityType.CREATED, extraInfo);
                } else {
                    activityService.postActivity(site, user, dep.getUri(), activityType, extraInfo);
                }
            }
        }
    }
}
