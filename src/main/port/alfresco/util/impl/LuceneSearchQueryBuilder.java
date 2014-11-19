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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.to.*;
import org.craftercms.cstudio.alfresco.util.SearchUtils;
import org.craftercms.cstudio.alfresco.util.api.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * build lucene search query
 * 
 * @author hyanghee
 *
 */
public class LuceneSearchQueryBuilder implements SearchQueryBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(LuceneSearchQueryBuilder.class);
	
    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /** regular expression unwanted character in Keyword field \*".+?^$(){}| **/
	protected String unwantedCharsInKeyword = "\\-|\\\\|\\*|\\\"|\\.|\\+|\\?|\\^|\\$|\\(|\\)|\\{|\\}|\\|";
	
	/** regular expression unwanted character in Keyword term field **/
	protected String unwantedCharsInKeywordTerm = "\\&";
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.util.api.SearchQueryBuilder#createQuery(org.craftercms.cstudio.alfresco.to.SearchCriteriaTO)
	 */
	public String createQuery(SearchCriteriaTO criteria) {
//PORT
		return null;
		// StringBuffer buffer = new StringBuffer();
		// String site = criteria.getSite();
		// // add content type query if provided
		// addContentTypeQuery(buffer, site, criteria.getContentTypes());
		// if (criteria.isExcludeWorkingCopy()) {
		// 	excludeWorkingCopy(buffer);
		// }
		// // search for keyword provided
		// addKeywordQuery(buffer, site, criteria.getKeyword(), criteria.getColumns(), criteria.getContentTypes(), criteria.isApplyBaseSearchableColumns());
		// // search for the properties selected
		// addFilterQuery(buffer, site, criteria.getFilters());
		// addAspectQuery(buffer, site, criteria.getIncludeAspects(), criteria.getExcludeAspects());
		// return buffer.toString();
	}

	/**
	 * add aspect query 
	 * 
	 * @param buffer
	 * @param site
	 * @param includeAspects
	 * @param excludeAspects
	 */
	protected void addAspectQuery(StringBuffer buffer, String site, List<String> includeAspects,
			List<String> excludeAspects) {
//PORT

  //       NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
		// if (includeAspects != null && includeAspects.size() > 0) {
		// 	if (buffer.length() > 0) {
		// 		buffer.append(" AND ");
		// 	}
		// 	buffer.append("(");
		// 	boolean added = false;
		// 	for (String aspect : includeAspects) {
		// 		if (added) {
		// 			buffer.append(" OR ");
		// 		}
		// 		QName aspectName = namespaceService.createQName(aspect);
		// 		buffer.append("(+" + SearchUtils.createAspectQuery(aspectName) + ")");
		// 		added = true;
		// 	}
		// 	buffer.append(")");
		// }
		// if (excludeAspects != null && excludeAspects.size() > 0) {
		// 	boolean added = false;
		// 	for (String aspect : excludeAspects) {
		// 		if (added) {
		// 			buffer.append(" ");
		// 		}
		// 		QName aspectName = namespaceService.createQName(aspect);
		// 		buffer.append("-" + SearchUtils.createAspectQuery(aspectName));
		// 		added = true;
		// 	}
		// }
	}

	/**
	 * add filter query
	 * 
	 * @param buffer
	 * @param site
	 * @param filters
	 */
	protected void addFilterQuery(StringBuffer buffer, String site, List<FilterTO> filters) {
//PORT
		// if (filters != null) {
		// 	for (FilterTO filter : filters) {
		// 		addFilterQuery(site, buffer, filter);
		// 	}
		// }
	}

	/**
	 * add keyword query
	 * 
	 * @param buffer
	 * @param site
	 * @param keyword
	 * @param columns
	 * @param contentTypes
	 * @param isApplyBaseSearchableColumns
	 */
	protected void addKeywordQuery(StringBuffer buffer, String site, String keyword, List<SearchColumnTO> columns, List<String> contentTypes, boolean isApplyBaseSearchableColumns) {
//PORT
		// // set the base filters
  //       NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
  //       ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
		// if (isApplyBaseSearchableColumns) {
		// 	List<SearchColumnTO> baseColumns = null;
		// 	QName type = null;
		// 	if (contentTypes.size() == 1) {
		// 		type = namespaceService.createQName(contentTypes.get(0));
		// 	}
		// 	// if there is only one type provided, get base columns of the content type
		// 	// otherwise get the common base columns for all content types
		// 	if (type != null) {
		// 		ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, namespaceService.getPrefixedTypeName(type));
		// 		baseColumns = config.getSearchConfig().getBaseSearchableColumns();
		// 	} else {
		// 		SearchConfigTO config = servicesConfig.getDefaultSearchConfig(site);
		// 		if (config != null) {	
		// 			baseColumns = config.getBaseSearchableColumns();
		// 		}
		// 	}
		// 	if (baseColumns != null) {
		// 		columns.addAll(baseColumns);
		// 	}
		// }
		// // add searchable column queries
		// if (columns != null) {
		// 	addKeywordSearch(buffer, keyword, columns);
		// }
	}

	/**
	 * exclude working copy
	 * 
	 * @param buffer
	 */
	protected void excludeWorkingCopy(StringBuffer buffer) {
//PORT
		// if (buffer.length() > 0) {
		// 	buffer.append(" AND ");
		// }
		// buffer.append("-" + SearchUtils.createAspectQuery(ContentModel.ASPECT_WORKING_COPY));
	}

	/**
	 * add content type query by its path and type
	 * 
	 * @param buffer
	 * @param site
	 * @param contentTypes
	 */
	protected void addContentTypeQuery(StringBuffer buffer, String site, List<String> contentTypes) {
//PORT
		// if (contentTypes != null && contentTypes.size() > 0) {
		// 	buffer.append("(");
		// 	boolean added = false;
  //           NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
		// 	for (String contentType : contentTypes) {
		// 		if (!StringUtils.isEmpty(contentType)) {
		// 			QName type = namespaceService.createQName(contentType);
		// 			if (type != null) {
		// 				if (added) {
		// 					buffer.append(" OR ");
		// 				}
		// 				buffer.append(SearchUtils.createTypeQuery(type));
		// 				added = true;
		// 			}
		// 		}
		// 	}
		// 	buffer.append(")");
		// } 
	}

	/**
	 * keyword search on all searchable columns (text) 
	 * 
	 * @param buffer
	 * @param keyword
	 * @param columns
	 */
	protected void addKeywordSearch(StringBuffer buffer, String keyword, List<SearchColumnTO> columns) {
//PORT
		// String lkeyword = "";
		// lkeyword = removeUnexpectedCharacter(keyword);
		// String[] terms = {};
		
		// if (!StringUtils.isEmpty(lkeyword)) {
		// 	lkeyword = lkeyword.trim();
		// 	terms = lkeyword.split("\\s+");
		// }
		// if (terms.length > 0) {
		// 	if (buffer.length() > 0) {
		// 		buffer.append(" AND ");
		// 	}
		// 	buffer.append(" (");
		// 	buffer.append(" (");
		// 	for (String term : terms) {
		// 		term = modifySearchTerm(SearchLanguageConversion.escapeForLucene(term));
		// 		for (SearchColumnTO column : columns) {
		// 			if (column.isSearchable()) {
		// 				if (column.isUseWildCard()) {
		// 					buffer.append("+@" + column.getName().replaceFirst(":", "\\\\:") + ":\"*" + term + "*\" ");
		// 				} else {
		// 					buffer.append("+@" + column.getName().replaceFirst(":", "\\\\:") + ":\"" + term + "\" ");
		// 				}
		// 			}
		// 		}
		// 		buffer.append("+TEXT:\"*" + term + "*\" ");
		// 	}
		// 	buffer.append(") ");
		// 	buffer.append(" OR (+TEXT:\"*" + modifySearchTerm(SearchLanguageConversion.escapeForLucene(lkeyword)) + "*\"^10)");
		// 	buffer.append(") ");
		// } else {
		// 	if(columns.size() > 0) {
		// 		if (buffer.length() > 0) {
		// 			buffer.append(" AND ");
		// 		}
		// 		buffer.append(" (");
		// 		for (SearchColumnTO column : columns) {
		// 			if (column.isSearchable()) {
		// 				if (column.isUseWildCard()) {
		// 					buffer.append("@" + column.getName().replaceFirst(":", "\\\\:") + ":* ");
		// 				} 
		// 			}
		// 		}
		// 		buffer.append("TEXT:* " );
		// 		buffer.append(") ");
		// 	}
		// }
	}
	
	protected String removeUnexpectedCharacter(String str) {
//PORT
		return null;
		// String ret = new String(str);
		// if (StringUtils.isEmpty(str))
		// 	return str;
		// return ret.replaceAll(unwantedCharsInKeyword, " ");
	}
	/**
	 * Modifies the string for native Lucene, e.g. replace & with ?
	 * @param str
	 * @return
	 */
	protected String modifySearchTerm(String str) {
//PORT
		return null;
		// String ret = new String(str);
		// if (StringUtils.isEmpty(str))
		// 	return str;
		// return ret.replaceAll(unwantedCharsInKeywordTerm, "?");
	}
	
	/**
	 * add a filter query 
	 * 
	 * @param buffer
	 * @param filter
	 */
	protected void addFilterQuery(String site, StringBuffer buffer, FilterTO filter) {
//PORT
		// String key = filter.getKey();
  //       NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
  //       PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		// QName propName = namespaceService.createQName(key);
		// // if the key is a property qname, create a property query
		// if (propName != null) {
		// 	PropertyDefinition propDef = persistenceManagerService.getProperty(propName);
		// 	if (propDef != null) {
		// 		String javaClassName = propDef.getDataType().getJavaClassName();
		// 		if (!StringUtils.isEmpty(javaClassName) && 
		// 				(!StringUtils.isEmpty(filter.getValue()) 
		// 						|| !StringUtils.isEmpty(filter.getStartDate()) 
		// 						|| !StringUtils.isEmpty(filter.getEndDate()))) {
		// 			String query = SearchUtils.createPropertyQuery(propName, javaClassName, filter.getValue(), 
		// 					filter.getStartDate(), filter.getEndDate(), !filter.isUseWildcard(),
		// 					org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE,
		// 					namespaceService);
		// 			if (buffer.length() > 0) {
		// 				buffer.append(" AND ");
		// 			}
		// 			buffer.append(query);
		// 		}
		// 	}
		// 	else {
		// 		// seperate the localname into components e.g. cm:content.mimetype seperates into content -> mimetype
		// 		String qNameParts[] = propName.getLocalName().split("\\.");
		// 		if (qNameParts != null && qNameParts.length > 1) {
		// 			QName propRootQName = namespaceService.createQName(QName.splitPrefixedQName(propName.toPrefixString())[0].concat(":").concat(qNameParts[0]));
					
		// 			if (propRootQName != null) {
		// 				PropertyDefinition propRootDef = persistenceManagerService.getProperty(propRootQName);
		// 				if (propRootDef != null) {
		// 					String propRootJavaClassName = propRootDef.getDataType().getJavaClassName();
		// 					if (!StringUtils.isEmpty(propRootJavaClassName) && 
		// 							(!StringUtils.isEmpty(filter.getValue()) 
		// 									|| !StringUtils.isEmpty(filter.getStartDate()) 
		// 									|| !StringUtils.isEmpty(filter.getEndDate()))) {
								
		// 						String className = propRootJavaClassName;
		// 						boolean found = false;
								
		// 						for (int i = 1; i < qNameParts.length; i++) {
		// 							found = false;
									
		// 							String methodNamePart = qNameParts[i]; 
		// 							methodNamePart = "get".concat(Character.toUpperCase(methodNamePart.charAt(0)) + methodNamePart.substring(1));
									
		// 							try {
		// 								Class c = Class.forName(className);
		// 								Method mehtods[] = c.getMethods();
		// 								for (Method m:mehtods) {
		// 									if (m.getName().equals(methodNamePart)) {
		// 										className = m.getReturnType().getName();
		// 										if (className.indexOf('.') < 0) {
		// 											if (className.equals("int")) {
		// 												className = "integer";
		// 											} else if (className.equals("char")) {
		// 												className = "character";
		// 											}
													
		// 											className = "java.lang.".concat(Character.toUpperCase(className.charAt(0)) + className.substring(1));
		// 										}
		// 										found = true;
		// 										break;
		// 									}
		// 								}
		// 							} catch (Exception e) {
		// 								// TODO: Log Error
		// 							}
		// 						}
								
		// 						if (found) {
		// 							String query = SearchUtils.createPropertyQuery(propName, className, filter.getValue(), 
		// 									filter.getStartDate(), filter.getEndDate(), filter.isUseWildcard(),
		// 									org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE,
		// 									namespaceService);
		// 							if (buffer.length() > 0) {
		// 								buffer.append(" AND ");
		// 							}
		// 							buffer.append(query);
									
		// 						}
		// 					}
		// 				}
		// 			}
					
		// 		} else {
		// 			// Is this a possible scenario?
		// 		}	
		// 	}
		
		// } else if (!StringUtils.isEmpty(key)){
		// 	// otherwise, it's a full txt search on article body
		// 	// TODO: make sure to check on body tags
		// 	if (!StringUtils.isEmpty(filter.getValue())) {
  //               if (key.equalsIgnoreCase(AbstractLuceneQueryParser.FIELD_PATH)) {
  //                   ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
  //                   String filterPath = servicesConfig.getRepositoryRootPath(site) + filter.getValue();
  //                   String[] pathSegments = filterPath.split("/");
  //                   StringBuilder location = new StringBuilder();
  //                   for (String segment : pathSegments) {
  //                       if (StringUtils.isNotEmpty(segment)) {
  //                           location.append("/cm:").append(segment);
  //                       }
  //                   }
  //                   if (StringUtils.isNotEmpty(location.toString())) {
  //                       if (buffer.length() > 0) {
  //                           buffer.append(" AND ");
  //                       }
  //                       buffer.append("(").append("PATH:\"/app:company_home").append(location.toString());
  //                       buffer.append("//*\"").append(")");
  //                   }
  //               } else {
  //                   if (buffer.length() > 0) {
  //                       buffer.append(" AND ");
  //                   }
  //                   buffer.append(" ( ");
  //                   String[] terms = filter.getValue().split(" ");
  //                   for (String term : terms) {
  //                       buffer.append("TEXT:*" + term + "* ");
  //                   }
  //                   buffer.append(" )");
  //               }
		// 	}
		// }
	}
}
