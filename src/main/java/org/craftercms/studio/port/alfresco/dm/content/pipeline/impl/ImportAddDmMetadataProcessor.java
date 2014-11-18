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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportAddDmMetadataProcessor extends BaseContentProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ImportAddDmMetadataProcessor.class);

	public static final String NAME = "ImportAddDmMetdataProcessor";

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
	public ImportAddDmMetadataProcessor() {
		super(NAME);
	}

	/**
	 * constructor that sets the process name
	 *
	 * @param name
	 */
	public ImportAddDmMetadataProcessor(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor#isProcessable(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent)
	 */
	public boolean isProcessable(PipelineContent content) {
		String fullPath = content.getProperty(DmConstants.KEY_FULL_PATH);
		return (fullPath != null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor#process(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent, org.craftercms.cstudio.alfresco.to.ResultTO)
	 */
	public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
		String fullPath = content.getProperty(DmConstants.KEY_FULL_PATH);
		//String sandbox = content.getProperty(WcmConstants.KEY_SANDBOX);
		// String fullPath = path.getRelativePath();
		if (logger.isDebugEnabled())
			logger.debug("[DMIMPORTSERVICE] addDmProperties: filePath [" + fullPath + "] ");
		// add dm properties
		//_avmService.addAspect(fullPath, CStudioContentModel.ASPECT_COLLABORATIVE_SANDBOX);
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        persistenceManagerService.addAspect(node, CStudioContentModel.ASPECT_COLLABORATIVE_SANDBOX, null);
        persistenceManagerService.setProperty(node, CStudioContentModel.PROP_CREATED_BY, null);
        persistenceManagerService.setProperty(node, CStudioContentModel.PROP_LAST_MODIFIED_BY, null);
        persistenceManagerService.setProperty(node, ContentModel.PROP_CREATOR, null);
        persistenceManagerService.setProperty(node, ContentModel.PROP_MODIFIER, null);
        persistenceManagerService.setProperty(node, ContentModel.PROP_OWNER, null);
        persistenceManagerService.setProperty(node, CStudioContentModel.PROP_STATUS, DmConstants.DM_STATUS_LIVE);
	}
}
