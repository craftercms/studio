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

import javolution.util.FastList;
import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmFolderConfigTO;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ContentTypesConfig;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.to.*;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Implementation of ServicesConfigImpl. This class requires a configuration
 * file in the repository
 * 
 * 
 */
public class ServicesConfigImpl extends AbstractRegistrableService implements ServicesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicesConfigImpl.class);
    
    protected static final String WEM_PROJECTS_PATH = "/wem-projects";
    protected static final String WORK_AREA_PATH = "/work-area";
    protected static final String LIVE_PATH = "/live";
    
	/** path keys **/
	protected static final String PATH_CONTENT = "content";
	protected static final String PATH_WCM_CONTENT = "wcm-content";
	protected static final String PATH_PROTOTYPE = "prototype";
	protected static final String PATH_TEMPLATE = "template";

	/** pattern keys **/
	protected static final String PATTERN_PAGE = "page";
	protected static final String PATTERN_COMPONENT = "component";
	protected static final String PATTERN_ASSET = "asset";
	protected static final String PATTERN_DOCUMENT = "document";
    protected static final String PATTERN_RENDERING_TEMPLATE = "rendering-template";
    protected static final String PATTERN_LEVEL_DESCRIPTOR = "level-descriptor";
    protected static final String PATTERN_PREVIEWABLE_MIMETYPES = "previewable-mimetypes";

	/** xml element names **/
	protected static final String ELM_PATTERN = "pattern"; 
	
	/** xml attribute names **/
	protected static final String ATTR_DEPTH = "@depth";
	protected static final String ATTR_DISPLAY_NAME = "@displayName";
	protected static final String ATTR_NAMESPACE = "@namespace";
	protected static final String ATTR_NAME = "@name";
    protected static final String ATTR_SITE = "@site";
	protected static final String ATTR_PATH = "@path";
	protected static final String ATTR_READ_DIRECT_CHILDREN = "@read-direct-children";
	protected static final String ATTR_ATTACH_ROOT_PREFIX = "@attach-root-prefix";

    protected static final String LIVE_REPOSITORY_PATH_SUFFIX = "-live";

	/**
	 * the location where to find the configuration file
	 */
	protected String _configPath;

	/**
	 * configuration file name
	 */
	protected String _configFileName;

	/**
	 * site configuration mapping
	 */
	protected Map<String, SiteConfigTO> _siteMapping = new HashMap<String, SiteConfigTO>();
	
	/**
	 * content types configuration
	 */
	protected ContentTypesConfig _contentTypesConfig;

    @Override
    public void register() {
        this._servicesManager.registerService(ServicesConfig.class, this);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getContentType(java.lang.String)
      */
	public QName getContentType(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO siteConfig = _siteMapping.get(site);
		if (siteConfig != null) {
			String name = siteConfig.getDefaultContentType();
			ContentTypeConfigTO typeConfig = _contentTypesConfig.getContentTypeConfig(site, name);
			if (typeConfig != null) {
				typeConfig.getContentType();
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getTypeByNamespace(java.lang.String, java.lang.String)
	 */
	public QName getTypeByNamespace(String site, String namespace) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getNamespaceToTypeMap() != null) {
			return config.getNamespaceToTypeMap().get(namespace);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getModelConfig(
	 * java.lang.String)
	 */
	public Map<QName, ModelConfigTO> getModelConfig(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getModelConfig();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getSearchColumnsConfig(java.lang.String)
	 */
	public Map<String, QName> getSearchColumnsConfig(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null) {
			String contentType = config.getDefaultContentType();
			ContentTypeConfigTO contentTypeConfig = _contentTypesConfig.getContentTypeConfig(site, contentType);
			if (contentTypeConfig != null) {
				return contentTypeConfig.getSearchConfig().getSearchColumnMap();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getWebProject(java.lang.String)
	 */
	public String getWemProject(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getWemProject() != null) {
			return config.getWemProject();
		}
		return null;
	}

    @Override
    public List<DmFolderConfigTO> getFolders(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getFolders();
        }
        return null;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmServicesConfig#getRootPrefix(java.lang.String)
      */
	public String getRootPrefix(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getRootPrefix();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getContentType(java.lang.String, java.lang.String)
	 */
	public ContentTypeConfigTO getContentTypeConfig(String site, String name) {
		return _contentTypesConfig.getContentTypeConfig(site, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getAssetPatterns(java.lang.String)
	 */
	public List<String> getAssetPatterns(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getAssetPatterns();
		}
		return null;
	}
	
	
	public List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(String site,String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = _contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getDeleteDependencyPattern();
		}
        return Collections.emptyList();
	}
	
	public List<CopyDependencyConfigTO> getCopyDependencyPatterns(String site,String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = _contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getCopyDepedencyPattern();
		}
        return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getComponentPatterns(java.lang.String)
	 */
	public List<String> getComponentPatterns(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getComponentPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getPagePatterns(java.lang.String)
	 */
	public List<String> getPagePatterns(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getPagePatterns();
		}
		return null;
	}

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getRenderingTemplatePatterns(java.lang.String)
      */
    public List<String> getRenderingTemplatePatterns(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getRenderingTemplatePatterns();
        }
        return null;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getLevelDescriptorPatterns(java.lang.String)
      */
    public List<String> getLevelDescriptorPatterns(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getLevelDescriptorPatterns();
        }
        return null;
    }


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDocumentPatterns(java.lang.String)
	 */
	public List<String> getDocumentPatterns(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDocumentPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getCategoryRootPath(java.lang.String, java.lang.String)
	 */
	public String getCategoryRootPath(String site, String category) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getCategoryRootPath(category);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getLevelDescriptorName(java.lang.String)
	 */
	public String getLevelDescriptorName(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getLevelDescriptorName();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDefaultSearchConfig(java.lang.String)
	 */
	public SearchConfigTO getDefaultSearchConfig(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getDefaultSearchConfig();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getExcludePaths(java.lang.String)
	 */
	public List<String> getExcludePaths(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig().getExcludePaths() != null) {
			return config.getRepositoryConfig().getExcludePaths();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDisplayInWidgetPathPatterns(java.lang.String)
	 */
	public List<String> getDisplayInWidgetPathPatterns(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDisplayPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDefaultTimezone(java.lang.String)
	 */
	public String getDefaultTimezone(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getTimezone();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getTemplateConfig(java.lang.String)
	 */
	public TemplateConfigTO getTemplateConfig(String site) {
		if (isConfigUpdated(site)) {
			loadConfiguration(site);
		}
		SiteConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getRepositoryConfig().getTemplateConfig();
		} else {
			return new TemplateConfigTO();
		}
	}

    @Override
    public String getRepositoryRootPath(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null) {
            StringBuilder sbRepoPath = new StringBuilder(WEM_PROJECTS_PATH);
            sbRepoPath.append("/").append(config.getWemProject());
            sbRepoPath.append("/").append(config.getSiteName());
            sbRepoPath.append(WORK_AREA_PATH);
            return sbRepoPath.toString();
        }
        return null;
    }
    
    @Override
    public String getLiveRepositoryPath(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null) {
            StringBuilder sbRepoPath = new StringBuilder(WEM_PROJECTS_PATH);
            sbRepoPath.append("/").append(config.getWemProject());
            sbRepoPath.append("/").append(config.getSiteName());
            sbRepoPath.append("/").append(DmConstants.DM_LIVE_REPO_FOLDER);
            return sbRepoPath.toString();
        }
        return null;
    }

    @Override
	public boolean isUpdated(String site) {
		return isConfigUpdated(site);
	}

	/**
	 * is configuration file updated?
	 * 
	 * @return
	 */
	protected boolean isConfigUpdated(String site) {
		SiteConfigTO config = _siteMapping.get(site);
		if (config == null) {
			return true;
		} else {
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			
			String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);
			NodeRef configRef = persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
            if (configRef != null) {
			    Serializable modifiedDateVal = persistenceManagerService.getProperty(configRef, ContentModel.PROP_MODIFIED);
                if (modifiedDateVal == null) return false;
                Date modifiedDate = (Date)modifiedDateVal;
			    return modifiedDate.after(config.getLastUpdated());
            } else {
                NodeRef siteConfigFolder = persistenceManagerService.getNodeRef(siteConfigPath);
                if (siteConfigFolder == null) {
                    _siteMapping.remove(site);
                }
                return true;
            }
		}
	}

	/**
	 * load services configuration
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected void loadConfiguration(String site) {
		String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);	
		Document document = persistenceManagerService.loadXml(siteConfigPath + "/" + _configFileName);
		if (document != null) {
			Element root = document.getRootElement();
			Node configNode = root.selectSingleNode("/site-config");
			String name = configNode.valueOf("display-name");
			SiteConfigTO siteConfig = new SiteConfigTO();
			siteConfig.setName(name);
            siteConfig.setSiteName(configNode.valueOf("name"));
            siteConfig.setWemProject(configNode.valueOf("wem-project"));
			siteConfig.setDefaultContentType(configNode.valueOf("default-content-type"));
			String assetUrl = configNode.valueOf("assets-url");
			siteConfig.setTimezone(configNode.valueOf("default-timezone"));
			siteConfig.setAssetUrl(assetUrl);
			loadNamespaceToTypeMap(siteConfig, configNode.selectNodes("namespace-to-type-map/namespace"));
			loadModelConfig(siteConfig, configNode.selectNodes("models/model"));
			SearchConfigTO searchConfig = _contentTypesConfig.loadSearchConfig(configNode.selectSingleNode("search"));
			siteConfig.setDefaultSearchConfig(searchConfig);
            loadSiteRepositoryConfiguration(siteConfig, configNode.selectSingleNode("repository"));
			// set the last updated date
			siteConfig.setLastUpdated(new Date());
			_siteMapping.put(site, siteConfig);
		} else {
			LOGGER.error("No site configuration found for " + site + " at " + siteConfigPath);
		}
	}

	/**
	 * load namespaces to types mapping
	 * 
	 * @param siteConfig
	 * @param nodes
	 */
	protected void loadNamespaceToTypeMap(SiteConfigTO siteConfig, List<Node> nodes) {
		if (nodes != null && nodes.size() > 0) {
			Map<String, QName> namespaceToTypeMap = new FastMap<String, QName>();
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (Node node : nodes) {
				String namespace = node.valueOf("@name");
				QName type = persistenceManagerService.createQName(node.getText());
				if (!StringUtils.isEmpty(namespace) && type != null) {
					namespaceToTypeMap.put(namespace, type);
				}
			}
			siteConfig.setNamespaceToTypeMap(namespaceToTypeMap);
		} else {
			siteConfig.setNamespaceToTypeMap(new FastMap<String, QName>(0));
		}
	}

    /**
     * load the web-project configuration
     *
     * @param siteConfig
     * @param node
     */
    @SuppressWarnings("unchecked")
    protected void loadSiteRepositoryConfiguration(SiteConfigTO siteConfig, Node node) {
        RepositoryConfigTO repoConfigTO = new RepositoryConfigTO();
        repoConfigTO.setRootPrefix(node.valueOf("@rootPrefix"));
        repoConfigTO.setLevelDescriptorName(node.valueOf("level-descriptor"));
        repoConfigTO.setIndexRepository(ContentFormatUtils.getBooleanValue(node.valueOf("index-repository")));
        String timeValue = node.valueOf("index-time-to-live");
        if (!StringUtils.isEmpty(timeValue)) {
            long indexTimeToLive = ContentFormatUtils.getLongValue(timeValue);
            if (indexTimeToLive > 0) {
                repoConfigTO.setIndexTimeToLive(indexTimeToLive);
            }
        }
        repoConfigTO.setCheckForRenamed(ContentFormatUtils.getBooleanValue(node.valueOf("check-for-renamed")));
        loadFolderConfiguration(siteConfig, repoConfigTO, node.selectNodes("folders/folder"));
        loadPatterns(siteConfig, repoConfigTO, node.selectNodes("patterns/pattern-group"));
        List<String> excludePaths = getStringList(node.selectNodes("exclude-paths/exclude-path"));
        repoConfigTO.setExcludePaths(excludePaths);
        List<String> displayPatterns = getStringList(node.selectNodes("display-in-widget-patterns/display-in-widget-pattern"));
        repoConfigTO.setDisplayPatterns(displayPatterns);
        loadTemplateConfig(repoConfigTO, node.selectSingleNode("common-prototype-config"));
        siteConfig.setRepositoryConfig(repoConfigTO);
    }

    /**
     * load common prototype configuration
     *
     * @param repoConfig
     * @param node
     */
    protected void loadTemplateConfig(RepositoryConfigTO repoConfig, Node node) {
        TemplateConfigTO templateConfig = new TemplateConfigTO();
        if (node != null) {
            List<String> excludedPaths = getStringList(node.selectNodes("excluded-on-convert-paths/excluded-on-convert-path"));
            List<String> multiValuedPaths = getStringList(node.selectNodes("multi-valued-on-convert-paths/multi-valued-on-convert-path"));
            templateConfig.setExcludedPathsOnConvert(excludedPaths);
            templateConfig.setMultiValuedPathsOnConvert(multiValuedPaths);
        }
        repoConfig.setTemplateConfig(templateConfig);
    }

	/**
	 * get a list of string values
	 * 
	 * @param nodes
	 * @return a list of string values
	 */
	protected List<String> getStringList(List<Node> nodes) {
		List<String> items = null;
		if (nodes != null && nodes.size() > 0) {
			items = new FastList<String>(nodes.size());
			for (Node node : nodes) {
				items.add(node.getText());
			}
		} else {
			items = new FastList<String>(0);
		}
		return items;
	}

	/**
	 * load models from the given nodes
	 * 
	 * @param nodes
	 * @return model configuration
	 */
	protected void loadModelConfig(SiteConfigTO config, List<Node> nodes) {
		Map<QName, ModelConfigTO> models = new FastMap<QName, ModelConfigTO>();
		if (nodes != null && nodes.size() > 0) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (Node node : nodes) {
				String name = node.valueOf(ATTR_NAME);
				QName type = persistenceManagerService.createQName(name);
				if (type != null) {
					int depth = 1;
					try {
						depth = Integer.parseInt(node.valueOf(ATTR_DEPTH));
					} catch (NumberFormatException e) {
						// do nothing
					}
					String path = node.valueOf(ATTR_PATH);
					String displayName = node.valueOf(ATTR_DISPLAY_NAME);
					String namespace = node.valueOf(ATTR_NAMESPACE);
					models.put(type, new ModelConfigTO(path, depth, displayName, namespace));
				}
			}
		}
		config.setModelConfig(models);
	}

    /**
     * load page/component/assets patterns configuration
     *
     * @param site
     * @param nodes
     */
    @SuppressWarnings("unchecked")
    protected void loadPatterns(SiteConfigTO site, RepositoryConfigTO repo, List<Node> nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                String patternKey = node.valueOf(ATTR_NAME);
                if (!StringUtils.isEmpty(patternKey)) {
                    List<Node> patternNodes = node.selectNodes(ELM_PATTERN);
                    if (patternNodes != null) {
                        List<String> patterns = new FastList<String>(patternNodes.size());
                        for (Node patternNode : patternNodes) {
                            String pattern = patternNode.getText();
                            if (!StringUtils.isEmpty(pattern)) {
                                patterns.add(pattern);
                            }
                        }
                        if (patternKey.equals(PATTERN_PAGE)) {
                            repo.setPagePatterns(patterns);
                        } else if (patternKey.equals(PATTERN_COMPONENT)) {
                            repo.setComponentPatterns(patterns);
                        } else if (patternKey.equals(PATTERN_ASSET)) {
                            repo.setAssetPatterns(patterns);
                        } else if (patternKey.equals(PATTERN_DOCUMENT)) {
                            repo.setDocumentPatterns(patterns);
                        } else if (patternKey.equals(PATTERN_RENDERING_TEMPLATE)) {
                            repo.setRenderingTemplatePatterns(patterns);
                        } else if (patternKey.equals(PATTERN_LEVEL_DESCRIPTOR)) {
                            repo.setLevelDescriptorPatterns(patterns);
                        } else if (patternKey.equals(PATTERN_PREVIEWABLE_MIMETYPES)) {
                            repo.setPreviewableMimetypesPaterns(patterns);
                        } else {
                            LOGGER.error("Unknown pattern key: " + patternKey + " is provided in " + site.getName());
                        }
                    }
                } else {
                    LOGGER.error("no pattern key provided in " + site.getName() + " configuration. Skipping the pattern.");
                }
            }
        } else {
            LOGGER.warn(site.getName() + " does not have any pattern configuration.");
        }
    }

    /**
     * load top level folder configuration
     *
     * @param site
     * @param folderNodes
     */
    protected void loadFolderConfiguration(SiteConfigTO site, RepositoryConfigTO repo, List<Node> folderNodes) {
        if (folderNodes != null) {
            List<DmFolderConfigTO> folders = new FastList<DmFolderConfigTO>(folderNodes.size());
            for (Node folderNode : folderNodes) {
                DmFolderConfigTO folderConfig = new DmFolderConfigTO();
                folderConfig.setName(folderNode.valueOf(ATTR_NAME));
                folderConfig.setPath(folderNode.valueOf(ATTR_PATH));
                folderConfig.setReadDirectChildren(ContentFormatUtils.getBooleanValue(folderNode.valueOf(ATTR_READ_DIRECT_CHILDREN)));
                folderConfig.setAttachRootPrefix(ContentFormatUtils.getBooleanValue(folderNode.valueOf(ATTR_ATTACH_ROOT_PREFIX)));
                folders.add(folderConfig);
            }
            repo.setFolders(folders);
        } else {
            LOGGER.warn(site.getName() + " does not have any folder configuration.");
        }
    }

    @Override
    public Set<String> getAllAvailableSites() {
        Set<String> siteNames = _siteMapping.keySet();
        return siteNames;
    }

	/**
	 * Return if we check for renaming for the given site.
	 * 
	 * @param site
	 * @return true if we check for renames, false otherwise
	 */
	public boolean isCheckForRename(String site) {
		return _siteMapping.get(site).getRepositoryConfig().isCheckForRenamed();
	}
	
	
	/**
	 * set configuration path
	 * 
	 * @param configPath
	 */
	public void setConfigPath(String configPath) {
		this._configPath = configPath;
	}

	/**
	 * set configuration file name
	 * 
	 * @param configFileName
	 */
	public void setConfigFileName(String configFileName) {
		this._configFileName = configFileName;
	}

	/**
	 * @param contentTypesConfig the contentTypesConfig to set
	 */
	public void setContentTypesConfig(ContentTypesConfig contentTypesConfig) {
		this._contentTypesConfig = contentTypesConfig;
	}

    public List<String> getPreviewableMimetypesPaterns(String site) {
        if (isConfigUpdated(site)) {
            loadConfiguration(site);
        }
        SiteConfigTO config = _siteMapping.get(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getPreviewableMimetypesPaterns();
        }
        return null;
    }
    
    @Override
    public boolean siteExists(String site) {
        if (_siteMapping == null) return false;
        return _siteMapping.get(site) != null;
    }

}
