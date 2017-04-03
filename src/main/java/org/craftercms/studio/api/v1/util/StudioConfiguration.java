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
    String REPO_PREVIEW_ROOT_PATH = "studio.repo.previewRootPath";
    String REPO_REBUILD_METADATA_BATCH_SIZE = "studio.repo.rebuildMetadata.batchSize";

    /** Database */
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
    String DB_INITIALIZER_URL = "studio.db.initializer.url";
    String DB_INITIALIZER_SCRIPT_LOCATION = "studio.db.initializer.scriptLocation";
    String DB_TEST_ON_BORROW = "studio.db.testOnBorrow";
    String DB_VALIDATION_QUERY = "studio.db.validationQuery";
    String DB_VALIDATION_INTERVAL = "studio.db.validationInterval";
    String DB_BASE_PATH = "studio.db.basePath";
    String DB_DATA_PATH = "studio.db.dataPath";
    String DB_PORT = "studio.db.port";
    String DB_SOCKET = "studio.db.socket";


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
    String CONFIGURATION_SITE_DEFAULT_GROUPS = "studio.configuration.site.defaultGroups";
    String CONFIGURATION_SITE_DEFAULT_ADMIN_GROUP = "studio.configuration.site.defaultAdminGroup";

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
    String SECURITY_URLS_TO_INCLUDE = "security.urlsToInclude";
    String SECURITY_URLS_TO_EXCLUDE = "studio.security.urlsToExclude";
    String SECURITY_EXCEPTION_URLS = "studio.security.exceptionUrls";
    String SECURITY_TYPE = "studio.security.type";
    String SECURITY_FILE_CONFIG_LOCATION = "studio.security.file.configLocation";
    String SECURITY_DB_SESSION_TIMEOUT = "studio.security.db.sessionTimeout";
    String SECURITY_CIPHER_SALT = "studio.security.cipher.salt";
    String SECURITY_CIPHER_KEY = "studio.security.cipher.key";
    String SECURITY_CIPHER_TYPE = "studio.security.cipher.type";
    String SECURITY_CIPHER_ALGORITHM = "studio.security.cipher.algorithm";
    String SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT = "studio.security.forgotPassword.message.subject";
    String SECURITY_FORGOT_PASSWORD_MESSAGE_TEXT = "studio.security.forgotPassword.message.text";
    String SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT = "studio.security.forgotPassword.token.timeout";

    /** Page Navigation Order Service */
    String PAGE_NAVIGATION_ORDER_INCREMENT = "studio.pageNavigationOrder.increment";

    /** Content Processors */
    String CONTENT_PROCESSOR_EXTRACT_METADATA_SCRIPT_LOCATION = "studio.contentProcessor.extractMetadata.scriptLocation";
    String CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION = "studio.contentProcessor.contentLifeCycle.scriptLocation";
    String CONTENT_PROCESSOR_ASSETS_SYSTEM_PATH = "studio.contentProcessor.assetsSystemPath";

    /** Email Service */
    String MAIL_FROM_DEFAULT = "studio.mail.from.default";
    String MAIL_HOST = "studio.mail.host";
    String MAIL_PORT = "studio.mail.port";
    String MAIL_USERNAME = "studio.mail.username";
    String MAIL_PASSWORD = "studio.mail.password";
    String MAIL_SMTP_AUTH = "studio.mail.smtp.auth";
    String MAIL_SMTP_START_TLS_ENABLE = "studio.mail.smtp.starttls.enable";
    String MAIL_SMTP_EHLO = "studio.mail.smtp.ehlo";
    String MAIL_DEBUG = "studio.mail.debug";

    /** Jobs */
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_PROCESSING_CHUNK_SIZE = "studio.job.deployContentToEnvironment.processingChunkSize";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED = "studio.job.deployContentToEnvironment.mandatoryDependenciesCheckEnabled";
    String JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE = "studio.job.deployment.masterPublishingNode";
    String JOB_SYNC_TARGETS_MAX_TOLERABLE_RETRIES = "studio.job.syncTargets.maxTolerableRetries";

    /** Content Types Filter Patterns */
    String CONTENT_TYPES_FILTER_PAGES_INCLUDE_PATTERN = "studio.contentTypes.filter.pages.includePattern";
    String CONTENT_TYPES_FILTER_COMPONENTS_INCLUDE_PATTERN = "studio.contentTypes.filter.components.includePattern";
    String CONTENT_TYPES_FILTER_DOCUMENTS_INCLUDE_PATTERN = "studio.contentTypes.filter.documents.includePattern";

    /** Preview Deployer **/
    String PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL = "studio.preview.defaultPreviewDeployerUrl";
    String PREVIEW_DEFAULT_CREATE_TARGET_URL = "studio.preview.createTargetUrl";
    String PREVIEW_DEFAULT_DELETE_TARGET_URL = "studio.preview.deleteTargetUrl";
    String PREVIEW_REPLACE = "studio.preview.replace";
    String PREVIEW_DISABLE_DEPLOY_CRON = "studio.preview.disableDeployCron";
    String PREVIEW_TEMPLATE_NAME = "studio.preview.templateName";
    String PREVIEW_REPO_URL = "studio.preview.repoUrl";
    String PREVIEW_ENGINE_URL = "studio.preview.engineUrl";

    /** Preview Search **/
    String PREVIEW_SEARCH_CREATE_URL = "studio.preview.search.createUrl";
    String PREVIEW_SEARCH_DELETE_URL = "studio.preview.search.deleteUrl";

    /** Publishing Manager */
    String PUBLISHING_MANAGER_INDEX_FILE = "studio.publishingManager.indexFile";
    String PUBLISHING_MANAGER_IMPORT_MODE_ENABLED  = "studio.publishingManager.importModeEnabled";
    String PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED = "studio.publishingManager.publishingWithoutDependencies.enabled";

    void loadConfig();

    String getProperty(String key);
}
