/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
    String REPO_SANDBOX_BRANCH = "studio.repo.siteSandboxBranch";
    String PUBLISHED_PATH = "studio.repo.sitePublishedPath";
    String BLUE_PRINTS_PATH = "studio.repo.blueprintsPath";
    String BOOTSTRAP_REPO = "studio.repo.bootstrapRepo";
    String REPO_REBUILD_METADATA_BATCH_SIZE = "studio.repo.rebuildMetadata.batchSize";
    String REPO_SANDBOX_WRITE_COMMIT_MESSAGE = "studio.repo.sandbox.write.commitMessage";
    String REPO_PUBLISHED_COMMIT_MESSAGE = "studio.repo.published.commitMessage";
    String REPO_PUBLISHED_LIVE = "studio.repo.published.live";
    String REPO_PUBLISHED_STAGING = "studio.repo.published.staging";
    String REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING = "studio.repo.syncDB.commitMessage.noProcessing";

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
    String DB_INITIALIZER_CONFIGURE_DB_SCRIPT_LOCATION = "studio.db.initializer.configureDbscriptLocation";
    String DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION = "studio.db.initializer.createDbscriptLocation";
    String DB_INITIALIZER_CREATE_SCHEMA_SCRIPT_LOCATION = "studio.db.initializer.createSchemaScriptLocation";
    String DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED = "studio.db.initializer.randomAdminPassword.enabled";
    String DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH = "studio.db.initializer.randomAdminPassword.length";
    String DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS = "studio.db.initializer.randomAdminPassword.chars";
    String DB_TEST_ON_BORROW = "studio.db.testOnBorrow";
    String DB_VALIDATION_QUERY = "studio.db.validationQuery";
    String DB_VALIDATION_INTERVAL = "studio.db.validationInterval";
    String DB_BASE_PATH = "studio.db.basePath";
    String DB_DATA_PATH = "studio.db.dataPath";
    String DB_PORT = "studio.db.port";
    String DB_SOCKET = "studio.db.socket";


    /** Configuration */
    String CONFIGURATION_GLOBAL_CONFIG_BASE_PATH = "studio.configuration.global.configBasePath";
    String CONFIGURATION_GLOBAL_MENU_FILE_NAME = "studio.configuration.global.menuFileName";
    String CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME = "studio.configuration.global.roleMappingFileName";
    String CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME = "studio.configuration.global.permissionMappingFileName";
    String CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH = "studio.configuration.global.ui.resource.override.path";
    String CONFIGURATION_GLOBAL_SYSTEM_SITE = "studio.configuration.global.systemSite";
    String CONFIGURATION_SITE_CONFIG_BASE_PATH = "studio.configuration.site.configBasePath";
    String CONFIGURATION_SITE__MUTLI_ENVIRONMENT_CONFIG_BASE_PATH = "studio.configuration.site.multiEnvironment" +
            ".configBasePath";
    String CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH = "studio.configuration.site.environment.configBasePath";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH = "studio.configuration.site.contentTypes.configBasePath";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_PATH = "studio.configuration.site.contentTypes.configPath";
    String CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME = "studio.configuration.site.generalConfigFileName";
    String CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME = "studio.configuration.site.permissionMappingsFileName";
    String CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME = "studio.configuration.site.roleMappingsFileName";
    String CONFIGURATION_SITE_ENVIRONMENT = "studio.configuration.site.environment";
    String CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME = "studio.configuration.site.environment.configFileName";
    String CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME = "studio.configuration.site.contentTypes.configFileName";
    String CONFIGURATION_DEFAULT_GROUPS = "studio.configuration.defaultGroups";
    String CONFIGURATION_DEFAULT_ADMIN_GROUP = "studio.configuration.defaultAdminGroup";
    String CONFIGURATION_SITE_DATA_SOURCES_CONFIG_BASE_PATH = "studio.configuration.site.dataSources.configBasePath";
    String CONFIGURATION_SITE_DATA_SOURCES_CONFIG_FILE_NAME = "studio.configuration.site.dataSources.configFileName";
    String CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL = "studio.configuration.site.preview.destroy.sontext.url";
    String CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME =
            "studio.configuration.site.dependencyResolver.configFileName";
    String CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH =
            "studio.configuration.site.dependencyResolver.configBasePath";
    String CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS = "studio.configuration.dependency.itemSpecificPatterns";

    String CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG = "studio.authentication.chain";

    /** Import Service */
    String IMPORT_ASSIGNEE = "studio.import.assignee";
    String IMPORT_XML_CHAIN_NAME = "studio.import.xmlChainName";
    String IMPORT_ASSET_CHAIN_NAME = "studio.import.assetChainName";

    /** Notification Service */
    String NOTIFICATION_CONFIGURATION_FILE = "studio.notification.configurationFile";
    String NOTIFICATION_TIMEZONE = "studio.notification.timezone";

    /** Workflow Service */
    String WORKFLOW_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED = "studio.workflow.publishingWithoutDependencies.enabled";

    /** Activity Service */
    String ACTIVITY_USERNAME_CASE_SENSITIVE = "studio.activity.user.name.caseSensitive";

    /** Object State Service */
    String OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE = "studio.objectState.bulkOperationsBatchSize";

    /** Security Service */
    String SECURITY_SESSION_TIMEOUT = "studio.security.sessionTimeout";
    String SECURITY_PUBLIC_URLS = "studio.security.publicUrls";
    String SECURITY_IGNORE_RENEW_TOKEN_URLS = "studio.security.ignoreRenewTokenUrls";
    String SECURITY_TYPE = "studio.security.type";
    String SECURITY_CIPHER_SALT = "studio.security.cipher.salt";
    String SECURITY_CIPHER_KEY = "studio.security.cipher.key";
    String SECURITY_CIPHER_TYPE = "studio.security.cipher.type";
    String SECURITY_CIPHER_ALGORITHM = "studio.security.cipher.algorithm";
    String SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT = "studio.security.forgotPassword.message.subject";
    String SECURITY_FORGOT_PASSWORD_EMAIL_TEMPLATE = "studio.security.forgotPassword.email.template";
    String SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT = "studio.security.forgotPassword.token.timeout";
    String SECURITY_RESET_PASSWORD_SERVICE_URL = "studio.security.resetPassword.serviceUrl";

    /** LDAP */
    String SECURITY_LDAP_SERVER_URL = "studio.security.ldap.serverUrl";
    String SECURITY_LDAP_BIND_DN = "studio.security.ldap.bindDN";
    String SECURITY_LDAP_BIND_PASSWORD = "studio.security.ldap.bindPassword";
    String SECURITY_LDAP_BASE_CONTEXT = "studio.security.ldap.baseContext";

    /** Authentication headers **/
    String AUTHENTICATION_HEADERS_ENABLED = "studio.authentication.headers.enabled";
    String AUTHENTICATION_HEADERS_USERNAME = "studio.authentication.headers.username";
    String AUTHENTICATION_HEADERS_FIRST_NAME = "studio.authentication.headers.firstName";
    String AUTHENTICATION_HEADERS_LAST_NAME = "studio.authentication.headers.lastName";
    String AUTHENTICATION_HEADERS_EMAIL = "studio.authentication.headers.email";
    String AUTHENTICATION_HEADERS_LOGOUT_ENABLED = "studio.authentication.headers.logout.enabled";
    String AUTHENTICATION_HEADERS_LOGOUT_URL = "studio.authentication.headers.logout.url";
    String AUTHENTICATION_HEADERS_LOGOUT_METHOD = "studio.authentication.headers.logout.method";

    /** Page Navigation Order Service */
    String PAGE_NAVIGATION_ORDER_INCREMENT = "studio.pageNavigationOrder.increment";

    /** Content Processors */
    String CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION =
            "studio.contentProcessor.contentLifeCycle.scriptLocation";
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
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_PROCESSING_CHUNK_SIZE =
            "studio.job.deployContentToEnvironment.processingChunkSize";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED =
            "studio.job.deployContentToEnvironment.mandatoryDependenciesCheckEnabled";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT =
            "studio.job.deployContentToEnvironment.status.message.default";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_IDLE =
            "studio.job.deployContentToEnvironment.status.message.idle";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_BUSY =
            "studio.job.deployContentToEnvironment.status.message.busy";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_PUBLISHING =
            "studio.job.deployContentToEnvironment.status.message.publishing";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED =
            "studio.job.deployContentToEnvironment.status.message.queued";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR =
            "studio.job.deployContentToEnvironment.status.message.stopped.error";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_USER =
            "studio.job.deployContentToEnvironment.status.message.stopped.user";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STARTED_USER =
            "studio.job.deployContentToEnvironment.status.message.started.user";
    String JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE = "studio.job.deployment.masterPublishingNode";

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
    String PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED =
            "studio.publishingManager.publishingWithoutDependencies.enabled";

    /** Authentication Chain properties **/
    String AUTHENTICATION_CHAIN_PROVIDER_TYPE = "provider";
    String AUTHENTICATION_CHAIN_PROVIDER_ENABLED = "enabled";
    /** DB **/
    String AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB = "DB";
    /** LDAP **/
    String AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP = "LDAP";
    String AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL = "ldapUrl";
    String AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME = "ldapUsername";
    String AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD = "ldapPassword";
    String AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT = "ldapBaseContext";
    String AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE = "usernameLdapAttribute";
    String AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE = "firstNameLdapAttribute";
    String AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE = "lastNameLdapAttribute";
    String AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE = "emailLdapAttribute";
    String AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE = "groupNameLdapAttribute";
    String AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE = "groupNameLdapAttributeRegex";
    String AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE = "groupNameLdapAttributeMatchIndex";
    /** HEADERS **/
    String AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS = "HEADERS";
    String AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER = "secureKeyHeader";
    String AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE = "secureKeyHeaderValue";
    String AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER = "usernameHeader";
    String AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER = "firstNameHeader";
    String AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER = "lastNameHeader";
    String AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER = "emailHeader";
    String AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER = "groupsHeader";

    /** Publishing Thread Pool **/
    String PUBLISHING_THREAD_POOL_NAME_PREFIX = "studio.publishing.threadPool.namePrefix";
    String PUBLISHING_THREAD_POOL_CORE_POOL_SIZE = "studio.publishing.threadPool.corePoolSize";
    String PUBLISHING_THREAD_POOL_MAX_POOL_SIZE = "studio.publishing.threadPool.maxPoolSize";

    /** Clustering Thread Pool **/
    String CLUSTERING_SANDBOX_SYNC_JOB_INTERVAL = "studio.clustering.sandboxSyncJob.interval";
    String CLUSTERING_PUBLISHED_SYNC_JOB_INTERVAL = "studio.clustering.publishedSyncJob.interval";
    String CLUSTERING_THREAD_POOL_NAME_PREFIX = "studio.clustering.threadPool.namePrefix";
    String CLUSTERING_THREAD_POOL_CORE_POOL_SIZE = "studio.clustering.threadPool.corePoolSize";
    String CLUSTERING_THREAD_POOL_MAX_POOL_SIZE = "studio.clustering.threadPool.maxPoolSize";

    /** Cluster Node Registration **/
    String CLUSTERING_NODE_REGISTRATION = "studio.clustering.node.registration";

    /** Active environment **/
    String ENVIRONMENT = "environment";

    void loadConfig();

    String getProperty(String key);

    <T> T getProperty(String key, Class<T> clazz);
}
