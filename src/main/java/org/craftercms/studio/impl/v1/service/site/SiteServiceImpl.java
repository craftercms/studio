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

package org.craftercms.studio.impl.v1.service.site;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.collections4.CollectionUtils;
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
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.plugin.model.SearchEngines;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.DeployerTargetException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteCreationException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.content.ImportService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunner;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunnerFactory;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.event.site.SiteEvent;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.upgrade.StudioUpgradeManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.repository.job.RebuildRepositoryMetadata;
import org.craftercms.studio.impl.v1.repository.job.SyncDatabaseWithRepository;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v1.constant.DmConstants.ROOT_PATTERN_ASSETS;
import static org.craftercms.studio.api.v1.constant.DmConstants.ROOT_PATTERN_PAGES;
import static org.craftercms.studio.api.v1.constant.DmConstants.XML_PATTERN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_DEFAULT_GROUPS_DESCRIPTION;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_CONTENT_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_DISABLED;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_INTERNAL_TITLE;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_DELETED;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ADD_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REMOVE_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_BLUEPRINT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.dal.ItemState.DISABLED;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteItemRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.insertItemRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteDependencyRows;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteDependencySourcePathRows;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.insertDependencyRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.moveItemRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.updateItemRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.updateParentId;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_GROUPS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v2.utils.PluginUtils.validatePluginParameters;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 *
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

    protected Deployer deployer;
    protected SiteServiceDAL _siteServiceDAL;
    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected org.craftercms.studio.api.v1.repository.ContentRepository contentRepository;
    protected ContentRepository contentRepositoryV2;
    protected DependencyService dependencyService;
    protected SecurityService securityService;
    protected DeploymentService deploymentService;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected ImportService importService;
    protected GeneralLockService generalLockService;
    protected RebuildRepositoryMetadata rebuildRepositoryMetadata;
    protected SyncDatabaseWithRepository syncDatabaseWithRepository;
    protected GroupServiceInternal groupServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected StudioUpgradeManager upgradeManager;
    protected StudioConfiguration studioConfiguration;
    protected SitesServiceInternal sitesServiceInternal;
    protected AuditServiceInternal auditServiceInternal;
    protected ConfigurationService configurationService;
    protected ItemServiceInternal itemServiceInternal;
    protected StudioClusterUtils studioClusterUtils;
    protected ClusterDAO clusterDao;
    protected UserDAO userDao;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected ApplicationContext applicationContext;

    @Autowired
    protected SiteFeedMapper siteFeedMapper;

    protected EntitlementValidator entitlementValidator;

    protected String[] configurationPatterns;

    protected StudioDBScriptRunnerFactory studioDBScriptRunnerFactory;
    protected DependencyServiceInternal dependencyServiceInternal;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    /**
     * given a site ID return the configuration as a document
     * This method allows extensions to add additional properties to the configuration that
     * are not made available through the site configuration object
     *
     * @param site the name of the site
     * @return a Document containing the entire site configuration
     */
    @Override
    @ValidateParams
    public Document getSiteConfiguration(@ValidateStringParam(name = "site") String site)
            throws SiteConfigNotFoundException {
        return _siteServiceDAL.getSiteConfiguration(site);
    }

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
    @ValidateParams
    public void createSiteFromBlueprint(
            @ValidateStringParam(name = "blueprintId") String blueprintId,
            @ValidateStringParam(name = "siteId", maxLength = 50, whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
            @ValidateNoTagsParam(name = "siteName") String siteName,
            @ValidateStringParam(name = "sandboxBranch") String sandboxBranch,
            @ValidateNoTagsParam(name = "desc") String desc,
            Map<String, String> params, boolean createAsOrphan)
            throws SiteAlreadyExistsException, SiteCreationException, DeployerTargetException,
            BlueprintNotFoundException, MissingPluginParameterException {
        if (exists(siteId) || existsByName(siteName)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Get blueprint descriptor for: " + blueprintId);
        PluginDescriptor descriptor = sitesServiceInternal.getBlueprintDescriptor(blueprintId);
        if (Objects.isNull(descriptor)) {
            throw new BlueprintNotFoundException("Blueprint not found " + blueprintId);
        }

        logger.debug("Validating blueprint parameters");
        validatePluginParameters(descriptor.getPlugin(), params);

        String blueprintLocation = sitesServiceInternal.getBlueprintLocation(blueprintId);

        logger.debug("Validate site entitlements");
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
        } catch (EntitlementException e) {
            throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact " +
                    "your system administrator.", e);
        }

        logger.info("Starting site creation process for site " + siteName + " from " + blueprintId + " blueprint.");
        boolean success = true;

        // We must fail site creation if any of the site creations steps fail and rollback
        // For example: Create site => create Deployer Target (fail) = fail
        // and rollback the whole thing.
        // What we need to do for site creation and the order of execution:
        // 1) deployer target, 2) git repo, 3) database, 4) kick deployer
        String siteUuid = UUID.randomUUID().toString();

        // Create the site in the preview deployer
        logger.info("Creating deployer targets.");
        try {
            deployer.createTargets(siteId);
        } catch (Exception e) {
            success = false;
            String msg = "Error while creating site: " + siteName + " ID: " + siteId + " from blueprint: " +
                    blueprintId + ". The required Deployer targets couldn't be created";

            logger.error(msg, e);

            throw new DeployerTargetException(msg, e);
        }

        if (success) {
            try {
                logger.info("Copying site content from blueprint.");
                success = createSiteFromBlueprintGit(blueprintLocation, siteId, sandboxBranch, params);
                ZonedDateTime now = DateUtils.getCurrentTime();

                logger.debug("Adding site UUID.");
                addSiteUuidFile(siteId, siteUuid);

                logger.info("Adding site record to database for site " + siteId);
                // insert database records
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteName);
                siteFeed.setSiteId(siteId);
                siteFeed.setSiteUuid(siteUuid);
                siteFeed.setDescription(desc);
                siteFeed.setPublishingStatus(READY);
                siteFeed.setSandboxBranch(sandboxBranch);
                retryingDatabaseOperationFacade.createSite(siteFeed);

                logger.info("Upgrading site.");
                upgradeManager.upgrade(siteId);

                // Add default groups
                logger.info("Adding default groups");
                addDefaultGroupsForNewSite(siteId);

                String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);
                String creator = securityService.getCurrentUser();

                long startGetChangeSetCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
                Map<String, String> createdFiles =
                        contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Get change set created files finished in " +
                            (System.currentTimeMillis() - startGetChangeSetCreatedFilesMark) + " milliseconds");
                }

                logger.info("Adding audit log");
                insertCreateSiteAuditLog(siteId, siteName, blueprintId);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                contentRepositoryV2.insertGitLog(siteId, lastCommitId, 1, 1);
                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, lastCommitId);

                logger.info("Reload site configuration");
            } catch (Exception e) {
                success = false;
                logger.error("Error while creating site: " + siteName + " ID: " + siteId + " from blueprint: " +
                        blueprintId + ". Rolling back.", e);

                deleteSite(siteId);

                throw new SiteCreationException("Error while creating site: " + siteName + " ID: " + siteId +
                        " from blueprint: " + blueprintId + ". Rolling back.");
            }
        }

        if (success) {
            logger.info("Syncing all content to preview.");
            // Now that everything is created, we can sync the preview deployer with the new content
            try {
                applicationContext.publishEvent(new SiteEvent(securityService.getAuthentication(), siteId));
            } catch (Exception e) {
                logger.error("Error while syncing site: " + siteName + " ID: " + siteId + " to preview. Site was "
                        + "successfully created otherwise. Ignoring.", e);

                throw new SiteCreationException("Error while syncing site: " + siteName + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the Preview " +
                        "Deployer is reachable.");
            }
            setSiteState(siteId, STATE_READY);
        } else {
            throw new SiteCreationException("Error while creating site: " + siteName + " ID: " + siteId + ".");
        }
        logger.info("Finished creating site " + siteId);
    }

    private void insertCreateSiteAuditLog(String siteId, String siteName, String blueprint) throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteName);
        List<AuditLogParameter> auditLogParameters = new ArrayList<AuditLogParameter>();
        AuditLogParameter auditLogParameter = new AuditLogParameter();
        auditLogParameter.setTargetId(siteId + ":" + blueprint);
        auditLogParameter.setTargetType(TARGET_TYPE_BLUEPRINT);
        auditLogParameter.setTargetValue(blueprint);
        auditLogParameters.add(auditLogParameter);

        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void processCreatedFiles(String siteId, Map<String, String> createdFiles, String creator,
                                     ZonedDateTime now, String lastCommitId) {
        long startProcessCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        SiteFeed siteFeed = null;
        try {
            siteFeed = getSite(siteId);
        } catch (SiteNotFoundException e) {
            logger.error("Unexpected error during creation of items. Site not found " + siteId, e);
            return;
        }
        User userObj = null;
        try {
            userObj = userServiceInternal.getUserByGitName(creator);
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Unexpected error during creation of items. User not found " + creator, e);
            return;
        }

        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();

        try {
            String createdFileScriptFilename = "createdFiles_" + UUID.randomUUID();
            Path createdFileScriptPath = Files.createTempFile(createdFileScriptFilename, ".sql");
            String updateParentIdScriptFilename = "updateParentId_" + UUID.randomUUID();
            Path updateParentIdScriptPath = Files.createTempFile(updateParentIdScriptFilename, ".sql");
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
                            if (StringUtils.isNotEmpty(internalName)) {
                                label = internalName;
                            }
                            contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                            disabled = Boolean.valueOf(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                        }
                    } catch (DocumentException e) {
                        logger.error("Error extracting metadata from xml file " + siteId + ":" + path);
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
                logger.debug("Process created files finished in " +
                        (System.currentTimeMillis() - startProcessCreatedFilesMark) + " milliseconds");
            }
        } catch (IOException e) {
            logger.error("Error while creating db script file for processing created files for site " + siteId);
        }
    }

    private void processAncestors(long siteId, String path, long userId, ZonedDateTime now, String commitId,
                                  Path createFileScriptPath) throws IOException {
        Path p = Paths.get(path);
        List<Path> parts = new LinkedList<>();
        if (Objects.nonNull(p.getParent())) {
            p.getParent().iterator().forEachRemaining(parts::add);
        }
        String currentPath = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                if (StringUtils.isNotEmpty(ancestor.toString())) {
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
                FilenameUtils.getPathNoEndSeparator(StringUtils.replace(path, "/index.xml", ""));
        if (StringUtils.isNotEmpty(parentPath) && !StringUtils.equals(parentPath, path)) {
            addUpdateParentIdScriptSnippets(siteId, parentPath, updateParentIdScriptPath);
            if (StringUtils.endsWith(path, "/index.xml")) {
                addUpdateParentIdScriptSnippets(siteId, StringUtils.replace(path, "/index.xml", ""), updateParentIdScriptPath);
            }
            Files.write(updateParentIdScriptPath, updateParentId(siteId, path, parentPath).getBytes(UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(updateParentIdScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
        }
    }

    private void addDependenciesScriptSnippets(String siteId, String path, String oldPath, Path file) throws IOException {
        long startDependencyResolver = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        Map<String, Set<String>> dependencies = dependencyServiceInternal.resolveDependnecies(siteId, path);
        if (logger.isDebugEnabled()) {
            logger.debug("Dependency resolver for " + path + " finished in " +
                    (System.currentTimeMillis() - startDependencyResolver) + " milliseconds");
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
        if (Objects.nonNull(dependencies) && !dependencies.isEmpty()) {
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
                                                 Map<String, String> params)
            throws Exception {
        boolean success = true;

        // create site with git repo
        success = contentRepositoryV2.createSiteFromBlueprint(blueprintLocation, siteId, sandboxBranch, params);

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

    private void addDefaultGroupsForNewSite(String siteId) {
        List<String> defaultGroups = getDefaultGroups();
        for (String group : defaultGroups) {
            String description = group + SITE_DEFAULT_GROUPS_DESCRIPTION;
            try {
                if (!groupServiceInternal.groupExists(-1, group)) {
                    try {
                        groupServiceInternal.createGroup(DEFAULT_ORGANIZATION_ID, group, description);
                    } catch (GroupAlreadyExistsException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    logger.warn("Default group: " + group + " not created. It already exists.");
                }
            } catch (ServiceLayerException e) {
                logger.warn("Error creating group " + group, e);
            }
        }
    }

    @Override
    @ValidateParams
    public void createSiteWithRemoteOption(
            @ValidateStringParam(name = "siteId", maxLength = 50, whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
            @ValidateStringParam(name = "siteName") String siteName,
            @ValidateStringParam(name = "sandboxBranch") String sandboxBranch,
            @ValidateNoTagsParam(name = "description") String description,
            String blueprintName,
            @ValidateStringParam(name = "remoteName") String remoteName,
            @ValidateStringParam(name = "remoteUrl") String remoteUrl,
            String remoteBranch, boolean singleBranch, String authenticationType,
            String remoteUsername, String remotePassword, String remoteToken,
            String remotePrivateKey,
            @ValidateStringParam(name = "createOption") String createOption,
            Map<String, String> params, boolean createAsOrphan)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, InvalidRemoteUrlException {
        if (exists(siteId) || existsByName(siteName)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Validate site entitlements");
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
        } catch (EntitlementException e) {
            throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
                    + "system administrator.", e);
        }

        if (REMOTE_REPOSITORY_CREATE_OPTION_CLONE.equals(createOption)) {
            logger.info("Clone from remote repository create option selected");
            createSiteCloneRemote(siteId, siteName, sandboxBranch, description, remoteName, remoteUrl, remoteBranch,
                    singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                    params, createAsOrphan);
        } else {
            logger.error("Invalid create option for create site using remote repository: " + createOption +
                    "\nAvailable options: [" + REMOTE_REPOSITORY_CREATE_OPTION_CLONE + "]");
            throw new SiteCreationException("Invalid create option for create site using remote repository: "
                    + createOption);
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
        boolean success = true;

        // We must fail site creation if any of the site creations steps fail and rollback
        // For example: Create site => create Deployer Target (fail) = fail
        // and rollback the whole thing.
        // What we need to do for site creation and the order of execution:
        // 1) git repo, 2) deployer target, 3) database, 4) kick deployer
        String siteUuid = UUID.randomUUID().toString();

        try {
            // create site by cloning remote git repo
            logger.info("Creating site " + siteId + " by cloning remote repository " + remoteName +
                    " (" + remoteUrl + ")");
            success = contentRepositoryV2.createSiteCloneRemote(siteId, sandboxBranch, remoteName, remoteUrl,
                    remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                    remotePrivateKey, params, createAsOrphan);

        } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
                RemoteRepositoryNotFoundException | InvalidRemoteUrlException | ServiceLayerException e) {

            contentRepository.deleteSite(siteId);

            logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from " +
                    "remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);

            throw e;
        }

        if (!success) {
            contentRepository.removeRemoteRepositoriesForSite(siteId);
            contentRepository.deleteSite(siteId);
            throw new ServiceLayerException("Failed to create site: " + siteId + " ID: " + siteId + " as clone from " +
                    "remote repository: " + remoteName + " (" + remoteUrl + ")");
        }

        // try to get the search engine from the blueprint descriptor file
        String searchEngine = studioConfiguration.getProperty(StudioConfiguration.PREVIEW_SEARCH_ENGINE);

        PluginDescriptor descriptor = sitesServiceInternal.getSiteBlueprintDescriptor(siteId);
        if (Objects.nonNull(descriptor) && Objects.nonNull(descriptor.getPlugin())) {
            searchEngine = descriptor.getPlugin().getSearchEngine();
            logger.info("Using search engine {0} from plugin descriptor", searchEngine);
        } else if (Objects.nonNull(descriptor) && Objects.nonNull(descriptor.getBlueprint())) {
            searchEngine = descriptor.getBlueprint().getSearchEngine();
            logger.info("Using search engine {0} from blueprint descriptor", searchEngine);
        } else {
            logger.info("Missing descriptor, using default search engine {0}", searchEngine);
        }

        if (StringUtils.equals(searchEngine, SearchEngines.CRAFTER_SEARCH)) {
            logger.error("Error creating site {0}, unsupported search engine CrafterSearch, please update your " +
                "site to use Elasticsearch. For more information see " +
                "https://docs.craftercms.org/en/4.0/developers/cook-books/how-tos/migrate-site-to-elasticsearch.html",
                siteId);

            // rollback ...
            contentRepositoryV2.removeRemote(siteId, remoteName);
            contentRepository.deleteSite(siteId);

            throw new SiteCreationException("Unsupported search engine CrafterSearch, please update your site to use " +
                "Elasticsearch. For more information see " +
                "https://docs.craftercms.org/en/4.0/developers/cook-books/how-tos/migrate-site-to-elasticsearch.html");
        }

        if (success) {
            // Create the site in the preview deployer
            try {
                logger.info("Creating Deployer targets for site " + siteId);
                deployer.createTargets(siteId);
            } catch (Exception e) {
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from" +
                        " remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back...", e);

                contentRepositoryV2.removeRemote(siteId, remoteName);
                boolean deleted = contentRepository.deleteSite(siteId);

                if (!deleted) {
                    logger.error("Error while rolling back site: " + siteId);
                }

                throw new DeployerTargetException("Error while creating site: " + siteId + " ID: " + siteId +
                        " as clone from remote repository: " + remoteName +
                        " (" + remoteUrl + "). The required Deployer targets couldn't " +
                        "be created", e);
            }
        }

        if (success) {
            ZonedDateTime now = DateUtils.getCurrentTime();
            String creator = securityService.getCurrentUser();
            try {
                logger.debug("Adding site UUID.");
                addSiteUuidFile(siteId, siteUuid);

                // insert database records
                logger.info("Adding site record to database for site " + siteId);
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteName);
                siteFeed.setSiteId(siteId);
                siteFeed.setSiteUuid(siteUuid);
                siteFeed.setDescription(description);
                siteFeed.setPublishingStatus(READY);
                siteFeed.setSandboxBranch(sandboxBranch);
                retryingDatabaseOperationFacade.createSite(siteFeed);

                upgradeManager.upgrade(siteId);


                // Add default groups
                logger.info("Adding default groups for site " + siteId);
                addDefaultGroupsForNewSite(siteId);

                String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);
                String firstCommitId = contentRepositoryV2.getRepoFirstCommitId(siteId);

                long startGetChangeSetCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L ;
                Map<String, String> createdFiles =
                        contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Get change set created files finished in " +
                            (System.currentTimeMillis() - startGetChangeSetCreatedFilesMark) + " milliseconds");
                }

                insertCreateSiteAuditLog(siteId, siteId, remoteName + "/" + remoteBranch);
                contentRepositoryV2.insertGitLog(siteId, firstCommitId, 1, 1);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, firstCommitId);

                logger.info("Loading configuration for site " + siteId);
            } catch (Exception e) {
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from " +
                        "remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);

                deleteSite(siteId);

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                        " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);
            }
        }

        if (success) {
            // Now that everything is created, we can sync the preview deployer with the new content
            logger.info("Sync all site content to preview for " + siteId);
            try {
                applicationContext.publishEvent(new SiteEvent(securityService.getAuthentication(), siteId));
            } catch (Exception e) {
                logger.error("Error while syncing site: " + siteId + " ID: " + siteId + " to preview. Site was "
                        + "successfully created otherwise. Ignoring.", e);

                throw new SiteCreationException("Error while syncing site: " + siteId + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the " +
                        "Preview Deployer is reachable.");
            }
            setSiteState(siteId, STATE_READY);
        } else {
            throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId + ".");
        }
        logger.info("Finished creating site " + siteId);
    }

    @Override
    @ValidateParams
    public boolean deleteSite(@ValidateStringParam(name = "siteId") String siteId) {
        boolean success = true;
        logger.debug("Deleting site:" + siteId);
        try {
            enablePublishing(siteId, false);
        } catch (SiteNotFoundException e) {
            success = false;
            logger.error("Failed to stop publishing for site:" + siteId, e);
        }

        try {
            logger.debug("Deleting Deployer targets");

            deployer.deleteTargets(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the Deployer target for sites:" + siteId, e);
        }

        try {
            success = success && destroySitePreviewContext(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to destroy the preview context for site:" + siteId, e);
        }

        try {
            logger.debug("Deleting repo");
            contentRepository.deleteSite(siteId);
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the repository for site:" + siteId, e);
        }

        // clear cache
        configurationService.invalidateConfiguration(siteId);

        try {
            // delete database records
            logger.debug("Deleting database records");
            SiteFeed siteFeed = getSite(siteId);
            workflowServiceInternal.deleteWorkflowEntriesForSite(siteFeed.getId());
            retryingDatabaseOperationFacade.deleteSite(siteId, STATE_DELETED);
            retryingDatabaseOperationFacade.deleteUserPropertiesBySiteId(siteFeed.getId());
            dependencyService.deleteSiteDependencies(siteId);
            deploymentService.deleteDeploymentDataForSite(siteId);
            itemServiceInternal.deleteItemsForSite(siteFeed.getId());
            dmPageNavigationOrderService.deleteSequencesForSite(siteId);
            contentRepository.deleteGitLogForSite(siteId);
            contentRepository.removeRemoteRepositoriesForSite(siteId);
            auditServiceInternal.deleteAuditLogForSite(siteFeed.getId());
            insertDeleteSiteAuditLog(siteId, siteFeed.getName());
        } catch (Exception e) {
            success = false;
            logger.error("Failed to delete the database for site:" + siteId, e);
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

        CloseableHttpClient client = HttpClientBuilder.create().build();
        try {
            CloseableHttpResponse response = client.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending destroy preview context request for site " + site, e);
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
        List<SiteBlueprintTO> blueprints = new ArrayList<SiteBlueprintTO>();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                SiteBlueprintTO blueprintTO = new SiteBlueprintTO();
                blueprintTO.id = folder.name;
                blueprintTO.label = StringUtils.capitalize(folder.name);
                blueprintTO.description = ""; // How do we populate this dynamicly
                blueprintTO.screenshots = null;
                blueprints.add(blueprintTO);
            }
        }

        return blueprints.toArray(new SiteBlueprintTO[0]);
    }

    @Override
    @ValidateParams
    public void syncRepository(@ValidateStringParam(name = "site") String site) throws SiteNotFoundException {
        if (!exists(site)) {
            throw new SiteNotFoundException();
        } else {
            String lastDbCommitId =
                    siteFeedMapper.getLastCommitId(site, studioClusterUtils.getClusterNodeLocalAddress());
            if (lastDbCommitId != null) {
                syncDatabaseWithRepository.execute(site, lastDbCommitId);
            } else {
                rebuildDatabase(site);
            }
        }
    }

    @Override
    @ValidateParams
    public void rebuildDatabase(@ValidateStringParam(name = "site") String site) {
        rebuildRepositoryMetadata.execute(site);
    }


    @Override
    @ValidateParams
    public void updateLastCommitId(@ValidateStringParam(name = "site") String site,
                                   @ValidateStringParam(name = "commitId") String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("lastCommitId", commitId);
        retryingDatabaseOperationFacade.updateSiteLastCommitId(params);
    }

    @Override
    public void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingDatabaseOperationFacade.updateSiteLastVerifiedGitlogCommitId(params);
    }

    @Override
    public void updateLastSyncedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingDatabaseOperationFacade.updateSiteLastSyncedGitlogCommitId(params);
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId)
            throws ServiceLayerException, UserNotFoundException {
        return syncDatabaseWithRepo(site, fromCommitId, true);
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId,
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
            logger.debug("Get Repo Operations from Delta finished in " +
                    (System.currentTimeMillis() - startGetOperationsFromDeltaMark) + " milliseconds");
            logger.debug("Number of Repo operations from delta " + repoOperationsDelta.size());
        }
        if (CollectionUtils.isEmpty(repoOperationsDelta)) {
            logger.debug("Database is up to date with repository for site: " + site);
            contentRepositoryV2.markGitLogVerifiedProcessed(site, fromCommitId);
            updateLastCommitId(site, repoLastCommitId);
            updateLastVerifiedGitlogCommitId(site, repoLastCommitId);
            return toReturn;
        }

        logger.info("Syncing database with repository for site: " + site + " fromCommitId = " +
                (StringUtils.isEmpty(fromCommitId) ? "Empty repo" : fromCommitId));
        logger.debug("Operations to sync: ");
        for (RepoOperation repoOperation : repoOperationsDelta) {
            logger.debug("\tOperation: " + repoOperation.getAction().toString() + " " + repoOperation.getPath());
        }

        long startUpdateDBMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
        try {
            String repoOperationsScriptFilename = "repoOperations_" + UUID.randomUUID();
            Path repoOperationsScriptPath = Files.createTempFile(repoOperationsScriptFilename, ".sql");
            String updateParentIdScriptFilename = "updateParentId_" + UUID.randomUUID();
            Path updateParentIdScriptPath = Files.createTempFile(updateParentIdScriptFilename, ".sql");
            toReturn = processRepoOperations(site, repoOperationsDelta, repoOperationsScriptPath,
                    updateParentIdScriptPath);
            studioDBScriptRunner.execute(repoOperationsScriptPath.toFile());
            studioDBScriptRunner.execute(updateParentIdScriptPath.toFile());
        } catch (IOException e) {
            logger.error("Error while creating DB script file for processing created files for site " + site);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Update DB finished in " + (System.currentTimeMillis() - startUpdateDBMark) + " milliseconds");
        }

        // At this point we have attempted to process all operations, some may have failed
        // We will update the lastCommitId of the database ignoring errors if any
        logger.debug("Done syncing operations with a result of: " + toReturn);
        logger.debug("Syncing database lastCommitId for site: " + site);

        // Update database
        logger.debug("Update last commit id " + repoLastCommitId + " for site " + site);
        updateLastCommitId(site, repoLastCommitId);
        updateLastVerifiedGitlogCommitId(site, repoLastCommitId);
        if (logger.isDebugEnabled()) {
            logger.debug("Update DB finished in " + (System.currentTimeMillis() - startUpdateDBMark) + " milliseconds");
        }

        logger.info("Done syncing database with repository for site: " + site + " fromCommitId = " +
                (StringUtils.isEmpty(fromCommitId) ? "Empty repo" : fromCommitId) + " with a final result of: " +
                toReturn);
        logger.info("Last commit ID for site: " + site + " is " + repoLastCommitId);

        if (!toReturn) {
            // Some operations failed during sync database from repo
            // Must log and make some noise here, this isn't great
            logger.error("Some operations failed to sync to database for site: " + site + " see previous error logs");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sync Repo finished in " + (System.currentTimeMillis() - startSyncRepoMark) + " milliseconds");
        }
        return toReturn;
    }

    private boolean processRepoOperations(String siteId, List<RepoOperation> repoOperations,
                                          Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException {
        boolean toReturn = true;
        long startProcessRepoOperationMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        SiteFeed siteFeed = null;
        try {
            siteFeed = getSite(siteId);
        } catch (SiteNotFoundException e) {
            logger.error("Unexpected error during creation of items. Site not found " + siteId, e);
            return false;
        }
        User userObj = null;
        Map<String, User> cachedUsers = new HashMap<String, User>();
        try {
            cachedUsers.put(GIT_REPO_USER_USERNAME, userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME));
        } catch (UserNotFoundException | ServiceLayerException e) {
            logger.error("Unexpected error. Git repo user should be in DB", e);
        }

        String label;
        String contentTypeId;
        String previewUrl;
        boolean disabled = false;
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
                            logger.debug("User not found " + repoOperation.getAuthor());
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
                                if (StringUtils.isNotEmpty(internalName)) {
                                    label = internalName;
                                }
                                contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                disabled = Boolean.valueOf(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                            }
                        } catch (DocumentException e) {
                            logger.error("Error extracting metadata from xml file " + siteId + ":" + repoOperation.getPath());
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
                        logger.debug("Extract dependencies for site: " + siteId + " path: " +
                                repoOperation.getPath());
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
                                logger.debug("User not found " + repoOperation.getAuthor());
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
                                    if (StringUtils.isNotEmpty(internalName)) {
                                        label = internalName;
                                    }
                                    contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                    disabled = Boolean.valueOf(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                                }
                            } catch (DocumentException e) {
                                logger.error("Error extracting metadata from xml file " + siteId + ":" + repoOperation.getPath());
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
                        logger.debug("Extract dependencies for site: " + siteId + " path: " +
                                repoOperation.getPath());
                        addDependenciesScriptSnippets(siteId, repoOperation.getPath(), null, repoOperationsScriptPath);
                    }
                    break;
                case DELETE:
                    Files.write(repoOperationsScriptPath,
                            deleteItemRow(siteFeed.getId(), repoOperation.getPath()).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    String folder = FILE_SEPARATOR + FilenameUtils.getPathNoEndSeparator(repoOperation.getPath());
                    if (!contentRepositoryV2.contentExists(siteId, folder)) {
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
                            logger.debug("User not found " + repoOperation.getAuthor());
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
                                if (StringUtils.isNotEmpty(internalName)) {
                                    label = internalName;
                                }
                                contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                                disabled = Boolean.valueOf(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
                            }
                        } catch (DocumentException e) {
                            logger.error("Error extracting metadata from xml file " + siteId + ":" + repoOperation.getMoveToPath());
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
                    break;

                default:
                    logger.error("Error: Unknown repo operation for site " + siteId + " operation: " +
                            repoOperation.getAction());
                    toReturn = false;
                    break;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Process Repo operations finished in " + (System.currentTimeMillis() - startProcessRepoOperationMark) +
                    " milliseconds");
        }
        return toReturn;
    }

    @Override
    @ValidateParams
    public boolean exists(@ValidateStringParam(name = "site") String site) {
        return siteFeedMapper.exists(site) > 0;
    }

    @Override
    @ValidateParams
    public boolean existsById(@ValidateStringParam(name = "siteId") String siteId) {
        return siteFeedMapper.existsById(siteId) > 0;
    }

    @Override
    @ValidateParams
    public boolean existsByName(@ValidateStringParam(name = "siteName") String siteName) {
        return siteFeedMapper.existsByName(siteName) > 0;
    }

    @Override
    @ValidateParams
    public int getSitesPerUserTotal()
            throws UserNotFoundException, ServiceLayerException {
        return getSitesPerUserTotal(securityService.getCurrentUser());
    }

    @Override
    @ValidateParams
    public int getSitesPerUserTotal(@ValidateStringParam(name = "username") String username)
            throws UserNotFoundException, ServiceLayerException {
        if (securityService.userExists(username)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            return siteFeedMapper.getSitesPerUserQueryTotal(params);
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public List<SiteFeed> getSitesPerUser(@ValidateIntegerParam(name = "start") int start,
                                          @ValidateIntegerParam(name = "number") int number)
            throws UserNotFoundException, ServiceLayerException {
        return getSitesPerUser(securityService.getCurrentUser(), start, number);
    }

    @Override
    @ValidateParams
    public List<SiteFeed> getSitesPerUser(@ValidateStringParam(name = "username") String username,
                                          @ValidateIntegerParam(name = "start") int start,
                                          @ValidateIntegerParam(name = "number") int number)
            throws UserNotFoundException, ServiceLayerException {
        if (securityService.userExists(username)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            params.put("start", start);
            params.put("number", number);
            List<String> siteIds = siteFeedMapper.getSitesPerUserQuery(params);
            List<SiteFeed> toRet = new ArrayList<SiteFeed>();
            if (siteIds != null && !siteIds.isEmpty()) {
                params = new HashMap<String, Object>();
                params.put("siteids", siteIds);
                toRet = siteFeedMapper.getSitesPerUserData(params);
            }
            return toRet;
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public SiteFeed getSite(@ValidateStringParam(name = "siteId") String siteId) throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            return siteFeedMapper.getSite(params);
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public boolean isPublishingEnabled(@ValidateStringParam(name = "siteId") String siteId) {
        try {
            SiteFeed siteFeed = getSite(siteId);
            return siteFeed.getPublishingEnabled() > 0;
        } catch (SiteNotFoundException e) {
            logger.debug("Site " + siteId + " not found. Publishing disabled");
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean enablePublishing(@ValidateStringParam(name = "siteId") String siteId, boolean enabled)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            params.put("enabled", enabled ? 1 : 0);
            retryingDatabaseOperationFacade.enableSitePublishing(params);
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public boolean updatePublishingStatus(@ValidateStringParam(name = "siteId") String siteId,
                                          @ValidateStringParam(name = "status") String status)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            retryingDatabaseOperationFacade.updateSitePublishingStatus(siteId, status);
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
    public boolean tryLockPublishingForSite(String siteId, String lockOwnerId, int ttl) {
        logger.debug("Locking publishing for site " + siteId + " with lock owner " + lockOwnerId);
        int result = siteFeedMapper.tryLockPublishingForSite(siteId, lockOwnerId, ttl);
        if (result == 1) {
            logger.debug("Locked publishing for site " + siteId + " with lock owner " + lockOwnerId);
        } else {
            logger.debug("Failed to publishing for site " + siteId + " with lock owner " + lockOwnerId);
        }
        return result == 1;
    }

    @Override
    public boolean unlockPublishingForSite(String siteId, String lockOwnerId) {
        logger.debug("Unlocking publishing for site " + siteId);
        retryingDatabaseOperationFacade.unlockPublishingForSite(siteId, lockOwnerId);
        return true;
    }

    @Override
    public void updatePublishingLockHeartbeatForSite(String siteId) {
        logger.debug("Update publishing lock heartbeat for site " + siteId);
        retryingDatabaseOperationFacade.updatePublishingLockHeartbeatForSite(siteId);
    }

    @Override
    public String getLastCommitId(String siteId) {
        return siteFeedMapper.getLastCommitId(siteId, studioClusterUtils.getClusterNodeLocalAddress());
    }

    @Override
    public String getLastVerifiedGitlogCommitId(String siteId) {
        return siteFeedMapper.getLastVerifiedGitlogCommitId(siteId, studioClusterUtils.getClusterNodeLocalAddress());
    }

    @Override
    public List<String> getAllCreatedSites() {
        return siteFeedMapper.getAllCreatedSites(STATE_READY);
    }

    @Override
    public void setSiteState(String siteId, String state) {
        retryingDatabaseOperationFacade.setSiteState(siteId, state);
    }

    @Override
    public String getSiteState(String siteId) {
        return siteFeedMapper.getSiteState(siteId, studioClusterUtils.getClusterNodeLocalAddress());
    }

    @Override
    public boolean isPublishedRepoCreated(String siteId) {
        return siteFeedMapper.getPublishedRepoCreated(siteId, studioClusterUtils.getClusterNodeLocalAddress()) > 0;
    }

    @Override
    public void setPublishedRepoCreated(String siteId) {
        retryingDatabaseOperationFacade.setSitePublishedRepoCreated(siteId);
    }

    @Override
    public String getLastSyncedGitlogCommitId(String siteId) {
        return siteFeedMapper.getLastSyncedGitlogCommitId(siteId, studioClusterUtils.getClusterNodeLocalAddress());
    }

    public String getEnvironment() {
        return studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
    }

    public List<String> getDefaultGroups() {
        return Arrays.asList(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_GROUPS).split(","));
    }

    public String getDefaultAdminGroup() {
        return studioConfiguration.getProperty(CONFIGURATION_DEFAULT_ADMIN_GROUP);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * getter site service dal
     */
    public SiteServiceDAL getSiteService() {
        return _siteServiceDAL;
    }

    /**
     * setter site service dal
     */
    public void setSiteServiceDAL(SiteServiceDAL service) {
        _siteServiceDAL = service;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public org.craftercms.studio.api.v1.repository.ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(org.craftercms.studio.api.v1.repository.ContentRepository repo) {
        contentRepository = repo;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public DmPageNavigationOrderService getDmPageNavigationOrderService() {
        return dmPageNavigationOrderService;
    }

    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) {
        this.dmPageNavigationOrderService = dmPageNavigationOrderService;
    }

    public ImportService getImportService() {
        return importService;
    }

    public void setImportService(ImportService importService) {
        this.importService = importService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public RebuildRepositoryMetadata getRebuildRepositoryMetadata() {
        return rebuildRepositoryMetadata;
    }

    public void setRebuildRepositoryMetadata(RebuildRepositoryMetadata rebuildRepositoryMetadata) {
        this.rebuildRepositoryMetadata = rebuildRepositoryMetadata;
    }

    public SyncDatabaseWithRepository getSyncDatabaseWithRepository() {
        return syncDatabaseWithRepository;
    }

    public void setSyncDatabaseWithRepository(SyncDatabaseWithRepository syncDatabaseWithRepository) {
        this.syncDatabaseWithRepository = syncDatabaseWithRepository;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public Deployer getDeployer() {
        return deployer;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public GroupServiceInternal getGroupServiceInternal() {
        return groupServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setUpgradeManager(final StudioUpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    public SitesServiceInternal getSitesServiceInternal() {
        return sitesServiceInternal;
    }

    public void setSitesServiceInternal(SitesServiceInternal sitesServiceInternal) {
        this.sitesServiceInternal = sitesServiceInternal;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ContentRepository getContentRepositoryV2() {
        return contentRepositoryV2;
    }

    public void setContentRepositoryV2(ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public StudioClusterUtils getStudioClusterUtils() {
        return studioClusterUtils;
    }

    public void setStudioClusterUtils(StudioClusterUtils studioClusterUtils) {
        this.studioClusterUtils = studioClusterUtils;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public void setConfigurationPatterns(String[] configurationPatterns) {
        this.configurationPatterns = configurationPatterns;
    }

    public WorkflowServiceInternal getWorkflowServiceInternal() {
        return workflowServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public StudioDBScriptRunnerFactory getStudioDBScriptRunner() {
        return studioDBScriptRunnerFactory;
    }

    public void setStudioDBScriptRunner(StudioDBScriptRunnerFactory studioDBScriptRunner) {
        this.studioDBScriptRunnerFactory = studioDBScriptRunner;
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
