/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.api.v1.util;

public interface StudioConfiguration {

    /** Override Configuration */
    String STUDIO_CONFIG_OVERRIDE_CONFIG = "studio.config.overrideConfig";

    /** Content Repository */
    String REPO_BASE_PATH = "studio.repo.basePath";
    String GLOBAL_REPO_PATH = "studio.repo.globalRepoPath";
    String SITES_REPOS_PATH = "studio.repo.sitesRepoBasePath";
    String SANDBOX_PATH = "studio.repo.siteSandboxPath";
    String PUBLISHED_PATH = "studio.repo.sitePublishedPath";
    String BLUE_PRINTS_PATH = "studio.repo.blueprintsPath";
    String BOOTSTRAP_REPO = "studio.repo.bootstrapRepo";

    /** Database */
    String DB_PLATFORM = "studio.db.platform";
    String DB_DRIVER = "studio.db.driver";
    String DB_URL = "studio.db.url";
    String DB_USERNAME = "studio.db.username";
    String DB_PASSWORD = "studio.db.password";
    String DB_POOL_INITIAL_CONNECTIONS = "studio.db.pool.initialConnections";
    String DB_POOL_MAX_ACTIVE_CONNECTIONS = "studio.db.pool.maxActiveConnections";
    String DB_POOL_MAX_IDLE_CONNECTIONS = "studio.db.pool.maxIdleConnections";
    String DB_POOL_MIN_IDLE_CONNECTIONS = "studio.db.pool.minIdleConnections";
    String DB_POOL_MAX_WAIT_TIME = "studio.db.pool.maxWaitTime";
    String DB_INITIALIZER_ENABLED = "studio.db.initializer.enabled";
    String DB_TEST_ON_BORROW = "studio.db.testOnBorrow";
    String DB_VALIDATION_QUERY = "studio.db.validationQuery";
    String DB_VALIDATION_QUERY_SQLSERVER = "studio.db.validationQuery.sqlserver";
    String DB_VALIDATION_QUERY_POSTGRES = "studio.db.validationQuery.postgres";
    String DB_VALIDATION_QUERY_MYSQL = "studio.db.validationQuery.mysql";
    String DB_VALIDATION_QUERY_ORACLE = "studio.db.validationQuery.oracle";
    String DB_VALIDATION_QUERY_DERBY = "studio.db.validationQuery.derby";
    String DB_VALIDATION_QUERY_DB2 = "studio.db.validationQuery.db2";
    String DB_VALIDATION_INTERVAL = "studio.db.validationInterval";

    /** Configuration */
    String CONFIGURATION_GLOBAL_CONFIG_BASE_PATH = "studio.configuration.global.configBasePath";
    String CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME = "studio.configuration.global.roleMappingFileName";
    String CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME = "studio.configuration.global.permissionMappingFileName";
    String CONFIGURATION_SITE_CONFIG_BASE_PATH = "studio.configuration.site.configBasePath";
    String CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH = "studio.configuration.site.environment.configBasePath";
    String CONFIGURATION_SITE_DEPLOYMENT_CONFIG_BASE_PATH = "studio.configuration.site.deployment.configBasePath";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH = "studio.configuration.site.contentTypes.configBasePath";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_PATH = "studio.configuration.site.contentTypes.configPath";
    String CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME = "studio.configuration.site.generalConfigFileName";
    String CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME = "studio.configuration.site.permissionMappingsFileName";
    String CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME = "studio.configuration.site.roleMappingsFileName";
    String CONFIGURATION_SITE_ENVIRONMENT = "studio.configuration.site.environment";
    String CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME = "studio.configuration.site.environment.configFileName";
    String CONFIGURATION_SITE_NOTIFICATIONS_CONFIG_FILE_NAME = "studio.configuration.site.notificationsConfigFileName";
    String CONFIGURATION_SITE_NOTIFICATIONS_CONFIG_FILE_NAME_V2 = "studio.configuration.site.notificationsConfigFileName.v2";
    String CONFIGURATION_SITE_DEPLOYMENT_CONFIG_FILE_NAME = "studio.configuration.site.deployment.configFileName";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME = "studio.configuration.site.contentTypes.configFileName";

    /** Import Service */
    String IMPORT_ASSIGNEE = "studio.import.assignee";
    String IMPORT_XML_CHAIN_NAME = "studio.import.xmlChainName";
    String IMPORT_ASSET_CHAIN_NAME = "studio.import.assetChainName";

    /** Notification Service */
    String NOTIFICATION_CUSTOM_CONTENT_PATH_NOTIFICATION_ENABLED = "studio.notification.customContentPathNotification.enabled";
    String NOTIFICATION_CUSTOM_CONTENT_PATH_NOTIFICATION_PATTERN = "studio.notification.customContentPathNotificationPattern";
    String NOTIFICATION_V2_ENABLED = "studio.notification.v2.enabled";
    String NOTIFICATION_V2_TIMEZONE = "studio.notification.v2.timezone";

    /** Workflow Service */
    String WORKFLOW_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED = "studio.workflow.publishingWithoutDependencies.enabled";

    /** Activity Service */
    String ACTIVITY_USERNAME_CASE_SENSITIVE = "studio.activity.user.name.caseSensitive";

    /** Dependencies Service */
    String DEPENDENCIES_IGNORE_DEPENDENCIES_RULES = "studio.dependencies.ignoreDependenciesRules";
    String DEPENDENCIES_MANUAL_DEPENDENCY_APPROVING_ENABLED = "studio.dependencies.manualDependencyApproving.enabled";

    /** Object State Service */
    String OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE = "studio.objectState.bulkOperationsBatchSize";

    /** Security Service */
    String SECURITY_SESSION_TIMEOUT = "studio.security.sessionTimeout";

    /** Page Navigation Order Service */
    String PAGE_NAVIGATION_ORDER_INCREMENT = "studio.pageNavigationOrder.increment";

    void loadConfig();

    String getProperty(String key);
}
