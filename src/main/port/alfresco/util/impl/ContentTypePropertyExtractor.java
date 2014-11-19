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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.util.api.ContentPropertyExtractor;
import org.craftercms.cstudio.alfresco.util.api.ContentTypeExtractor;

/**
 * This class extracts content type
 * 
 * @author hyanghee
 *
 */
public class ContentTypePropertyExtractor implements ContentPropertyExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypePropertyExtractor.class);

	/**
	 * a map of content type extractors
	 */
	protected Map<String, ContentTypeExtractor> _contentTypeExtractors;

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.util.api.ContentMetadataExtractor#extractMetadata(org.alfresco.service.cmr.repository.NodeRef)
      */
	public Serializable extractMetadata(NodeRef nodeRef) {
		if (nodeRef != null) {
            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
            NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
			QName type = persistenceManagerService.getType(nodeRef);
			String prefixedName = namespaceService.getPrefixedTypeName(type);
			ContentTypeExtractor extractor = _contentTypeExtractors.get(prefixedName);
			if (extractor != null) {
				return extractor.getContentType(nodeRef);
			} else {
				LOGGER.error("No content type extractor foudn by type: " + type + " for nodeRef: " + nodeRef);
			}
		} else {
			LOGGER.error("NodeRef cannot be empty. returning null for content stauts.");
		}
		return null;
	}

	/**
	 * @param contentTypeExtractors the contentTypeExtractors to set
	 */
	public void setContentTypeExtractors(Map<String, ContentTypeExtractor> contentTypeExtractors) {
		_contentTypeExtractors = contentTypeExtractors;
	}

}
