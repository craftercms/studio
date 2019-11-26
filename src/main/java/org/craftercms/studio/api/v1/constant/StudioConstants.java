/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
   String PATTERN_MODULE = "\\{module\\}";
   String PATTERN_ID = "\\$\\{id\\}";
   String PATTERN_KEY = "\\{key\\}";
   String PATTERN_SANDBOX = "\\$\\{sandbox\\}";
   String PATTERN_SITE = "\\{site\\}";
   String PATTERN_PATH = "\\{path\\}";
   String PATTERN_FROM_PATH = "\\{fromPath\\}";
   String PATTERN_TO_PATH = "\\{toPath\\}";
   String PATTERN_WEB_PROJECT = "\\$\\{webproject\\}";
   String PATTERN_BASE_URL = "\\{baseUrl\\}";

   /** Studio Structure Constants **/
   String DESCRIPTOR_ROOT_PATH  = "/site";

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
    String CONFIG_SITEENV_VARIABLE = "\\{siteEnv\\}";

    /**
     * Site Constants
     */
    String SITE_DEFAULT_GROUPS_DESCRIPTION = " site default group";
    String SITE_NAME = "siteName";
    String SITE_UUID_FILENAME = "site-uuid.txt";
    String SITE_UUID_FILE_COMMENT = "# THIS IS A SYSTEM FILE. PLEASE DO NOT EDIT NOR DELETE IT!!!";

    /**
     * Content types constants
     */
    String CONTENT_TYPE_PAGE= "page";
    String CONTENT_TYPE_ASSET= "asset";
    String CONTENT_TYPE_COMPONENT= "component";
    String CONTENT_TYPE_DOCUMENT= "document";
    String CONTENT_TYPE_RENDERING_TEMPLATE= "renderingTemplate";
    String CONTENT_TYPE_UNKNOWN = "unknown";
    String CONTENT_TYPE_TAXONOMY = "taxonomy";
    String CONTENT_TYPE_CONTENT_TYPE = "content type";
    String CONTENT_TYPE_CONFIGURATION = "configuration";
    String CONTENT_TYPE_FOLDER = "folder";
    String CONTENT_TYPE_USER = "user";
    String CONTENT_TYPE_GROUP = "group";
    String CONTENT_TYPE_TAXONOMY_REGEX = "/site/taxonomy/([^<]+)\\.xml";
    String CONTENT_TYPE_ALL = "all";
    String CONTENT_TYPE_FORM_DEFINITION = "formDefinition";
    String CONTENT_TYPE_SITE = "site";
    String CONTENT_TYPE_REMOTE_REPOSITORY = "remoteRepository";
    String CONTENT_TYPE_CONFIG_FOLDER = "content-types";
    String CONTENT_TYPE_SCRIPT = "script";

    /**
     * System constants
     */
    String FILE_SEPARATOR = "/";
    String SYSTEM_ADMIN_GROUP = "system_admin";
    String ADMIN_ROLE = "admin";

    /**
     * Remote repository create option
     */
    String REMOTE_REPOSITORY_CREATE_OPTION_CLONE = "clone";
    String REMOTE_REPOSITORY_CREATE_OPTION_PUSH = "push";

    String INDEX_FILE = "index.xml";

    String SECURITY_AUTHENTICATION_TYPE = "authentication_type";
    String SECURITY_AUTHENTICATION_TYPE_DB = "db";
    String SECURITY_AUTHENTICATION_TYPE_LDAP = "ldap";
    String SECURITY_AUTHENTICATION_TYPE_HEADERS = "headers";

    String JSON_PROPERTY_ITEM = "item";
    String JSON_PROPERTY_DEPENDENCIES = "dependencies";

    /**
     * API Request Parameter Names
     */
    String API_REQUEST_PARAM_SITE = "site";
    String API_REQUEST_PARAM_SITE_ID = "site_id";
    String API_REQUEST_PARAM_ENTITIES = "entities";
    String API_REQUEST_PARAM_ENVIRONMENT = "environment";

    /**
     * Site config xml elements
     */
    String SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY = "published-repository";
    String SITE_CONFIG_XML_ELEMENT_ENABLE_STAGING_ENVIRONMENT = "enable-staging-environment";
    String SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT = "staging-environment";
    String SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT = "live-environment";
    String SITE_CONFIG_ELEMENT_SANDBOX_BRANCH = "sandbox-branch";
    String SITE_CONFIG_ELEMENT_PLUGIN_FOLDER_PATTERN = "plugin-folder-pattern";

    /**
     * Repository commit messages variables
     */
    String REPO_COMMIT_MESSAGE_USERNAME_VAR = "{username}";
    String REPO_COMMIT_MESSAGE_PATH_VAR = "{path}";

    /**
     * Session attributes
     */
    String HTTP_SESSION_ATTRIBUTE_AUTHENTICATION = "studio_authentication";

    int DEFAULT_ORGANIZATION_ID = 1;

    String REMOVE_SYSTEM_ADMIN_MEMBER_LOCK = "remove_system_admin_member_lock";

    /**
     * Cluster registration properties
     */
    String CLUSTER_MEMBER_LOCAL_ADDRESS = "localAddress";
    String CLUSTER_MEMBER_AUTHENTICATION_TYPE = "authenticationType";
    String CLUSTER_MEMBER_USERNAME = "username";
    String CLUSTER_MEMBER_PASSWORD = "password";
    String CLUSTER_MEMBER_TOKEN = "token";
    String CLUSTER_MEMBER_PRIVATE_KEY = "privateKey";

    /* Map keys */
    String KEY_CONTENT_TYPE = "contentType";

    /* Modules */
    String MODULE_STUDIO = "studio";
    String MODULE_ENGINE = "engine";
}
