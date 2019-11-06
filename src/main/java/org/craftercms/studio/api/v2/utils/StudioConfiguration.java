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

package org.craftercms.studio.api.v2.utils;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.List;

public interface StudioConfiguration {

    /** Override Configuration */
    String STUDIO_CONFIG_OVERRIDE_CONFIG = "studio.config.overrideConfig";
    String STUDIO_CONFIG_GLOBAL_REPO_OVERRIDE_CONFIG = "studio.config.globalRepoOverrideConfig";

    /** Content Repository */
    String REPO_BASE_PATH = "studio.repo.basePath";
    String GLOBAL_REPO_PATH = "studio.repo.globalRepoPath";
    String SITES_REPOS_PATH = "studio.repo.sitesRepoBasePath";
    String SANDBOX_PATH = "studio.repo.siteSandboxPath";
    String REPO_SANDBOX_BRANCH = "studio.repo.siteSandboxBranch";
    String REPO_DEFAULT_REMOTE_NAME = "studio.repo.defaultRemoteName";
    String PUBLISHED_PATH = "studio.repo.sitePublishedPath";
    String BLUE_PRINTS_PATH = "studio.repo.blueprintsPath";
    String REPO_BLUEPRINTS_DESCRIPTOR_FILENAME = "studio.repo.blueprints.descriptor.filename";
    String BOOTSTRAP_REPO = "studio.repo.bootstrapRepo";
    String REPO_COMMIT_MESSAGE_PROLOGUE = "studio.repo.commitMessagePrologue";
    String REPO_COMMIT_MESSAGE_POSTSCRIPT = "studio.repo.commitMessagePostscript";
    String REPO_SANDBOX_WRITE_COMMIT_MESSAGE = "studio.repo.sandbox.write.commitMessage";
    String REPO_PUBLISHED_COMMIT_MESSAGE = "studio.repo.published.commitMessage";
    String REPO_PUBLISHED_LIVE = "studio.repo.published.live";
    String REPO_PUBLISHED_STAGING = "studio.repo.published.staging";
    String REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING = "studio.repo.syncDB.commitMessage.noProcessing";
    String REPO_CLEANUP_CRON = "studio.repo.cleanup.cron";
    String REPO_CREATE_REPOSITORY_COMMIT_MESSAGE = "studio.repo.createRepository.commitMessage";
    String REPO_CREATE_SANDBOX_BRANCH_COMMIT_MESSAGE = "studio.repo.createSandboxBranch.commitMessage";
    String REPO_INITIAL_COMMIT_COMMIT_MESSAGE = "studio.repo.initialCommit.commitMessage";
    String REPO_CREATE_AS_ORPHAN_COMMIT_MESSAGE = "studio.repo.createAsOrphan.commitMessage";
    String REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE = "studio.repo.blueprintsUpdated.commitMessage";
    String REPO_CREATE_FOLDER_COMMIT_MESSAGE = "studio.repo.createFolder.commitMessage";
    String REPO_DELETE_CONTENT_COMMIT_MESSAGE = "studio.repo.deleteContent.commitMessage";
    String REPO_MOVE_CONTENT_COMMIT_MESSAGE = "studio.repo.moveContent.commitMessage";
    String REPO_COPY_CONTENT_COMMIT_MESSAGE = "studio.repo.copyContent.commitMessage";
    String REPO_PULL_FROM_REMOTE_CONFLICT_NOTIFICATION_ENABLED =
            "studio.repo.pullFromRemote.conflict.notificationEnabled";
    String REPO_DEFAULT_IGNORE_FILE = "studio.repo.defaultIgnoreFile";

    /** Database */
    String DB_DRIVER = "studio.db.driver";
    String DB_SCHEMA = "studio.db.schema";
    String DB_URL = "studio.db.url";
    String DB_POOL_INITIAL_CONNECTIONS = "studio.db.pool.initialConnections";
    String DB_POOL_MAX_ACTIVE_CONNECTIONS = "studio.db.pool.maxActiveConnections";
    String DB_POOL_MAX_IDLE_CONNECTIONS = "studio.db.pool.maxIdleConnections";
    String DB_POOL_MIN_IDLE_CONNECTIONS = "studio.db.pool.minIdleConnections";
    String DB_POOL_MAX_WAIT_TIME = "studio.db.pool.maxWaitTime";
    String DB_INITIALIZER_ENABLED = "studio.db.initializer.enabled";
    String DB_INITIALIZER_URL = "studio.db.initializer.url";
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

    /** Galera Cluster **/
    String DB_GALERA_LIB_LOCATION = "studio.db.galera.lib.location";
    String DB_GALERA_CLUSTER_ADDRESS = "studio.db.galera.clusterAddress";
    String DB_GALERA_CLUSTER_NAME = "studio.db.galera.clusterName";
    String DB_GALERA_CLUSTER_NODE_ADDRESS = "studio.db.galera.clusterNodeAddress";
    String DB_GALERA_CLUSTER_NODE_NAME = "studio.db.galera.clusterNodeName";

    /** Configuration */
    String CONFIGURATION_GLOBAL_CONFIG_BASE_PATH = "studio.configuration.global.configBasePath";
    String CONFIGURATION_GLOBAL_MENU_FILE_NAME = "studio.configuration.global.menuFileName";
    String CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME = "studio.configuration.global.roleMappingFileName";
    String CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME = "studio.configuration.global.permissionMappingFileName";
    String CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH = "studio.configuration.global.ui.resource.override.path";
    String CONFIGURATION_GLOBAL_SYSTEM_SITE = "studio.configuration.global.systemSite";
    String CONFIGURATION_SITE_CONFIG_BASE_PATH = "studio.configuration.site.configBasePath";
    String CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN = "studio.configuration.site.configBasePathPattern";
    String CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH =
            "studio.configuration.site.multiEnvironment.configBasePath";
    String CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN =
            "studio.configuration.site.multiEnvironment.configBasePathPattern";
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
    String CONFIGURATION_SITE_DATA_SOURCES_CONFIG_LOCATION = "studio.configuration.site.dataSources.configLocation";
    String CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL = "studio.configuration.site.preview.destroy.context.url";
    String CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME =
            "studio.configuration.default.dependencyResolver.configFileName";
    String CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH =
            "studio.configuration.default.dependencyResolver.configBasePath";
    String CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME =
            "studio.configuration.site.dependencyResolver.configFileName";
    String CONFIGURATION_SITE_AWS_CONFIGURATION_PATH = "studio.configuration.site.aws.configurationPath";
    String CONFIGURATION_SITE_BOX_CONFIGURATION_PATH = "studio.configuration.site.box.configurationPath";
    String CONFIGURATION_SITE_WEBDAV_CONFIGURATION_PATH = "studio.configuration.site.webdav.configurationPath";
    String CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS = "studio.configuration.dependency.itemSpecificPatterns";
    String CONFIGURATION_SITE_ASSET_PROCESSING_CONFIGURATION_PATH =
            "studio.configuration.site.asset.processing.configurationPath";

    String CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG = "studio.authentication.chain";
    String CONFIGURATION_ENVIRONMENT_ACTIVE = "studio.configuration.environment.active";
    String CONFIGURATION_SITE_DEFAULT_PREVIEW_URL = "studio.configuration.site.defaultPreviewUrl";
    String CONFIGURATION_SITE_DEFAULT_AUTHORING_URL = "studio.configuration.site.defaultAuthoringUrl";
    String CONFIGURATION_SITE_DEFAULT_GRAPHQL_SERVER_URL = "studio.configuration.site.defaultGraphqlServerUrl";

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
    String SECURITY_PASSWORD_REQUIREMENTS_ENABLED = "studio.security.passwordRequirements.enabled";
    String SECURITY_PASSWORD_REQUIREMENTS_VALIDATION_REGEX = "studio.security.passwordRequirements.validationRegex";
    String SECURITY_PASSWORD_REQUIREMENTS_DESCRIPTION = "studio.security.passwordRequirements.description";

    /** Authentication headers **/
    String AUTHENTICATION_HEADERS_LOGOUT_ENABLED = "studio.authentication.headers.logout.enabled";

    /** Page Navigation Order Service */
    String PAGE_NAVIGATION_ORDER_INCREMENT = "studio.pageNavigationOrder.increment";

    /** Content Processors */
    String CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION =
            "studio.contentProcessor.contentLifeCycle.scriptLocation";

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
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED =
            "studio.job.deployContentToEnvironment.mandatoryDependenciesCheckEnabled";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT =
            "studio.job.deployContentToEnvironment.status.message.default";
    String JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_READY =
            "studio.job.deployContentToEnvironment.status.message.ready";
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

    /** Authoring Deployer **/
    String AUTHORING_REPLACE = "studio.authoring.replace";
    String AUTHORING_DISABLE_DEPLOY_CRON = "studio.authoring.disableDeployCron";
    String AUTHORING_TEMPLATE_NAME = "studio.authoring.templateName";

    /** Preview Search **/
    String PREVIEW_SEARCH_CREATE_URL = "studio.preview.search.createUrl";
    String PREVIEW_SEARCH_DELETE_URL = "studio.preview.search.deleteUrl";
    String PREVIEW_SEARCH_ENGINE = "studio.preview.search.engine";

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
    String AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_ENABLED = "logoutEnabled";
    String AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_URL = "logoutUrl";

    /** Publishing Thread Pool **/
    String PUBLISHING_THREAD_POOL_NAME_PREFIX = "studio.publishing.threadPool.namePrefix";
    String PUBLISHING_THREAD_POOL_CORE_POOL_SIZE = "studio.publishing.threadPool.corePoolSize";
    String PUBLISHING_THREAD_POOL_MAX_POOL_SIZE = "studio.publishing.threadPool.maxPoolSize";

    /** Clustering **/
    String CLUSTERING_SYNC_URL_FORMAT = "studio.clustering.sync.urlFormat";

    /** Clustering Thread Pool **/
    String CLUSTERING_SANDBOX_SYNC_JOB_INTERVAL = "studio.clustering.sandboxSyncJob.interval";
    String CLUSTERING_PUBLISHED_SYNC_JOB_INTERVAL = "studio.clustering.publishedSyncJob.interval";
    String CLUSTERING_GLOBAL_REPO_SYNC_JOB_INTERVAL = "studio.clustering.globalRepoSyncJob.interval";
    String CLUSTERING_HEARTBEAT_JOB_INTERVAL = "studio.clustering.heartbeatJob.interval";
    String CLUSTERING_INACTIVITY_CHECK_JOB_INTERVAL = "studio.clustering.inactivityCheckJob.interval";
    String CLUSTERING_THREAD_POOL_NAME_PREFIX = "studio.clustering.threadPool.namePrefix";
    String CLUSTERING_THREAD_POOL_CORE_POOL_SIZE = "studio.clustering.threadPool.corePoolSize";
    String CLUSTERING_THREAD_POOL_MAX_POOL_SIZE = "studio.clustering.threadPool.maxPoolSize";
    String CLUSTERING_HEARTBEAT_STALE_TIME_LIMIT = "studio.clustering.heartbeatStale.timeLimit";
    String CLUSTERING_INACTIVITY_TIME_LIMIT = "studio.clustering.inactivity.timeLimit";

    /** Cluster Node Registration **/
    String CLUSTERING_NODE_REGISTRATION = "studio.clustering.node.registration";

    /** Asset processing **/
    String CONFIGURATION_ASSET_PROCESSING_TINIFY_API_KEY = "studio.configuration.asset.processing.tinify.apiKey";

    /** Upgrade Configuration **/
    String UPGRADE_BRANCH_NAME = "studio.upgrade.branchName";
    String UPGRADE_COMMIT_MESSAGE = "studio.upgrade.commitMessage";
    String UPGRADE_VERSION_FILE = "studio.upgrade.versionFile";
    String UPGRADE_VERSION_TEMPLATE = "studio.upgrade.versionTemplate";
    String UPGRADE_VERSION_DEFAULT = "studio.upgrade.versionDefault";
    String UPGRADE_VERSION_XPATH = "studio.upgrade.versionXPath";
    String UPGRADE_DEFAULT_VERSION_SITE = "studio.upgrade.defaultVersion.site";
    String UPGRADE_DEFAULT_VERSION_FILE = "studio.upgrade.defaultVersion.file";
    String UPGRADE_CONFIGURATION_FILE = "studio.upgrade.configurationFile";
    String UPGRADE_PIPELINE_PREFIX = "studio.upgrade.pipeline.prefix";
    String UPGRADE_PIPELINE_SYSTEM = "studio.upgrade.pipeline.system";
    String UPGRADE_PIPELINE_SITE = "studio.upgrade.pipeline.site";
    String UPGRADE_PIPELINE_BLUEPRINT = "studio.upgrade.pipeline.blueprint";
    String UPGRADE_PIPELINE_CONFIGURATIONS = "studio.upgrade.pipeline.configurations";
    String UPGRADE_SCRIPT_FOLDER = "studio.upgrade.scriptFolder";

    /** Serverless Delivery Configuration **/
    String SERVERLESS_DELIVERY_ENABLED = "studio.serverless.delivery.enabled";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_CREATE_URL = "studio.serverless.delivery.deployer.target.createUrl";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_DELETE_URL = "studio.serverless.delivery.deployer.target.deleteUrl";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE = "studio.serverless.delivery.deployer.target.template";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_REPLACE = "studio.serverless.delivery.deployer.target.replace";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_REMOTE_REPO_URL = "studio.serverless.delivery.deployer.target.remoteRepoUrl";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_LOCAL_REPO_PATH = "studio.serverless.delivery.deployer.target.localRepoPath";
    String SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE_PARAMS = "studio.serverless.delivery.deployer.target.template.params";

    void loadConfig();

    String getProperty(String key);

    <T> T getProperty(String key, Class<T> clazz);

    <T> T getProperty(String key, Class<T> clazz, T defaultVal);

    HierarchicalConfiguration<ImmutableNode> getSubConfig(String key);

    List<HierarchicalConfiguration<ImmutableNode>> getSubConfigs(String key);

}
