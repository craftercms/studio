/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmMetadataService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExtractMetadataProcessor extends BaseContentProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ImportExtractMetadataProcessor.class);
	
	public static final String NAME = "ImportExtractMetadataProcessor";
	
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
	public ImportExtractMetadataProcessor() {
		super(NAME);
	}
	
	/**
	 * constructor that sets the process name
	 * 
	 * @param name
	 */
	public ImportExtractMetadataProcessor(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor#isProcessable(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent)
	 */
	public boolean isProcessable(PipelineContent content) {
		String fullPath = content.getProperty(DmConstants.KEY_FULL_PATH);
		return (fullPath != null && fullPath.endsWith(DmConstants.XML_PATTERN));
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor#process(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent, org.craftercms.cstudio.alfresco.to.ResultTO)
	 */
	public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
		String site = content.getProperty(DmConstants.KEY_SITE);
		//String sandbox = content.getProperty(DmConstants.KEY_SANDBOX);
		String fullPath = content.getProperty(DmConstants.KEY_FULL_PATH);
		try {
			DmPathTO path = new DmPathTO(fullPath);
            DmMetadataService dmMetadataService = getServicesManager().getService(DmMetadataService.class);
			dmMetadataService.extractMetadata(site, null, "", path.getRelativePath(), null, null, null);
		} catch (ServiceException e) {
			logger.error("[DMIMPORTSERVICE] Error while extracting metadata from " + fullPath, e);
		}
	}
}
