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
package org.craftercms.studio.impl.v1.service.configuration;


import javolution.util.FastList;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of ServicesConfigImpl. This class requires a configuration
 * file in the repository
 * 
 * 
 */
public class ServicesConfigImpl implements ServicesConfig {

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
	protected String configPath;

	/**
	 * configuration file name
	 */
	protected String configFileName;
	
	/**
	 * content types configuration
	 */
	protected ContentTypesConfig contentTypesConfig;

	/**
	 * Content service
	 */
	protected ContentService contentService;

	protected ContentRepository contentRepository;

    protected CacheTemplate cacheTemplate;

    protected SiteConfigTO getSiteConfig(final String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        SiteConfigTO config = cacheTemplate.getObject(cacheContext, new Callback<SiteConfigTO>() {
            @Override
            public SiteConfigTO execute() {
                return loadConfiguration(site);
            }
        }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        return config;
    }

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getWebProject(java.lang.String)
	 */
	@Override
	@ValidateParams
	public String getWemProject(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getWemProject() != null) {
			return config.getWemProject();
		}
		return null;
	}

    @Override
    @ValidateParams
    public List<DmFolderConfigTO> getFolders(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getFolders();
        }
        return null;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmServicesConfig#getRootPrefix(java.lang.String)
      */
    @Override
    @ValidateParams
	public String getRootPrefix(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getRootPrefix();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getContentType(java.lang.String, java.lang.String)
	 */
	@Override
	@ValidateParams
	public ContentTypeConfigTO getContentTypeConfig(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "name") String name) {
		return contentTypesConfig.getContentTypeConfig(site, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getAssetPatterns(java.lang.String)
	 */
	@Override
	@ValidateParams
	public List<String> getAssetPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getAssetPatterns();
		}
		return null;
	}

	@Override
    @ValidateParams
	public List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "contentType") String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getDeleteDependencyPattern();
		}
        return Collections.emptyList();
	}

	@Override
    @ValidateParams
	public List<CopyDependencyConfigTO> getCopyDependencyPatterns(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "contentType") String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getCopyDepedencyPattern();
		}
        return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getComponentPatterns(java.lang.String)
	 */
	@Override
	@ValidateParams
	public List<String> getComponentPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getComponentPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getPagePatterns(java.lang.String)
	 */
	@Override
	@ValidateParams
	public List<String> getPagePatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getPagePatterns();
		}
		return null;
	}

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getRenderingTemplatePatterns(java.lang.String)
      */
    @Override
    @ValidateParams
    public List<String> getRenderingTemplatePatterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getRenderingTemplatePatterns();
        }
        return null;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getLevelDescriptorPatterns(java.lang.String)
      */
    @Override
    @ValidateParams
    public List<String> getLevelDescriptorPatterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getLevelDescriptorPatterns();
        }
        return null;
    }


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDocumentPatterns(java.lang.String)
	 */
	@Override
	@ValidateParams
	public List<String> getDocumentPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDocumentPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getLevelDescriptorName(java.lang.String)
	 */
	@Override
	@ValidateParams
	public String getLevelDescriptorName(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getLevelDescriptorName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDisplayInWidgetPathPatterns(java.lang.String)
	 */
	@Override
	@ValidateParams
	public List<String> getDisplayInWidgetPathPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDisplayPatterns();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ServicesConfig#getDefaultTimezone(java.lang.String)
	 */
	@Override
	@ValidateParams
	public String getDefaultTimezone(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null) {
			return config.getTimezone();
		} else {
			return null;
		}
	}


	/**
	 * load services configuration
	 * 
	 */
	 @SuppressWarnings("unchecked")
     protected SiteConfigTO loadConfiguration(String site) {
         String siteConfigPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);

         Document document = null;
         SiteConfigTO siteConfig = null;
         try {
             document = contentService.getContentAsDocument(siteConfigPath + "/" + configFileName);
         } catch (DocumentException e) {
             LOGGER.error("Error while loading configuration for " + site + " at " + siteConfigPath, e);
         }
         if (document != null) {
             Element root = document.getRootElement();
             Node configNode = root.selectSingleNode("/site-config");
             String name = configNode.valueOf("display-name");
             siteConfig = new SiteConfigTO();
             siteConfig.setName(name);
             //siteConfig.setSiteName(configNode.valueOf("name"));
             siteConfig.setWemProject(configNode.valueOf("wem-project"));
             //siteConfig.setDefaultContentType(configNode.valueOf("default-content-type"));
             //String assetUrl = configNode.valueOf("assets-url");
             siteConfig.setTimezone(configNode.valueOf("default-timezone"));
             //siteConfig.setAssetUrl(assetUrl);
             //loadNamespaceToTypeMap(siteConfig, configNode.selectNodes("namespace-to-type-map/namespace"));
             //loadModelConfig(siteConfig, configNode.selectNodes("models/model"));
             //SearchConfigTO searchConfig = _contentTypesConfig.loadSearchConfig(configNode.selectSingleNode("search"));
             //siteConfig.setDefaultSearchConfig(searchConfig);
             loadSiteRepositoryConfiguration(siteConfig, configNode.selectSingleNode("repository"));
             // set the last updated date
             siteConfig.setLastUpdated(new Date());
         } else {
             LOGGER.error("No site configuration found for " + site + " at " + siteConfigPath);
         }
         return siteConfig;
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
        //repoConfigTO.setIndexRepository(ContentFormatUtils.getBooleanValue(node.valueOf("index-repository")));
        /*String timeValue = node.valueOf("index-time-to-live");
        if (!StringUtils.isEmpty(timeValue)) {
            long indexTimeToLive = ContentFormatUtils.getLongValue(timeValue);
            if (indexTimeToLive > 0) {
                repoConfigTO.setIndexTimeToLive(indexTimeToLive);
            }
        }*/
        //repoConfigTO.setCheckForRenamed(org.craftercms.cstudio.alfresco.util.ContentFormatUtils.getBooleanValue(node.valueOf("check-for-renamed")));
        loadFolderConfiguration(siteConfig, repoConfigTO, node.selectNodes("folders/folder"));
        loadPatterns(siteConfig, repoConfigTO, node.selectNodes("patterns/pattern-group"));
        //List<String> excludePaths = getStringList(node.selectNodes("exclude-paths/exclude-path"));
        //repoConfigTO.setExcludePaths(excludePaths);
        List<String> displayPatterns = getStringList(node.selectNodes("display-in-widget-patterns/display-in-widget-pattern"));
        repoConfigTO.setDisplayPatterns(displayPatterns);
        //loadTemplateConfig(repoConfigTO, node.selectSingleNode("common-prototype-config"));
        siteConfig.setRepositoryConfig(repoConfigTO);
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
    @ValidateParams
    public List<String> getPreviewableMimetypesPaterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getPreviewableMimetypesPaterns();
        }
        return null;
    }

    @Override
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        cacheService.remove(cacheContext, cacheKey);
        SiteConfigTO config = loadConfiguration(site);
        cacheService.put(cacheContext, cacheKey, config);
    }

    public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public ContentTypesConfig getContentTypesConfig() {
		return contentTypesConfig;
	}

	public void setContentTypesConfig(ContentTypesConfig contentTypesConfig) {
		this.contentTypesConfig = contentTypesConfig;
	}

	public ContentRepository getContentRepository() { return contentRepository; }
	public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    protected GeneralLockService generalLockService;
}
