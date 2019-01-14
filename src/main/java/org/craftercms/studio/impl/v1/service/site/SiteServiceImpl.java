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

package org.craftercms.studio.impl.v1.service.site;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.GitLog;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.PreviewDeployerUnreachableException;
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
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.configuration.SiteEnvironmentConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.content.ImportService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.EnvironmentConfigTO;
import org.craftercms.studio.api.v1.to.PublishStatus;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.RepoOperationTO;
import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import org.craftercms.studio.api.v1.to.SiteTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.impl.v1.repository.job.RebuildRepositoryMetadata;
import org.craftercms.studio.impl.v1.repository.job.SyncDatabaseWithRepository;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIG_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_PUSH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_DEFAULT_GROUPS_DESCRIPTION;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_DEFAULT_ADMIN_GROUP;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_DEFAULT_GROUPS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PREVIEW_ENGINE_URL;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService {

	private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

    protected PreviewDeployer previewDeployer;
    protected SiteServiceDAL _siteServiceDAL;
    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected SiteEnvironmentConfig environmentConfig;
    protected ContentRepository contentRepository;
    protected ObjectStateService objectStateService;
    protected DependencyService dependencyService;
    protected SecurityService securityService;
    protected ActivityService activityService;
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

    @Autowired
    protected SiteFeedMapper siteFeedMapper;

    protected EntitlementValidator entitlementValidator;

    @Override
    @ValidateParams
    public boolean writeConfiguration(@ValidateStringParam(name = "site") String site,
                                      @ValidateSecurePathParam(name = "path") String path, InputStream content)
            throws ServiceLayerException {
        // Write site configuration
        ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
        if (!contentRepository.contentExists(site, path)) {
            activityType = ActivityService.ActivityType.CREATED;
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
        activityService.postActivity(site, user, path, activityType, ActivityService.ActivitySource.API, extraInfo);
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
            contentRepository.insertGitLog(site, commitId, 1);
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
                                                @ValidateStringParam(name = "environment") String environment,
                                                boolean applyEnv) {
		String configPath;
		if (StringUtils.isEmpty(site)) {
			configPath = getGlobalConfigRoot() + path;
		} else {
		    if (path.startsWith(FILE_SEPARATOR + CONTENT_TYPE_CONFIG_FOLDER + FILE_SEPARATOR)) {
                configPath = getSitesConfigPath() + path;
            } else if (StringUtils.isEmpty(environment)) {
                if (applyEnv) {
                    configPath = getEnvironmentConfigPath().replaceAll(PATTERN_SITE, site).replaceAll(
                            PATTERN_ENVIRONMENT, getEnvironment()) + path;
                } else {
                    configPath = getSitesConfigPath() + path;
                }
            } else {
                configPath = getSitesMultiEnvironmentConfigPath().replaceAll(PATTERN_ENVIRONMENT, environment) + path;
            }
		}
		String configContent = contentService.getContentAsString(site, configPath);

		Map<String, Object> toRet = null;
		if (configContent != null) {
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


	/***
	 * load site environment specific info
	 *
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteEnvironmentConfig(String site, SiteTO siteConfig) {
		// get environment specific configuration
		logger.debug("Loading site environment configuration for " + site + "; Environemnt: " + getEnvironment());
		EnvironmentConfigTO environmentConfigTO = environmentConfig.getEnvironmentConfig(site);
		if (environmentConfigTO == null) {
			logger.error("Environment configuration for site " + site + " does not exist.");
			return;
		}
		siteConfig.setLiveUrl(environmentConfigTO.getLiveServerUrl());
		siteConfig.setAuthoringUrl(environmentConfigTO.getAuthoringServerUrl());
		siteConfig.setPreviewUrl(environmentConfigTO.getPreviewServerUrl());
		siteConfig.setAdminEmail(environmentConfigTO.getAdminEmailAddress());
		siteConfig.setOpenSiteDropdown(environmentConfigTO.getOpenDropdown());
	}

    @Override
    @ValidateParams
    public List<PublishingTargetTO> getPublishingTargetsForSite(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getPublishingTargetsForSite(site);
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
   	public void createSiteFromBlueprint(@ValidateStringParam(name = "blueprintName") String blueprintName,
                                           @ValidateNoTagsParam(name = "siteName") String siteName,
                                           @ValidateStringParam(name = "siteId") String siteId,
                                           @ValidateStringParam(name = "sandboxBranch") String sandboxBranch,
                                           @ValidateNoTagsParam(name = "desc") String desc,
                                           @ValidateStringParam(name = "searchEngine") String searchEngine)
            throws SiteAlreadyExistsException, SiteCreationException, PreviewDeployerUnreachableException,
            BlueprintNotFoundException {
	    if (exists(siteId)) {
	        throw new SiteAlreadyExistsException();
        }

        if (!contentService.contentExists(StringUtils.EMPTY,
                studioConfiguration.getProperty(BLUE_PRINTS_PATH) + FILE_SEPARATOR +
                        blueprintName)) {
            throw new BlueprintNotFoundException();
        }

        try {
	    	long start = 0;
	    	if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
	    		start = System.currentTimeMillis();
	    		logger.debug("Starting entitlement validation");
			}
			entitlementValidator.validateEntitlement(Module.STUDIO, EntitlementType.SITE, countSites(), 1);
	    	if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
	    		logger.debug("Validation completed, duration : {0} ms", System.currentTimeMillis() - start);
			}
		} catch (EntitlementException e) {
	    	throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
				+ "system administrator.", e);
		}

        boolean success = true;

	    // TODO: SJ: We must fail site creation if any of the site creations steps fail and rollback
	    // TODO: SJ: For example: Create site => Create Search Index (success), create Deployer Target (fail) = fail
	    // TODO: SJ: and rollback the whole thing.
	    // TODO: SJ: What we need to do for site creation and the order of execution:
	    // TODO: SJ: 1) search index, 2) deployer target, 3) git repo, 4) database, 5) kick deployer


	    // Create the site in the preview deployer
        try {
            success = previewDeployer.createTarget(siteId, searchEngine);
        } catch (Exception e) {
            success = false;
            logger.error("Error while creating site: " + siteName + " ID: " + siteId +
                    " from blueprint: " + blueprintName + ". Is the Preview Deployer running and configured " +
                    "correctly in Studio?", e);

            throw new PreviewDeployerUnreachableException("Error while creating site: " + siteName + " ID: "
                + siteId + " from blueprint: " + blueprintName + ". Is the Preview Deployer running and "
                + "configured correctly in Studio?");
        }

	    if (success) {
	 		try {
			    success = createSiteFromBlueprintGit(blueprintName, siteName, siteId, sandboxBranch, desc);

			    upgradeManager.upgradeSite(siteId);

			    // insert database records
			    SiteFeed siteFeed = new SiteFeed();
			    siteFeed.setName(siteName);
			    siteFeed.setSiteId(UUID.randomUUID().toString());
			    siteFeed.setDescription(desc);
			    siteFeed.setPublishingStatusMessage(
			            studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
			    siteFeed.setSandboxBranch(sandboxBranch);
			    siteFeed.setSearchEngine(searchEngine);
			    siteFeedMapper.createSite(siteFeed);

                insertCreateSiteAuditLog(siteId);

                // Add default groups
                addDefaultGroupsForNewSite(siteId);

                reloadSiteConfiguration(siteId);

                syncDatabaseWithRepo(siteId, null, false);

                // initial deployment
                List<PublishingTargetTO> publishingTargets = getPublishingTargetsForSite(siteId);
                if (publishingTargets != null && publishingTargets.size() > 0) {
                    for (PublishingTargetTO target : publishingTargets) {
                        if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                            contentRepository.initialPublish(siteId, sandboxBranch, target.getRepoBranchName(),
                                    securityService.getCurrentUser(), "Create site.");
                        }
                    }
                }
                objectStateService.setStateForSiteContent(siteId, State.EXISTING_UNEDITED_UNLOCKED);

	        } catch(Exception e) {
	            // TODO: SJ: We need better exception handling here
	            success = false;
	            logger.error("Error while creating site: " + siteName + " ID: " + siteId + " from blueprint: " +
	                    blueprintName + ". Rolling back.", e);

			    boolean deleted = previewDeployer.deleteTarget(siteId);
				if (!deleted) {
					logger.error("Error while rolling back/deleting site: " + siteName + " ID: " + siteId +
						" from blueprint: " + blueprintName + ". This means the site's preview deployer target is " +
						"still present, but the site is not successfully created.");
				}

			    throw new SiteCreationException("Error while creating site: " + siteName + " ID: " + siteId +
                        " from blueprint: " + blueprintName + ". Rolling back.");
		    }
	    }

	    if (success) {
		    // Now that everything is created, we can sync the preview deployer with the new content
		    try {
			    deploymentService.syncAllContentToPreview(siteId, true);
		    } catch (ServiceLayerException e) {
			    // TODO: SJ: We need better exception handling here
			    logger.error("Error while syncing site: " + siteName + " ID: " + siteId + " to preview. Site was "
				    + "successfully created otherwise. Ignoring.", e);

			    throw new SiteCreationException("Error while syncing site: " + siteName + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the Preview " +
                        "Deployer is reachable.");
		    }
	    } else {
		    throw new SiteCreationException("Error while creating site: " + siteName + " ID: " + siteId + ".");
	    }
    }

    private void insertCreateSiteAuditLog(String siteId) {
        ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATE_SITE;
        String user = securityService.getCurrentUser();
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_SITE);
        activityService.postActivity(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE), user,
                siteId , activityType, ActivityService.ActivitySource.API, extraInfo);
    }

    protected boolean createSiteFromBlueprintGit(String blueprintName, String siteName, String siteId,
                                                 String sandboxBranch, String desc)
            throws Exception {
        boolean success = true;

        // create site with git repo
        contentRepository.createSiteFromBlueprint(blueprintName, siteId, sandboxBranch);

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

    protected void replaceFileContent(String path, String find, String replace) throws Exception {
    	InputStream content = contentRepository.getContent("", path);
    	String contentAsString = IOUtils.toString(content);

    	contentAsString = contentAsString.replaceAll(find, replace);

    	InputStream contentToWrite = IOUtils.toInputStream(contentAsString);

		contentRepository.writeContent("", path, contentToWrite);
    }

	protected void createObjectStatesforNewSite(String site) {
		createObjectStateNewSiteObjectFolder(site, FILE_SEPARATOR);
	}

	protected void createObjectStateNewSiteObjectFolder(String site, String path) {
		RepositoryItem[] children = contentRepository.getContentChildren(site, path);
		for (RepositoryItem child : children) {
			if (child.isFolder) {
				createObjectStateNewSiteObjectFolder(site, child.path + FILE_SEPARATOR + child.name);
			} else {
				objectStateService.insertNewEntry(site, child.path + FILE_SEPARATOR + child.name);
			}
		}
	}

    protected void createObjectMetadataforNewSite(String site, String lastCommitId) {
        createObjectMetadataNewSiteObjectFolder(site, FILE_SEPARATOR, lastCommitId);
    }

    protected void createObjectMetadataNewSiteObjectFolder(String site, String path, String lastCommitId) {
        RepositoryItem[] children = contentRepository.getContentChildren(site, path);
        for (RepositoryItem child : children) {
            if (child.isFolder) {
                createObjectMetadataNewSiteObjectFolder(site, child.path + FILE_SEPARATOR + child.name,
                        lastCommitId);
            } else {
                objectMetadataManager.insertNewObjectMetadata(site, child.path + FILE_SEPARATOR + child.name);
                objectMetadataManager.updateCommitId(site, child.path + FILE_SEPARATOR + child.name,
                        lastCommitId);
            }
        }
    }

	protected void extractDependenciesForNewSite(String site) {
        Map<String, Set<String>> globalDeps = new HashMap<String, Set<String>>();
        extractDependenciesItemForNewSite(site, FILE_SEPARATOR, globalDeps);
	}

    private void extractDependenciesItemForNewSite(String site, String fullPath, Map<String, Set<String>> globalDeps) {
        RepositoryItem[] children = contentRepository.getContentChildren(site, fullPath);
        for (RepositoryItem child : children) {
            if (child.isFolder) {
                extractDependenciesItemForNewSite(site, child.path + FILE_SEPARATOR + child.name, globalDeps);
            } else {
                String childPath = child.path + FILE_SEPARATOR + child.name;

                if (childPath.endsWith(DmConstants.XML_PATTERN)) {
                    try {
                        dependencyService.upsertDependencies(site, childPath);
                    } catch (ContentNotFoundException e) {
                        logger.error("Failed to extract dependencies for document: site " + site + " path " +
                                childPath, e);
                    } catch (ServiceLayerException e) {
                        logger.error("Failed to extract dependencies for document: site " + site + " path " +
                                childPath, e);
                    }
                } else {

                    boolean isCss = childPath.endsWith(DmConstants.CSS_PATTERN);
                    boolean isJs = childPath.endsWith(DmConstants.JS_PATTERN);
                    List<String> templatePatterns = servicesConfig.getRenderingTemplatePatterns(site);
                    boolean isTemplate = false;
                    for (String templatePattern : templatePatterns) {
                        Pattern pattern = Pattern.compile(templatePattern);
                        Matcher matcher = pattern.matcher(childPath);
                        if (matcher.matches()) {
                            isTemplate = true;
                            break;
                        }
                    }
                    try {
                        if (isCss || isJs || isTemplate) {
                            dependencyService.upsertDependencies(site, childPath);
                        }
                    } catch (ServiceLayerException e) {
                        logger.error("Failed to extract dependencies for: site " + site + " path " + childPath, e);
                    }
                }
            }
        }
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
    public void createSiteWithRemoteOption(@ValidateStringParam(name = "siteId") String siteId,
                                           @ValidateStringParam(name = "sandboxBranch") String sandboxBranch,
                                           @ValidateNoTagsParam(name = "description") String description,
                                           String blueprintName,
                                           @ValidateStringParam(name = "remoteName") String remoteName,
                                           @ValidateStringParam(name = "remoteUrl") String remoteUrl,
                                           String remoteBranch, boolean singleBranch, String authenticationType,
                                           String remoteUsername, String remotePassword, String remoteToken,
                                           String remotePrivateKey,
                                           @ValidateStringParam(name = "createOption") String createOption,
                                           @ValidateStringParam(name = "searchEngine") String searchEngine)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, InvalidRemoteUrlException {
        if (exists(siteId)) {
            throw new SiteAlreadyExistsException();
        }

		try {
        	long start = 0;
			if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
				start = System.currentTimeMillis();
				logger.debug("Starting entitlement validation");
			}
			entitlementValidator.validateEntitlement(Module.STUDIO, EntitlementType.SITE, countSites(), 1);
			if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
				logger.debug("Validation completed, duration : {0} ms", System.currentTimeMillis() - start);
			}
		} catch (EntitlementException e) {
			throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
				+ "system administrator.", e);
		}

        switch (createOption) {
            case REMOTE_REPOSITORY_CREATE_OPTION_CLONE:
                logger.debug("Clone from remote repository create option selected");
                createSiteCloneRemote(siteId, sandboxBranch, description, remoteName, remoteUrl, remoteBranch, singleBranch,
                        authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey, searchEngine);
                break;

            case REMOTE_REPOSITORY_CREATE_OPTION_PUSH:
                logger.debug("Push to remote repository create option selected");
                createSitePushToRemote(siteId, sandboxBranch, description, blueprintName, remoteName, remoteUrl,
                        remoteBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                        remotePrivateKey, searchEngine);
                break;

            default:
                logger.error("Invalid create option for create site using remote repository: " + createOption +
                        "\nAvailable options: [" + REMOTE_REPOSITORY_CREATE_OPTION_CLONE + ", " +
                        REMOTE_REPOSITORY_CREATE_OPTION_PUSH + "]");
                break;
        }
    }

    private void createSiteCloneRemote(String siteId, String sandboxBranch, String description, String remoteName,
                                       String remoteUrl, String remoteBranch, boolean singleBranch,
                                       String authenticationType, String remoteUsername, String remotePassword,
                                       String remoteToken, String remotePrivateKey, String searchEngine)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, InvalidRemoteUrlException {
        boolean success = true;

        // TODO: SJ: We must fail site creation if any of the site creations steps fail and rollback
        // TODO: SJ: For example: Create site => Create Search Index (success), create Deployer Target (fail) = fail
        // TODO: SJ: and rollback the whole thing.
        // TODO: SJ: What we need to do for site creation and the order of execution:
        // TODO: SJ: 1) search index, 2) deployer target, 3) git repo, 4) database, 5) kick deployer


        // Create the site in the preview deployer
        try {
            logger.debug("Creating preview deployer target for site " + siteId);
            success = previewDeployer.createTarget(siteId, searchEngine);
        } catch (Exception e) {
            success = false;
            logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from" +
                    " remote repository: " + remoteName + " (" + remoteUrl + "). Is the Preview Deployer running " +
                    "and configured correctly in Studio?", e);

            throw new PreviewDeployerUnreachableException("Error while creating site: " + siteId + " ID: "
                    + siteId + " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). " +
                    "Is the Preview Deployer running and configured correctly in Studio?");
        }


        if (success) {
            try {
                // create site by cloning remote git repo
                logger.debug("Creating site " + siteId + " by cloning remote repository " + remoteName +
                        " (" + remoteUrl + ")");
                contentRepository.createSiteCloneRemote(siteId, sandboxBranch, remoteName, remoteUrl, remoteBranch,
                        singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                        remotePrivateKey);
            } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
                    RemoteRepositoryNotFoundException | InvalidRemoteUrlException | ServiceLayerException e) {

                contentRepository.deleteSite(siteId);

                boolean deleted = previewDeployer.deleteTarget(siteId);
                if (!deleted) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                            " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). This means" +
                            " the site's preview deployer target is still present, but the site is not " +
                            "successfully created.");
                }

                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from " +
                        "remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);

                throw e;
            }
        }

        if (success) {
            try {
                upgradeManager.upgradeSite(siteId);

                // insert database records
                logger.debug("Adding site record to database for site " + siteId);
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteId);
                siteFeed.setSiteId(UUID.randomUUID().toString());
                siteFeed.setDescription(description);
                siteFeed.setPublishingStatusMessage(
                        studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
                siteFeed.setSandboxBranch(sandboxBranch);
                siteFeed.setSearchEngine(searchEngine);
                siteFeedMapper.createSite(siteFeed);

                insertCreateSiteAuditLog(siteId);

                // Add default groups
                logger.debug("Adding default groups for site " + siteId);
                addDefaultGroupsForNewSite(siteId);

                logger.debug("Loading configuration for site " + siteId);
                reloadSiteConfiguration(siteId);

                syncDatabaseWithRepo(siteId, null, false);

                // initial deployment
                logger.debug("Executing initial deployement for site " + siteId);
                List<PublishingTargetTO> publishingTargets = getPublishingTargetsForSite(siteId);
                if (publishingTargets != null && publishingTargets.size() > 0) {
                    for (PublishingTargetTO target : publishingTargets) {
                        if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                            contentRepository.initialPublish(siteId, sandboxBranch, target.getRepoBranchName(),
                                    securityService.getCurrentUser(), "Create site.");
                        }
                    }
                }
                objectStateService.setStateForSiteContent(siteId, State.EXISTING_UNEDITED_UNLOCKED);
            } catch(Exception e) {
                // TODO: SJ: We need better exception handling here
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " as clone from " +
                        "remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);

                deleteSite(siteId);

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                        " as clone from remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.");
            }
        }
    }

    private void createSitePushToRemote(String siteId, String sandboxBranch, String description, String blueprintName,
                                        String remoteName, String remoteUrl, String remoteBranch,
                                        String authenticationType, String remoteUsername, String remotePassword,
                                        String remoteToken, String remotePrivateKey, String searchEngine)
            throws ServiceLayerException, InvalidRemoteRepositoryCredentialsException, InvalidRemoteRepositoryException,
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, InvalidRemoteUrlException {
        if (exists(siteId)) {
            throw new SiteAlreadyExistsException();
        }
        if (!contentService.contentExists(StringUtils.EMPTY, studioConfiguration.getProperty(
                BLUE_PRINTS_PATH) + FILE_SEPARATOR + blueprintName)) {
            throw new BlueprintNotFoundException();
        }
        boolean success = true;

        // TODO: SJ: We must fail site creation if any of the site creations steps fail and rollback
        // TODO: SJ: For example: Create site => Create Search Index (success), create Deployer Target (fail) = fail
        // TODO: SJ: and rollback the whole thing.
        // TODO: SJ: What we need to do for site creation and the order of execution:
        // TODO: SJ: 1) search index, 2) deployer target, 3) git repo, 4) database, 5) kick deployer


        // Create the site in the preview deployer
        try {
            logger.debug("Creating preview deployer target for site " + siteId);
            success = previewDeployer.createTarget(siteId, searchEngine);
        } catch (Exception e) {
            success = false;
            logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                    blueprintName + ". Is the Preview Deployer running and configured correctly in Studio?", e);

            throw new PreviewDeployerUnreachableException("Error while creating site: " + siteId + " ID: "
                    + siteId + " from blueprint: " + blueprintName + ". Is the Preview Deployer running and "
                    + "configured correctly in Studio?");
        }

        if (success) {
            try {
                logger.debug("Creating site " + siteId + " from blueprint " + blueprintName);
                success = createSiteFromBlueprintGit(blueprintName, siteId, siteId, sandboxBranch, description);

                upgradeManager.upgradeSite(siteId);
            } catch (Exception e) {
                // TODO: SJ: We need better exception handling here
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                        blueprintName + ". Rolling back.", e);

                boolean deleted = previewDeployer.deleteTarget(siteId);
                if (!deleted) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                            " from blueprint: " + blueprintName + ". This means the site's preview deployer target is " +
                            "still present, but the site is not successfully created.");
                }

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                        " from blueprint: " + blueprintName + ". Rolling back.");
            }

            if (success) {
                try {
                    logger.debug("Pushing site " + siteId + " to remote repository " + remoteName + " (" +
                            remoteUrl + ")");
                    contentRepository.addRemote(siteId, remoteName, remoteUrl, authenticationType,
                            remoteUsername, remotePassword, remoteToken, remotePrivateKey);
                    insertAddRemoteAuditLog(siteId, remoteName);
                    contentRepository.createSitePushToRemote(siteId, remoteName, remoteUrl, authenticationType,
                            remoteUsername, remotePassword, remoteToken, remotePrivateKey);
                } catch (RemoteRepositoryNotFoundException | InvalidRemoteRepositoryException |
                        InvalidRemoteRepositoryCredentialsException | RemoteRepositoryNotBareException |
                        InvalidRemoteUrlException | ServiceLayerException e) {
                    // TODO: SJ: We need better exception handling here
                    success = false;
                    logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                            blueprintName + ". Rolling back.", e);

                    contentRepository.removeRemote(siteId, remoteName);

                    contentRepository.deleteSite(siteId);

                    boolean deleted = previewDeployer.deleteTarget(siteId);
                    if (!deleted) {
                        logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                                " from blueprint: " + blueprintName + ". This means the site's preview deployer" +
                                " target is still present, but the site is not successfully created.");
                    }

                    throw e;
                }
            }

            try {

                // insert database records
                logger.debug("Adding site record to database for site " + siteId);
                SiteFeed siteFeed = new SiteFeed();
                siteFeed.setName(siteId);
                siteFeed.setSiteId(UUID.randomUUID().toString());
                siteFeed.setDescription(description);
                siteFeed.setPublishingStatusMessage(
                        studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT));
                siteFeed.setSandboxBranch(sandboxBranch);
                siteFeed.setSearchEngine(searchEngine);
                siteFeedMapper.createSite(siteFeed);

                insertCreateSiteAuditLog(siteId);

                // Add default groups
                logger.debug("Adding default groups for site " + siteId);
                addDefaultGroupsForNewSite(siteId);

                logger.debug("Loading configuration for site " + siteId);
                reloadSiteConfiguration(siteId);

                syncDatabaseWithRepo(siteId, null, false);

                // initial deployment
                logger.debug("Executing initial deployement for site " + siteId);
                List<PublishingTargetTO> publishingTargets = getPublishingTargetsForSite(siteId);
                if (publishingTargets != null && publishingTargets.size() > 0) {
                    for (PublishingTargetTO target : publishingTargets) {
                        if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                            contentRepository.initialPublish(siteId, sandboxBranch, target.getRepoBranchName(),
                                    securityService.getCurrentUser(), "Create site.");
                        }
                    }
                }
                objectStateService.setStateForSiteContent(siteId, State.EXISTING_UNEDITED_UNLOCKED);


            } catch(Exception e) {
                // TODO: SJ: We need better exception handling here
                success = false;
                logger.error("Error while creating site: " + siteId + " ID: " + siteId + " from blueprint: " +
                        blueprintName + ". Rolling back.", e);

                deleteSite(siteId);
                boolean deleted = previewDeployer.deleteTarget(siteId);
                if (!deleted) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                            " from blueprint: " + blueprintName + ". This means the site's preview deployer target is " +
                            "still present, but the site is not successfully created.");
                }

                throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
                        " from blueprint: " + blueprintName + ". Rolling back.");
            }
        }

        if (success) {
            // Now that everything is created, we can sync the preview deployer with the new content
            try {
                deploymentService.syncAllContentToPreview(siteId, true);
            } catch (ServiceLayerException e) {
                // TODO: SJ: We need better exception handling here
                logger.error("Error while syncing site: " + siteId + " ID: " + siteId + " to preview. Site was "
                        + "successfully created otherwise. Ignoring.", e);

                throw new SiteCreationException("Error while syncing site: " + siteId + " ID: " + siteId +
                        " to preview. Site was successfully created, but it won't be preview-able until the " +
                        "Preview Deployer is reachable.");
            }
        } else {
            throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId + ".");
        }
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
		    logger.debug("Deleting preview deployer");
		    previewDeployer.deleteTarget(siteId);
		} catch(Exception e) {
			success = false;
			logger.error("Failed to delete the preview deployer target for site:" + siteId, e);
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
			siteFeedMapper.deleteSite(siteId);
			activityService.deleteActivitiesForSite(siteId);
			dependencyService.deleteSiteDependencies(siteId);
	        deploymentService.deleteDeploymentDataForSite(siteId);
	        objectStateService.deleteObjectStatesForSite(siteId);
	        objectMetadataManager.deleteObjectMetadataForSite(siteId);
	        dmPageNavigationOrderService.deleteSequencesForSite(siteId);
	        contentRepository.deleteGitLogForSite(siteId);
	        contentRepository.removeRemoteRepositoriesForSite(siteId);
	        insertDeleteSiteAuditLog(siteId);
	    } catch(Exception e) {
		    success = false;
		    logger.error("Failed to delete the database for site:" + siteId, e);
	    }

	 	return success;
    }

    private void insertDeleteSiteAuditLog(String siteId) {
        ActivityService.ActivityType activityType = ActivityService.ActivityType.DELETE_SITE;
        String user = securityService.getCurrentUser();
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_SITE);
        activityService.postActivity(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE), user,
                siteId , activityType, ActivityService.ActivitySource.API, extraInfo);
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
	    StringBuilder sb = new StringBuilder(studioConfiguration.getProperty(PREVIEW_ENGINE_URL));
        sb.append(studioConfiguration.getProperty(CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL));
        String url = sb.toString();
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
    @ValidateParams
    public String getPreviewServerUrl(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getPreviewServerUrl(site);
    }

    @Override
    @ValidateParams
    public String getLiveServerUrl(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getLiveServerUrl(site);
    }

    @Override
    @ValidateParams
    public String getAuthoringServerUrl(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getAuthoringServerUrl(site);
    }

    @Override
    @ValidateParams
    public String getAdminEmailAddress(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getAdminEmailAddress(site);
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
        environmentConfig.reloadConfiguration(site);
        loadSiteEnvironmentConfig(site, siteConfig);
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
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("siteId", site);
			String lastDbCommitId = siteFeedMapper.getLastCommitId(params);
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
        siteFeedMapper.updateLastCommitId(params);
    }

    private void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        siteFeedMapper.updateLastVerifiedGitlogCommitId(params);
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId) {
        return syncDatabaseWithRepo(site, fromCommitId, true);
    }

    @Override
    @ValidateParams
    public boolean syncDatabaseWithRepo(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "fromCommitId") String fromCommitId,
                                        boolean generateAuditLog) {
		boolean toReturn = true;
        List<RepoOperationTO> repoOperations = contentRepository.getOperations(site, fromCommitId, contentRepository
		    .getRepoLastCommitId(site));
        if (CollectionUtils.isEmpty(repoOperations)) {
            logger.debug("Database is up to date with repository for site: " + site);
            contentRepository.markGitLogVerifiedProcessed(site, fromCommitId);
            return toReturn;
        }

        logger.info("Syncing database with repository for site: " + site + " fromCommitId = " +
                (StringUtils.isEmpty(fromCommitId) ? "Empty repo" : fromCommitId));
        logger.debug("Operations to sync: ");
	    for (RepoOperationTO repoOperation: repoOperations) {
	    	logger.debug("\tOperation: " + repoOperation.getOperation().toString() + " " + repoOperation.getPath());
	    }

	    boolean diverged = false;
	    GitLog current = null;

	    // Process all operations and track if one or more have failed
	    for (RepoOperationTO repoOperation: repoOperations) {
            boolean gitLogProcessed = false;
            logger.debug("Verifying repo opertation " + repoOperation.getOperation().toString() + " " +
                    repoOperation.getPath());
            logger.debug("Get Git Log from database for commit id " + repoOperation.getCommitId());
            GitLog gitLog = contentRepository.getGitLog(site, repoOperation.getCommitId());
            if (gitLog != null) {
                diverged = diverged || gitLog.getProcessed() < 1;
            } else {
                logger.debug("Git Log does not exist in database for commit id " + repoOperation.getCommitId());
                logger.debug("Inserting Git Log for commit id " + repoOperation.getCommitId() + " and site " + site);
                contentRepository.insertGitLog(site, repoOperation.getCommitId(), 0);
                logger.debug("Repository diverged from database. All repository operations onwards need to be processed");
                diverged = true;
                gitLogProcessed = false;
                gitLog = contentRepository.getGitLog(site, repoOperation.getCommitId());
            }

            if (current == null) {
                current = gitLog;
            } else {
	            if (!current.getCommitId().equals(gitLog.getCommitId())) {
                    contentRepository.markGitLogVerifiedProcessed(site, current.getCommitId());
                    current = gitLog;
                }
            }

	        if (diverged) {
                Map<String, String> activityInfo = new HashMap<String, String>();
                String contentClass;
                Map<String, Object> properties;
                switch (repoOperation.getOperation()) {
                    case CREATE:
                    case COPY:
                        ItemState state = objectStateService.getObjectState(site, repoOperation.getPath(), false);

                        if (state == null) {
                            logger.debug("Insert item state for site: " + site + " path: " + repoOperation.getPath());
                            objectStateService.insertNewEntry(site, repoOperation.getPath());
                        } else {
                            logger.debug("Set item state for site: " + site + " path: " + repoOperation.getPath());
                            objectStateService.transition(site, repoOperation.getPath(), TransitionEvent.SAVE);
                        }

                        logger.debug("Set item metadata for site: " + site + " path: " + repoOperation.getPath());
                        if (!objectMetadataManager.metadataExist(site, repoOperation.getPath())) {
                            objectMetadataManager.insertNewObjectMetadata(site, repoOperation.getPath());
                        }
                        properties = new HashMap<String, Object>();
                        properties.put(ItemMetadata.PROP_SITE, site);
                        properties.put(ItemMetadata.PROP_PATH, repoOperation.getPath());
                        properties.put(ItemMetadata.PROP_MODIFIER, repoOperation.getAuthor());
                        properties.put(ItemMetadata.PROP_MODIFIED, repoOperation.getDateTime());
                        properties.put(ItemMetadata.PROP_COMMIT_ID, repoOperation.getCommitId());
                        objectMetadataManager.setObjectMetadata(site, repoOperation.getMoveToPath(), properties);
                        logger.debug("Extract dependencies for site: " + site + " path: " +
                                repoOperation.getPath());
                        toReturn = toReturn && extractDependenciesForItem(site, repoOperation.getPath());
                        contentClass = contentService.getContentTypeClass(site, repoOperation.getPath());
                        if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                        }
                        if (generateAuditLog) {
                            logger.debug("Insert audit log for site: " + site + " path: " + repoOperation.getPath());
                            activityService.postActivity(site, repoOperation.getAuthor(), repoOperation.getPath(),
                                    ActivityService.ActivityType.CREATED, ActivityService.ActivitySource.REPOSITORY,
                                    activityInfo);
                        }
                        break;

                    case UPDATE:
                        logger.debug("Set item state for site: " + site + " path: " + repoOperation.getPath());
                        objectStateService.getObjectState(site, repoOperation.getPath());
                        objectStateService.transition(site, repoOperation.getPath(), TransitionEvent.SAVE);

                        logger.debug("Set item metadata for site: " + site + " path: " + repoOperation.getPath());
                        if (!objectMetadataManager.metadataExist(site, repoOperation.getPath())) {
                            objectMetadataManager.insertNewObjectMetadata(site, repoOperation.getPath());
                        }
                        properties = new HashMap<String, Object>();
                        properties.put(ItemMetadata.PROP_SITE, site);
                        properties.put(ItemMetadata.PROP_PATH, repoOperation.getPath());
                        properties.put(ItemMetadata.PROP_MODIFIER, repoOperation.getAuthor());
                        properties.put(ItemMetadata.PROP_MODIFIED, repoOperation.getDateTime());
                        properties.put(ItemMetadata.PROP_COMMIT_ID, repoOperation.getCommitId());
                        objectMetadataManager.setObjectMetadata(site, repoOperation.getMoveToPath(), properties);

                        logger.debug("Extract dependencies for site: " + site + " path: " + repoOperation.getPath());
                        toReturn = toReturn && extractDependenciesForItem(site, repoOperation.getPath());
                        contentClass = contentService.getContentTypeClass(site, repoOperation.getPath());
                        if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                        }
                        if (generateAuditLog) {
                            logger.debug("Insert audit log for site: " + site + " path: " + repoOperation.getPath());
                            activityService.postActivity(site, repoOperation.getAuthor(), repoOperation.getPath(),
                                    ActivityService.ActivityType.UPDATED, ActivityService.ActivitySource.REPOSITORY,
                                    activityInfo);
                        }
                        break;

                    case DELETE:
                        logger.debug("Delete item state for site: " + site + " path: " + repoOperation.getPath());
                        objectStateService.deleteObjectStateForPath(site, repoOperation.getPath());
                        logger.debug("Delete item metadata for site: " + site + " path: " + repoOperation.getPath());
                        objectMetadataManager.deleteObjectMetadata(site, repoOperation.getPath());
                        logger.debug("Extract dependencies for site: " + site + " path: " + repoOperation.getPath());
                        try {
                            dependencyService.deleteItemDependencies(site, repoOperation.getPath());
                        } catch (ServiceLayerException e) {
                            logger.error("Error deleting dependencies for site " + site + " file: " +
                                    repoOperation.getPath(), e);
                        }
                        contentClass = contentService.getContentTypeClass(site, repoOperation.getPath());
                        if (repoOperation.getPath().endsWith(DmConstants.XML_PATTERN)) {
                            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                        }
                        if (generateAuditLog) {
                            logger.debug("Insert audit log for site: " + site + " path: " + repoOperation.getPath());
                            activityService.postActivity(site, repoOperation.getAuthor(), repoOperation.getPath(),
                                    ActivityService.ActivityType.DELETED, ActivityService.ActivitySource.REPOSITORY,
                                    activityInfo);
                        }
                        break;

                    case MOVE:
                        ItemState stateRename = objectStateService.getObjectState(site, repoOperation.getPath(), false);
                        logger.debug("Set item state for site: " + site + " path: " + repoOperation.getMoveToPath());
                        if (stateRename == null) {
                            objectStateService.getObjectState(site, repoOperation.getMoveToPath());
                            objectStateService.transition(site, repoOperation.getMoveToPath(), TransitionEvent.SAVE);
                        } else {
                            objectStateService.updateObjectPath(site, repoOperation.getPath(),
                                    repoOperation.getMoveToPath());
                            objectStateService.transition(site, repoOperation.getMoveToPath(), TransitionEvent.SAVE);
                        }

                        logger.debug("Set item metadata for site: " + site + " path: " +
                                repoOperation.getMoveToPath());
                        if (!objectMetadataManager.metadataExist(site, repoOperation.getPath())) {
                            if (!objectMetadataManager.metadataExist(site, repoOperation.getMoveToPath())) {
                                objectMetadataManager.insertNewObjectMetadata(site, repoOperation.getMoveToPath());
                            } else {
                                if (!objectMetadataManager.isRenamed(site, repoOperation.getMoveToPath())) {
                                    // set renamed and old path
                                    properties = new HashMap<String, Object>();
                                    properties.put(ItemMetadata.PROP_SITE, site);
                                    properties.put(ItemMetadata.PROP_PATH, repoOperation.getMoveToPath());
                                    properties.put(ItemMetadata.PROP_RENAMED, 1);
                                    properties.put(ItemMetadata.PROP_OLD_URL, repoOperation.getPath());
                                    properties.put(ItemMetadata.PROP_COMMIT_ID, repoOperation.getCommitId());
                                    properties.put(ItemMetadata.PROP_MODIFIER, repoOperation.getAuthor());
                                    properties.put(ItemMetadata.PROP_MODIFIED, repoOperation.getDateTime());
                                    objectMetadataManager.setObjectMetadata(site, repoOperation.getMoveToPath(),
                                            properties);
                                }
                            }
                        } else {
                            if (!objectMetadataManager.metadataExist(site, repoOperation.getMoveToPath())) {
                                // preform move: update path, set renamed, set old url
                                objectMetadataManager.updateObjectPath(site, repoOperation.getPath(),
                                        repoOperation.getMoveToPath());
                                properties = new HashMap<String, Object>();
                                properties.put(ItemMetadata.PROP_SITE, site);
                                properties.put(ItemMetadata.PROP_PATH, repoOperation.getMoveToPath());
                                properties.put(ItemMetadata.PROP_RENAMED, 1);
                                properties.put(ItemMetadata.PROP_OLD_URL, repoOperation.getPath());
                                properties.put(ItemMetadata.PROP_COMMIT_ID, repoOperation.getCommitId());
                                properties.put(ItemMetadata.PROP_MODIFIER, repoOperation.getAuthor());
                                objectMetadataManager.setObjectMetadata(site, repoOperation.getMoveToPath(), properties);
                            } else {
                                // if not already renamed set renamed and old url
                                if (!objectMetadataManager.isRenamed(site, repoOperation.getMoveToPath())) {
                                    // set renamed and old path
                                    properties = new HashMap<String, Object>();
                                    properties.put(ItemMetadata.PROP_SITE, site);
                                    properties.put(ItemMetadata.PROP_PATH, repoOperation.getMoveToPath());
                                    properties.put(ItemMetadata.PROP_RENAMED, 1);
                                    properties.put(ItemMetadata.PROP_OLD_URL, repoOperation.getPath());
                                    properties.put(ItemMetadata.PROP_COMMIT_ID, repoOperation.getCommitId());
                                    properties.put(ItemMetadata.PROP_MODIFIER, repoOperation.getAuthor());
                                    objectMetadataManager.setObjectMetadata(site, repoOperation.getMoveToPath(),
                                            properties);
                                }
                                objectMetadataManager.deleteObjectMetadata(site, repoOperation.getPath());
                            }
                        }

                        logger.debug("Extract dependencies for site: " + site + " path: " + repoOperation.getPath());
                        toReturn = toReturn && extractDependenciesForItem(site, repoOperation.getMoveToPath());
                        contentClass = contentService.getContentTypeClass(site, repoOperation.getMoveToPath());
                        if (repoOperation.getMoveToPath().endsWith(DmConstants.XML_PATTERN)) {
                            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
                        }
                        if (generateAuditLog) {
                            logger.debug("Insert audit log for site: " + site + " path: " +
                                    repoOperation.getMoveToPath());
                            activityService.postActivity(site, repoOperation.getAuthor(), repoOperation.getMoveToPath(),
                                    ActivityService.ActivityType.UPDATED, ActivityService.ActivitySource.REPOSITORY,
                                    activityInfo);
                        }
                        break;

                    default:
                        logger.error("Error: Unknown repo operation for site " + site + " operation: " +
                                repoOperation.getOperation());
                        toReturn = false;
                        break;
                }
            }
	    }
        if (current != null) {
            contentRepository.markGitLogVerifiedProcessed(site, current.getCommitId());
            updateLastVerifiedGitlogCommitId(site, current.getCommitId());
        }

	    // At this point we have attempted to process all operations, some may have failed
	    // We will update the lastCommitId of the database ignoring errors if any
	    logger.debug("Done syncing operations with a result of: " + toReturn);
        logger.debug("Syncing database lastCommitId for site: " + site);

	    // Update database
        String lastCommitId = contentRepository.getRepoLastCommitId(site);
        logger.debug("Update last commit id " + lastCommitId + " for site " + site);
        updateLastCommitId(site, lastCommitId);
        // Sync all preview deployers
        try {
            logger.debug("Sync preview for site " + site);
            deploymentService.syncAllContentToPreview(site, false);
        } catch (ServiceLayerException e) {
            logger.error("Error synchronizing preview with repository for site: " + site, e);
        }

	    logger.info("Done syncing database with repository for site: " + site + " fromCommitId = " +
                (StringUtils.isEmpty(fromCommitId) ? "Empty repo" : fromCommitId) + " with a final result of: " +
                toReturn);
        logger.info("Last commit ID for site: " + site + " is " + lastCommitId);

        if (!toReturn) {
	        // Some operations failed during sync database from repo
	        // Must log and make some noise here, this isn't great
	        logger.error("Some operations failed to sync to database for site: " + site + " see previous error logs");
        }

	    return toReturn;
    }

    protected boolean extractDependenciesForItem(String site, String path) {
		boolean toReturn = true;

	    try {
		    if (path.endsWith(DmConstants.XML_PATTERN)) {
			    SAXReader saxReader = new SAXReader();
				try {
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
					saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				}catch (SAXException ex){
					logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
				}

			    dependencyService.upsertDependencies(site, path);
		    } else {
			    boolean isCss = path.endsWith(DmConstants.CSS_PATTERN);
			    boolean isJs = path.endsWith(DmConstants.JS_PATTERN);
			    boolean isTemplate = ContentUtils.matchesPatterns(path, servicesConfig.getRenderingTemplatePatterns
				    (site));
			    if (isCss || isJs || isTemplate) {
				    dependencyService.upsertDependencies(site, path);
			    }
		    }
	    } catch (ServiceLayerException e) {
		    logger.error("Error extracting dependencies for site " + site + " file: " + path, e);
		    toReturn = false;
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
            siteFeedMapper.enablePublishing(params);
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public boolean updatePublishingStatusMessage(@ValidateStringParam(name = "siteId") String siteId,
                                                 @ValidateStringParam(name = "message") String message)
            throws SiteNotFoundException {
        if (exists(siteId)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            params.put("message", message);
            siteFeedMapper.updatePublishingStatusMessage(params);
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    @ValidateParams
    public PublishStatus getPublishStatus(@ValidateStringParam(name = "site") String site)
            throws SiteNotFoundException {
        SiteFeed siteFeed = getSite(site);
        String psm = siteFeed.getPublishingStatusMessage();
        PublishStatus ps = new PublishStatus();
        if (StringUtils.isNotEmpty(psm)) {
            StringTokenizer tokenizer = new StringTokenizer(psm, "|");
            if (tokenizer.countTokens() > 1) {
                ps.setStatus(tokenizer.nextToken());
                ps.setMessage(tokenizer.nextToken());
            } else {
                ps.setMessage(psm);
            }
        } else {
            psm = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_DEFAULT);
            StringTokenizer tokenizer = new StringTokenizer(psm, "|");
            if (tokenizer.countTokens() > 1) {
                ps.setStatus(tokenizer.nextToken());
                ps.setMessage(tokenizer.nextToken());
            } else {
                ps.setMessage(psm);
            }
        }
        return ps;
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

    private void insertAddRemoteAuditLog(String siteId, String remoteName) {
        ActivityService.ActivityType activityType = ActivityService.ActivityType.ADD_REMOTE;
        String user = securityService.getCurrentUser();
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_REMOTE_REPOSITORY);
        activityService.postActivity(siteId, user,
                remoteName , activityType, ActivityService.ActivitySource.API, extraInfo);
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) throws SiteNotFoundException {
        if (!exists(siteId)) {
            throw new SiteNotFoundException();
        }
        boolean toRet = contentRepository.removeRemote(siteId, remoteName);
        insertRemoveRemoteAuditLog(siteId, remoteName);
        return toRet;
    }

    private void insertRemoveRemoteAuditLog(String siteId, String remoteName) {
        ActivityService.ActivityType activityType = ActivityService.ActivityType.REMOVE_REMOTE;
        String user = securityService.getCurrentUser();
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_REMOTE_REPOSITORY);
        activityService.postActivity(siteId, user,
                remoteName , activityType, ActivityService.ActivitySource.API, extraInfo);
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId) throws ServiceLayerException {
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

    public String getGlobalConfigRoot() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    public String getSitesConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
    }

    public String getSitesMultiEnvironmentConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH);
    }

    public String getEnvironment() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT);
    }

    public String getEnvironmentConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH);
    }

    public List<String> getDefaultGroups() {
        return Arrays.asList(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_GROUPS).split(","));
    }

    public String getDefaultAdminGroup() {
	    return studioConfiguration.getProperty(CONFIGURATION_DEFAULT_ADMIN_GROUP);
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

	public SiteEnvironmentConfig getEnvironmentConfig() {
	    return environmentConfig;
	}
	public void setEnvironmentConfig(SiteEnvironmentConfig environmentConfig) {
	    this.environmentConfig = environmentConfig;
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

	public ActivityService getActivityService() {
	    return activityService;
	}
	public void setActivityService(ActivityService activityService) {
	    this.activityService = activityService;
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

	public PreviewDeployer getPreviewDeployer() {
		return previewDeployer;
	}
	public void setPreviewDeployer(final PreviewDeployer previewDeployer) {
		this.previewDeployer = previewDeployer;
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

}
