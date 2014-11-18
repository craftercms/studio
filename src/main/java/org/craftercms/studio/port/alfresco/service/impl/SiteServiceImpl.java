/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.service.impl;

import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.api.service.deployment.DeploymentService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmMetadataService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.*;
import org.craftercms.cstudio.alfresco.util.SearchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteServiceImpl extends ConfigurableServiceBase implements SiteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiteServiceImpl.class);

	public static final String KEY_SITES_MAPPINGS_SITE_ID = "siteId";
	public static final String KEY_SITES_MAPPINGS_PREVIEW_URL = "previewUrl";
	public static final String KEY_SITES_MAPPINGS_AUTHORING_URL = "authoringUrl";

	/**
	 * sites configuration
	 */
	protected SitesConfigTO _sitesConfig = null;
  
	/** environment key (e.g. dev, qa, staging..) **/
	protected String _environment;

	/**
	 * Sites Environment Configuration
	 */
	protected SiteEnvironmentConfig _environmentConfig;

	/**
	 * the root folder path of all site configuration files
	 */
    protected String _sitesConfigPath = null;

	/**
	 * the root folder of all configuration files 
	 */
	protected String _configRoot = null;

	/**
	 * the environment configuration file path
	 */
	protected String _environmentConfigPath = null;

	/**
	 * a map of site key and site information
	 */
	protected Map<String, SiteTO> sitesMappings;

	/** 
	 * a map of authroing url and site information
	 */
	protected List<SiteTO> sitesMappingsByAuthoringUrl;
	
	/** 
	 * a map of authroing url and site information
	 */
	protected List<SiteTO> sitesMappingsByPreviewUrl;

    protected String deploymentConfigPath;

    protected DeploymentEndpointConfig deploymentEndpointConfig;

    protected DeploymentService deploymentService;

    @Override
    public void register() {
        this._servicesManager.registerService(SiteService.class, this);
    }

    /*
    * (non-Javadoc)
    *
    * @see
    * org.craftercms.cstudio.alfresco.service.api.SiteService#getConfiguration
    * (java.lang.String, java.lang.String)
    */
	public String getConfiguration(String site, String path, boolean applyEnv) {
		//
		String configPath = "";
		NodeRef configRef = null;
		if (StringUtils.isEmpty(site)) {
			configPath = this._configRoot + path;
		} else {
			if (applyEnv) {
				configPath = this._environmentConfigPath.replaceAll(CStudioConstants.PATTERN_SITE, site).replaceAll(
						CStudioConstants.PATTERN_ENVIRONMENT, _environment)
						+ path;
			} else {
				configPath = this._sitesConfigPath + "/" + site + path;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[SITESERVICE] loading configuration at " + configPath);
		}
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getContentAsString(configPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SiteService#createPreviewUrl
	 * (java.lang.String, java.lang.String, java.lang.String, int,
	 * java.lang.String)
	 */
	public String createPreviewUrl(String site, String assetId, String storeId, int versionId, String geoId)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SiteService#getSitesMenuItems
	 * ()
	 */
	public Map<String, String> getSitesMenuItems() {
		checkForUpdate(null);
		if (_sitesConfig != null) {
			return _sitesConfig.getSitesMenu();
		} else {
			LOGGER.error("[SITESERVICE] No configuration found for SiteService");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SiteService#getPreviewServerUrl
	 * (java.lang.String)
	 */
	public String getPreviewServerUrl(String site) {
		return _environmentConfig.getPreviewServerUrl(site);
	}

	public String getLiveServerUrl(String site) {
		return _environmentConfig.getLiveServerUrl(site);
	}

	public String getAdminEmailAddress(String site) {
		return _environmentConfig.getAdminEmailAddress(site);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SiteService#getAuthoringServerUrl
	 * (java.lang.String)
	 */
	public String getAuthoringServerUrl(String site) {
		return _environmentConfig.getAuthoringServerUrl(site);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SiteService#getFormServerUrl(java.lang.String)
	 */
	public String getFormServerUrl(String site) {
		return _environmentConfig.getFormServerUrl(site);
	}
    
    @Override
    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site) {
        return _environmentConfig.getPublishingChannelGroupConfigs(site);
    }

    @Override
    public DeploymentEndpointConfigTO getPreviewDeploymentEndpoint(String site) {
        String endpoint = _environmentConfig.getPreviewDeploymentEndpoint(site);
        return getDeploymentEndpoint(site, endpoint);
    }

    @Override
    public String getLiveEnvironmentName(String site) {
        PublishingChannelGroupConfigTO pcgcTO = _environmentConfig.getLiveEnvironmentPublishingGroup(site);
        if (pcgcTO != null) {
            return pcgcTO.getName();
        } else {
            return null;
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SiteService#getCollabSandbox
	 * (java.lang.String)
	 */
	public String getCollabSandbox(String site) {
		return null;// _servicesConfig.getSandbox(site);
	}

	/*
	 * 
	 */
	public SiteTO getSite(String key, String value) {
		checkForUpdates();
		// set the default key to be site id if the key is not provided
		key = (StringUtils.isEmpty(key)) ? KEY_SITES_MAPPINGS_SITE_ID : key;
		if (this.sitesMappings != null) {
			SiteTO siteConfig = null;
			if (key.equalsIgnoreCase(KEY_SITES_MAPPINGS_SITE_ID)) {
				siteConfig = this.sitesMappings.get(value);
			} else {
				// find it by url matching
				for (String site : this.sitesMappings.keySet()) {
					SiteTO currentConfig = this.sitesMappings.get(site);
					if (currentConfig != null) {
						String pattern, url;
						if (key.equalsIgnoreCase(KEY_SITES_MAPPINGS_AUTHORING_URL)) {
							pattern = currentConfig.getAuthoringUrlPattern();
							url = currentConfig.getAuthoringUrl();
						} else {
							pattern = currentConfig.getPreviewUrlPattern();
							url = currentConfig.getPreviewUrl();
						}
						if (!StringUtils.isEmpty(pattern)) {
							if (value.matches(pattern)) {
								siteConfig = currentConfig;
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug("[SITESERVICE] " + value + " matches " + pattern + ". site: " + site);
								}
								break;
							}
						} else {
							if (value.startsWith(url)) {
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug("[SITESERVICE] " + value + " starts with " + url + ". site: " + site);
								}
								siteConfig = currentConfig;
								break;
							}
						}
					}
				}
			}
			if (siteConfig != null) {
				return siteConfig;
			} else {
				LOGGER.error("[SITESERVICE] No site found by key : " + key + " value: " + value);
				return null;
			}
		} else {
			LOGGER.error("[SITESERVICE] No sites mapping exists.");
			return null;
		}
	}

    @Override
    public void reloadSiteConfigurations() {
        if (this.sitesMappings == null) {
            this.sitesMappings = new FastMap<String, SiteTO>();
        }
        PersistenceManagerService persistenceManagerService=getService(PersistenceManagerService.class);
        //Map<String, SiteTO>
        String query = SearchUtils.createPathQuery(this._sitesConfigPath, true) + " AND "
                + SearchUtils.createTypeQuery(ContentModel.TYPE_FOLDER);
        List<FileInfo> sites = persistenceManagerService.listFolders(this._sitesConfigPath);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        
        if (sites != null && sites.size() > 0) {
            for (FileInfo siteInfo : sites) {
                NodeRef siteRef = siteInfo.getNodeRef();
                String site = (String)persistenceManagerService.getProperty(siteRef, ContentModel.PROP_NAME);
                SiteTO siteConfig = new SiteTO();
                if (this.sitesMappings.containsKey(site)) {
                    if (servicesConfig.isUpdated(site)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[SITESERVICE] " + site + " configuration is updated. reloading it.");
                        }
                        siteConfig = this.sitesMappings.get(site);
                        loadSiteConfig(site, siteConfig);
                    }
                    if (_environmentConfig.isUpdated(site)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[SITESERVICE] " + site
                                    + " environment configuration is updated. reloading it.");
                        }
                        siteConfig = this.sitesMappings.get(site);
                        loadSiteEnvironmentConfig(site, siteConfig);
                    }
                    if (deploymentEndpointConfig.isUpdated(site)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[SITESERVICE] " + site
                                    + " deployment configuration is updated. reloading it.");
                        }
                        siteConfig = this.sitesMappings.get(site);
                        loadSiteDeploymentConfig(site, siteConfig);
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[SITESERVICE] loading site configuration for " + site);
                    }
                    siteConfig.setSite(site);
                    siteConfig.setEnvironment(this._environment);
                    this.loadSiteConfig(site, siteConfig);
                    this.loadSiteEnvironmentConfig(site, siteConfig);
                    this.loadSiteDeploymentConfig(site, siteConfig);
                    this.sitesMappings.put(site, siteConfig);
                }
            }
        } else {
            LOGGER.error("[SITESERVICE] no sites found by query: " + query);
        }
    }

    /**
	 * check if any of site configuration is updated and reload it
	 */
	protected void checkForUpdates() {
		if (sitesMappings == null) {
			loadSitesMappings();
		} else {
			if (this.sitesMappings != null) {
                ServicesConfig servicesConfig = getService(ServicesConfig.class);
				for (String site : this.sitesMappings.keySet()) {
                    if (site != null) {
                        if (servicesConfig.isUpdated(site)) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[SITESERVICE] " + site + " configuration is updated. reloading it.");
                            }
                            if (servicesConfig.siteExists(site) && this.sitesMappings.containsKey(site)) {
                                SiteTO siteConfig = this.sitesMappings.get(site);
                                loadSiteConfig(site, siteConfig);
                            } else {
                                this.sitesMappings.remove(site);
                            }
                        }
                        if (_environmentConfig.isUpdated(site)) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[SITESERVICE] " + site
                                        + " environment configuration is updated. reloading it.");
                            }
                            if (_environmentConfig.exists(site) && this.sitesMappings.containsKey(site)) {
                                SiteTO siteConfig = this.sitesMappings.get(site);
                                loadSiteEnvironmentConfig(site, siteConfig);
                            } else {
                                this.sitesMappings.remove(site);
                            }
                        }
                        if (deploymentEndpointConfig.isUpdated(site)) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[SITESERVICE] " + site
                                        + " environment configuration is updated. reloading it.");
                            }
                            if (deploymentEndpointConfig.exists(site) && this.sitesMappings.containsKey(site)) {
                                SiteTO siteConfig = this.sitesMappings.get(site);
                                loadSiteDeploymentConfig(site, siteConfig);
                            }
                        }
                    } 
				}
			}
		}
	}

	/***
	 * load site environment specific info
	 * 
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteEnvironmentConfig(String site, SiteTO siteConfig) {
		// get environment specific configuration
        LOGGER.debug("Loading site environment configuration for " + site + "; Environemnt: " + _environment);
		EnvironmentConfigTO environmentConfig = _environmentConfig.getEnvironmentConfig(site);
        if (environmentConfig == null) {
            LOGGER.error("Environment configuration for site " + site + " does not exist.");
            return;
        }
		siteConfig.setLiveUrl(environmentConfig.getLiveServerUrl());
		siteConfig.setAuthoringUrl(environmentConfig.getAuthoringServerUrl());
		siteConfig.setAuthoringUrlPattern(environmentConfig.getAuthoringServerUrlPattern());
		siteConfig.setPreviewUrl(environmentConfig.getPreviewServerUrl());
		siteConfig.setPreviewUrlPattern(environmentConfig.getPreviewServerUrlPattern());
		siteConfig.setAdminEmail(environmentConfig.getAdminEmailAddress());
		siteConfig.setCookieDomain(environmentConfig.getCookieDomain());
		siteConfig.setOpenSiteDropdown(environmentConfig.getOpenDropdown());
		siteConfig.setFormServerUrl(environmentConfig.getFormServerUrlPattern());
        siteConfig.setPublishingChannelGroupConfigs(environmentConfig.getPublishingChannelGroupConfigs());
	}

    /***
     * load site environment specific info
     *
     * @param site
     * @param siteConfig
     */
    protected void loadSiteDeploymentConfig(String site, SiteTO siteConfig) {
        // get environment specific configuration
        LOGGER.debug("Loading deployment configuration for " + site + "; Environment: " + _environment);
        DeploymentConfigTO deploymentConfig = deploymentEndpointConfig.getSiteDeploymentConfig(site);
        if (deploymentConfig == null) {
            LOGGER.error("Deployment configuration for site " + site + " does not exist.");
            return;
        }
        siteConfig.setDeploymentEndpointConfigs(deploymentConfig.getEndpointMapping());
    }

	/**
	 * load site configuration info (not environment specific)
	 * 
	 * @param site
	 * @param siteConfig
	 */
	protected void loadSiteConfig(String site, SiteTO siteConfig) {
		// get site configuration
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		siteConfig.setWebProject(servicesConfig.getWemProject(site));
        siteConfig.setRepositoryRootPath(servicesConfig.getRepositoryRootPath(site));
	}

	/**
	 * load sites mappings
	 */
	protected void loadSitesMappings() {
		Map<String, SiteTO> sitesMapping = new FastMap<String, SiteTO>();
       PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef sitesNodeRef = persistenceManagerService.getNodeRef(this._sitesConfigPath);
        if (sitesNodeRef != null) {
            List<FileInfo> siteInfos = persistenceManagerService.listFolders(sitesNodeRef);
            if (siteInfos != null && siteInfos.size() > 0) {
                for (FileInfo siteInfo : siteInfos) {
                    NodeRef siteRef = siteInfo.getNodeRef();
                    String site = (String)persistenceManagerService.getProperty(siteRef, ContentModel.PROP_NAME);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[SITESERVICE] loading site configuration for " + site);
                    }
                    SiteTO siteConfig = new SiteTO();
                    siteConfig.setSite(site);
                    siteConfig.setEnvironment(this._environment);
                    this.loadSiteConfig(site, siteConfig);
                    this.loadSiteEnvironmentConfig(site, siteConfig);
                    this.loadSiteDeploymentConfig(site, siteConfig);
                    sitesMapping.put(site, siteConfig);
                }
                if (this.sitesMappings != null) {
                    this.sitesMappings.clear();
                    this.sitesMappings = null;
                }
                this.sitesMappings = sitesMapping;
            } else {
                LOGGER.warn("[SITESERVICE] no sites found at : " + this._sitesConfigPath);
            }
		} else {
            LOGGER.warn("[SITESERVICE] no sites found at : " + this._sitesConfigPath);
		}

	}

	/**
	 * put in the site and its configuration into mappings
	 * 
	 * @param sitesMapping
	 * @param mappingKey
	 * @param
	 * @param siteConfig
	 */
	protected void putInMapping(Map<String, Map<String, SiteTO>> sitesMapping, String mappingKey, String key,
			SiteTO siteConfig) {
		if (!StringUtils.isEmpty(key)) {
			Map<String, SiteTO> sites = sitesMapping.get(mappingKey);
			if (sites == null) {
				sites = new FastMap<String, SiteTO>();
				sitesMapping.put(mappingKey, sites);
			}
			sites.put(key, siteConfig);
		} else {
			LOGGER.error("[SITESERVICE] cannot add site configuration to " + mappingKey
					+ " mappings. key is empty for " + siteConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#
	 * getConfigRef (java.lang.String)
	 */
	public NodeRef getConfigRef(String key) {
		// key is not being used here
        SearchService searchService = getService(SearchService.class);
		return searchService.findNodeFromPath(CStudioConstants.STORE_REF, _configPath, _configFileName, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#
	 * getConfiguration(java.lang.String)
	 */
	protected TimeStamped getConfiguration(String key) {
		// key is not being used here
		return _sitesConfig;
	}

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            _sitesConfig = null;
        }
    }

    /*
      * (non-Javadoc)
      *
      * @seeorg.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#
      * loadConfiguration(java.lang.String)
      */
	@SuppressWarnings("unchecked")
	protected void loadConfiguration(String key) {
		NodeRef configRef = getConfigRef(key);
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		Document document = persistenceManagerService.loadXml(configRef);
		if (document != null) {
			Element root = document.getRootElement();
			SitesConfigTO config = new SitesConfigTO();
			Map<String, String> sitesMenu = loadMap(root.selectNodes("sites-menu/menu-item"));
			config.setSitesMenu(sitesMenu);
			Map<String, String> siteTypes = loadMap(root.selectNodes("site-types/site-type"));
			config.setSiteTypes(siteTypes);
			Map<String, String> repositoryTypes = loadMap(root.selectNodes("repository-types/repository-type"));
			config.setRepositoryTypes(repositoryTypes);
			config.setSitesLocation(root.valueOf("sites-location"));
			config.setLastUpdated(new Date());
			_sitesConfig = config;
		}
	}
	
	
	

	/**
	 * create a map from the given list of nodes
	 * 
	 * @param nodes
	 * @return a map of key and value pairs
	 */
	protected Map<String, String> loadMap(List<Node> nodes) {
		if (nodes != null && nodes.size() > 0) {
			Map<String, String> mapping = new HashMap<String, String>();
			for (Node node : nodes) {
				String key = node.valueOf("@key");
				String value = node.getText();
				mapping.put(key, value);
			}
			return mapping;
		} else {
			return new HashMap<String, String>(0);
		}
	}

    @Override
    public void deleteSite(String site) {
        checkForUpdates();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        persistenceManagerService.deleteSite(site);
        deploymentService.deleteDeploymentDataForSite(site);
        sitesMappings.remove(site);
    }
    
	@Override
	public void createObjectStatesforNewSite(NodeRef siteRoot) {	
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		List<FileInfo> childNodes = persistenceManagerService.list(siteRoot);
		for (FileInfo fileInfo : childNodes) {
			if(fileInfo.isFolder()){
                persistenceManagerService.insertNewObjectEntry(fileInfo.getNodeRef());
                createObjectStatesforNewSite(fileInfo.getNodeRef());
			}else{
				persistenceManagerService.insertNewObjectEntry(fileInfo.getNodeRef());	
			}
            addDefaultAspects(fileInfo.getNodeRef());
		}
	}

    @Override
    public void extractDependenciesForNewSite(NodeRef siteRoot) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        List<FileInfo> childNodes = persistenceManagerService.list(siteRoot);
        Map<String, Set<String>> globalDeps = new FastMap<String, Set<String>>();
        for (FileInfo fileInfo : childNodes) {
            if(fileInfo.isFolder()){
                extractDependenciesForNewSite(fileInfo.getNodeRef());
            }else{
                NodeRef nodeRef = fileInfo.getNodeRef();
                String fullPath = persistenceManagerService.getNodePath(nodeRef);
                DmPathTO dmPathTO = new DmPathTO(fullPath);
                String site = dmPathTO.getSiteName();
                String relativePath = dmPathTO.getRelativePath();
                DmContentService dmContentService = getService(DmContentService.class);
                DmDependencyService dmDependencyService = getService(DmDependencyService.class);

                if (fullPath.endsWith(DmConstants.XML_PATTERN)) {
                    try {
                        Document doc = dmContentService.getContentXml(site, null, relativePath);
                        dmDependencyService.extractDependencies(site, relativePath, doc, globalDeps);
                    } catch (ContentNotFoundException e) {
                        LOGGER.error("Failed to extract dependencies for document: " + fullPath, e);
                    } catch (ServiceException e) {
                        LOGGER.error("Failed to extract dependencies for document: " + fullPath ,e);
                    }
                } else {

                    boolean isCss = fullPath.endsWith(DmConstants.CSS_PATTERN);
                    boolean isJs = fullPath.endsWith(DmConstants.JS_PATTERN);
                    List<String> templatePatterns = servicesConfig.getRenderingTemplatePatterns(site);
                    boolean isTemplate = false;
                    for (String templatePattern : templatePatterns) {
                        Pattern pattern = Pattern.compile(templatePattern);
                        Matcher matcher = pattern.matcher(relativePath);
                        if (matcher.matches()) {
                            isTemplate = true;
                            break;
                        }
                    }
                    try {
                        if (isCss || isJs || isTemplate) {
                            StringBuffer sb = new StringBuffer(persistenceManagerService.getContentAsString(nodeRef));
                            if (isCss) {
                                dmDependencyService.extractDependenciesStyle(site, relativePath, sb, globalDeps);
                            } else if (isJs) {
                                dmDependencyService.extractDependenciesJavascript(site, relativePath, sb, globalDeps);
                            } else if (isTemplate) {
                                dmDependencyService.extractDependenciesTemplate(site, relativePath, sb, globalDeps);
                            }
                        }
                    } catch (ServiceException e) {
                        LOGGER.error("Failed to extract dependencies for: " + fullPath, e);
                    }
                }
            }
        }
    }

    @Override
    public void extractMetadataForNewSite(NodeRef siteRoot) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        List<FileInfo> childNodes = persistenceManagerService.list(siteRoot);
        for (FileInfo fileInfo : childNodes) {
            if(fileInfo.isFolder()){
                extractMetadataForNewSite(fileInfo.getNodeRef());
            }else{
                NodeRef nodeRef = fileInfo.getNodeRef();
                String fullPath = persistenceManagerService.getNodePath(nodeRef);
                DmPathTO dmPathTO = new DmPathTO(fullPath);
                String site = dmPathTO.getSiteName();
                String relativePath = dmPathTO.getRelativePath();
                DmContentService dmContentService = getService(DmContentService.class);
                DmMetadataService dmMetadataService = getService(DmMetadataService.class);
                if (fullPath.endsWith(DmConstants.XML_PATTERN)) {
                    try {
                        Document doc = dmContentService.getContentXml(site, null, relativePath);
                        dmMetadataService.extractMetadata(site, persistenceManagerService.getCurrentUserName(), null, relativePath, null, nodeRef, doc);
                    } catch (ContentNotFoundException e) {
                        LOGGER.error("Failed to extract metadata for document: " + fullPath, e);
                    } catch (ServiceException e) {
                        LOGGER.error("Failed to extract metadata for document: " + fullPath ,e);
                    }
                }
            }
        }
    }

    @Override
    public DeploymentEndpointConfigTO getDeploymentEndpoint(String site, String endpoint) {
        return deploymentEndpointConfig.getDeploymentConfig(site, endpoint);
    }

    private void addDefaultAspects(NodeRef nodeRef) {
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		persistenceManagerService.addAspect(nodeRef, CStudioContentModel.ASPECT_PREVIEWABLE, new HashMap<QName, Serializable>());
        HashMap<QName, Serializable> versionableProps = new HashMap<QName, Serializable>();
        versionableProps.put(ContentModel.PROP_AUTO_VERSION, false);
        versionableProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
		persistenceManagerService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, versionableProps);
		
	}

    @Override
    public Set<String> getAllAvailableSites() {
        checkForUpdates();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        return servicesConfig.getAllAvailableSites();
    }

    @Override
    public void addConfigSpaceExportAspect(final String site) {
        String configSpaceRoot = this._sitesConfigPath + "/" + site;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(configSpaceRoot);
        if (nodeRef != null) {
            addConfigSpaceExportAspectToNodes(nodeRef);
        }
    }

    private void addConfigSpaceExportAspectToNodes(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(nodeRef);
            for (FileInfo child : children) {
                addConfigSpaceExportAspectToNodes(child.getNodeRef());
            }
        } else {
            persistenceManagerService.addAspect(nodeRef, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT, null);
        }
    }

    /**
	 * @param environmentConfig
	 *            the environmentConfig to set
	 */
	public void setEnvironmentConfig(SiteEnvironmentConfig environmentConfig) {
		this._environmentConfig = environmentConfig;
	}


    /**
     * @param sitesConfigPath
     *            the sitesConfigPath to set
     */
    public void setSitesConfigPath(String sitesConfigPath) {
        this._sitesConfigPath = sitesConfigPath;
    }

	/**
	 * @param environment
	 *            the environment to set
	 */
	public void setEnvironment(String environment) {
		this._environment = environment;
	}

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return _environment;
	}

	/**
	 * @return the configRoot
	 */
	public String getConfigRoot() {
		return _configRoot;
	}

	/**
	 * @param configRoot
	 *            the configRoot to set
	 */
	public void setConfigRoot(String configRoot) {
		this._configRoot = configRoot;
	}

	/**
	 * @return the environmentConfigPath
	 */
	public String getEnvironmentConfigPath() {
		return _environmentConfigPath;
	}

	/**
	 * @param environmentConfigPath
	 *            the environmentConfigPath to set
	 */
	public void setEnvironmentConfigPath(String environmentConfigPath) {
		this._environmentConfigPath = environmentConfigPath;
	}

    public DeploymentEndpointConfig getDeploymentEndpointConfig() {
        return deploymentEndpointConfig;
    }

    public void setDeploymentEndpointConfig(DeploymentEndpointConfig deploymentEndpointConfig) {
        this.deploymentEndpointConfig = deploymentEndpointConfig;
    }

    public String getDeploymentConfigPath() {
        return deploymentConfigPath;
    }

    public void setDeploymentConfigPath(String deploymentConfigPath) {
        this.deploymentConfigPath = deploymentConfigPath;
    }

    public void setDeploymentService(final DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }
}
