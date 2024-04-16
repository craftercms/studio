/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.service.site;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.*;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.event.repository.RepositoryEvent;
import org.craftercms.studio.api.v2.event.site.SiteDeletedEvent;
import org.craftercms.studio.api.v2.event.site.SiteDeletingEvent;
import org.craftercms.studio.api.v2.event.site.SiteReadyEvent;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.upgrade.StudioUpgradeManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.repository.job.RebuildRepositoryMetadata;
import org.craftercms.studio.impl.v1.repository.job.SyncDatabaseWithRepository;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.blobstore.BlobStoreDetails;
import org.craftercms.studio.model.site.SiteDetails;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.commons.file.blob.BlobStore.*;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.*;
import static org.craftercms.studio.api.v1.dal.SiteFeed.*;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v2.utils.PluginUtils.validatePluginParameters;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CREATE_SITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_DELETE_SITE;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 *
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);
    private final static String UPDATE_PARENT_ID_SCRIPT_PREFIX = "updateParentId_";
    private final static String CREATED_FILES_SCRIPT_PREFIX = "createdFiles_";
    private final static String REPO_OPERATIONS_SCRIPT_PREFIX = "repoOperations_";

    protected Deployer deployer;
    protected ContentService contentService;
    protected org.craftercms.studio.api.v1.repository.ContentRepository contentRepository;
    protected ContentRepository contentRepositoryV2;
    protected DependencyService dependencyService;
    protected SecurityService securityService;
    protected DeploymentService deploymentService;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected RebuildRepositoryMetadata rebuildRepositoryMetadata;
    protected SyncDatabaseWithRepository syncDatabaseWithRepository;
    protected GroupServiceInternal groupServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected StudioUpgradeManager upgradeManager;
    protected StudioConfiguration studioConfiguration;
    protected SitesService sitesServiceInternal;
    protected AuditServiceInternal auditServiceInternal;
    protected ConfigurationService configurationService;
    protected ConfigurationService configurationServiceInternal;
    protected ItemServiceInternal itemServiceInternal;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected ApplicationContext applicationContext;

    @Autowired
    protected SiteFeedMapper siteFeedMapper;

    protected EntitlementValidator entitlementValidator;

    protected String[] configurationPatterns;

    protected StudioDBScriptRunnerFactory studioDBScriptRunnerFactory;
    protected DependencyServiceInternal dependencyServiceInternal;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    protected UserDAO userDao;

    @Override
    public Set<String> getAllAvailableSites() {
        List<SiteFeed> sites = siteFeedMapper.getSites();
        Set<String> toRet = new HashSet<>();
        for (SiteFeed site : sites) {
            toRet.add(site.getSiteId());
        }
        return toRet;
    }

    @Override
    public int countSites() {
        return siteFeedMapper.countSites();
    }

    @Override
    @Valid
    @HasPermission(type= DefaultPermission.class, action = PERMISSION_CREATE_SITE)
    public void createSiteFromBlueprint(
            @ValidateStringParam String blueprintId,
            @Size(max = 50) @ValidateStringParam(whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
            @ValidateNoTagsParam String siteName,
            @ValidateStringParam String sandboxBranch,
            @ValidateNoTagsParam String desc,
            Map<String, String> params, boolean createAsOrphan)
            throws SiteAlreadyExistsException, SiteCreationException, DeployerTargetException,
            BlueprintNotFoundException, MissingPluginParameterException {
        if (exists(siteId) || existsByName(siteName)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Get the plugin descriptor for the blueprint '{}'", blueprintId);
        PluginDescriptor descriptor = sitesServiceInternal.getBlueprintDescriptor(blueprintId);
        if (Objects.isNull(descriptor)) {
            throw new BlueprintNotFoundException("Blueprint not found " + blueprintId);
        }

        logger.debug("Validate the parameters for blueprint '{}'", blueprintId);
        validatePluginParameters(descriptor.getPlugin(), params);

        String blueprintLocation = sitesServiceInternal.getBlueprintLocation(blueprintId);

        logger.debug("Validate the entitlements for the site '{}'", siteName);
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
        } catch (EntitlementException e) {
            throw new SiteCreationException("Unable to complete the request due to entitlement limits. " +
                    "Please contact your system administrator.", e);
        }

        logger.info("Started the site creation process for site '{}' based on the blueprint '{}'",
                siteName, blueprintId);
        boolean success = true;

        // We must fail site creation if any of the site creations steps fail and rollback
        // For example: Create site => create Deployer Target (fail) = fail
        // and rollback the whole thing.
        // What we need to do for site creation and the order of execution:
        // 1) deployer target, 2) git repo, 3) database, 4) kick deployer
        String siteUuid = UUID.randomUUID().toString();

        String creator = securityService.getCurrentUser();

        // Create the site in the preview deployer
        logger.info("Create the deployer targets for site '{}'", siteName);
        try {
            deployer.createTargets(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to create site '{}' ID '{}' based on blueprint '{}'. The deployer targets " +
                    "couldn't be created. Rolling back site creation.", siteName, siteId, blueprintId, e);
            throw new DeployerTargetException(format("Failed to create site '%s' ID '%s' based on blueprint '%s'. " +
                    "The deployer targets couldn't be created. Rolling back site creation.",
                    siteName, siteId, blueprintId), e);
        }

        if (success) {
            try {
                logger.info("Initialize site '{}' with blueprint '{}'", siteName, blueprintId);
                success = createSiteFromBlueprintGit(blueprintLocation, siteId, sandboxBranch, params, creator);
                ZonedDateTime now = DateUtils.getCurrentTime();

                logger.debug("Add the site UUID to site '{}'", siteName);
                addSiteUuidFile(siteId, siteUuid);

                logger.debug("Add the site record to the database for site '{}' ID '{}'", siteName, siteId);
                // insert database records
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteName);
                siteFeed.setSiteId(siteId);
                siteFeed.setSiteUuid(siteUuid);
                siteFeed.setDescription(desc);
                siteFeed.setPublishingStatus(READY);
                siteFeed.setSandboxBranch(sandboxBranch);
                retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.createSite(siteFeed));

                logger.info("Upgrade the site '{}'", siteName);
                upgradeManager.upgrade(siteId);

                // Add default groups
                logger.debug("Add the default groups to site '{}'", siteName);
                addDefaultGroupsForNewSite();

                String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);

                long startGetChangeSetCreatedFilesMark = 0;
                if (logger.isDebugEnabled()) {
                    logger.debug("Start change-set calculation for site '{}'", siteName);
                    startGetChangeSetCreatedFilesMark = System.currentTimeMillis();
                }
                Map<String, String> createdFiles =
                        contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished change-set calculation for site '{}' in '{}' milliseconds",
                            siteName, (System.currentTimeMillis() - startGetChangeSetCreatedFilesMark));
                }

                logger.debug("Add audit log to site '{}'", siteName);
                insertCreateSiteAuditLog(siteId, siteName, blueprintId, creator);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                contentRepositoryV2.insertGitLog(siteId, lastCommitId, 1, 1);
                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, lastCommitId);

                logger.info("Reload the site configuration for site '{}'", siteName);
            } catch (Exception e) {
                success = false;
                logger.error("Failed to create site '{}' ID '{}' based on blueprint '{}'. Rolling back site creation.",
                        siteName, siteId, blueprintId, e);

                deleteSite(siteId);

                throw new SiteCreationException(format("Failed to create site '%s' ID '%s' based on " +
                        "blueprint '%s'. Rolling back site creation.",
                        siteName, siteId, blueprintId), e);
            }
        }

        if (success) {
            logger.info("Sync created site content to preview for site '{}'", siteName);
            setSiteState(siteId, STATE_READY);
            configureBlobStores(siteId);
            // Now that everything is created, we can sync the preview deployer with the new content
            try {
                applicationContext.publishEvent(new SiteReadyEvent(siteId, siteUuid));
            } catch (Exception e) {
                setSiteState(siteId, STATE_INITIALIZING);
                logger.warn("Failed to sync site content to preview for site '{}' ID '{}'. While site creation was " +
                            "successful, the site won't be preview-able until the Preview Deployer is reachable " +
                            "and has successfully synced.",
                            siteName, siteId, e);
                throw new SiteCreationException(format("Failed to sync site content to preview for site '%s' " +
                                "ID '%s'. While site creation was successful, the site won't be preview-able until " +
                                "the Preview Deployer is reachable and has successfully synced.",
                                siteName, siteId), e);
            }
        } else {
            throw new SiteCreationException(format("Site creation failed site '%s' ID '%s' based on " +
                    "blueprint '%s'", siteName, siteId, blueprintId));
        }
        logger.info("Site '{}' ID '{}' created successfully", siteName, siteId);
    }

    /**
     * When serverless mode is enabled, checks if the site has blob-stores-config.xml file and if not, creates it.
     *
     * @param siteId The site ID
     */
    private void configureBlobStores(String siteId) {
        // TODO: JM: consider moving this kind of operations to a site-create pipeline
        if (!studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false)) {
            logger.info("Serverless delivery is disabled, blob-stores configuration will not be updated for site '{}'", siteId);
            return;
        }

        try {
            String configLocation = studioConfiguration.getProperty(BLOB_STORES_CONFIG_PATH);
            HierarchicalConfiguration<?> xmlConfiguration = configurationService.getXmlConfiguration(siteId, MODULE_STUDIO, configLocation);
            if (xmlConfiguration == null) {
                logger.info("Serverless delivery is enabled, configuring default blob stores for site '{}'", siteId);
                String environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
                String defaultBlobConfigsLocation = studioConfiguration.getProperty(BLOB_STORES_SERVERLESS_DEFAULT_CONFIG_PATH);
                Resource resource = applicationContext.getResource(defaultBlobConfigsLocation);
                configurationService.writeConfiguration(siteId, MODULE_STUDIO, configLocation, environment, resource.getInputStream());
            }
        } catch (Exception e) {
            logger.error("Failed to configure blob stores for site '{}'", siteId, e);
        }
    }

    private void insertCreateSiteAuditLog(String siteId, String siteName, String blueprint, String creator) throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(creator);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteName);
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
        AuditLogParameter auditLogParameter = new AuditLogParameter();
        auditLogParameter.setTargetId(siteId + ":" + blueprint);    // TODO: SJ: This feels hokey
        auditLogParameter.setTargetType(TARGET_TYPE_BLUEPRINT);
        auditLogParameter.setTargetValue(blueprint);
        auditLogParameters.add(auditLogParameter);

        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void processCreatedFiles(String siteId, Map<String, String> createdFiles, String creator,
                                     ZonedDateTime now, String lastCommitId) {
        long startProcessCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        SiteFeed siteFeed;
        try {
            siteFeed = getSite(siteId);
        } catch (SiteNotFoundException e) {
            logger.error("Failed to process created files, site ID '{}' not found", siteId, e);
            return;
        }
        User userObj;
        try {
            userObj = userServiceInternal.getUserByGitName(creator);
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to process created files in site ID '{}', user '{}' not found",
                    siteId, creator, e);
            return;
        }

        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();

        Path createdFileScriptPath = null;
        Path updateParentIdScriptPath = null;
        // TODO: SJ: Refactor to avoid string literals
        try {
            Path studioTempDir = getStudioTemporaryFilesRoot();
            String createdFileScriptFilename = CREATED_FILES_SCRIPT_PREFIX + UUID.randomUUID();
            createdFileScriptPath = Files.createTempFile(studioTempDir, createdFileScriptFilename, SQL_SCRIPT_SUFFIX);
            String updateParentIdScriptFilename = UPDATE_PARENT_ID_SCRIPT_PREFIX + UUID.randomUUID();
            updateParentIdScriptPath = Files.createTempFile(studioTempDir, updateParentIdScriptFilename, SQL_SCRIPT_SUFFIX);
            for (String key : createdFiles.keySet()) {
                String path = key;
                if (StringUtils.equals("D", createdFiles.get(path))) {
                    continue;
                }
                if (createdFiles.get(path).length() > 1) {
                    path = createdFiles.get(path);
                }

                // Item
                String label = FilenameUtils.getName(path);
                String contentTypeId = StringUtils.EMPTY;
                boolean disabled = false;
                if (StringUtils.endsWith(path, XML_PATTERN)) {
                    try {
                        Document contentDoc = contentService.getContentAsDocument(siteId, path);
                        if (contentDoc != null) {
                            Element rootElement = contentDoc.getRootElement();
                            String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
                            if (isNotEmpty(internalName)) {
                                label = internalName;
                            }
                            contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                            disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                        }
                    } catch (DocumentException e) {
                        logger.error("Failed to extract metadata from XML file at site '{}' path '{}'",
                                siteId, path, e);
                    }
                }
                String previewUrl = null;
                if (StringUtils.startsWith(path, ROOT_PATTERN_PAGES) ||
                        StringUtils.startsWith(path, ROOT_PATTERN_ASSETS)) {
                    previewUrl = itemServiceInternal.getBrowserUrl(siteId, path);
                }
                processAncestors(siteFeed.getId(), path, userObj.getId(), now, lastCommitId, createdFileScriptPath);
                long state = NEW.value;
                if (disabled) {
                    state = state | DISABLED.value;
                }

                if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(path))) {
                    addUpdateParentIdScriptSnippets(siteFeed.getId(), path, updateParentIdScriptPath);
                } else {
                    Files.write(createdFileScriptPath, insertItemRow(siteFeed.getId(), path, previewUrl, state,
                            null, userObj.getId(), now, userObj.getId(), now, null, label, contentTypeId,
                            contentService.getContentTypeClass(siteId, path),
                            StudioUtils.getMimeType(FilenameUtils.getName(path)), Locale.US.toString(), null,
                            contentRepositoryV2.getContentSize(siteId, path), null, lastCommitId, null).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(createdFileScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);

                    addUpdateParentIdScriptSnippets(siteFeed.getId(), path, updateParentIdScriptPath);

                    addDependenciesScriptSnippets(siteId, path, null, createdFileScriptPath);
                }
            }

            studioDBScriptRunner.execute(createdFileScriptPath.toFile());
            studioDBScriptRunner.execute(updateParentIdScriptPath.toFile());
            if (logger.isDebugEnabled()) {
                logger.debug("ProcessCreatedFiles finished in '{}' milliseconds",
                        (System.currentTimeMillis() - startProcessCreatedFilesMark));
            }
        } catch (IOException e) {
            logger.error("Failed to create the database script file for processingCreatedFiles in site '{}'", siteId, e);
        } finally {
            if (createdFileScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", createdFileScriptPath);
                FileUtils.deleteQuietly(createdFileScriptPath.toFile());
            }
            if (updateParentIdScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", updateParentIdScriptPath);
                FileUtils.deleteQuietly(updateParentIdScriptPath.toFile());
            }
        }
    }

    private void processAncestors(long siteId, String path, long userId, ZonedDateTime now, String commitId,
                                  Path createFileScriptPath) throws IOException {
        Path p = Paths.get(path);
        List<Path> parts = new LinkedList<>();
        if (nonNull(p.getParent())) {
            p.getParent().iterator().forEachRemaining(parts::add);
        }
        String currentPath = StringUtils.EMPTY;
        if (isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                if (isNotEmpty(ancestor.toString())) {
                    currentPath = currentPath + FILE_SEPARATOR + ancestor;
                    Files.write(createFileScriptPath, insertItemRow(siteId, currentPath, null, NEW.value, null, userId
                            , now, userId, now, null, ancestor.toString(), null, CONTENT_TYPE_FOLDER, null,
                            Locale.US.toString(), null, 0L, null, commitId, null).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(createFileScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                }
            }
        }
    }

    private void addUpdateParentIdScriptSnippets(long siteId, String path, Path updateParentIdScriptPath) throws IOException {
        String parentPath = FilenameUtils.getPrefix(path) +
                FilenameUtils.getPathNoEndSeparator(StringUtils.replace(path, SLASH_INDEX_FILE, ""));
        if (isEmpty(parentPath) || StringUtils.equals(parentPath, path)) {
            return;
        }
        addUpdateParentIdScriptSnippets(siteId, parentPath, updateParentIdScriptPath);
        if (StringUtils.endsWith(path, SLASH_INDEX_FILE)) {
            addUpdateParentIdScriptSnippets(siteId, StringUtils.replace(path,
                    "/index.xml", ""), updateParentIdScriptPath);
            if (StringUtils.startsWith(path, ROOT_PATTERN_PAGES)) {
                Files.write(updateParentIdScriptPath, updateNewPageChildren(siteId, path).getBytes(UTF_8),
                        StandardOpenOption.APPEND);
            }
        }
        Files.write(updateParentIdScriptPath, updateParentId(siteId, path, parentPath).getBytes(UTF_8),
                StandardOpenOption.APPEND);
        Files.write(updateParentIdScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
    }

    private void addDependenciesScriptSnippets(String siteId, String path, String oldPath, Path file) throws IOException {
        long startDependencyResolver = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        Map<String, Set<String>> dependencies = dependencyServiceInternal.resolveDependencies(siteId, path);
        if (logger.isDebugEnabled()) {
            logger.debug("Dependency resolver for site '{}' path '{}' finished in '{}' milliseconds",
                    siteId, path, (System.currentTimeMillis() - startDependencyResolver));
        }
        if (StringUtils.isEmpty(oldPath)) {
            Files.write(file, deleteDependencySourcePathRows(siteId, path).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } else {
            Files.write(file, deleteDependencySourcePathRows(siteId, oldPath).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        if (nonNull(dependencies) && !dependencies.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
                for (String targetPath : entry.getValue()) {
                    Files.write(file, insertDependencyRow(siteId, path, targetPath, entry.getKey())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }
        }
    }

    protected boolean createSiteFromBlueprintGit(String blueprintLocation, String siteId, String sandboxBranch,
                                                 Map<String, String> params, String creator)
            throws Exception {
        boolean success;

        // create site with git repo
        success = contentRepositoryV2.createSiteFromBlueprint(blueprintLocation, siteId, sandboxBranch, params, creator);

        String siteConfigFolder = FILE_SEPARATOR + "config" + FILE_SEPARATOR + "studio";
        replaceFileContentGit(siteId, siteConfigFolder + FILE_SEPARATOR + "site-config.xml", "SITENAME",
                siteId);

        return success;
    }

    protected void replaceFileContentGit(String site, String path, String find, String replace) throws Exception {
        InputStream content = contentRepository.getContent(site, path);
        String contentAsString = IOUtils.toString(content, UTF_8);

        contentAsString = contentAsString.replaceAll(find, replace);

        InputStream contentToWrite = IOUtils.toInputStream(contentAsString, UTF_8);

        contentRepository.writeContent(site, path, contentToWrite);
    }

    private void addDefaultGroupsForNewSite() {
        List<String> defaultGroups = getDefaultGroups();
        for (String group : defaultGroups) {
            String description = group + SITE_DEFAULT_GROUPS_DESCRIPTION;
            try {
                if (!groupServiceInternal.groupExists(-1, group)) {
                    try {
                        groupServiceInternal.createGroup(DEFAULT_ORGANIZATION_ID, group, description, false);
                    } catch (GroupAlreadyExistsException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    logger.info("Default group '{}' was not created since it already exists", group);
                }
            } catch (ServiceLayerException e) {
                logger.error("Failed to create group '{}'", group, e);
            }
        }
    }

    @Override
    @Valid
    @HasPermission(type= DefaultPermission.class, action = PERMISSION_CREATE_SITE)
    public void createSiteWithRemoteOption(
            @Size(max = 50) @ValidateStringParam(whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
            @ValidateStringParam String siteName,
            @ValidateStringParam String sandboxBranch,
            @ValidateNoTagsParam String description,
            String blueprintName,
            @ValidateStringParam String remoteName,
            @ValidateStringParam String remoteUrl,
            String remoteBranch, boolean singleBranch, String authenticationType,
            String remoteUsername, String remotePassword, String remoteToken,
            String remotePrivateKey,
            @ValidateStringParam String createOption,
            Map<String, String> params, boolean createAsOrphan)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, InvalidRemoteUrlException {
        if (exists(siteId) || existsByName(siteName)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Validate site entitlements for site '{}'", siteId);
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
        } catch (EntitlementException e) {
            throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
                    + "system administrator.", e);
        }

        if (REMOTE_REPOSITORY_CREATE_OPTION_CLONE.equals(createOption)) {
            logger.info("Clone site from remote repository for site '{}', " +
                            "remoteUrl '{}', remote branch '{}'",
                    siteId, remoteUrl, remoteBranch);
            createSiteCloneRemote(siteId, siteName, sandboxBranch, description, remoteName, remoteUrl, remoteBranch,
                    singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                    params, createAsOrphan);
        } else {
            logger.error("Invalid site creation option '{}' for site '{}'", createOption, siteId);
            throw new SiteCreationException(format("Invalid site creation option '%s' for site '%s'",
                    createOption, siteId));
        }
    }

    @SuppressWarnings("deprecation")
    private void createSiteCloneRemote(String siteId, String siteName, String sandboxBranch, String description,
                                       String remoteName, String remoteUrl, String remoteBranch, boolean singleBranch,
                                       String authenticationType, String remoteUsername, String remotePassword,
                                       String remoteToken, String remotePrivateKey, Map<String, String> params,
                                       boolean createAsOrphan)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, InvalidRemoteUrlException {
        boolean success;

        // We must fail site creation if any of the site creations steps fail and rollback
        // For example: Create site => create Deployer Target (fail) = fail
        // and rollback the whole thing.
        // What we need to do for site creation and the order of execution:
        // 1) git repo, 2) deployer target, 3) database, 4) kick deployer
        String siteUuid = UUID.randomUUID().toString();

        String creator = securityService.getCurrentUser();

        try {
            // create site by cloning remote git repo
            logger.info("Create site '{}' by cloning the remote '{}' url '{}' branch '{}'",
                    siteId, remoteName, remoteUrl, remoteBranch);
            success = contentRepositoryV2.createSiteCloneRemote(siteId, sandboxBranch, remoteName, remoteUrl,
                    remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                    remotePrivateKey, params, createAsOrphan, creator);

        } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
                RemoteRepositoryNotFoundException | ServiceLayerException e) {

            contentRepository.deleteSite(siteId);

            logger.error("Failed to create site '{}' by cloning '{}' url '{}' branch '{}'. Rolling back.",
                    siteId, remoteName, remoteUrl, remoteBranch, e);

            throw e;
        }

        if (!success) {
            contentRepository.removeRemoteRepositoriesForSite(siteId);
            contentRepository.deleteSite(siteId);
            throw new ServiceLayerException("Failed to create site: " + siteId + " ID: " + siteId + " as clone from " +
                    "remote repository: " + remoteName + " (" + remoteUrl + ")");
        }

        // Create the site in the preview deployer
        try {
            logger.info("Create Deployer targets for site '{}'", siteId);
            deployer.createTargets(siteId);
        } catch (Exception e) {
            logger.error("Failed to create Deployer targets for site '{}' as a clone of '{}' url '{}' " +
                    "branch '{}'. Site creation failed.", siteId, remoteName, remoteUrl, remoteBranch, e);

            contentRepositoryV2.removeRemote(siteId, remoteName);
            boolean deleted = contentRepository.deleteSite(siteId);

            if (!deleted) {
                logger.error("Failed to rollback site creation for site '{}'.", siteId);
            }

            throw new DeployerTargetException("Error while creating site: " + siteId + " ID: " + siteId +
                    " as clone from remote repository: " + remoteName +
                    " (" + remoteUrl + "). The required Deployer targets couldn't " +
                    "be created", e);
        }

        ZonedDateTime now = DateUtils.getCurrentTime();
        try {
            logger.debug("Add site UUID to site '{}'", siteId);
            addSiteUuidFile(siteId, siteUuid);

            // insert database records
            logger.debug("Add site record to the database for site '{}'", siteId);
            SiteFeed siteFeed = new SiteFeed();
            siteFeed.setName(siteName);
            siteFeed.setSiteId(siteId);
            siteFeed.setSiteUuid(siteUuid);
            siteFeed.setDescription(description);
            siteFeed.setPublishingStatus(READY);
            siteFeed.setSandboxBranch(sandboxBranch);
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.createSite(siteFeed));

            upgradeManager.upgrade(siteId);


            // Add default groups
            logger.info("Add default groups to site '{}'", siteId);
            addDefaultGroupsForNewSite();

            String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);
            String firstCommitId = contentRepositoryV2.getRepoFirstCommitId(siteId);

            long startGetChangeSetCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L ;
            Map<String, String> createdFiles =
                    contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
            if (logger.isDebugEnabled()) {
                logger.debug("Get the change-set of created files finished in '{}' milliseconds",
                        (System.currentTimeMillis() - startGetChangeSetCreatedFilesMark));
            }

            insertCreateSiteAuditLog(siteId, siteId, remoteName + "/" + remoteBranch, creator);
            processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);
            contentRepositoryV2.populateGitLog(siteId);

            updateLastCommitId(siteId, lastCommitId);
            updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
            updateLastSyncedGitlogCommitId(siteId, firstCommitId);

            logger.info("Load the configuration for site '{}'", siteId);
        } catch (Exception e) {
            logger.error("Failed to create site '{}' by cloning '{}' url '{}' branch '{}'",
                    siteId, remoteName, remoteUrl, remoteBranch, e);

            deleteSite(siteId);

            throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                    " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);
        }

        // Now that everything is created, we can sync the preview deployer with the new content
        logger.info("Sync site '{}' to preview", siteId);
        setSiteState(siteId, STATE_READY);
        configureBlobStores(siteId);
        try {
            applicationContext.publishEvent(new SiteReadyEvent(siteId, siteUuid));
        } catch (Exception e) {
            setSiteState(siteId, STATE_INITIALIZING);
            // TODO: SJ: This seems to leave the site in a bad state, review
            logger.error("Failed to sync site '{}' to preview. The site will become previewable once " +
                    "the preview deployer is reachable.", siteId, e);

            throw new SiteCreationException(format("Failed to sync site '%s' to preview. The site will become " +
                    "previewable once the preview deployer is reachable.", siteId), e);
        }
        logger.info("Site '{}' created successfully", siteId);
    }

    @Override
    @Valid
    @HasPermission(type= DefaultPermission.class, action = PERMISSION_DELETE_SITE)
    public boolean deleteSite(@ValidateStringParam String siteId) {
        boolean success = true;
        logger.info("Delete site '{}'", siteId);
        try {
            SiteFeed siteFeed = getSite(siteId);
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(siteId, STATE_DELETING));
            applicationContext.publishEvent(new SiteDeletingEvent(siteId, siteFeed.getSiteUuid()));
            logger.debug("Disable publishing for site '{}' prior to deleting it", siteId);
            enablePublishing(siteId, false);
        } catch (SiteNotFoundException e) {
            success = false;
            logger.error("Failed to stop publishing for site '{}'", siteId, e);
        }

        try {
            logger.debug("Delete the Deployer targets for site '{}'", siteId);
            deployer.deleteTargets(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the Deployer targets for site '{}'", siteId, e);
        }

        try {
            logger.debug("Destroy the preview context for site '{}'", siteId);
            success = success && destroySitePreviewContext(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to destroy the preview context for site '{}'", siteId, e);
        }

        try {
            logger.debug("Delete the git repo for site '{}'", siteId);
            contentRepository.deleteSite(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the repository for site '{}'", siteId, e);
        }

        try {
            configurationServiceInternal.invalidateConfiguration(siteId);
        } catch (Exception e) {
            logger.error("Failed to invalidate the configuration for site '{}'", siteId, e);
        }
        try {
            // delete database records
            logger.debug("Delete the database records for site '{}'", siteId);
            SiteFeed siteFeed = getSite(siteId);
            workflowServiceInternal.deleteWorkflowEntriesForSite(siteFeed.getId());
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.deleteSite(siteId, STATE_DELETED));
            retryingDatabaseOperationFacade.retry(() -> userDao.deleteUserPropertiesBySiteId(siteFeed.getId()));
            dependencyService.deleteSiteDependencies(siteId);
            deploymentService.deleteDeploymentDataForSite(siteId);
            itemServiceInternal.deleteItemsForSite(siteFeed.getId());
            dmPageNavigationOrderService.deleteSequencesForSite(siteId);
            contentRepository.deleteGitLogForSite(siteId);
            contentRepository.removeRemoteRepositoriesForSite(siteId);
            auditServiceInternal.deleteAuditLogForSite(siteFeed.getId());
            insertDeleteSiteAuditLog(siteId, siteFeed.getName());
            applicationContext.publishEvent(new SiteDeletedEvent(siteFeed.getSiteId(), siteFeed.getSiteUuid()));
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the database records for site '{}'", siteId, e);
        }

        return success;
    }

    private void insertDeleteSiteAuditLog(String siteId, String siteName) throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_DELETE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteName);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private boolean destroySitePreviewContext(String site) {
        boolean toReturn = true;
        String requestUrl = getDestroySitePreviewContextUrl(site);

        HttpGet getRequest = new HttpGet(requestUrl);
        RequestConfig requestConfig = RequestConfig.custom().setExpectContinueEnabled(true).build();
        getRequest.setConfig(requestConfig);

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            CloseableHttpResponse response = client.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Failed to send the destroy preview context request for site '{}'", site, e);
            toReturn = false;
        } finally {
            getRequest.releaseConnection();
        }
        return toReturn;
    }

    private String getDestroySitePreviewContextUrl(String site) {
        String url = studioConfiguration.getProperty(CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL);
        url = url.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
        return url;
    }

    @Override
    public SiteBlueprintTO[] getAvailableBlueprints() {
        RepositoryItem[] blueprintsFolders =
                contentRepository.getContentChildren("", studioConfiguration.getProperty(BLUE_PRINTS_PATH));
        List<SiteBlueprintTO> blueprints = new ArrayList<>();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                SiteBlueprintTO blueprintTO = new SiteBlueprintTO();
                blueprintTO.id = folder.name;
                blueprintTO.label = StringUtils.capitalize(folder.name);
                blueprintTO.description = ""; // How do we populate this dynamically
                blueprintTO.screenshots = null;
                blueprints.add(blueprintTO);
            }
        }

        return blueprints.toArray(new SiteBlueprintTO[0]);
    }

    @Override
    @Valid
    public void syncRepository(@ValidateStringParam String site) throws SiteNotFoundException {
        checkSiteExists(site);
        String lastDbCommitId = siteFeedMapper.getLastCommitId(site);
        if (lastDbCommitId != null) {
            syncDatabaseWithRepository.execute(site, lastDbCommitId);
        } else {
            rebuildDatabase(site);
        }
    }

    @Override
    @Valid
    public void rebuildDatabase(@ValidateStringParam String site) {
        rebuildRepositoryMetadata.execute(site);
    }

    @Override
    @Valid
    public void updateLastCommitId(@ValidateStringParam String site,
                                   @ValidateStringParam String commitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", site);
        params.put("lastCommitId", commitId);
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updateLastCommitId(params));
    }

    @Override
    public void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updateLastVerifiedGitlogCommitId(params));
    }

    @Override
    public void updateLastSyncedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updateLastSyncedGitlogCommitId(params));
    }

    @Override
    @Valid
    public boolean syncDatabaseWithRepo(@ValidateStringParam String site,
                                        @ValidateStringParam String fromCommitId) {
        return syncDatabaseWithRepo(site, fromCommitId, true);
    }

    @Override
    @Valid
    public boolean syncDatabaseWithRepo(@ValidateStringParam String site,
                                        @ValidateStringParam String fromCommitId,
                                        boolean generateAuditLog) {
        // TODO: Switch to new item table instead of using old state and metadata - Dejan
        // TODO: Remove references to old data layer - Dejan
        long startSyncRepoMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        boolean toReturn = true;

        String repoLastCommitId = contentRepository.getRepoLastCommitId(site);
        long startGetOperationsFromDeltaMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        List<RepoOperation> repoOperationsDelta = contentRepositoryV2.getOperationsFromDelta(site, fromCommitId,
                repoLastCommitId);
        if (logger.isDebugEnabled()) {
            logger.debug("Get Repo Operations from delta finished in '{}' milliseconds",
                    (System.currentTimeMillis() - startGetOperationsFromDeltaMark));
            logger.debug("The number of repo operations from delta is '{}'", repoOperationsDelta.size());
        }
        if (CollectionUtils.isEmpty(repoOperationsDelta)) {
            logger.debug("The database is up to date with the repository in site '{}'", site);
            contentRepositoryV2.markGitLogVerifiedProcessed(site, fromCommitId);
            updateLastCommitId(site, repoLastCommitId);
            updateLastVerifiedGitlogCommitId(site, repoLastCommitId);
            return toReturn;
        }

        logger.info("Sync the database with the repository in site '{}' starting at commit ID '{}'",
                site, (StringUtils.isEmpty(fromCommitId) ? "none (empty repo)" : fromCommitId));
        logger.debug("The operations to sync for site '{}' are", site);
        for (RepoOperation repoOperation : repoOperationsDelta) {
            RepoOperation.Action action = repoOperation.getAction();
            if (action == RepoOperation.Action.DELETE) {
                logger.warn("\tSite '{}' Operation '{}' path '{}'",
                        site, action, repoOperation.getPath());
            } else if (action == RepoOperation.Action.CREATE) {
                logger.info("\tSite '{}' Operation '{}' path '{}'",
                        site, action, repoOperation.getPath());
            } else {
                logger.debug("\tSite '{}' Operation '{}' path '{}'",
                        site, action, repoOperation.getPath());
            }

        }

        long startUpdateDBMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
        Path repoOperationsScriptPath = null;
        Path updateParentIdScriptPath = null;
        try {
            Path studioTempDir = getStudioTemporaryFilesRoot();
            String repoOperationsScriptFilename = REPO_OPERATIONS_SCRIPT_PREFIX + UUID.randomUUID();
            repoOperationsScriptPath = Files.createTempFile(studioTempDir, repoOperationsScriptFilename, SQL_SCRIPT_SUFFIX);
            String updateParentIdScriptFilename = UPDATE_PARENT_ID_SCRIPT_PREFIX + UUID.randomUUID();
            updateParentIdScriptPath = Files.createTempFile(studioTempDir, updateParentIdScriptFilename, SQL_SCRIPT_SUFFIX);
            toReturn = processRepoOperations(site, repoOperationsDelta, repoOperationsScriptPath,
                    updateParentIdScriptPath);
            studioDBScriptRunner.execute(repoOperationsScriptPath.toFile());
            studioDBScriptRunner.execute(updateParentIdScriptPath.toFile());
        } catch (IOException e) {
            logger.error("Failed to create the database script for processing the created files in site '{}'", site);
        } finally {
            if (repoOperationsScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", repoOperationsScriptPath);
                FileUtils.deleteQuietly(repoOperationsScriptPath.toFile());
            }
            if (updateParentIdScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", updateParentIdScriptPath);
                FileUtils.deleteQuietly(updateParentIdScriptPath.toFile());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Database update completed in '{}' milliseconds",
                    (System.currentTimeMillis() - startUpdateDBMark));
        }

        // At this point we have attempted to process all operations, some may have failed
        // We will update the lastCommitId of the database ignoring errors if any
        logger.debug("Done syncing repo operations to the database with a result of '{}'", toReturn);
        logger.debug("Sync the database lastCommitId for site '{}'", site);

        // Update database
        logger.debug("Update the last commit id '{}' in site '{}'", repoLastCommitId, site);
        updateLastCommitId(site, repoLastCommitId);
        updateLastVerifiedGitlogCommitId(site, repoLastCommitId);
        if (logger.isDebugEnabled()) {
            logger.debug("Update the database finished in '{}' milliseconds",
                    (System.currentTimeMillis() - startUpdateDBMark));
        }

        logger.info("Done syncing the database with the git repository for site '{}' starting at commit ID '{}' " +
                "with a final result of '{}'",
                site, (StringUtils.isEmpty(fromCommitId) ? "none (empty repo)" : fromCommitId), toReturn);
        logger.info("The last commit ID for site '{}' is now '{}'", site, repoLastCommitId);

        if (!toReturn) {
            // Some operations failed during sync database from repo
            // Must log and make some noise here, this isn't great
            logger.error("Some operations failed to sync to the database for site '{}', see prior errors", site);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sync database from repo finished in '{}' milliseconds", (System.currentTimeMillis() - startSyncRepoMark));
        }

        // Sync all preview deployers
        try {
            logger.debug("Sync preview for site '{}'", site);
            applicationContext.publishEvent(new RepositoryEvent(site));
        } catch (Exception e) {
            logger.error("Failed to sync preview for site '{}'", site, e);
        }
        return toReturn;
    }

    private boolean processRepoOperations(String siteId, List<RepoOperation> repoOperations,
                                          Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException {
        boolean toReturn = true;
        long startProcessRepoOperationMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        SiteFeed siteFeed;
        try {
            siteFeed = getSite(siteId);
        } catch (SiteNotFoundException e) {
            logger.error("Failed to process repo operations in site '{}'. Site not found.", siteId, e);
            return false;
        }

        User userObj = null;
        Map<String, User> cachedUsers = new HashMap<>();
        try {
            cachedUsers.put(GIT_REPO_USER_USERNAME, userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME));
        } catch (UserNotFoundException | ServiceLayerException e) {
            logger.error("Failed to process repo operations in site '{}'. git_repo_user should be in the the database",
                    siteId,e);
        }

        String label;
        String contentTypeId;
        String previewUrl;
        boolean disabled;
        long state;
        long onStateBitMap;
        long offStateBitmap;
        for (RepoOperation repoOperation : repoOperations) {
            switch (repoOperation.getAction()) {
                case CREATE:
                case COPY:
                    if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                        userObj = cachedUsers.get(repoOperation.getAuthor());
                    } else {
                        try {
                            userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                        } catch (UserNotFoundException | ServiceLayerException e) {
                            logger.debug("User '{}' not found while processing operations in site '{}'",
                                    repoOperation.getAuthor(), siteId, e);
                        }
                    }
                    if (Objects.isNull(userObj)) {
                        userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                    }
                    label = FilenameUtils.getName(repoOperation.getPath());
                    contentTypeId = StringUtils.EMPTY;
                    disabled = false;
                    if (StringUtils.endsWith(repoOperation.getPath(), XML_PATTERN)) {
                        try {
                            Document contentDoc = contentService.getContentAsDocument(siteId, repoOperation.getPath());
                            if (contentDoc != null) {
                                Element rootElement = contentDoc.getRootElement();
                                String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
                                if (isNotEmpty(internalName)) {
                                    label = internalName;
                                }
                                contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                            }
                        } catch (DocumentException e) {
                            logger.error("Failed to extract metadata from the XML site '{}' path '{}'",
                                    siteId, repoOperation.getPath(), e);
                        }
                    }
                    previewUrl = null;
                    if (StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) ||
                            StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_ASSETS)) {
                        previewUrl = itemServiceInternal.getBrowserUrl(siteId, repoOperation.getPath());
                    }
                    processAncestors(siteFeed.getId(), repoOperation.getPath(), userObj.getId(),
                            repoOperation.getDateTime(), repoOperation.getCommitId(), repoOperationsScriptPath);
                    state = NEW.value;
                    if (disabled) {
                        state = state | DISABLED.value;
                    }

                    if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
                        addUpdateParentIdScriptSnippets(siteFeed.getId(), repoOperation.getPath(),
                                updateParentIdScriptPath);
                    } else {
                        Files.write(repoOperationsScriptPath, insertItemRow(siteFeed.getId(),
                                repoOperation.getPath(), previewUrl, state, null, userObj.getId(),
                                repoOperation.getDateTime(), userObj.getId(), repoOperation.getDateTime(),
                                null, label, contentTypeId,
                                contentService.getContentTypeClass(siteId, repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                Locale.US.toString(), null,
                                contentRepositoryV2.getContentSize(siteId, repoOperation.getPath()), null,
                                repoOperation.getCommitId(), null).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        logger.debug("Extract dependencies from site '{}' path '{}'",
                                siteId, repoOperation.getPath());
                        addUpdateParentIdScriptSnippets(siteFeed.getId(), repoOperation.getPath(),
                                updateParentIdScriptPath);
                        addDependenciesScriptSnippets(siteId, repoOperation.getPath(), null,
                                repoOperationsScriptPath);
                    }
                    break;

                case UPDATE:
                    if (!ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
                        if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                            userObj = cachedUsers.get(repoOperation.getAuthor());
                        } else {
                            try {
                                userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                            } catch (UserNotFoundException | ServiceLayerException e) {
                                logger.debug("User '{}' not found while processing operations in site '{}'",
                                        repoOperation.getAuthor(), siteId, e);
                            }
                        }
                        if (Objects.isNull(userObj)) {
                            userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                        }
                        label = FilenameUtils.getName(repoOperation.getPath());
                        contentTypeId = StringUtils.EMPTY;
                        disabled = false;
                        if (StringUtils.endsWith(repoOperation.getPath(), XML_PATTERN)) {
                            try {
                                Document contentDoc = contentService.getContentAsDocument(siteId, repoOperation.getPath());
                                if (contentDoc != null) {
                                    Element rootElement = contentDoc.getRootElement();
                                    String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
                                    if (isNotEmpty(internalName)) {
                                        label = internalName;
                                    }
                                    contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                    disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                                }
                            } catch (DocumentException e) {
                                logger.error("Failed to extract metadata from the XML site '{}' path '{}'",
                                        siteId, repoOperation.getPath(), e);
                            }
                        }
                        previewUrl = null;
                        if (StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) ||
                                StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_ASSETS)) {
                            previewUrl = itemServiceInternal.getBrowserUrl(siteId, repoOperation.getPath());
                        }
                        onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
                        offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
                        if (disabled) {
                            onStateBitMap = onStateBitMap | DISABLED.value;
                        } else {
                            offStateBitmap = offStateBitmap | DISABLED.value;
                        }

                        Files.write(repoOperationsScriptPath, updateItemRow(siteFeed.getId(),
                                repoOperation.getPath(), previewUrl, onStateBitMap, offStateBitmap, userObj.getId(),
                                repoOperation.getDateTime(), label, contentTypeId,
                                contentService.getContentTypeClass(siteId, repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                contentRepositoryV2.getContentSize(siteId, repoOperation.getPath()),
                                repoOperation.getCommitId()).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        logger.debug("Extract dependencies from site '{}' path '{}'",
                                siteId, repoOperation.getPath());
                        addDependenciesScriptSnippets(siteId, repoOperation.getPath(), null, repoOperationsScriptPath);
                    }
                    break;
                case DELETE:
                    String folder = FILE_SEPARATOR + FilenameUtils.getPathNoEndSeparator(repoOperation.getPath());
                    boolean folderExists = contentRepositoryV2.contentExists(siteId, folder);

                    // If the folder exists and the deleted file is the index file, then we need to update the parent id for the children
                    if (folderExists && StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) &&
                            StringUtils.endsWith(repoOperation.getPath(), SLASH_INDEX_FILE)) {
                        Files.write(repoOperationsScriptPath,
                                updateDeletedPageChildren(siteFeed.getId(), folder).getBytes(UTF_8), StandardOpenOption.APPEND);
                    }

                    Files.write(repoOperationsScriptPath,
                            deleteItemRow(siteFeed.getId(), repoOperation.getPath()).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    if (!folderExists) {
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath,
                                deleteItemRow(siteFeed.getId(), folder).getBytes(UTF_8), StandardOpenOption.APPEND);
                    }
                    Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                    Files.write(repoOperationsScriptPath,
                            deleteDependencyRows(siteId, repoOperation.getPath()).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                    break;

                case MOVE:
                    if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                        userObj = cachedUsers.get(repoOperation.getAuthor());
                    } else {
                        try {
                            userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                        } catch (UserNotFoundException | ServiceLayerException e) {
                            logger.debug("User '{}' not found while processing operations in site '{}'",
                                    repoOperation.getAuthor(), siteId, e);
                        }
                    }
                    if (Objects.isNull(userObj)) {
                        userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                    }
                    label = FilenameUtils.getName(repoOperation.getMoveToPath());
                    contentTypeId = StringUtils.EMPTY;
                    disabled = false;
                    if (StringUtils.endsWith(repoOperation.getMoveToPath(), XML_PATTERN)) {
                        try {
                            Document contentDoc = contentService.getContentAsDocument(siteId, repoOperation.getMoveToPath());
                            if (contentDoc != null) {
                                Element rootElement = contentDoc.getRootElement();
                                String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
                                if (isNotEmpty(internalName)) {
                                    label = internalName;
                                }
                                contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                            }
                        } catch (DocumentException e) {
                            logger.error("Failed to extact metadata from the XML site '{}' path '{}'",
                                    siteId, repoOperation.getMoveToPath(), e);
                        }
                    }
                    previewUrl = null;
                    if (StringUtils.startsWith(repoOperation.getMoveToPath(), ROOT_PATTERN_PAGES) ||
                            StringUtils.startsWith(repoOperation.getMoveToPath(), ROOT_PATTERN_ASSETS)) {
                        previewUrl = itemServiceInternal.getBrowserUrl(siteId, repoOperation.getMoveToPath());
                    }
                    processAncestors(siteFeed.getId(), repoOperation.getMoveToPath(), userObj.getId(),
                            repoOperation.getDateTime(), repoOperation.getCommitId(), repoOperationsScriptPath);
                    onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
                    offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
                    if (disabled) {
                        onStateBitMap = onStateBitMap | DISABLED.value;
                    } else {
                        offStateBitmap = offStateBitmap | DISABLED.value;
                    }
                    if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath())) ||
                            ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getMoveToPath()))) {
                        addUpdateParentIdScriptSnippets(siteFeed.getId(), repoOperation.getMoveToPath(),
                                updateParentIdScriptPath);
                    } else {
                        Files.write(repoOperationsScriptPath, moveItemRow(siteId, repoOperation.getPath(),
                                repoOperation.getMoveToPath(), onStateBitMap, offStateBitmap).getBytes(UTF_8),
                                StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, updateItemRow(siteFeed.getId(),
                                repoOperation.getPath(), previewUrl, onStateBitMap, offStateBitmap, userObj.getId(),
                                repoOperation.getDateTime(), label, contentTypeId,
                                contentService.getContentTypeClass(siteId, repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                contentRepositoryV2.getContentSize(siteId, repoOperation.getPath()),
                                repoOperation.getCommitId()).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        addUpdateParentIdScriptSnippets(siteFeed.getId(), repoOperation.getMoveToPath(), updateParentIdScriptPath);
                        addDependenciesScriptSnippets(siteId, repoOperation.getMoveToPath(),
                                repoOperation.getPath(), repoOperationsScriptPath);
                    }
                    invalidateConfigurationCacheIfRequired(siteId, repoOperation.getMoveToPath());
                    break;

                default:
                    logger.error("Failed to process unknown repo operation '{}' in site '{}'",
                            siteId, repoOperation.getAction());
                    toReturn = false;
                    break;
            }
            invalidateConfigurationCacheIfRequired(siteId, repoOperation.getPath());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Completed processing repo operations in site '{}'. Processing finished in '{}' milliseconds",
                    siteId, (System.currentTimeMillis() - startProcessRepoOperationMark));
        }
        return toReturn;
    }

    protected void invalidateConfigurationCacheIfRequired(String siteId, String path) {
        if (RegexUtils.matchesAny(path, configurationPatterns)) {
            configurationService.invalidateConfiguration(siteId, path);
        }
    }

    @Override
    @Valid
    public boolean exists(@ValidateStringParam String site) {
        return siteFeedMapper.exists(site) > 0;
    }

    @Override
    @Valid
    public void checkSiteExists(@ValidateStringParam final String site) throws SiteNotFoundException {
        if (!exists(site)) {
            throw new SiteNotFoundException(format("Site '%s' not found.", site));
        }
    }

    @Override
    @Valid
    public boolean existsById(@ValidateStringParam String siteId) {
        return siteFeedMapper.existsById(siteId) > 0;
    }

    @Override
    @Valid
    public boolean existsByName(@ValidateStringParam String siteName) {
        return siteFeedMapper.existsByName(siteName) > 0;
    }

    @Override
    @Valid
    public int getSitesPerUserTotal()
            throws UserNotFoundException, ServiceLayerException {
        return getSitesPerUserTotal(securityService.getCurrentUser());
    }

    @Override
    @Valid
    public int getSitesPerUserTotal(@ValidateStringParam String username)
            throws UserNotFoundException, ServiceLayerException {
        if (securityService.userExists(username)) {
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            return siteFeedMapper.getSitesPerUserQueryTotal(params);
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    @Valid
    public List<SiteFeed> getSitesPerUser(int start,
                                          int number)
            throws UserNotFoundException, ServiceLayerException {
        return getSitesPerUser(securityService.getCurrentUser(), start, number);
    }

    @Override
    @Valid
    public List<SiteFeed> getSitesPerUser(@ValidateStringParam String username,
                                          int start,
                                          int number)
            throws UserNotFoundException, ServiceLayerException {
        if (securityService.userExists(username)) {
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("start", start);
            params.put("number", number);
            List<String> siteIds = siteFeedMapper.getSitesPerUserQuery(params);
            List<SiteFeed> toRet = new ArrayList<>();
            if (siteIds != null && !siteIds.isEmpty()) {
                params = new HashMap<>();
                params.put("siteids", siteIds);
                toRet = siteFeedMapper.getSitesPerUserData(params);
            }
            return toRet;
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    @Valid
    public SiteFeed getSite(@ValidateStringParam String siteId) throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<>();
            params.put("siteId", siteId);
            return siteFeedMapper.getSite(params);
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    public SiteDetails getSiteDetails(@SiteId String siteId) throws ServiceLayerException {
        checkSiteExists(siteId);
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", siteId);

        List<BlobStoreDetails> storeDetails = Collections.emptyList();
        if (!studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false)) {
            String configLocation = studioConfiguration.getProperty(BLOB_STORES_CONFIG_PATH);
            HierarchicalConfiguration<?> xmlConfiguration = configurationService.getXmlConfiguration(siteId, MODULE_STUDIO, configLocation);
            storeDetails = getBlobStoreDetails(xmlConfiguration);
        }
        return new SiteDetails(siteFeedMapper.getSite(params), storeDetails);
    }

    /**
     * Reads blob-stores-config.xml and returns a list of BlobStoreDetails
     * @param xmlConfiguration the blob-stores-config.xml configuration
     * @return a list of BlobStoreDetails
     */
    @NotNull
    private static List<BlobStoreDetails> getBlobStoreDetails(HierarchicalConfiguration<?> xmlConfiguration) {
        if (xmlConfiguration == null) {
            return Collections.emptyList();
        }

        List<? extends HierarchicalConfiguration<?>> blobStores = xmlConfiguration.configurationsAt(CONFIG_KEY_STORE);
        return blobStores.stream().map(store -> {
            String id = store.getString(CONFIG_KEY_ID);
            String type = store.getString(CONFIG_KEY_TYPE);
            String pattern = store.getString(CONFIG_KEY_PATTERN);
            boolean readOnly = store.getBoolean(CONFIG_KEY_READ_ONLY, false);

            List<BlobStoreDetails.Mapping> mappings = store.configurationsAt(CONFIG_KEY_MAPPING).stream()
                    .map(mapping -> {
                        String publishingTarget = mapping.getString(CONFIG_KEY_MAPPING_PUBLISHING_TARGET);
                        String storeTarget = mapping.getString(CONFIG_KEY_MAPPING_STORE_TARGET);
                        String prefix = mapping.getString(CONFIG_KEY_MAPPING_PREFIX);
                        return new BlobStoreDetails.Mapping(publishingTarget, storeTarget, prefix);
                    }).collect(Collectors.toList());

            BlobStoreDetails details = new BlobStoreDetails();
            details.setId(id);
            details.setPattern(pattern);
            details.setType(type);
            details.setReadOnly(readOnly);
            details.setMappings(mappings);

            return details;
        }).collect(Collectors.toList());
    }

    @Override
    @Valid
    public boolean isPublishingEnabled(@ValidateStringParam String siteId) {
        try {
            SiteFeed siteFeed = getSite(siteId);
            return siteFeed.getPublishingEnabled() > 0;
        } catch (SiteNotFoundException e) {
            logger.warn("Failed to check if publishing is enabled for Site '{}'. Site not found.",
                    siteId, e);
            return false;
        }
    }

    @Override
    @Valid
    public boolean enablePublishing(@ValidateStringParam String siteId, boolean enabled)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<>();
            params.put("siteId", siteId);
            params.put("enabled", enabled ? 1 : 0);
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.enablePublishing(params));
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    @Valid
    public boolean updatePublishingStatus(@ValidateStringParam String siteId,
                                          @ValidateStringParam String status)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updatePublishingStatus(siteId, status));
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    public boolean addRemote(String siteId, String remoteName, String remoteUrl,
                             String authenticationType, String remoteUsername, String remotePassword,
                             String remoteToken, String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException {
        if (!exists(siteId)) {
            throw new SiteNotFoundException();
        }
        boolean toRet = contentRepository.addRemote(siteId, remoteName, remoteUrl, authenticationType, remoteUsername,
                remotePassword, remoteToken, remotePrivateKey);
        insertAddRemoteAuditLog(siteId, remoteName);
        return toRet;
    }

    private void insertAddRemoteAuditLog(String siteId, String remoteName) throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(siteId);
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_ADD_REMOTE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(remoteName);
        auditLog.setPrimaryTargetType(TARGET_TYPE_REMOTE_REPOSITORY);
        auditLog.setPrimaryTargetValue(remoteName);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) throws SiteNotFoundException {
        if (!exists(siteId)) {
            throw new SiteNotFoundException();
        }
        boolean toRet = contentRepositoryV2.removeRemote(siteId, remoteName);
        insertRemoveRemoteAuditLog(siteId, remoteName);
        return toRet;
    }

    private void insertRemoveRemoteAuditLog(String siteId, String remoteName) throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(siteId);
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_REMOVE_REMOTE);
        auditLog.setActorId(user);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(remoteName);
        auditLog.setPrimaryTargetType(TARGET_TYPE_REMOTE_REPOSITORY);
        auditLog.setPrimaryTargetValue(remoteName);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId) throws ServiceLayerException, CryptoException {
        if (!exists(siteId)) {
            throw new SiteNotFoundException();
        }
        SiteFeed siteFeed = getSite(siteId);
        return contentRepository.listRemote(siteId, siteFeed.getSandboxBranch());
    }

    @Override
    public List<SiteFeed> getDeletedSites() {
        return siteFeedMapper.getDeletedSites();
    }

    private void addSiteUuidFile(String site, String siteUuid) throws IOException {
        Path path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                StudioConstants.SITE_UUID_FILENAME);
        String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
        Files.write(path, toWrite.getBytes());
    }

    @Override
    public String getLastCommitId(String siteId) {
        return siteFeedMapper.getLastCommitId(siteId);
    }

    @Override
    public String getLastVerifiedGitlogCommitId(String siteId) {
        return siteFeedMapper.getLastVerifiedGitlogCommitId(siteId);
    }

    @Override
    public List<String> getAllCreatedSites() {
        return siteFeedMapper.getAllCreatedSites(STATE_READY);
    }

    @Override
    public void setSiteState(String siteId, String state) {
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(siteId, state));
    }

    @Override
    public String getSiteState(String siteId) {
        return siteFeedMapper.getSiteState(siteId);
    }

    @Override
    public boolean isPublishedRepoCreated(String siteId) {
        return siteFeedMapper.getPublishedRepoCreated(siteId) > 0;
    }

    @Override
    public void setPublishedRepoCreated(String siteId) {
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setPublishedRepoCreated(siteId));
    }

    @Override
    public String getLastSyncedGitlogCommitId(String siteId) {
        return siteFeedMapper.getLastSyncedGitlogCommitId(siteId);
    }

    public List<String> getDefaultGroups() {
        return Arrays.asList(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_GROUPS).split(","));
    }

    @Override
    public boolean checkSiteUuid(final String siteId, final String siteUuid) {
        try {
            Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH), siteId, SITE_UUID_FILENAME);
            return Files.readAllLines(path).stream()
                    .anyMatch(siteUuid::equals);
        } catch (IOException e) {
            logger.info("Invalid site UUID in site '{}'", siteId);
            return false;
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setContentRepository(org.craftercms.studio.api.v1.repository.ContentRepository repo) {
        contentRepository = repo;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) {
        this.dmPageNavigationOrderService = dmPageNavigationOrderService;
    }

    public void setRebuildRepositoryMetadata(RebuildRepositoryMetadata rebuildRepositoryMetadata) {
        this.rebuildRepositoryMetadata = rebuildRepositoryMetadata;
    }

    public void setSyncDatabaseWithRepository(SyncDatabaseWithRepository syncDatabaseWithRepository) {
        this.syncDatabaseWithRepository = syncDatabaseWithRepository;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setUpgradeManager(final StudioUpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    public void setSitesServiceInternal(SitesService sitesServiceInternal) {
        this.sitesServiceInternal = sitesServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setConfigurationServiceInternal(final ConfigurationService configurationServiceInternal) {
        this.configurationServiceInternal = configurationServiceInternal;
    }

    public void setContentRepositoryV2(ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setConfigurationPatterns(String[] configurationPatterns) {
        this.configurationPatterns = configurationPatterns;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setStudioDBScriptRunner(StudioDBScriptRunnerFactory studioDBScriptRunner) {
        this.studioDBScriptRunnerFactory = studioDBScriptRunner;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }
}
