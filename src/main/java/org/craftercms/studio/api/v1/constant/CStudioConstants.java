/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.constant;

import java.text.SimpleDateFormat;


/**
 * CStudio Common constants
 * 
 * @author hyanghee
 *
 */
public interface CStudioConstants {

 	/** content encoding **/
	public static final String CONTENT_ENCODING = "UTF-8";
	public static final String URL_ENCODING = "UTF-8";

	// DM storeRef 
//	public static final StoreRef STORE_REF = new StoreRef("workspace", "SpacesStore");

	/** document property names in return results **/
	public static final String PROPERTY_TOTAL = "total";
	public static final String PROPERTY_SORTED_BY = "sortedBy";
	public static final String PROPERTY_SORT_ASCENDING = "ascending";
	public static final String PROPERTY_DOCUMENTS = "documents";
	public static final String DM_GO_LIVE_CACHE_KEY = "goliveItems";
	public static final String DATE_PATTERN_WORKFLOW = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_PATTERN_WORKFLOW_WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssX";

	// date format of form content
	public static final String DATE_PATTERN_MODEL = "yyyy-MM-dd'T'HH:mm:ssZ";

	// date format of scheduled deployment dates
	public static final String DATE_FORMAT_SCHEDULED = "MM/dd hh:mma";

	// date format of deployed dates
	public static final String DATE_FORMAT_DEPLOYED = "MM/dd";

	/** variable names in configuration properties **/
	public static final String PATTERN_CONTENT_TYPE = "\\{content\\-type\\}";
	public static final String PATTERN_ENVIRONMENT = "\\{environment\\}";
	public static final String PATTERN_ID = "\\$\\{id\\}";
	public static final String PATTERN_KEY = "\\{key\\}";
	public static final String PATTERN_SANDBOX = "\\$\\{sandbox\\}";
	public static final String PATTERN_SITE = "\\{site\\}";
	public static final String PATTERN_WEB_PROJECT = "\\$\\{webproject\\}";

 	/** error codes **/
	public static final int HTTP_STATUS_IMAGE_SIZE_ERROR = 499;
	public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500; //PORT Status.STATUS_INTERNAL_SERVER_ERROR;

    public static final String CONTENT_TYPE = "content-type";
 
    public static final String INTERNAL_NAME = "internalName";
 
    public static final String BROWSER_URI = "browserUri";
 
    public static final String USER = "USER";
 
 	public static final String PERMISSION_VALUE_READ = "read";
 	public static final String PERMISSION_VALUE_NOT_ALLOWED= "not allowed";
    public static final String PERMISSION_VALUE_PUBLISH= "publish";

    // Locking constants
    public static final String LOCKING_CACHE_CREATE_SCOPE = "lockingCacheCreateScope";

    String STUDIO_SESSION_TOKEN_ATRIBUTE = "studioSessionToken";

    String JSON_PROPERTY_ITEM = "item";
    String JSON_PROPERTY_DEPENDENCIES = "dependencies";

    String API_REQUEST_PARAM_SITE = "site";
    String API_REQUEST_PARAM_ENTITIES = "entities";

    String FILE_SEPARATOR = "/";

    // Cache constants
    String CACHE_GLOBAL_SCOPE = "###GLOBAL###";
    String CACHE_USERS_SCOPE = "###USERS###";
    String CACHE_KEY_USERS_GROUPS = "groups";
    String CACHE_KEY_USERS_PROFILE = "profile";
}
