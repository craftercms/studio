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

import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmMetadataService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractMetadataProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractMetadataProcessor.class);

    public static final String NAME = "ExtractMetadataProcessor";

    public static final int VERSION_TO_LOOK_UP = -1;

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
    public ExtractMetadataProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ExtractMetadataProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String contentType = content.getProperty(DmConstants.KEY_CONTENT_TYPE);
        String site = content.getProperty(DmConstants.KEY_SITE);
        String user = content.getProperty(DmConstants.KEY_USER);
        String sub = content.getProperty(DmConstants.KEY_SUB);
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String nodeRef = content.getProperty(DmConstants.KEY_NODE_REF);
        String path = folderPath + "/" + "" + fileName;

        try {
            DmMetadataService dmMetadataService = getServicesManager().getService(DmMetadataService.class);
            dmMetadataService.extractMetadata(site, user, sub, path, contentType, content.getActionedNodeRef(), content.getDocument());
        } catch (ServiceException e) {
            throw new ContentProcessException(e);
        }
    }
}
