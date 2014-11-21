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

import javolution.util.FastMap;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class ExtractDependencyProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractDependencyProcessor.class);

    public static final String NAME = "ExtractDependencyProcessor";

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
    public ExtractDependencyProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ExtractDependencyProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String site = content.getProperty(DmConstants.KEY_SITE);
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String path = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
        Document document = content.getDocument();
        try {
            DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
            Map<String, Set<String>> globalDeps = new FastMap<String, Set<String>>();
            dmDependencyService.extractDependencies(site, path, document, globalDeps);
        } catch (ServiceException e) {
            throw new ContentProcessException(e);
        }
    }
}
