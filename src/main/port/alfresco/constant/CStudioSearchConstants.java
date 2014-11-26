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
package org.craftercms.cstudio.alfresco.constant;

/**
 * All Search related constants
 * 
 * @author tanveer
 */
public interface CStudioSearchConstants {

	/** JSON request properteis **/
	public static final String SEARCH_JSON_TYPE = "searchType";
	public static final String SEARCH_JSON_CONTENT_TYPES = "contentTypes";
	public static final String SEARCH_JSON_INCLUDE_ASPECTS = "includeAspects";
	public static final String SEARCH_JSON_EXCLUDE_ASPECTS = "excludeAspects";
	public static final String SEARCH_JSON_NODE_NAME = "nodeName";
	public static final String SEARCH_JSON_KEYWORD = "keyword";
	public static final String SEARCH_JSON_PAGE = "page"; 
	public static final String SEARCH_JSON_PAGESIZE = "pageSize";  

	/** JSON request filter properteis **/
	public static final String SEARCH_JSON_FILTERS = "filters";
	public static final String SEARCH_JSON_FILTERS_END_DATE = "endDate";
	public static final String SEARCH_JSON_FILTERS_QNAME = "qname";
	public static final String SEARCH_JSON_FILTERS_START_DATE = "startDate";
	public static final String SEARCH_JSON_FILTERS_USE_WILD_CARD = "useWildCard";
	public static final String SEARCH_JSON_FILTERS_VALUE = "value";
	
	/** JSON request return column properteis **/
	public static final String SEARCH_JSON_COLOUMNS = "columns";
	public static final String SEARCH_JSON_COLOUMNS_QNAME = "qname";
	public static final String SEARCH_JSON_COLOUMNS_TITLE = "title";
	public static final String SEARCH_JSON_COLOUMNS_SEARCHABLE = "searchable";
	public static final String SEARCH_JSON_COLOUMNS_USE_WILD_CARD = "useWildCard";
	public static final String SEARCH_JSON_TRUE = "true";
	public static final String SEARCH_JSON_FALSE = "false";

	/** JSON request sort properteis **/
	public static final String SEARCH_JSON_SORT = "sortBy";
	public static final String SEARCH_JSON_SORT_ASCENDING = "sortAscending";
	public static final String SEARCH_DEFAULT_SORT = "relevance";
	
	/** JSON request return result properteis **/
	public static final String SEARCH_RESULT_COUNT = "resultCount";
	public static final String SEARCH_RESULT_PAGE_TOTAL = "pageTotal";
	public static final String SEARCH_RESULT_PER_PAGE = "resultPerPage";
	public static final String SEARCH_RESULT_SEARCH_FAILED = "searchFailed";
	public static final String SEARCH_RESULT_FAIL_CAUSE = "failCause";
	public static final String SEARCH_RESULT_OBJECT_LIST = "objectList";
	
}
