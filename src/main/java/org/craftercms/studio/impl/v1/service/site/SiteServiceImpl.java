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
package org.craftercms.studio.impl.v1.service.site;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.ConfigurableServiceBase;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.configuration.SiteEnvironmentConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;

import org.craftercms.studio.api.v1.to.SiteBlueprintTO;
import reactor.core.Reactor;

import javax.annotation.PostConstruct;
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
public class SiteServiceImpl extends ConfigurableServiceBase implements SiteService {

	private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

	@Override
	public void register() {
		this._servicesManager.registerService(SiteService.class, this);
	}
	
	@Override
	public boolean writeConfiguration(String site, String path, InputStream content) {
		return contentRepository.writeContent("/cstudio/config/sites/"+site+"/"+path, content);
	}

	@Override	
	public boolean writeConfiguration(String path, InputStream content) {
		boolean toRetrun = contentRepository.writeContent(path, content);
        reloadSiteConfigurations();
        return toRetrun;
	}

	@Override
	public Map<String, Object> getConfiguration(String path) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#
	 * getConfigurationById(java.lang.String)
	 */
	protected TimeStamped getConfigurationById(String key) {
		// key is not being used here
		return sitesConfig;
	}

	@Override
	protected void removeConfiguration(String key) {
		if (!StringUtils.isEmpty(key)) {
			sitesConfig = null;
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
		String configLocation = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key)
				.replaceFirst(CStudioConstants.PATTERN_ENVIRONMENT, environment);
		configLocation = configLocation + "/" + configFileName;
		Document document = null;
		try {
			document = contentService.getContentAsDocument(configLocation);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
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
			sitesConfig = config;
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

	/**
	 * given a site ID return the configuration as a document
	 * This method allows extensions to add additional properties to the configuration that
	 * are not made available through the site configuration object
	 * @param site the name of the site
	 * @return a Document containing the entire site configuration
	 */
	public Document getSiteConfiguration(String site) 
	throws SiteConfigNotFoundException {
		return _siteServiceDAL.getSiteConfiguration(site);
	}

	@Override
	public Map<String, Object> getConfiguration(String site, String path, boolean applyEnv) {
		//
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
			//XMLSerializer xmlSerializer = new XMLSerializer();
			//response = xmlSerializer.read(configContent);
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
				/*
				if (currentElement.attributeCount() > 0) {
					for (int j = 0; j < currentElement.attributes().size(); j++) {
						Attribute item = currentElement.attribute(j);
						map.put(item.getName(), item.getValue());
					}
				}*/
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
				/*
				Iterator elIter = currentElement.elementIterator();
				if (elIter.hasNext()) {
					Element firstChild = (Element)elIter.next();
					if (firstChild.isTextOnly()) {
						map.put(currentElement.getName(), currentElement.getStringValue());
					} else {
						map.putAll(createMap(currentElement));
					}
				}*/
			}
		}
		return map;
	}

	/**
	 * check if any of site configuration is updated and reload it
	 */
	protected void checkForUpdates() {
        /*
		if (sitesMappings == null) {
			loadSitesMappings();
		} else {
			if (this.sitesMappings != null) {
				for (String site : this.sitesMappings.keySet()) {
					if (site != null) {
						if (servicesConfig.isUpdated(site)) {
							logger.debug("[SITESERVICE] " + site + " configuration is updated. reloading it.");

							if (servicesConfig.siteExists(site) && this.sitesMappings.containsKey(site)) {
								SiteTO siteConfig = this.sitesMappings.get(site);
								loadSiteConfig(site, siteConfig);
							} else {
								this.sitesMappings.remove(site);
							}
						}
						if (environmentConfig.isUpdated(site)) {
							logger.debug("[SITESERVICE] " + site
									+ " environment configuration is updated. reloading it.");

							if (environmentConfig.exists(site) && this.sitesMappings.containsKey(site)) {
								SiteTO siteConfig = this.sitesMappings.get(site);
								loadSiteEnvironmentConfig(site, siteConfig);
							} else {
								this.sitesMappings.remove(site);
							}
						}
						if (deploymentEndpointConfig.isUpdated(site)) {
							logger.debug("[SITESERVICE] " + site
									+ " environment configuration is updated. reloading it.");
						}
						if (deploymentEndpointConfig.exists(site) && this.sitesMappings.containsKey(site)) {
							SiteTO siteConfig = this.sitesMappings.get(site);
							loadSiteDeploymentConfig(site, siteConfig);
						}
					}
				}
			}
		}*/
	}


	/**
	 * load sites mappings
	 *//*
	protected void loadSitesMappings() {
		Map<String, SiteTO> sitesMapping = new HashMap<>();
		ContentItemTO sitesTree = contentService.getContentItemTree(sitesConfigPath, 1);
		if (sitesTree != null && sitesTree.children.size() > 0) {
			for (ContentItemTO siteItem : sitesTree.children) {
				String site = siteItem.getName();
				logger.debug("[SITESERVICE] loading site configuration for " + site);

				SiteTO siteConfig = new SiteTO();
				siteConfig.setSite(site);
				siteConfig.setEnvironment(this.environment);
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
			logger.warn("[SITESERVICE] no sites found at : " + this.sitesConfigPath);
		}
	}
*/
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
		if (environmentConfig == null) {
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
    public DeploymentEndpointConfigTO getDeploymentEndpoint(String site, String endpoint) {
        return deploymentEndpointConfig.getDeploymentConfig(site, endpoint);
    }

    @Override
    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site) {
        return environmentConfig.getPublishingChannelGroupConfigs(site);
    }

	@Override
	public List<SiteFeed> getUserSites(String user) {
        String username = user;
        if (StringUtils.isEmpty(username)) {
            username = securityService.getCurrentUser();
        }
        List<SiteFeed> sites = siteFeedMapper.getSites();
        List<SiteFeed> toRet = new ArrayList<SiteFeed>();
        for (SiteFeed site : sites) {
            Set<String> userRoles = securityService.getUserRoles(site.getSiteId(),username);
            if (CollectionUtils.isNotEmpty(userRoles)) {
                toRet.add(site);
            }
        }
        return toRet;
	}

    @Override
    public DeploymentEndpointConfigTO getPreviewDeploymentEndpoint(String site) {
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
    public String getLiveEnvironmentName(String site) {
        PublishingChannelGroupConfigTO pcgcTO = environmentConfig.getLiveEnvironmentPublishingGroup(site);
        if (pcgcTO != null) {
            return pcgcTO.getName();
        } else {
            return null;
        }
    }

   	@Override
   	public boolean createSiteFromBlueprint(String blueprintName, String siteName, String siteId, String desc) {
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
	 		//replaceFileContent(siteConfigFolder+"/site-config.xml", "SITENAME", siteName);
	 		replaceFileContent(siteConfigFolder+"/role-mappings-config.xml", "SITENAME", siteId);
	 		replaceFileContent(siteConfigFolder + "/permission-mappings-config.xml", "SITENAME", siteId);

			// Add user groups
			securityService.addUserGroup("crafter_" + siteId);
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_admin");
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_author");
			securityService.addUserGroup("crafter_" + siteId, "crafter_" + siteId + "_viewer");
			securityService.addUserToGroup("crafter_" + siteId + "_admin", securityService.getCurrentUser());

			// Set object states
			createObjectStatesforNewSite(siteId);

			// Extract dependencies
			extractDependenciesForNewSite(siteId);

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
            deploymentService.syncAllContentToPreview(siteId);
            reloadSiteConfiguration(siteId);
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
                DmPathTO dmPathTO = new DmPathTO(fullPath);
                String relativePath = dmPathTO.getRelativePath();
                if (fullPath.endsWith(DmConstants.XML_PATTERN)) {
                    try {
                        Document doc = contentService.getContentAsDocument(fullPath);
                        dmDependencyService.extractDependencies(site, relativePath, doc, globalDeps);
                    } catch (ContentNotFoundException e) {
                        logger.error("Failed to extract dependencies for document: " + fullPath, e);
                    } catch (ServiceException e) {
                        logger.error("Failed to extract dependencies for document: " + fullPath, e);
                    } catch (DocumentException e) {
                        logger.error("Failed to extract dependencies for document: " + fullPath, e);
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
                            StringBuffer sb = new StringBuffer(contentService.getContentAsString(fullPath));
                            if (isCss) {
                                dmDependencyService.extractDependenciesStyle(site, relativePath, sb, globalDeps);
                            } else if (isJs) {
                                dmDependencyService.extractDependenciesJavascript(site, relativePath, sb, globalDeps);
                            } else if (isTemplate) {
                                dmDependencyService.extractDependenciesTemplate(site, relativePath, sb, globalDeps);
                            }
                        }
                    } catch (ServiceException e) {
                        logger.error("Failed to extract dependencies for: " + fullPath, e);
                    }
                }
            }
        }
    }

	@Override
   	public boolean deleteSite(String siteId) {
 		boolean success = true;
 		try {
 			contentRepository.deleteContent("/wem-projects/"+siteId);
 			contentRepository.deleteContent("/cstudio/config/sites/" + siteId);

	 		// delete database records
			siteFeedMapper.deleteSite(siteId);
			activityService.deleteActivitiesForSite(siteId);
			dmDependencyService.deleteDependenciesForSite(siteId);
            deploymentService.deleteDeploymentDataForSite(siteId);
            objectStateService.deleteObjectStatesForSite(siteId);
            dmPageNavigationOrderService.deleteSequencesForSite(siteId);
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
    public String getPreviewServerUrl(String site) {
        return environmentConfig.getPreviewServerUrl(site);
    }

    @Override
    public String getLiveServerUrl(String site) {
        return environmentConfig.getLiveServerUrl(site);
    }

    @Override
    public String getAdminEmailAddress(String site) {
        return environmentConfig.getAdminEmailAddress(site);
    }

    @Override
    protected String getConfigFullPath(String key) {
        return null;
    }

    @Override
    public void reloadSiteConfigurations() {
        reloadGlobalConfiguration();
        if (this.sitesMappings == null) {
            this.sitesMappings = new HashMap<String, SiteTO>();
        }
        Set<String> sites = getAllAvailableSites();

        if (sites != null && sites.size() > 0) {
            for (String site : sites) {
                reloadSiteConfiguration(site);
            }
        } else {
            logger.error("[SITESERVICE] no sites found");
        }
    }

    @Override
    public void reloadSiteConfiguration(String site) {
        if (this.sitesMappings == null) {
            this.sitesMappings = new HashMap<String, SiteTO>();
        }
        SiteTO siteConfig = new SiteTO();
        if (this.sitesMappings.containsKey(site)) {
            if (servicesConfig.isUpdated(site)) {
                logger.debug("[SITESERVICE] " + site + " configuration is updated. reloading it.");
                siteConfig = this.sitesMappings.get(site);
                loadSiteConfig(site, siteConfig);
            }
            if (environmentConfig.isUpdated(site)) {
                logger.debug("[SITESERVICE] " + site + " environment configuration is updated. reloading it.");
                siteConfig = this.sitesMappings.get(site);
                environmentConfig.reloadConfiguration(site);
                loadSiteEnvironmentConfig(site, siteConfig);
            }
            if (deploymentEndpointConfig.isUpdated(site)) {
                logger.debug("[SITESERVICE] " + site + " deployment configuration is updated. reloading it.");
                siteConfig = this.sitesMappings.get(site);
                deploymentEndpointConfig.reloadConfiguration(site);
                loadSiteDeploymentConfig(site, siteConfig);
            }
            notificationService.reloadConfiguration(site);
        } else {
            logger.debug("[SITESERVICE] loading site configuration for " + site);
            siteConfig.setSite(site);
            siteConfig.setEnvironment(this.environment);
            this.loadSiteConfig(site, siteConfig);
            environmentConfig.reloadConfiguration(site);
            this.loadSiteEnvironmentConfig(site, siteConfig);
            deploymentEndpointConfig.reloadConfiguration(site);
            this.loadSiteDeploymentConfig(site, siteConfig);
            this.sitesMappings.put(site, siteConfig);
            notificationService.reloadConfiguration(site);
            securityService.reloadConfiguration(site);
            contentTypeService.reloadConfiguration(site);
        }
    }

    @Override
    public void reloadGlobalConfiguration() {
        securityService.reloadGlobalConfiguration();
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

	public DmDependencyService getDmDependencyService() { return dmDependencyService; }
	public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

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
	protected DmDependencyService dmDependencyService;
	protected SecurityService securityService;
	protected ActivityService activityService;
	protected DeploymentService deploymentService;
    protected ObjectMetadataManager objectMetadataManager;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected Reactor repositoryRector;
    protected NotificationService notificationService;
    protected ContentTypeService contentTypeService;

	@Autowired
	protected SiteFeedMapper siteFeedMapper;

	/**
	 * a map of site key and site information
	 */
	protected Map<String, SiteTO> sitesMappings;

	protected SitesConfigTO sitesConfig = null;
}
