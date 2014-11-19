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

import java.util.List;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.FilterTO;
import org.craftercms.cstudio.alfresco.to.SearchColumnTO;
import org.craftercms.cstudio.alfresco.to.SearchConfigTO;
import org.craftercms.cstudio.alfresco.to.SearchCriteriaTO;
import org.craftercms.cstudio.alfresco.util.SearchUtils;
import org.craftercms.cstudio.alfresco.util.api.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathSearchQueryBuilder implements SearchQueryBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(XPathSearchQueryBuilder.class);
	
	protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.util.api.SearchQueryBuilder#createQuery(org.craftercms.cstudio.alfresco.to.SearchCriteriaTO)
      */
	public String createQuery(SearchCriteriaTO criteria) {
		StringBuffer buffer = new StringBuffer();
		boolean isAttributeAdded = false;
		List<SearchColumnTO> baseColumns = null;
		// if a content type is given, search from the wcm path specified for the type
		// FIXME: handle multiple content types
		String site = criteria.getSite();
		String keyword = criteria.getKeyword();
		String contentType = (criteria.getContentTypes() != null && criteria.getContentTypes().size() > 0) 
			? criteria.getContentTypes().get(0) : null;
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
		if (!StringUtils.isEmpty(contentType)) {
			ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
			String searchPath = config.getSearchConfig().getWcmSearchPath();
			buffer.append(searchPath);
			baseColumns = config.getSearchConfig().getBaseSearchableColumns();
		} else {
			// search for every node - this will be very slow
			buffer.append("//*");
			SearchConfigTO config = servicesConfig.getDefaultSearchConfig(site);
			if (config != null) {	
				baseColumns = config.getBaseSearchableColumns();
			}
		}
		if (!StringUtils.isEmpty(keyword)) {
			List<SearchColumnTO> columns = criteria.getColumns();
			// set the base filters
			if (criteria.isApplyBaseSearchableColumns()) {
				if (baseColumns != null) {
					columns.addAll(baseColumns);
				}
			}
			// add searchable column queries
			if (columns != null) {
				String keywordSearch = createKeywordSearch(buffer, keyword, columns);
				if (!StringUtils.isEmpty(keywordSearch)) {
					buffer.append("[");
					buffer.append(keywordSearch);
					isAttributeAdded = true;
				}
			}
		}
		List<FilterTO> filters = criteria.getFilters();
		// search for the properties selected
		if (filters != null) {
			String filterQuery = createFilterQuery(site, filters);
			if (!StringUtils.isEmpty(filterQuery)) {
				if (!isAttributeAdded) {
					buffer.append("[");
					isAttributeAdded = true;
				} else {
					buffer.append(" and ");
				}
				buffer.append(filterQuery);
			}
		}
		if (isAttributeAdded) {
			buffer.append("]");
		}
		return buffer.toString();
	}
	
	/**
	 * keyword search on all searchable columns (text) 
	 * 
	 * @param buffer
	 * @param keyword
	 * @param columns
	 */
	protected String createKeywordSearch(StringBuffer buffer, String keyword, List<SearchColumnTO> columns) {
		StringBuffer columnSearchBuffer = new StringBuffer();
		String[] terms = keyword.split(" ");
		if (terms.length > 0) {
			for (String term : terms) {
				//term = SearchLanguageConversion.escapeForXPathLike(term); 
				for (SearchColumnTO column : columns) {
					if (column.isSearchable()) {
						if (columnSearchBuffer.length() > 0) {
							columnSearchBuffer.append("or ");
						}
						if (column.isUseWildCard()) {
							columnSearchBuffer.append("like(@" + column.getName() + ", '%" + term + "%', false) ");
						} else {
							columnSearchBuffer.append("@" + column.getName() + "='" + term + "' ");
						}
					}
				}
			}
		}
		// add to the main query
		if (columnSearchBuffer.length() > 0) {
			columnSearchBuffer.append(") ");
			return "(" + columnSearchBuffer.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * add a filter query
	 */
	protected String createFilterQuery(String site, List<FilterTO> filters) {
		StringBuffer buffer = new StringBuffer();
        NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		for (FilterTO filter : filters) {
			String key = filter.getKey();
			QName propName = namespaceService.createQName(key);
			// if the key is a property qname, create a property query
			if (propName != null) {
				PropertyDefinition propDef = persistenceManagerService.getProperty(propName);
				if (propDef != null) {
					String javaClassName = propDef.getDataType().getJavaClassName();
					if (!StringUtils.isEmpty(javaClassName) && 
							(!StringUtils.isEmpty(filter.getValue()) 
									|| !StringUtils.isEmpty(filter.getStartDate()) 
									|| !StringUtils.isEmpty(filter.getEndDate()))) {
						String query = SearchUtils.createPropertyQuery(propName, javaClassName, filter.getValue(), 
								filter.getStartDate(), filter.getEndDate(), !filter.isUseWildcard(), 
								org.alfresco.service.cmr.search.SearchService.LANGUAGE_XPATH, 
								namespaceService);
						if (buffer.length() > 0) {
							buffer.append(" and ");
						}
						buffer.append(query);
					}
				}
			}
		}
		return buffer.toString();
		// TODO: support full text search

	}
}
