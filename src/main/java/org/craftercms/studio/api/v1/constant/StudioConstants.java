/*******************************************************************************
 * 	   Crafter Studio
 *
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General  License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General  License for more details.
 *
 *     You should have received a copy of the GNU General  License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.constant;

/**
 * Studio Constants
 *
 * @author Hyanghee Lim
 * @author Sumer Jabri
 *
 */
 public interface StudioConstants {

 	/** content encoding **/
   String CONTENT_ENCODING = "UTF-8";
   String URL_ENCODING = "UTF-8";

	/** document property names in return results **/
   String PROPERTY_TOTAL = "total";
   String PROPERTY_SORTED_BY = "sortedBy";
   String PROPERTY_SORT_ASCENDING = "ascending";
   String PROPERTY_DOCUMENTS = "documents";
   String DM_GO_LIVE_CACHE_KEY = "goliveItems";
   String DATE_PATTERN_WORKFLOW = "yyyy-MM-dd'T'HH:mm:ss";
   String DATE_PATTERN_WORKFLOW_WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssX";

	// date format of form content
   String DATE_PATTERN_MODEL = "yyyy-MM-dd'T'HH:mm:ssZ";

	// date format of scheduled deployment dates
   String DATE_FORMAT_SCHEDULED = "MM/dd hh:mma";

	// date format of deployed dates
   String DATE_FORMAT_DEPLOYED = "MM/dd";

	/** variable names in configuration properties **/
   String PATTERN_CONTENT_TYPE = "\\{content\\-type\\}";
   String PATTERN_ENVIRONMENT = "\\{environment\\}";
   String PATTERN_ID = "\\$\\{id\\}";
   String PATTERN_KEY = "\\{key\\}";
   String PATTERN_SANDBOX = "\\$\\{sandbox\\}";
   String PATTERN_SITE = "\\{site\\}";
   String PATTERN_WEB_PROJECT = "\\$\\{webproject\\}";

	/**
	 * Error Codes
	 */
	int HTTP_STATUS_IMAGE_SIZE_ERROR = 499;
	int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500; //PORT Status.STATUS_INTERNAL_SERVER_ERROR;

   String CONTENT_TYPE = "content-type";

   String INTERNAL_NAME = "internalName";

   String BROWSER_URI = "browserUri";

   String USER = "USER";

   String PERMISSION_VALUE_READ = "read";
   String PERMISSION_VALUE_NOT_ALLOWED= "not allowed";
   String PERMISSION_VALUE_PUBLISH= "publish";

    // Locking constants
   String LOCKING_CACHE_CREATE_SCOPE = "lockingCacheCreateScope";

	/**
	 * Repository Constants
	 */
	String BOOTSTRAP_REPO_PATH = "repo-bootstrap";		// Path to repository boostrap
	String BOOTSTRAP_REPO_GLOBAL_PATH = "global";		// Path to the global repository inside the bootstrap repo
    String CONFIG_SITENAME_VARIABLE = "\\{siteName\\}";
}
