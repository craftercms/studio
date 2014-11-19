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
package org.craftercms.cstudio.alfresco.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;

public class SearchUtils {

	public static final SimpleDateFormat DATE_SEARCH_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchUtils.class);
	
	/**
	 * create path query
	 * 
	 * @param path
	 * @param direct
	 *            if true it only searches for a direct child. Otherwise, it
	 *            will search for all nodes at any depth and returns the first
	 *            matching node
	 * @return path query
	 */
	public static String createPathQuery(String path, boolean direct) {
		path = (direct) ? path + "/*" : path + "//*";
		return "PATH:\"" + path + "\"";
	}

	/**
	 * create type query
	 * 
	 * @param type
	 * @return type query
	 */
	public static String createTypeQuery(QName type) {
		return "TYPE:\"" + type.toString() + "\"";
	}

	/**
	 * create aspect query
	 * 
	 * @param aspect
	 * @return aspect query
	 */
	public static String createAspectQuery(QName aspect) {
		return "ASPECT:\"" + aspect.toString() + "\"";
	}

	/**
	 * create text property query to match the exact value
	 * 
	 * @param property
	 * @param value
	 *            exact text value to match
	 * @param language 
	 * @return property query
	 */
	public static String createTextQueryByValue(QName property, String value, String language, NamespaceService namespaceService) {
		String prefixedName = namespaceService.getPrefixedPropertyName(property);
		if (!StringUtils.isEmpty(prefixedName)) {
			if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
				return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":\"" + value + "\"";
			} 
			else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
				return "@" + prefixedName + "='" + value + "'";
			}
		}
		return "";
	}

	/**
	 * create text property query to match the pattern
	 * 
	 * @param property
	 * @param pattern
	 *            text value pattern to match
	 * @param language 
	 * @return property query
	 */
	public static String createTextQueryByPattern(QName property, String pattern,
			String language, NamespaceService namespaceService) {
		String prefixedName = namespaceService.getPrefixedPropertyName(property);
		if (!StringUtils.isEmpty(prefixedName)) {
			if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
				return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":" + pattern;
			} else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
				// FIXME: support pattern search
				return "";
			}
		}
		return "";
	}

	/**
	 * create numeric property query to match the exact value
	 * 
	 * @param property
	 * @param value
	 *            exact numeric value to match
	 * @param language 
	 * @return property query
	 */
	public static String createNumericQueryByValue(QName property, Object value, String language, NamespaceService namespaceService) {
		String prefixedName = namespaceService.getPrefixedPropertyName(property);
		if (!StringUtils.isEmpty(prefixedName)) {
			if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
				return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":" + value;
			} else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
				return "@" + prefixedName + "=" + value;
			}
		}
		return "";
	}

	/**
	 * create boolean property query 
	 * 
	 * @param property
	 * @param value
	 *            true or false
	 * @param language 
	 * @return boolean query
	 */
	public static String createBooleanQuery(QName property, Object value, String language, NamespaceService namespaceService) {
		String prefixedName = namespaceService.getPrefixedPropertyName(property);
		if (!StringUtils.isEmpty(prefixedName)) {
			if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
				return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":" + String.valueOf(value);
			} else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
				return "@" + prefixedName + "=" + String.valueOf(value);
			}
		}
		return "";
	}

	/**
	 * create date property query
	 * 
	 * @param property
	 * @param exactDateStr
	 * @param startDateStr
	 * @param endDateStr
	 * @param language
	 * 			search language
	 * @param namespaceService
	 * @return date query
	 */
	public static String createDateQuery(QName property, String exactDateStr, String startDateStr, String endDateStr,
			String language, NamespaceService namespaceService) {
		SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
		Date exactDate = (StringUtils.isEmpty(exactDateStr)) ? null : ContentFormatUtils.parseDate(format, exactDateStr); 
		Date startDate = (StringUtils.isEmpty(startDateStr)) ? null : ContentFormatUtils.parseDate(format, startDateStr);
		Date endDate = (StringUtils.isEmpty(endDateStr)) ? null : ContentFormatUtils.parseDate(format, endDateStr);
		return createDateQuery(property, exactDate, startDate, endDate, language, namespaceService);
	}

	/**
	 * create date property query
	 * 
	 * @param property
	 * @param value
	 * @param startDate
	 * @param endDate
	 * @param language
	 * @param namespaceService
	 */
	public static String createDateQuery(QName property, Date value, Date startDate, Date endDate,
			String language, NamespaceService namespaceService) {
		//TODO: get xpath date query
		// exact date query
		String prefixedName = namespaceService.getPrefixedPropertyName(property);
		if (value != null) {
			if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
				return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":\"" + DATE_SEARCH_FORMAT.format(value) + "\"";
			} else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
				return "@" + prefixedName + "=\'" + DATE_SEARCH_FORMAT.format(value) + "\'";
			}
		} else {
			if (startDate != null || endDate != null) {
				if (SearchService.LANGUAGE_LUCENE.equalsIgnoreCase(language) || SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equalsIgnoreCase(language)) {
					String startRange = (startDate != null) ? DATE_SEARCH_FORMAT.format(startDate) : "MIN";
					String endRange = (endDate != null) ? DATE_SEARCH_FORMAT.format(endDate) : "MAX";
					return "@" + prefixedName.replaceFirst(":", "\\\\:") + ":[" 
							+ startRange.replaceAll("-", "\\\\-") + " TO " 
							+ endRange.replaceAll("-", "\\\\-") + "]";
				} else if (SearchService.LANGUAGE_XPATH.equalsIgnoreCase(language)) {
					String range = null;
					if (startDate != null) {
						range = "@" + prefixedName + ">='" + DATE_SEARCH_FORMAT.format(startDate) + "'";
					}
					if (endDate != null) {
						if (startDate != null) {
							range = " and @" + prefixedName + "=<'" + DATE_SEARCH_FORMAT.format(endDate) + "'";
						} else {
							range = "@" + prefixedName + "=<'" + DATE_SEARCH_FORMAT.format(endDate) + "'";
						}
					}
					return range;
				}
			}
		}
		return "";
	}

	/**
	 * create property query to match the search criteria value
	 * 
	 * @param type
	 * @param javaTypeName
	 * @param value
	 * @param startDate
	 * @param endDate
	 */
	public static String createPropertyQuery(QName type, String javaTypeName, String value, String startDate,
			String endDate, boolean exact, String language, NamespaceService namespaceService) {
		// text property
		if (String.class.getName().equals(javaTypeName)) {
			if (exact) {
				return SearchUtils.createTextQueryByValue(type, value, language, namespaceService);
			} else {
				return SearchUtils.createTextQueryByPattern(type, value, language, namespaceService);
			}
		} else if (Integer.class.getName().equals(javaTypeName) || Long.class.getName().equals(javaTypeName)) {
			return SearchUtils.createNumericQueryByValue(type, value, language, namespaceService);
		} else if (Boolean.class.getName().equals(javaTypeName)) {
			return SearchUtils.createBooleanQuery(type, value, language, namespaceService);
		} else if (Date.class.getName().equals(javaTypeName)) {
			return SearchUtils.createDateQuery(type, value, startDate, endDate, language, namespaceService);
		} else if (NodeRef.class.getName().equals(javaTypeName)) {
			return SearchUtils.createTextQueryByValue(type, value, language, namespaceService);
		} else {
			LOGGER.error("Cannot create property search query. Unsupported class type provided: " 
					+ javaTypeName + " for property: " + type);
		}
		return "";
	}
}
