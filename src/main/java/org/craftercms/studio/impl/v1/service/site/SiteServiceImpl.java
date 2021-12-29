/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
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
import org.craftercms.commons.rest.RestServiceException;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.DeployerTargetException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteCreationException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.content.ImportService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import org.craftercms.studio.api.v1.to.SiteTO;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunner;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunnerFactory;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.job.RebuildRepositoryMetadata;
import org.craftercms.studio.impl.v1.repository.job.SyncDatabaseWithRepository;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIG_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_PUSH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_DEFAULT_GROUPS_DESCRIPTION;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_CREATED;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ADD_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REMOVE_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteDependencyRows;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteDependencySourcePathRows;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteItemMetadataRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.deleteItemStateRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.insertDependencyRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.moveItemMetadataRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.moveItemStateRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.transitionSaveItemStateRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.updateItemMetadataRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.upsertItemMetadataRow;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.upsertItemStateRow;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_GROUPS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.PREVIOUS_COMMIT_SUFFIX;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService {

	private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

    protected Deployer deployer;
    protected SiteServiceDAL _siteServiceDAL;
    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
    protected org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2;
    protected ObjectStateService objectStateService;
    protected DependencyService dependencyService;
    protected SecurityService securityService;
    protected DeploymentService deploymentService;
    protected ObjectMetadataManager objectMetadataManager;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected ContentTypeService contentTypeService;
    protected ImportService importService;
    protected NotificationService notificationService;
    protected GeneralLockService generalLockService;
    protected RebuildRepositoryMetadata rebuildRepositoryMetadata;
    protected SyncDatabaseWithRepository syncDatabaseWithRepository;
    protected EventService eventService;
    protected GroupServiceInternal groupServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected UpgradeManager upgradeManager;
    protected StudioConfiguration studioConfiguration;
    protected SitesServiceInternal sitesServiceInternal;
    protected AuditServiceInternal auditServiceInternal;
    protected ConfigurationService configurationService;
    protected StudioClusterUtils studioClusterUtils;
    protected ClusterDAO clusterDao;
    @Autowired
    protected SiteFeedMapper siteFeedMapper;

    protected EntitlementValidator entitlementValidator;

    protected StudioDBScriptRunnerFactory studioDBScriptRunnerFactory;
    protected DependencyServiceInternal dependencyServiceInternal;
    protected RetryingOperationFacade retryingOperationFacade;

    @Override
    @ValidateParams
    public boolean writeConfiguration(@ValidateStringParam(name = "site") String site,
                                      @ValidateSecurePathParam(name = "path") String path, InputStream content)
            throws ServiceLayerException {
        // Write site configuration
        String operation = OPERATION_UPDATE;
        if (!contentRepository.contentExists(site, path)) {
            operation = OPERATION_CREATE;
        }
        String commitId = contentRepository.writeContent(site, path, content);
        contentRepository.reloadRepository(site);

        PreviewEventContext context = new PreviewEventContext();
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);

        String user = securityService.getCurrentUser();
        Map<String, String> extraInfo = new HashMap<String, String>();
        if (StringUtils.startsWith(path, contentTypeService.getConfigPath())) {
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_CONTENT_TYPE);
        } else {
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_CONFIGURATION);
        }
        SiteFeed siteFeed = getSite(site);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(site + ":" + path);
        auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
        auditLog.setPrimaryTargetValue(path);
        auditServiceInternal.insertAuditLog(auditLog);
        objectStateService.transition(site, path, TransitionEvent.SAVE);
        if (!objectMetadataManager.metadataExist(site, path)) {
            objectMetadataManager.insertNewObjectMetadata(site, path);
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(ItemMetadata.PROP_NAME, FilenameUtils.getName(path));
        properties.put(ItemMetadata.PROP_MODIFIED, ZonedDateTime.now(ZoneOffset.UTC));
        properties.put(ItemMetadata.PROP_MODIFIER, user);
        objectMetadataManager.setObjectMetadata(site, path, properties);

        if (commitId != null) {
            objectMetadataManager.updateCommitId(site, path, commitId);
            contentRepositoryV2.insertGitLog(site, commitId, 1);
        }
        boolean toRet = StringUtils.isEmpty(commitId);

        return toRet;
    }

	@Override
    @ValidateParams
	public boolean writeConfiguration(@ValidateSecurePathParam(name = "path") String path, InputStream content)
            throws ServiceLayerException {
	    // Write global configuration
        String commitId = contentRepository.writeContent("", path, content);
        boolean toReturn = StringUtils.isEmpty(commitId);
        return toReturn;
	}

	@Override
    @ValidateParams
	public Map<String, Object> getConfiguration(@ValidateSecurePathParam(name = "path") String path) {
		return null;
	}


	/**
	 * given a site ID return the configuration as a document
	 * This method allows extensions to add additional properties to the configuration that
	 * are not made available through the site configuration object
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
    @ValidateParams
	public Map<String, Object> getConfiguration(@ValidateStringParam(name = "site") String site,
                                                @ValidateSecurePathParam(name = "path") String path,
                                                boolean applyEnv) {
		String configPath;
        String configContent;
		if (StringUtils.isEmpty(site)) {
			configPath = getGlobalConfigRoot() + path;
			configContent = contentService.getContentAsString(site, configPath);
		} else {
		    if (path.startsWith(FILE_SEPARATOR + CONTENT_TYPE_CONFIG_FOLDER + FILE_SEPARATOR)) {
                configPath = getSitesConfigPath() + path;
                configContent = contentService.getContentAsString(site, configPath);
            } else {
		        configContent = configurationService.getConfigurationAsString(site, MODULE_STUDIO, path,
                        studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            }
		}


		Map<String, Object> toRet = null;
		if (configContent != null) {
            configContent = configContent.replaceAll("\"\\n([\\s]+)?+", "\" ");
			configContent = configContent.replaceAll("\\n([\\s]+)?+", "");
			configContent = configContent.replaceAll("<!--(.*?)-->", "");
			toRet = convertNodesFromXml(configContent);
		}
		return toRet;
	}

	private Map<String, Object> convertNodesFromXml(String xml) {
		try {
			Document document = DocumentHelper.parseText(xml);
			return createMap(document.getRootElement());

		} catch (DocumentException e) {
            logger.error("Error reading xml string:\n" + xml);
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	private  Map<String, Object> createMap(Element element) {
		Map<String, Object> map = new HashMap<String, Object>();
		for ( int i = 0, size = element.nodeCount(); i < size; i++ ) {
			Node currentNode = element.node(i);
			if ( currentNode instanceof Element ) {
				Element currentElement = (Element)currentNode;
				String key = currentElement.getName();
				Object toAdd = null;
				if (currentElement.isTextOnly()) {
					 toAdd = currentElement.getStringValue();
				} else {
					toAdd = createMap(currentElement);
				}
				if (map.containsKey(key)) {
					Object value = map.get(key);
					List listOfValues = new ArrayList<Object>();
					if (value instanceof List) {
						listOfValues = (List<Object>)value;
					} else {
						listOfValues.add(value);
					}
					listOfValues.add(toAdd);
					map.put(key, listOfValues);
				} else {
					map.put(key, toAdd);
				}
			}
		}
		return map;
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
            @ValidateNoTagsParam(name = "siteName") String siteName,
            @ValidateStringParam(name = "siteId", maxLength = 50, whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
            @ValidateStringParam(name = "sandboxBranch") String sandboxBranch,
            @ValidateNoTagsParam(name = "desc") String desc,
            Map<String, String> params, boolean createAsOrphan)
            throws SiteAlreadyExistsException, SiteCreationException, DeployerTargetException,
            BlueprintNotFoundException, MissingPluginParameterException {
	    if (exists(siteId)) {
	        throw new SiteAlreadyExistsException();
        }

	    logger.debug("Get blueprint descriptor for: " + blueprintId);
        PluginDescriptor descriptor = sitesServiceInternal.getBlueprintDescriptor(blueprintId);
        if (Objects.isNull(descriptor)) {
            throw new BlueprintNotFoundException();
        }

        logger.debug("Validating blueprint parameters");
        sitesServiceInternal.validateBlueprintParameters(descriptor, params);
        String blueprintLocation = sitesServiceInternal.getBlueprintLocation(blueprintId);
        String searchEngine = descriptor.getPlugin().getSearchEngine();

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
            deployer.createTargets(siteId, searchEngine);
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
                success = createSiteFromBlueprintGit(blueprintLocation, siteName, siteId, sandboxBranch, desc, params);
                ZonedDateTime now = ZonedDateTime.now();

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
                siteFeed.setPublishingStatusMessage(
                        studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
                siteFeed.setSandboxBranch(sandboxBranch);
                siteFeed.setSearchEngine(searchEngine);
                siteFeedMapper.createSite(siteFeed);

                String localeAddress = studioClusterUtils.getClusterNodeLocalAddress();
                ClusterMember cm = clusterDao.getMemberByLocalAddress(localeAddress);
                if (Objects.nonNull(cm)) {
                    SiteFeed s = getSite(siteId);
                    clusterDao.insertClusterSiteSyncRepo(cm.getId(), s.getId(), null, null, null);
                }

                logger.info("Upgrading site.");
                upgradeManager.upgradeSite(siteId);

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
                insertCreateSiteAuditLog(siteId, siteName, createdFiles);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                contentRepositoryV2.insertGitLog(siteId, lastCommitId, 1, 1);
                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, lastCommitId);

                logger.info("Reload site configuration");
                reloadSiteConfiguration(siteId);
	        } catch(Exception e) {
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
			    deploymentService.syncAllContentToPreview(siteId, true);
		    } catch (ServiceLayerException e) {
			    logger.error("Error while syncing site: " + siteName + " ID: " + siteId + " to preview. Site was "
				    + "successfully created otherwise. Ignoring.", e);

			    throw new SiteCreationException("Error while syncing site: " + siteName + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the Preview " +
                        "Deployer is reachable.");
		    }
            setSiteState(siteId, STATE_CREATED);
	    } else {
		    throw new SiteCreationException("Error while creating site: " + siteName + " ID: " + siteId + ".");
	    }
	    logger.info("Finished creating site " + siteId);
    }

    private void insertCreateSiteAuditLog(String siteId, String siteName, Map<String, String> createdFiles)
            throws SiteNotFoundException {
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
        createdFiles.forEach((k, v) -> {
            String targetPath = k;
            if (StringUtils.length(v) > 1) {
                targetPath = v;
            }
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + targetPath);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(targetPath);
            auditLogParameters.add(auditLogParameter);
        });

        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void processCreatedFiles(String siteId, Map<String, String> createdFiles, String creator,
                                     ZonedDateTime now, String lastCommitId) {
        long startProcessCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
        try {
            String scriptFilename = "createdFiles_" + UUID.randomUUID();
            Path scriptPath = Files.createTempFile(scriptFilename, ".sql");
            for (String path : createdFiles.keySet()) {
                Files.write(scriptPath, upsertItemStateRow(siteId, path).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND);
                Files.write(scriptPath, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(scriptPath, upsertItemMetadataRow(siteId, path, creator, now, lastCommitId)
                        .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(scriptPath, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                addDependenciesScriptSnippets(siteId, path, null, scriptPath);
            }

            studioDBScriptRunner.execute(scriptPath.toFile());

            if (logger.isDebugEnabled()) {
                logger.debug("Process created files finished in " +
                        (System.currentTimeMillis() - startProcessCreatedFilesMark) + " milliseconds");
            }
        } catch (IOException e) {
            logger.error("Error while creating DB script file for processing created files for site " + siteId);
        }
    }

    private void addDependenciesScriptSnippets(String siteId, String path, String oldPath, Path file) throws IOException {
        long startDependencyResolver = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
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

    protected boolean createSiteFromBlueprintGit(String blueprintLocation, String siteName, String siteId,
                                                 String sandboxBranch, String desc, Map<String, String> params)
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
        String contentAsString = IOUtils.toString(content);

        contentAsString = contentAsString.replaceAll(find, replace);

        InputStream contentToWrite = IOUtils.toInputStream(contentAsString);

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
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, InvalidRemoteUrlException {
        if (exists(siteId)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Validate site entitlements");
		try {
			entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
		} catch (EntitlementException e) {
			throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
				+ "system administrator.", e);
		}

        switch (createOption) {
            case REMOTE_REPOSITORY_CREATE_OPTION_CLONE:
                logger.info("Clone from remote repository create option selected");
                createSiteCloneRemote(siteId, sandboxBranch, description, remoteName, remoteUrl, remoteBranch,
                        singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                        params, createAsOrphan);
                break;

            case REMOTE_REPOSITORY_CREATE_OPTION_PUSH:
                logger.info("Push to remote repository create option selected");
                createSitePushToRemote(siteId, sandboxBranch, description, blueprintName, remoteName, remoteUrl,
                        remoteBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                        remotePrivateKey, params, createAsOrphan);
                break;

            default:
                logger.error("Invalid create option for create site using remote repository: " + createOption +
                        "\nAvailable options: [" + REMOTE_REPOSITORY_CREATE_OPTION_CLONE + ", " +
                        REMOTE_REPOSITORY_CREATE_OPTION_PUSH + "]");
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private void createSiteCloneRemote(String siteId, String sandboxBranch, String description, String remoteName,
                                       String remoteUrl, String remoteBranch, boolean singleBranch,
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

        if (success) {
            // Create the site in the preview deployer
            try {
                logger.info("Creating Deployer targets for site " + siteId);
                deployer.createTargets(siteId, searchEngine);
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
            ZonedDateTime now = ZonedDateTime.now();
            String creator = securityService.getCurrentUser();
            try {
                logger.debug("Adding site UUID.");
                addSiteUuidFile(siteId, siteUuid);

                // insert database records
                logger.info("Adding site record to database for site " + siteId);
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteId);
                siteFeed.setSiteId(siteId);
                siteFeed.setSiteUuid(siteUuid);
                siteFeed.setDescription(description);
                siteFeed.setPublishingStatus(READY);
                siteFeed.setPublishingStatusMessage(
                        studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
                siteFeed.setSandboxBranch(sandboxBranch);
                siteFeed.setSearchEngine(searchEngine);
                siteFeedMapper.createSite(siteFeed);

                upgradeManager.upgradeSite(siteId);

                // Add default groups
                logger.info("Adding default groups for site " + siteId);
                addDefaultGroupsForNewSite(siteId);

                String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);
                String firstCommitId = contentRepositoryV2.getRepoFirstCommitId(siteId);

                long startGetChangeSetCreatedFilesMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
                Map<String, String> createdFiles =
                        contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Get change set created files finished in " +
                            (System.currentTimeMillis() - startGetChangeSetCreatedFilesMark) + " milliseconds");
                }

                insertCreateSiteAuditLog(siteId, siteId, createdFiles);
                contentRepositoryV2.insertGitLog(siteId, firstCommitId, 1, 1);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, firstCommitId);

                logger.info("Loading configuration for site " + siteId);
                reloadSiteConfiguration(siteId);
            } catch(Exception e) {
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from " +
                        "remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);

                deleteSite(siteId);

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                        " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.");
            }
        }

        if (success) {
            // Now that everything is created, we can sync the preview deployer with the new content
            logger.info("Sync all site content to preview for " + siteId);
            try {
                deploymentService.syncAllContentToPreview(siteId, true);
            } catch (ServiceLayerException e) {
                logger.error("Error while syncing site: " + siteId + " ID: " + siteId + " to preview. Site was "
                    + "successfully created otherwise. Ignoring.", e);

                throw new SiteCreationException("Error while syncing site: " + siteId + " ID: " + siteId +
                    " to preview. Site was successfully created, but it won't be preview-able until the " +
                    "Preview Deployer is reachable.");
            }
            setSiteState(siteId, STATE_CREATED);
        } else {
            throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId + ".");
        }
        logger.info("Finished creating site " + siteId);
    }

    private void createSitePushToRemote(String siteId, String sandboxBranch, String description, String blueprintId,
                                        String remoteName, String remoteUrl, String remoteBranch,
                                        String authenticationType, String remoteUsername, String remotePassword,
                                        String remoteToken, String remotePrivateKey, Map<String, String> params,
                                        boolean createAsOrphan)
            throws ServiceLayerException {
        if (exists(siteId)) {
            throw new SiteAlreadyExistsException();
        }

        logger.debug("Get blueprint descriptor for " + blueprintId);
        PluginDescriptor descriptor = sitesServiceInternal.getBlueprintDescriptor(blueprintId);
        if (Objects.isNull(descriptor)) {
            throw new BlueprintNotFoundException();
        }

        logger.debug("Validate blueprint parameters");
        sitesServiceInternal.validateBlueprintParameters(descriptor, params);
        String blueprintLocation = sitesServiceInternal.getBlueprintLocation(blueprintId);
        String searchEngine = descriptor.getPlugin().getSearchEngine();

        boolean success = true;

        // We must fail site creation if any of the site creations steps fail and rollback
        // For example: Create site => create Deployer Target (fail) = fail
        // and rollback the whole thing.
        // What we need to do for site creation and the order of execution:
        // 1) deployer target, 2) git repo, 3) database, 4) kick deployer
        String siteUuid = UUID.randomUUID().toString();

        logger.info("Starting site creation process for site " + siteId + " from " + blueprintId + " blueprint.");
        // Create the site in the preview deployer
        try {
            logger.info("Creating Deployer targets for site " + siteId);
            deployer.createTargets(siteId, searchEngine);
        } catch (RestServiceException e) {
            String msg = "Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                         blueprintId + ". The required Deployer targets couldn't be created";

            logger.error(msg, e);

            throw new DeployerTargetException(msg, e);
        }

        if (success) {
            try {
                logger.info("Creating site " + siteId + " from blueprint " + blueprintId);
                success = createSiteFromBlueprintGit(blueprintLocation, siteId, siteId, sandboxBranch, description,
                        params);

                addSiteUuidFile(siteId, siteUuid);

                // insert database records
                logger.info("Adding site record to database for site " + siteId);
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteId);
                siteFeed.setSiteId(siteId);
                siteFeed.setSiteUuid(siteUuid);
                siteFeed.setDescription(description);
                siteFeed.setPublishingStatus(READY);
                siteFeed.setPublishingStatusMessage(
                        studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
                siteFeed.setSandboxBranch(sandboxBranch);
                siteFeed.setSearchEngine(searchEngine);
                siteFeedMapper.createSite(siteFeed);

                logger.info("Upgrading site");
                upgradeManager.upgradeSite(siteId);
            } catch (Exception e) {
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                             blueprintId + ". Rolling back...", e);

                contentRepository.deleteSite(siteId);

                try {
                    deployer.deleteTargets(siteId);
                } catch (Exception ex) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                                 " from blueprint: " + blueprintId + ". This means the site's Deployer " +
                                 "targets are still present, but the site was not successfully created", e);
                }

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                                                " from blueprint: " + blueprintId, e);
            }

            if (success) {
                ZonedDateTime now = ZonedDateTime.now();
                String creator = securityService.getCurrentUser();
                try {

                    logger.info("Pushing site " + siteId + " to remote repository " + remoteName + " (" +
                            remoteUrl + ")");
                    contentRepository.addRemote(siteId, remoteName, remoteUrl, authenticationType, remoteUsername,
                            remotePassword, remoteToken, remotePrivateKey);
                    contentRepository.createSitePushToRemote(siteId, remoteName, remoteUrl, authenticationType,
                            remoteUsername, remotePassword, remoteToken, remotePrivateKey, createAsOrphan);
                } catch (RemoteRepositoryNotFoundException | InvalidRemoteRepositoryException |
                        InvalidRemoteRepositoryCredentialsException | RemoteRepositoryNotBareException |
                        InvalidRemoteUrlException | ServiceLayerException e) {
                    logger.error("Error while pushing site: " + siteId + " ID: " + siteId + " to remote repository "
                            + remoteName + " (" + remoteUrl + ")", e);
                    contentRepositoryV2.removeRemote(siteId, remoteName);
                }

            try {
                // Add default groups
                logger.info("Adding default groups for site " + siteId);
                addDefaultGroupsForNewSite(siteId);

                logger.debug("Adding audit logs.");
                String lastCommitId = contentRepositoryV2.getRepoLastCommitId(siteId);
                Map<String, String> createdFiles =
                        contentRepositoryV2.getChangeSetPathsFromDelta(siteId, null, lastCommitId);
                insertCreateSiteAuditLog(siteId, siteId, createdFiles);
                insertAddRemoteAuditLog(siteId, remoteName);

                processCreatedFiles(siteId, createdFiles, creator, now, lastCommitId);

                contentRepositoryV2.insertGitLog(siteId, lastCommitId, 1, 1);
                updateLastCommitId(siteId, lastCommitId);
                updateLastVerifiedGitlogCommitId(siteId, lastCommitId);
                updateLastSyncedGitlogCommitId(siteId, lastCommitId);

                logger.info("Loading configuration for site " + siteId);
                reloadSiteConfiguration(siteId);
                } catch (Exception e) {
                    success = false;
                    logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                            blueprintId + ". Rolling back.", e);

                    deleteSite(siteId);

                    throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                            " from blueprint: " + blueprintId + ". Rolling back.");
                }
            }
        }

        if (success) {
            // Now that everything is created, we can sync the preview deployer with the new content
            logger.info("Sync all content to preview for site " + siteId);
            try {
                deploymentService.syncAllContentToPreview(siteId, true);
            } catch (ServiceLayerException e) {
                logger.error("Error while syncing site: " + siteId + " ID: " + siteId + " to preview. Site was "
                        + "successfully created otherwise. Ignoring.", e);

                throw new SiteCreationException("Error while syncing site: " + siteId + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the " +
                        "Preview Deployer is reachable.");
            }
            setSiteState(siteId, STATE_CREATED);
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
        } catch(Exception e) {
			success = false;
			logger.error("Failed to delete the Deployer target for sites:" + siteId, e);
		}

		try {
            success = success && destroySitePreviewContext(siteId);
        } catch(Exception e) {
            success = false;
            logger.error("Failed to destroy the preview context for site:" + siteId, e);
        }

		try {
		    logger.debug("Deleting repo");
		    contentRepository.deleteSite(siteId);
		} catch(Exception e) {
			success = false;
			logger.error("Failed to delete the repository for site:" + siteId, e);
		}

	    try {
		    // delete database records
		    logger.debug("Deleting database records");
		    SiteFeed siteFeed = getSite(siteId);
			siteFeedMapper.deleteSite(siteId);
			dependencyService.deleteSiteDependencies(siteId);
	        deploymentService.deleteDeploymentDataForSite(siteId);
	        objectStateService.deleteObjectStatesForSite(siteId);
	        objectMetadataManager.deleteObjectMetadataForSite(siteId);
	        dmPageNavigationOrderService.deleteSequencesForSite(siteId);
	        contentRepository.deleteGitLogForSite(siteId);
	        contentRepository.removeRemoteRepositoriesForSite(siteId);
	        auditServiceInternal.deleteAuditLogForSite(siteFeed.getId());
	        insertDeleteSiteAuditLog(siteId, siteFeed.getName());
	    } catch(Exception e) {
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
		int idx = 0;
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

		return blueprints.toArray(new SiteBlueprintTO[blueprints.size()]);
	}

    @Override
    public void reloadSiteConfigurations() {
        reloadGlobalConfiguration();
        Set<String> sites = getAllAvailableSites();
    }

    @Override
    @ValidateParams
    public void reloadSiteConfiguration(@ValidateStringParam(name = "site") String site) {
        reloadSiteConfiguration(site, true);
    }

    @Override
    public void reloadSiteConfiguration(String site, boolean triggerEvent) {
        SiteTO siteConfig = new SiteTO();
        siteConfig.setSite(site);
        siteConfig.setEnvironment(getEnvironment());
        servicesConfig.reloadConfiguration(site);
		notificationService.reloadConfiguration(site);
        securityService.reloadConfiguration(site);
        contentTypeService.reloadConfiguration(site);
    }

    @Override
    public void reloadGlobalConfiguration() {
        securityService.reloadGlobalConfiguration();
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
        retryingOperationFacade.updateSiteLastCommitId(params);

        try {
            ClusterMember clusterMember = clusterDao.getMemberByLocalAddress(studioClusterUtils.getClusterNodeLocalAddress());
            if (Objects.nonNull(clusterMember)) {
                SiteFeed siteFeed = getSite(site);
                retryingOperationFacade.updateClusterNodeLastCommitId(clusterMember.getId(), siteFeed.getId(),
                        commitId);
            }
        } catch (SiteNotFoundException e) {
            logger.error("Site not found " + site);
        }

    }

    @Override
    public void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingOperationFacade.updateSiteLastVerifiedGitlogCommitId(params);

        try {
            ClusterMember clusterMember = clusterDao.getMemberByLocalAddress(studioClusterUtils.getClusterNodeLocalAddress());
            if (Objects.nonNull(clusterMember)) {
                SiteFeed siteFeed = getSite(site);
                retryingOperationFacade.updateClusterNodeLastVerifiedGitlogCommitId(clusterMember.getId(),
                        siteFeed.getId(), commitId);
            }
        } catch (SiteNotFoundException e) {
            logger.error("Site not found " + site);
        }
    }

    @Override
    public void updateLastSyncedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingOperationFacade.updateSiteLastSyncedGitlogCommitId(params);

        try {
            ClusterMember clusterMember = clusterDao.getMemberByLocalAddress(studioClusterUtils.getClusterNodeLocalAddress());
            if (Objects.nonNull(clusterMember)) {
                SiteFeed siteFeed = getSite(site);
                retryingOperationFacade.updateClusterNodeLastSyncedGitlogCommitId(clusterMember.getId(),
                        siteFeed.getId(), commitId);
            }
        } catch (SiteNotFoundException e) {
            logger.error("Site not found " + site);
        }
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepoUnprocessedCommits(@ValidateStringParam(name = "site") String site,
                                                          List<GitLog> commitIds) throws IOException {
        boolean toReturn = true;
        String repoLastCommitId = contentRepository.getRepoLastCommitId(site);
        String scriptFilename = "repoOperations_" + UUID.randomUUID();
        Path scriptPath = Files.createTempFile(scriptFilename, ".sql");
        boolean success = true;
        long startUpdateDBMark = 0;
        List<String> cIds = new ArrayList<String>();
        for (GitLog gitLog : commitIds) {
            String commitId = gitLog.getCommitId();
            long startGetOperationsFromDeltaMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
            List<RepoOperation> repoOperationsDelta =
                    contentRepositoryV2.getOperationsFromDelta(site, commitId + PREVIOUS_COMMIT_SUFFIX, commitId);
            if (logger.isDebugEnabled()) {
                logger.debug("Get Repo Operations from Delta finished in " +
                        (System.currentTimeMillis() - startGetOperationsFromDeltaMark) + " milliseconds");
                logger.debug("Number of Repo operations from delta " + repoOperationsDelta.size());
            }
            if (!CollectionUtils.isEmpty(repoOperationsDelta)) {
                logger.debug("Syncing database with repository for site: " + site + " commitId = " + commitId);
                logger.debug("Operations to sync: ");
                if (logger.isDebugEnabled()) {
                    for (RepoOperation repoOperation : repoOperationsDelta) {
                        logger.debug("\tOperation: " + repoOperation.getAction().toString() + " " + repoOperation.getPath());
                    }
                }
                startUpdateDBMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;

                success = processRepoOperations(site, repoOperationsDelta, scriptPath);
            }
            toReturn = toReturn && success;
        }
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
		studioDBScriptRunner.execute(scriptPath.toFile());

        // At this point we have attempted to process all operations, some may have failed
        // We will update the lastCommitId of the database ignoring errors if any
        logger.debug("Done syncing operations with a result of: " + success);
        logger.debug("Syncing database lastCommitId for site: " + site);

        // Update database
        if (success) {
            contentRepositoryV2.markGitLogVerifiedProcessedBulk(site, cIds);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Update DB finished in " + (System.currentTimeMillis() - startUpdateDBMark) + " milliseconds");
        }

        // Sync all preview deployers
        try {
            logger.debug("Sync preview for site " + site);
            deploymentService.syncAllContentToPreview(site, false);
        } catch (ServiceLayerException e) {
            logger.error("Error synchronizing preview with repository for site: " + site, e);
        }

        logger.info("Done syncing database with repository for site: " + site + " for unprocessed commits with a " +
                "final result of: " + toReturn);

        if (toReturn) {
            updateLastCommitId(site, repoLastCommitId);
            logger.info("Last commit ID for site: " + site + " is " + repoLastCommitId);
        } else {
            // Some operations failed during sync database from repo
            // Must log and make some noise here, this isn't great
            logger.error("Some operations failed to sync to database for site: " + site + " see previous error logs");
        }
        return toReturn;
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId)
            throws SiteNotFoundException {
        return syncDatabaseWithRepo(site, fromCommitId, true);
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId,
                                        boolean generateAuditLog) throws SiteNotFoundException {
		boolean toReturn = true;
        String repoLastCommitId = contentRepository.getRepoLastCommitId(site);
        long startGetOperationsFromDeltaMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
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

        long startUpdateDBMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
        try {
            String scriptFilename = "repoOperations_" + UUID.randomUUID();
            Path scriptPath = Files.createTempFile(scriptFilename, ".sql");
            toReturn = processRepoOperations(site, repoOperationsDelta, scriptPath);
            studioDBScriptRunner.execute(scriptPath.toFile());
        } catch (IOException e) {
            logger.error("Error while creating db script file for processing created files for site " + site);
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
	    return toReturn;
    }

    private boolean processRepoOperations(String siteId, List<RepoOperation> repoOperations, Path file) throws IOException {
        boolean toReturn = true;
        long startProcessRepoOperationMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        for (RepoOperation repoOperation : repoOperations) {
            switch (repoOperation.getAction()) {
                case CREATE:
                case COPY:
                    Files.write(file, upsertItemStateRow(siteId,
                            repoOperation.getPath()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, upsertItemMetadataRow(siteId, repoOperation.getPath(),
                            repoOperation.getAuthor(), repoOperation.getDateTime(), repoOperation.getCommitId())
                                    .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    logger.debug("Extract dependencies for site: " + siteId + " path: " +
                            repoOperation.getPath());
                    addDependenciesScriptSnippets(siteId, repoOperation.getPath(), null, file);
                    break;

                case UPDATE:
                    Files.write(file, transitionSaveItemStateRow(siteId, repoOperation.getPath())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, updateItemMetadataRow(siteId, repoOperation.getPath(),
                            repoOperation.getAuthor(), repoOperation.getDateTime(), repoOperation.getCommitId())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    logger.debug("Extract dependencies for site: " + siteId + " path: " + repoOperation.getPath());
                    addDependenciesScriptSnippets(siteId, repoOperation.getPath(), null, file);
                    break;

                case DELETE:
                    Files.write(file, deleteItemStateRow(siteId, repoOperation.getPath())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, deleteItemMetadataRow(siteId, repoOperation.getPath())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, deleteDependencyRows(siteId, repoOperation.getPath())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    break;

                case MOVE:
                    Files.write(file, moveItemStateRow(siteId, repoOperation.getPath(),
                            repoOperation.getMoveToPath()).getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, transitionSaveItemStateRow(siteId, repoOperation.getMoveToPath())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, moveItemMetadataRow(siteId, repoOperation.getPath(),
                            repoOperation.getMoveToPath()).getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, updateItemMetadataRow(siteId, repoOperation.getMoveToPath(),
                            repoOperation.getAuthor(), repoOperation.getDateTime(), repoOperation.getCommitId())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    addDependenciesScriptSnippets(siteId, repoOperation.getMoveToPath(),
                            repoOperation.getPath(), file);
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

    @RetryingOperation
    @Override
    @ValidateParams
    public boolean enablePublishing(@ValidateStringParam(name = "siteId") String siteId, boolean enabled)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            params.put("enabled", enabled ? 1 : 0);
            siteFeedMapper.enablePublishing(params);
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public boolean updatePublishingStatusMessage(@ValidateStringParam(name = "siteId") String siteId,
                                                 @ValidateStringParam(name = "status") String status,
                                                 @ValidateStringParam(name = "message") String message)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            siteFeedMapper.updatePublishingStatusMessage(siteId, status, message);
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

    @RetryingOperation
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

    @RetryingOperation
    @Override
    public boolean unlockPublishingForSite(String siteId, String lockOwnerId) {
        logger.debug("Unlocking publishing for site " + siteId + " lock owner " + lockOwnerId);
	    siteFeedMapper.unlockPublishingForSite(siteId, lockOwnerId);
        return true;
    }

    @RetryingOperation
    @Override
    public void updatePublishingLockHeartbeatForSite(String siteId) {
        logger.debug("Update publishing lock heartbeat for site " + siteId);
        siteFeedMapper.updatePublishingLockHeartbeatForSite(siteId);
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
        return siteFeedMapper.getAllCreatedSites(STATE_CREATED);
    }

    @Override
    public void setSiteState(String siteId, String state) {
        siteFeedMapper.setSiteState(siteId, state);
        try {
            ClusterMember clusterMember = clusterDao.getMemberByLocalAddress(studioClusterUtils.getClusterNodeLocalAddress());
            if (Objects.nonNull(clusterMember)) {
                SiteFeed siteFeed = getSite(siteId);
                clusterDao.setSiteState(clusterMember.getId(), siteFeed.getId(), state);
            }
        } catch (SiteNotFoundException e) {
            logger.error("Site not found " + siteId);
        }
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
        siteFeedMapper.setPublishedRepoCreated(siteId);
        try {
            ClusterMember clusterMember = clusterDao.getMemberByLocalAddress(studioClusterUtils.getClusterNodeLocalAddress());
            if (Objects.nonNull(clusterMember)) {
                SiteFeed siteFeed = getSite(siteId);
                clusterDao.setPublishedRepoCreated(clusterMember.getId(), siteFeed.getId());
            }
        } catch (SiteNotFoundException e) {
            logger.error("Site not found " + siteId);
        }
    }

    @Override
    public String getLastSyncedGitlogCommitId(String siteId) {
        return siteFeedMapper.getLastSyncedGitlogCommitId(siteId, studioClusterUtils.getClusterNodeLocalAddress());
    }

    public String getGlobalConfigRoot() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    public String getSitesConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
    }

    public String getEnvironment() {
        return studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
    }

    public List<String> getDefaultGroups() {
        return Arrays.asList(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_GROUPS).split(","));
    }

    /** getter site service dal */
	public SiteServiceDAL getSiteService() {
	    return _siteServiceDAL;
	}
	/** setter site service dal */
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

	public ContentRepository getContenetRepository() {
	    return contentRepository;
	}
	public void setContentRepository(ContentRepository repo) {
	    contentRepository = repo;
	}

	public ObjectStateService getObjectStateService() {
	    return objectStateService;
	}
	public void setObjectStateService(ObjectStateService objectStateService) {
	    this.objectStateService = objectStateService;
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

    public ObjectMetadataManager getObjectMetadataManager() {
	    return objectMetadataManager;
	}
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
	    this.objectMetadataManager = objectMetadataManager;
	}

    public DmPageNavigationOrderService getDmPageNavigationOrderService() {
	    return dmPageNavigationOrderService;
	}
    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) {
	    this.dmPageNavigationOrderService = dmPageNavigationOrderService;
	}

    public ContentTypeService getContentTypeService() {
	    return contentTypeService;
	}
    public void setContentTypeService(ContentTypeService contentTypeService) {
	    this.contentTypeService = contentTypeService;
	}

    public ImportService getImportService() {
	    return importService;
	}
    public void setImportService(ImportService importService) {
	    this.importService = importService;
	}

	public void setNotificationService(final NotificationService notificationService) {
		this.notificationService = notificationService;
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

    public EventService getEventService() {
	    return eventService;
	}
    public void setEventService(EventService eventService) {
	    this.eventService = eventService;
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

	public void setUpgradeManager(final UpgradeManager upgradeManager) {
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

    public org.craftercms.studio.api.v2.repository.ContentRepository getContentRepositoryV2() {
        return contentRepositoryV2;
    }

    public void setContentRepositoryV2(org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
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

    public StudioDBScriptRunnerFactory getStudioDBScriptRunner() {
        return studioDBScriptRunnerFactory;
    }

    public void setStudioDBScriptRunner(StudioDBScriptRunnerFactory studioDBScriptRunnerFactory) {
        this.studioDBScriptRunnerFactory = studioDBScriptRunnerFactory;
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public RetryingOperationFacade getRetryingOperationFacade() {
        return retryingOperationFacade;
    }

    public void setRetryingOperationFacade(RetryingOperationFacade retryingOperationFacade) {
        this.retryingOperationFacade = retryingOperationFacade;
    }
}
