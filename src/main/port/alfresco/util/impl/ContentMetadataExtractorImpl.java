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
package org.craftercms.cstudio.alfresco.util.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.util.api.ContentMetadataExtractor;
import org.craftercms.cstudio.alfresco.util.api.ContentPropertyExtractor;

/**
 * This class extracts content metadata
 * 
 * @author hyanghee
 *
 */
public class ContentMetadataExtractorImpl implements ContentMetadataExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentMetadataExtractorImpl.class);
	
	protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
	 * Content property extractors 
	 */
	protected Map<String, ContentPropertyExtractor> _propertyExtractors;
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.util.api.ContentMetadataExtractor#extractMetadata(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public Map<String, Serializable> extractMetadata(Map<String, QName> metadataMap, final NodeRef nodeRef) {
		Map<String, Serializable> metadata = new HashMap<String, Serializable>();
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		for (String name : metadataMap.keySet()) {
			QName type = metadataMap.get(name);
			if (type != null) {
				metadata.put(name, persistenceManagerService.getProperty(nodeRef, type));
			} else {
				ContentPropertyExtractor extractor = _propertyExtractors.get(name);
				if (extractor != null) {
					metadata.put(name, extractor.extractMetadata(nodeRef));
				} else {
					LOGGER.error(name + " is not a property and no extractor defined.");
					metadata.put(name, null);
				}
			}
		}
		return metadata;
	}

	/**
	 * @param propertyExtractors the propertyExtractors to set
	 */
	public void setPropertyExtractors(Map<String, ContentPropertyExtractor> propertyExtractors) {
		_propertyExtractors = propertyExtractors;
	}

}
