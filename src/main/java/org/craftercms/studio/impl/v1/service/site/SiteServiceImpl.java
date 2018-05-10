/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.service.site;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.ebus.annotations.EventHandler;
import org.craftercms.commons.ebus.annotations.EventSelectorType;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.ebus.*;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.configuration.SiteEnvironmentConfig;
import org.craftercms.studio.api.v1.service.content.*;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.impl.v1.ebus.ClearConfigurationCache;
import org.craftercms.studio.impl.v1.repository.job.RebuildRepositoryMetadata;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;

import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import reactor.core.Reactor;
import reactor.event.Event;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.  
 * This service provides access to site configuration
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService {

	private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

    private final static String CACHE_KEY_PATH = "/cstudio/config/sites/{site}";

    public void init() {
        CacheService cacheService = cacheTemplate.getCacheService();
        List<SiteFeed> sites = siteFeedMapper.getSites();
        StudioCacheContext cacheContext = new StudioCacheContext(CStudioConstants.CACHE_GLOBAL_SCOPE, true);
        cacheService.addScope(cacheContext);
        cacheContext = new StudioCacheContext(CStudioConstants.CACHE_USERS_SCOPE, true);
        cacheService.addScope(cacheContext);
        for (SiteFeed site : sites) {
            cacheContext = new StudioCacheContext(site.getSiteId(), false);
            cacheService.addScope(cacheContext);
            cacheContext = new StudioCacheContext(site.getSiteId(), true);
            cacheService.addScope(cacheContext);
        }
    }

    public void destroy() {
        CacheService cacheService = cacheTemplate.getCacheService();
        List<SiteFeed> sites = siteFeedMapper.getSites();
        StudioCacheContext cacheContext = new StudioCacheContext(CStudioConstants.CACHE_GLOBAL_SCOPE, true);
        cacheService.removeScope(cacheContext);
        cacheContext = new StudioCacheContext(CStudioConstants.CACHE_USERS_SCOPE, true);
        cacheService.removeScope(cacheContext);
        for (SiteFeed site : sites) {
            cacheContext = new StudioCacheContext(site.getSiteId(), false);
            cacheService.removeScope(cacheContext);
            cacheContext = new StudioCacheContext(site.getSiteId(), true);
            cacheService.removeScope(cacheContext);
        }
    }
	
	@Override
    @ValidateParams
	public boolean writeConfiguration(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, InputStream content) throws ServiceException {
		boolean toRet = contentRepository.writeContent("/cstudio/config/sites/"+site+"/"+path, content);
        clearConfigurationCache.clearConfigurationCache(site);
        return toRet;
	}

	@Override
    @ValidateParams
	public boolean writeConfiguration(@ValidateSecurePathParam(name = "path") String path, InputStream content) throws ServiceException {
		boolean toReturn = contentRepository.writeContent(path, content);
        String site = extractSiteFromConfigurationPath(path);
        clearConfigurationCache.clearConfigurationCache(site);
        return toReturn;
	}

    private String extractSiteFromConfigurationPath(String configurationPath) {
        String var = configurationPath.replace("/cstudio/config/sites/", "");
        int idx = var.indexOf("/");
        String site = var.substring(0, idx);
        return site;
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
	public Map<String, Object> getConfiguration(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, boolean applyEnv) {
		String configPath = "";
		if (StringUtils.isEmpty(site)) {
			configPath = this.configRoot + path;
		} else {
			if (applyEnv) {
				configPath = this.environmentConfigPath.replaceAll(CStudioConstants.PATTERN_SITE, site).replaceAll(
						CStudioConstants.PATTERN_ENVIRONMENT, environment)
						+ path;
			} else {
				configPath = this.sitesConfigPath + "/" + site + path;
			}
		}
		logger.debug("[SITESERVICE] loading configuration at " + configPath);
		String configContent = contentService.getContentAsString(configPath);

		JSON response = null;
		Map<String, Object> toRet = null;
		if (configContent != null) {
			configContent = configContent.replaceAll("\\n([\\s]+)?+", "");
			configContent = configContent.replaceAll("<!--(.*?)-->", "");
			toRet = convertNodesFromXml(configContent);
		} else {
			response = new JSONObject();
		}
		return toRet;
	}

	private Map<String, Object> convertNodesFromXml(String xml) {
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());

			Document document = DocumentHelper.parseText(xml);
			return createMap(document.getRootElement());

		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}

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

	/**
	 * load site configuration info (not environment specific)
	 *
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteConfig(String site, SiteTO siteConfig) {
		// get site configuration
		siteConfig.setWebProject(servicesConfig.getWemProject(site));
		siteConfig.setRepositoryRootPath("/wem-projects/" + site + "/" + site + "/work-area");
	}

	/***
	 * load site environment specific info
	 *
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteEnvironmentConfig(String site, SiteTO siteConfig) {
		// get environment specific configuration
		logger.debug("Loading site environment configuration for " + site + "; Environemnt: " + environment);
		EnvironmentConfigTO environmentConfigTO = environmentConfig.getEnvironmentConfig(site);
		if (environmentConfigTO == null) {
			logger.error("Environment configuration for site " + site + " does not exist.");
			return;
		}
		siteConfig.setLiveUrl(environmentConfigTO.getLiveServerUrl());
		siteConfig.setAuthoringUrl(environmentConfigTO.getAuthoringServerUrl());
		siteConfig.setAuthoringUrlPattern(environmentConfigTO.getAuthoringServerUrlPattern());
		siteConfig.setPreviewUrl(environmentConfigTO.getPreviewServerUrl());
		siteConfig.setPreviewUrlPattern(environmentConfigTO.getPreviewServerUrlPattern());
		siteConfig.setAdminEmail(environmentConfigTO.getAdminEmailAddress());
		siteConfig.setCookieDomain(environmentConfigTO.getCookieDomain());
		siteConfig.setOpenSiteDropdown(environmentConfigTO.getOpenDropdown());
		siteConfig.setFormServerUrl(environmentConfigTO.getFormServerUrlPattern());
		siteConfig.setPublishingChannelGroupConfigs(environmentConfigTO.getPublishingChannelGroupConfigs());
	}

	/***
	 * load site environment specific info
	 *
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteDeploymentConfig(String site, SiteTO siteConfig) {
		// get environment specific configuration
		logger.debug("Loading deployment configuration for " + site + "; Environment: " + environment);
		DeploymentConfigTO deploymentConfig = deploymentEndpointConfig.getSiteDeploymentConfig(site);
		if (deploymentConfig == null) {
			logger.error("Deployment configuration for site " + site + " does not exist.");
			return;
		}
		siteConfig.setDeploymentEndpointConfigs(deploymentConfig.getEndpointMapping());
	}

    @Override
    @ValidateParams
    public DeploymentEndpointConfigTO getDeploymentEndpoint(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "endpoint") String endpoint) {
        return deploymentEndpointConfig.getDeploymentConfig(site, endpoint);
    }

    @Override
    @ValidateParams
    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(@ValidateStringParam(name = "site") String site) {
        return environmentConfig.getPublishingChannelGroupConfigs(site);
    }

	@Override
    @ValidateParams
	public List<SiteFeed> getUserSites(@ValidateStringParam(name = "user") String user) {
        String username = user;
        if (StringUtils.isEmpty(username)) {
            username = securityService.getCurrentUser();
        }
        List<SiteFeed> sites = siteFeedMapper.getSites();
        List<SiteFeed> toRet = new ArrayList<SiteFeed>();
        for (SiteFeed site : sites) {
            Set<String> userRoles = securityService.getUserRoles(site.getSiteId(),username);
            if (CollectionUtils.isNotEmpty(userRoles)) {
                site.setLiveUrl(getLiveServerUrl(site.getSiteId()));
                toRet.add(site);
            }
        }
        return toRet;
	}

    @Override
    @ValidateParams
    public DeploymentEndpointConfigTO getPreviewDeploymentEndpoint(@ValidateStringParam(name = "site") String site) {
        String endpoint = environmentConfig.getPreviewDeploymentEndpoint(site);
        return getDeploymentEndpoint(site, endpoint);
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
    @ValidateParams
    public String getLiveEnvironmentName(@ValidateStringParam(name = "site") String site) {
        PublishingChannelGroupConfigTO pcgcTO = environmentConfig.getLiveEnvironmentPublishingGroup(site);
        if (pcgcTO != null) {
            return pcgcTO.getName();
        } else {
            return null;
        }
    }

   	@Override
    @ValidateParams
   	public boolean createSiteFromBlueprint(@ValidateStringParam(name = "blueprintName") String blueprintName, @ValidateNoTagsParam(name = "siteName") String siteName, @ValidateNoTagsParam(name = "siteId") String siteId, @ValidateNoTagsParam(name = "desc") String desc) {
 		boolean success = true;
 		try {
			contentRepository.createFolder("/wem-projects/"+siteId+"/"+siteId, "work-area");
			contentRepository.copyContent("/cstudio/blueprints/"+blueprintName+"/site-content",
				"/wem-projects/"+siteId+"/"+siteId+"/work-area");

	 		String siteConfigFolder = "/cstudio/config/sites/"+siteId;
 			contentRepository.createFolder("/cstudio/config/sites/", siteId);
	 		contentRepository.copyContent("/cstudio/blueprints/" + blueprintName + "/site-config",
					siteConfigFolder);

			replaceFileContent(siteConfigFolder + "/site-config.xml", "SITENAME", siteId);
	 		replaceFileContent(siteConfigFolder+"/role-mappings-config.xml", "SITENAME", siteId);
	 		replaceFileContent(siteConfigFolder + "/permission-mappings-config.xml", "SITENAME", siteId);

			// Add user groups
			securityService.addUserGroup("crafter_" + siteId);
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_admin");
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_author");
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_viewer");
			securityService.addUserToGroup("crafter_" + siteId + "_admin", securityService.getCurrentUser());

            // set permissions for groups
            securityProvider.addContentWritePermission("/wem-projects/"+siteId, "crafter_" + siteId + "_admin");
            securityProvider.addConfigWritePermission("/cstudio/config/sites/"+siteId, "crafter_" + siteId + "_admin");
            securityProvider.addContentWritePermission("/wem-projects/"+siteId, "crafter_" + siteId + "_author");

			// Set object states
			createObjectStatesforNewSite(siteId);

			// Extract metadata ?

	 		// permissions
	 		// environment overrides
	 		// deployment

	 		// insert database records
			SiteFeed siteFeed = new SiteFeed();
			siteFeed.setName(siteName);
			siteFeed.setSiteId(siteId);
			siteFeed.setDescription(desc);
			siteFeedMapper.createSite(siteFeed);

            // Extract dependencies
            extractDependenciesForNewSite(siteId);

            CacheService cacheService = cacheTemplate.getCacheService();
            StudioCacheContext cacheContext = new StudioCacheContext(siteId, true);
            cacheService.addScope(cacheContext);
            cacheContext = new StudioCacheContext(siteId, false);
            cacheService.addScope(cacheContext);
            reloadSiteConfiguration(siteId);
            deploymentService.syncAllContentToPreview(siteId);
        }
	 	catch(Exception err) {
            success = false;
            logger.error("Error while creating site", err);
	 	}

	 	return success;
    }

    protected void replaceFileContent(String path, String find, String replace) throws Exception {
    	InputStream content = contentRepository.getContent(path);
    	String contentAsString = IOUtils.toString(content);

    	contentAsString = contentAsString.replaceAll(find, replace);

    	InputStream contentToWrite = IOUtils.toInputStream(contentAsString);

		contentRepository.writeContent(path, contentToWrite);    	
    }

	protected void createObjectStatesforNewSite(String site) {
		createObjectStateNewSiteObjectFolder(site, contentService.expandRelativeSitePath(site, "/"));
	}

	protected void createObjectStateNewSiteObjectFolder(String site, String path) {
		RepositoryItem[] children = contentRepository.getContentChildren(path);
		for (RepositoryItem child : children) {
			if (child.isFolder) {
				createObjectStateNewSiteObjectFolder(site, child.path + "/" + child.name);
			} else {
				objectStateService.insertNewEntry(site, contentService.getRelativeSitePath(site, child.path) + "/" + child.name);
			}
		}
	}

	protected void extractDependenciesForNewSite(String site) {
        Map<String, Set<String>> globalDeps = new HashMap<String, Set<String>>();
        extractDependenciesItemForNewSite(site, contentService.expandRelativeSitePath(site, "/"), globalDeps);
	}

    private void extractDependenciesItemForNewSite(String site, String fullPath, Map<String, Set<String>> globalDeps) {
        RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
        for (RepositoryItem child : children) {
            if (child.isFolder) {
                extractDependenciesItemForNewSite(site, child.path + "/" + child.name, globalDeps);
            } else {
                String childFullPath = child.path + "/" + child.name;
                DmPathTO dmPathTO = new DmPathTO(childFullPath);
                String relativePath = dmPathTO.getRelativePath();

                try {
                    dependencyService.upsertDependencies(site, relativePath);
                } catch (ContentNotFoundException e) {
                    logger.error("Failed to extract dependencies for document: " + childFullPath, e);
                } catch (ServiceException e) {
                    logger.error("Failed to extract dependencies for document: " + childFullPath, e);
                }

            }
        }
    }

	@Override
    @ValidateParams
   	public boolean deleteSite(@ValidateStringParam(name = "siteId") String siteId) {
 		boolean success = true;
 		try {
 			contentRepository.deleteContent("/wem-projects/"+siteId);
 			contentRepository.deleteContent("/cstudio/config/sites/" + siteId);

	 		// delete database records
			siteFeedMapper.deleteSite(siteId);
			activityService.deleteActivitiesForSite(siteId);
			dependencyService.deleteSiteDependencies(siteId);
            deploymentService.deleteDeploymentDataForSite(siteId);
            objectStateService.deleteObjectStatesForSite(siteId);
            dmPageNavigationOrderService.deleteSequencesForSite(siteId);

            CacheService cacheService = cacheTemplate.getCacheService();
            StudioCacheContext cacheContext = new StudioCacheContext(siteId, true);
            if (cacheService.hasScope(cacheContext)) {
                cacheService.removeScope(cacheContext);
            }
            cacheContext = new StudioCacheContext(siteId, false);
            if (cacheService.hasScope(cacheContext)) {
                cacheService.removeScope(cacheContext);
            }
	 	}
	 	catch(Exception err) {
	 		success = false;
	 	}

	 	return success;
    }

    @Override
	public SiteBlueprintTO[] getAvailableBlueprints() {
		RepositoryItem[] blueprintsFolders = contentRepository.getContentChildren("/cstudio/blueprints");
		SiteBlueprintTO[] blueprints = new SiteBlueprintTO[blueprintsFolders.length];
		int idx = 0;
		for (RepositoryItem folder : blueprintsFolders) {
			SiteBlueprintTO blueprintTO = new SiteBlueprintTO();
			blueprintTO.id = folder.name;
			blueprintTO.label = StringUtils.capitalize(folder.name);
			blueprintTO.description = ""; // How do we populate this dynamicly
			blueprintTO.screenshots = null;
			blueprints[idx++] = blueprintTO;
		}

		return blueprints;
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

        if (sites != null && sites.size() > 0) {
            for (String site : sites) {
                clearConfigurationCache.clearConfigurationCache(site);
            }
        } else {
            logger.error("[SITESERVICE] no sites found");
        }
    }

    @Override
    @ValidateParams
    public void reloadSiteConfiguration(@ValidateStringParam(name = "site") String site) {
        reloadSiteConfiguration(site, true);
    }

    @EventHandler(
            event = EBusConstants.CLUSTER_CLEAR_CACHE_EVENT,
            ebus = EBusConstants.DISTRIBUTED_REACTOR,
            type = EventSelectorType.REGEX
    )
    public void onClearCacheEvent(final Event<ClearCacheEventMessage> event) {
        logger.debug("On clear cache event");
        ClearCacheEventMessage message = event.getData();
        reloadSiteConfiguration(message.getSite(), false);
    }

    @Override
    @ValidateParams
    public void reloadSiteConfiguration(@ValidateStringParam(name = "site") String site, boolean triggerEvent) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, CACHE_KEY_PATH.replaceFirst(CStudioConstants.PATTERN_SITE, site), "SiteTO");
        cacheService.remove(cacheContext, cacheKey);
        SiteTO siteConfig = new SiteTO();
        siteConfig.setSite(site);
        siteConfig.setEnvironment(this.environment);
        servicesConfig.reloadConfiguration(site);
        loadSiteConfig(site, siteConfig);
        environmentConfig.reloadConfiguration(site);
        loadSiteEnvironmentConfig(site, siteConfig);
        deploymentEndpointConfig.reloadConfiguration(site);
        loadSiteDeploymentConfig(site, siteConfig);
        cacheService.put(cacheContext, cacheKey, siteConfig);
        notificationService.reloadConfiguration(site);
		notificationService2.reloadConfiguration(site);
        securityService.reloadConfiguration(site);
        contentTypeService.reloadConfiguration(site);
        if (triggerEvent) {
            ClearCacheEventMessage message = new ClearCacheEventMessage(site);
            DistributedEventMessage distributedEventMessage = new DistributedEventMessage();
            distributedEventMessage.setEventKey(EBusConstants.CLUSTER_CLEAR_CACHE_EVENT);
            distributedEventMessage.setMessageClass(ClearCacheEventMessage.class);
            distributedEventMessage.setMessage(message);
            distributedPeerEBusFacade.notifyCluster(distributedEventMessage);
        }
        cacheService.put(cacheContext, cacheKey, siteConfig);
    }

    @Override
    public void reloadGlobalConfiguration() {
        securityService.reloadGlobalConfiguration();
    }

    @Override
    public void importSite(String config) {

    }

    @Override
    @ValidateParams
    public boolean exists(@ValidateStringParam(name = "site") String site) {
	    boolean toRet = siteFeedMapper.exists(site) > 0 ? true : false;
        return toRet;
    }

    @Override
    @ValidateParams
    public void rebuildRepositoryMetadata(@ValidateStringParam(name = "site") String site) {
        rebuildRepositoryMetadata.execute(site);
    }

    /** getter site service dal */
	public SiteServiceDAL getSiteService() { return _siteServiceDAL; }
	/** setter site service dal */
	public void setSiteServiceDAL(SiteServiceDAL service) { _siteServiceDAL = service; }

	public ServicesConfig getServicesConfig() { return servicesConfig; }
	public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

	public ContentService getContentService() { return contentService; }
	public void setContentService(ContentService contentService) { this.contentService = contentService; }

	public String getSitesConfigPath() { return sitesConfigPath; }
	public void setSitesConfigPath(String sitesConfigPath) { this.sitesConfigPath = sitesConfigPath; }

	public String getEnvironment() { return environment; }
	public void setEnvironment(String environment) { this.environment = environment; }

	public SiteEnvironmentConfig getEnvironmentConfig() { return environmentConfig; }
	public void setEnvironmentConfig(SiteEnvironmentConfig environmentConfig) { this.environmentConfig = environmentConfig; }

	public DeploymentEndpointConfig getDeploymentEndpointConfig() { return deploymentEndpointConfig; }
	public void setDeploymentEndpointConfig(DeploymentEndpointConfig deploymentEndpointConfig) { this.deploymentEndpointConfig = deploymentEndpointConfig; }

	public String getConfigRoot() { return configRoot; }
	public void setConfigRoot(String configRoot) { this.configRoot = configRoot; }

	public String getEnvironmentConfigPath() { return environmentConfigPath; }
	public void setEnvironmentConfigPath(String environmentConfigPath) { this.environmentConfigPath = environmentConfigPath; }

	public ContentRepository getContenetRepository() { return contentRepository; }
	public void setContentRepository(ContentRepository repo) { contentRepository = repo; }

	public ObjectStateService getObjectStateService() { return objectStateService; }
	public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

	public DependencyService getDependencyService() { return dependencyService; }
	public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

	public SecurityService getSecurityService() { return securityService; }
	public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

	public ActivityService getActivityService() { return activityService; }
	public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

	public DeploymentService getDeploymentService() { return deploymentService; }
	public void setDeploymentService(DeploymentService deploymentService) { this.deploymentService = deploymentService; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public DmPageNavigationOrderService getDmPageNavigationOrderService() { return dmPageNavigationOrderService; }
    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) { this.dmPageNavigationOrderService = dmPageNavigationOrderService; }

    public Reactor getRepositoryRector() { return repositoryRector; }
    public void setRepositoryRector(Reactor repositoryRector) { this.repositoryRector = repositoryRector; }

    public NotificationService getNotificationService() { return notificationService; }
    public void setNotificationService(NotificationService notificationService) { this.notificationService = notificationService; }

    public ContentTypeService getContentTypeService() { return contentTypeService; }
    public void setContentTypeService(ContentTypeService contentTypeService) { this.contentTypeService = contentTypeService; }

    public DistributedPeerEBusFacade getDistributedPeerEBusFacade() { return distributedPeerEBusFacade; }
    public void setDistributedPeerEBusFacade(DistributedPeerEBusFacade distributedPeerEBusFacade) { this.distributedPeerEBusFacade = distributedPeerEBusFacade; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public ClearConfigurationCache getClearConfigurationCache() { return clearConfigurationCache; }
    public void setClearConfigurationCache(ClearConfigurationCache clearConfigurationCache) { this.clearConfigurationCache = clearConfigurationCache; }

    public ImportService getImportService() { return importService; }
    public void setImportService(ImportService importService) { this.importService = importService; }

	public void setNotificationService2(final org.craftercms.studio.api.v2.service.notification.NotificationService
											notificationService2) {
		this.notificationService2 = notificationService2;
	}

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public RebuildRepositoryMetadata getRebuildRepositoryMetadata() { return rebuildRepositoryMetadata; }
    public void setRebuildRepositoryMetadata(RebuildRepositoryMetadata rebuildRepositoryMetadata) { this.rebuildRepositoryMetadata = rebuildRepositoryMetadata; }

    protected SiteServiceDAL _siteServiceDAL;
	protected ServicesConfig servicesConfig;
	protected ContentService contentService;
	protected String sitesConfigPath;
	protected String environment;
	protected SiteEnvironmentConfig environmentConfig;
	protected DeploymentEndpointConfig deploymentEndpointConfig;
	protected String configRoot = null;
	protected String environmentConfigPath = null;
	protected ContentRepository contentRepository;
	protected ObjectStateService objectStateService;
	protected DependencyService dependencyService;
	protected SecurityService securityService;
	protected ActivityService activityService;
	protected DeploymentService deploymentService;
    protected ObjectMetadataManager objectMetadataManager;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected Reactor repositoryRector;
    protected NotificationService notificationService;
    protected ContentTypeService contentTypeService;
    protected DistributedPeerEBusFacade distributedPeerEBusFacade;
    protected SecurityProvider securityProvider;
    protected CacheTemplate cacheTemplate;
    protected ClearConfigurationCache clearConfigurationCache;
    protected ImportService importService;
	protected org.craftercms.studio.api.v2.service.notification.NotificationService notificationService2;
    protected GeneralLockService generalLockService;
    protected RebuildRepositoryMetadata rebuildRepositoryMetadata;

	@Autowired
	protected SiteFeedMapper siteFeedMapper;


	/**
	 * a map of site key and site information
	 */

	protected SitesConfigTO sitesConfig = null;
}
