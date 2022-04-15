/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import java.util.List;

/**
 * Studio Constants
 *
 * @author Hyanghee Lim
 * @author Sumer Jabri
 */
public final class StudioConstants {

    /**
     * content encoding
     **/
    public static final String CONTENT_ENCODING = "UTF-8";
    public static final String URL_ENCODING = "UTF-8";

    /**
     * document property names in return results
     **/
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

    /**
     * variable names in configuration properties
     **/
    public static final String PATTERN_CONTENT_TYPE = "\\{content\\-type\\}";
    public static final String PATTERN_ENVIRONMENT = "\\{environment\\}";
    public static final String PATTERN_MODULE = "\\{module\\}";
    public static final String PATTERN_ID = "\\$\\{id\\}";
    public static final String PATTERN_KEY = "\\{key\\}";
    public static final String PATTERN_SANDBOX = "\\$\\{sandbox\\}";
    public static final String PATTERN_SITE = "\\{site\\}";
    public static final String PATTERN_PATH = "\\{path\\}";
    public static final  String PATTERN_FROM_PATH = "\\{fromPath\\}";
    public static final String PATTERN_TO_PATH = "\\{toPath\\}";
    public static final String PATTERN_WEB_PROJECT = "\\$\\{webproject\\}";
    public static final String PATTERN_BASE_URL = "\\{baseUrl\\}";

    /**
     * Studio Structure Constants
     **/
    public static final String DESCRIPTOR_ROOT_PATH = "/site";

    /**
     * Error Codes
     */
    public static final int HTTP_STATUS_IMAGE_SIZE_ERROR = 499;
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500; //PORT Status.STATUS_INTERNAL_SERVER_ERROR;

    public static final String CONTENT_TYPE = "content-type";

    public static final String INTERNAL_NAME = "internalName";

    public static final String BROWSER_URI = "browserUri";

    public static final String USER = "USER";

    public static final String PERMISSION_VALUE_READ = "read";
    public static final String PERMISSION_VALUE_NOT_ALLOWED = "not allowed";
    public static final String PERMISSION_VALUE_PUBLISH = "publish";

    // Locking constants
    public static final String LOCKING_CACHE_CREATE_SCOPE = "lockingCacheCreateScope";

    /**
     * Repository Constants
     */
    public static final String BOOTSTRAP_REPO_PATH = "repo-bootstrap";        // Path to repository boostrap
    public static final String BOOTSTRAP_REPO_GLOBAL_PATH = "global";        // Path to the global repository inside the bootstrap repo
    public static final String CONFIG_SITENAME_VARIABLE = "\\{siteName\\}";
    public static final String CONFIG_SITEENV_VARIABLE = "\\{siteEnv\\}";
    public static final String IN_PROGRESS_BRANCH_NAME_SUFFIX = "_in_progress";

    /**
     * Site Constants
     */
    public static final String SITE_DEFAULT_GROUPS_DESCRIPTION = " site default group";
    public static final String SITE_NAME = "siteName";
    public static final String SITE_UUID_FILENAME = "site-uuid.txt";
    public static final String SITE_UUID_FILE_COMMENT = "# THIS IS A SYSTEM FILE. PLEASE DO NOT EDIT NOR DELETE IT!!!";

    /**
     * Content types constants
     */
    public static final String CONTENT_TYPE_PAGE = "page";
    public static final String CONTENT_TYPE_ASSET = "asset";
    public static final String CONTENT_TYPE_COMPONENT = "component";
    public static final String CONTENT_TYPE_DOCUMENT = "document";
    public static final String CONTENT_TYPE_RENDERING_TEMPLATE = "renderingTemplate";
    public static final String CONTENT_TYPE_UNKNOWN = "unknown";
    public static final String CONTENT_TYPE_TAXONOMY = "taxonomy";
    public static final String CONTENT_TYPE_CONTENT_TYPE = "content type";
    public static final String CONTENT_TYPE_CONFIGURATION = "configuration";
    public static final String CONTENT_TYPE_FOLDER = "folder";
    public static final String CONTENT_TYPE_USER = "user";
    public static final String CONTENT_TYPE_GROUP = "group";
    public static final String CONTENT_TYPE_TAXONOMY_REGEX = "/site/taxonomy/([^<]+)\\.xml";
    public static final String CONTENT_TYPE_ALL = "all";
    public static final String CONTENT_TYPE_FORM_DEFINITION = "formDefinition";
    public static final String CONTENT_TYPE_SITE = "site";
    public static final String CONTENT_TYPE_REMOTE_REPOSITORY = "remoteRepository";
    public static final String CONTENT_TYPE_CONFIG_FOLDER = "content-types";
    public static final String CONTENT_TYPE_SCRIPT = "script";
    public static final String CONTENT_TYPE_LEVEL_DESCRIPTOR = "levelDescriptor";
    public static final String CONTENT_TYPE_FILE = "file";

    /**
     * System constants
     */
    public static final String FILE_SEPARATOR = "/";
    public static final String SYSTEM_ADMIN_GROUP = "system_admin";
    public static final String ADMIN_ROLE = "admin";

    /**
     * Remote repository create option
     */
    public static final String REMOTE_REPOSITORY_CREATE_OPTION_CLONE = "clone";
    public static final String REMOTE_REPOSITORY_CREATE_OPTION_PUSH = "push";

    public static final String INDEX_FILE = "index.xml";

    public static final List<String> TOP_LEVEL_FOLDERS = List.of(
        "/site/website/index.xml",
        "/site/components",
        "/site/taxonomy",
        "/static-assets",
        "/templates",
        "/scripts",
        "/sources"
    );

    public static final String JSON_PROPERTY_ITEM = "item";
    public static final String JSON_PROPERTY_DEPENDENCIES = "dependencies";

    /**
     * API Request Parameter Names
     */
    public static final String API_REQUEST_PARAM_SITE = "site";
    public static final String API_REQUEST_PARAM_SITE_ID = "site_id";
    public static final String API_REQUEST_PARAM_ENTITIES = "entities";
    public static final String API_REQUEST_PARAM_ENVIRONMENT = "environment";

    /**
     * Site config xml elements
     */
    public static final String SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY = "published-repository";
    public static final String SITE_CONFIG_XML_ELEMENT_ENABLE_STAGING_ENVIRONMENT = "enable-staging-environment";
    public static final String SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT = "staging-environment";
    public static final String SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT = "live-environment";
    public static final String SITE_CONFIG_ELEMENT_SANDBOX_BRANCH = "sandbox-branch";
    public static final String SITE_CONFIG_ELEMENT_PLUGIN_FOLDER_PATTERN = "plugin-folder-pattern";
    public static final String SITE_CONFIG_ELEMENT_SITE_URLS = "site-urls";
    public static final String SITE_CONFIG_ELEMENT_AUTHORING_URL = "authoring-url";
    public static final String SITE_CONFIG_ELEMENT_STAGING_URL = "staging-url";
    public static final String SITE_CONFIG_ELEMENT_LIVE_URL = "live-url";
    public static final String SITE_CONFIG_ELEMENT_ADMIN_EMAIL_ADDRESS = "admin-email-address";
    public static final String SITE_CONFIG_XML_ELEMENT_WORKFLOW = "workflow";
    public static final String SITE_CONFIG_XML_ELEMENT_PUBLISHER = "publisher";
    public static final String SITE_CONFIG_XML_ELEMENT_REQUIRE_PEER_REVIEW = "requirePeerReview";
    public static final String SITE_CONFIG_XML_ELEMENT_PROTECTED_FOLDER_PATTERNS = "protected-folders-patterns/pattern";
    public static final String SITE_CONFIG_XML_ELEMENT_LOCALE = "locale";
    public static final String SITE_CONFIG_XML_ELEMENT_DATE_TIME_FORMAT_OPTIONS = "dateTimeFormatOptions";
    public static final String SITE_CONFIG_XML_ELEMENT_TIME_ZONE = "timeZone";

    /**
     * Repository commit messages variables
     */
    public static final String REPO_COMMIT_MESSAGE_USERNAME_VAR = "{username}";
    public static final String REPO_COMMIT_MESSAGE_PATH_VAR = "{path}";

    /**
     * Session attributes
     */
    public static final String HTTP_SESSION_ATTRIBUTE_AUTHENTICATION = "studio_authentication";

    public static final int DEFAULT_ORGANIZATION_ID = 1;

    public static final String REMOVE_SYSTEM_ADMIN_MEMBER_LOCK = "remove_system_admin_member_lock";

    /**
     * Cluster registration properties
     */
    public static final String CLUSTER_MEMBER_LOCAL_ADDRESS = "localAddress";
    public static final String CLUSTER_MEMBER_AUTHENTICATION_TYPE = "authenticationType";
    public static final String CLUSTER_MEMBER_USERNAME = "username";
    public static final String CLUSTER_MEMBER_PASSWORD = "password";
    public static final String CLUSTER_MEMBER_TOKEN = "token";
    public static final String CLUSTER_MEMBER_PRIVATE_KEY = "privateKey";

    /* Map keys */
    public static final String KEY_CONTENT_TYPE = "contentType";

    /* Modules */
    public static final String MODULE_STUDIO = "studio";
    public static final String MODULE_ENGINE = "engine";

    public static final String DEFAULT_CONFIG_URL = "http://localhost:8080";

    public static final String DEFAULT_PUBLISHING_LOCK_OWNER_ID = "STANDALONE STUDIO";

    // General Lock Service
    public static final String GLOBAL_REPOSITORY_GIT_LOCK = "GLOBAL_REPOSITORY_GIT_LOCK";
    public static final String SITE_SANDBOX_REPOSITORY_GIT_LOCK = "{site}_SANDBOX_REPOSITORY_GIT_LOCK";
    public static final String SITE_PUBLISHED_REPOSITORY_GIT_LOCK = "{site}_PUBLISHED_REPOSITORY_GIT_LOCK";
    public static final String STUDIO_CLOCK_EXECUTOR_SITE_LOCK = "{site}_STUDIO_CLOCK_EXECUTOR_SITE_LOCK";

    private StudioConstants() {
    }
}
