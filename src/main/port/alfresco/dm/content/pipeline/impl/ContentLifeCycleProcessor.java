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

import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentLifeCycleService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ActivityService.ActivityType;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class ContentLifeCycleProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ContentLifeCycleProcessor.class);

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
    	String preview = content.getProperty(DmConstants.KEY_IS_PREVIEW);
    	// do not run on preview write
    	if (StringUtils.isEmpty(preview) || !preview.equalsIgnoreCase("true")) {
			String site = content.getProperty(DmConstants.KEY_SITE);
			//String sandbox = content.getProperty(DmConstants.KEY_SANDBOX);
			String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
			String contentType = content.getProperty(DmConstants.KEY_CONTENT_TYPE);
			String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
			String path = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
			String user = content.getProperty(DmConstants.KEY_USER);
			String operValue = content.getProperty(DmConstants.CONTENT_LIFECYCLE_OPERATION);
			DmContentLifeCycleService.ContentLifeCycleOperation operation = (DmContentLifeCycleService.ContentLifeCycleOperation.getOperation(operValue));
			if (operation == null) {
				String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
				operation = (ActivityType.CREATED.toString().equals(type)) ? DmContentLifeCycleService.ContentLifeCycleOperation.NEW : DmContentLifeCycleService.ContentLifeCycleOperation.UPDATE;
			}
            DmContentLifeCycleService dmContentLifeCycleService = getServicesManager().getService(DmContentLifeCycleService.class);
	    	dmContentLifeCycleService.process(site, user, path, contentType, operation, null);
    	}
    }
}
