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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.constant.CStudioSearchConstants;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentItemTO;
import org.craftercms.cstudio.alfresco.to.SearchColumnTO;
import org.craftercms.cstudio.alfresco.util.SearchResultItemComparator;
import org.craftercms.cstudio.alfresco.util.api.ContentMetadataExtractor;
import org.craftercms.cstudio.alfresco.util.api.SearchResultExtractor;

public class DmSearchResultExtractor implements SearchResultExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DmSearchResultExtractor.class);

	protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
	 * a map of content type extractors
	 */
	protected Map<String, ContentMetadataExtractor> _contentMetadataExtractors;

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.util.api.SearchResultExtractor#extract(java.lang.String, java.util.List, java.util.List, java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List extract(String site, List<NodeRef> nodeRefs, List<SearchColumnTO> columns, String sort,
			boolean ascending, int page, int pageSize) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
		if (nodeRefs == null) {
			return new FastList<ContentItemTO>(0);
		} else {
			Map<String, QName> metadataMap = createMetadataMap(columns);
			if (metadataMap != null) {
				List<ContentItemTO> items = new FastList<ContentItemTO>(nodeRefs.size());
				for (NodeRef nodeRef : nodeRefs) {
					QName type = persistenceManagerService.getType(nodeRef);
					String prefixedName = namespaceService.getPrefixedTypeName(type);
					ContentMetadataExtractor extractor = _contentMetadataExtractors.get(prefixedName);
					if (extractor != null) {
						Map<String, Serializable> metadata = extractor.extractMetadata(metadataMap, nodeRef);
						ContentItemTO item = new ContentItemTO();
						item.setProperties(metadata);
						items.add(item);
					} else {
						LOGGER.error("No metadata extractor found for " + prefixedName);
					}
				}
				if ((items != null) && (!sort.equalsIgnoreCase(CStudioSearchConstants.SEARCH_DEFAULT_SORT))) {
					SearchResultItemComparator comparator = new SearchResultItemComparator(sort, ascending);
					Collections.sort(items, comparator);
				}
				return items;
			} else {
				throw new ServiceException("No content metadata extractor or no metadata mapping defined for " + site);
			}
		}
	}

	/**
	 * create metadata map from the given columns
	 * 
	 * @param columns
	 * @return meatadata map
	 */
	protected Map<String, QName> createMetadataMap(List<SearchColumnTO> columns) {
		Map<String, QName> metaMap = new FastMap<String, QName>();
		if (columns != null && columns.size() > 0) {
            NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
			for (SearchColumnTO column : columns) {
				QName type = namespaceService.createQName(column.getName());
				metaMap.put(column.getTitle(), type);
			}
		}
		return metaMap;
	}

	/**
	 * @param contentMetadataExtractors the contentMetadataExtractors to set
	 */
	public void setContentMetadataExtractors(Map<String, ContentMetadataExtractor> contentMetadataExtractors) {
		_contentMetadataExtractors = contentMetadataExtractors;
	}
	
}
