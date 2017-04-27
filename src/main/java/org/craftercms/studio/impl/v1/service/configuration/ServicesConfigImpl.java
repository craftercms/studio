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

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME;

/**
 * Implementation of ServicesConfigImpl. This class requires a configuration
 * file in the repository
 *
 *
 */
public class ServicesConfigImpl implements ServicesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicesConfigImpl.class);


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
	 * content types configuration
	 */
	protected ContentTypesConfig contentTypesConfig;

	/**
	 * Content service
	 */
	protected ContentService contentService;

	protected ContentRepository contentRepository;

    protected SiteConfigTO getSiteConfig(final String site) {
        return loadConfiguration(site);
    }

	public String getWemProject(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getWemProject() != null) {
			return config.getWemProject();
		}
		return null;
	}

    @Override
    public List<DmFolderConfigTO> getFolders(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getFolders();
        }
        return null;
    }

	public String getRootPrefix(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getRootPrefix();
		}
		return null;
	}

	public ContentTypeConfigTO getContentTypeConfig(String site, String name) {
		return contentTypesConfig.getContentTypeConfig(site, name);
	}

	public List<String> getAssetPatterns(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getAssetPatterns();
		}
		return null;
	}


	public List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(String site,String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getDeleteDependencyPattern();
		}
        return Collections.emptyList();
	}


	public List<CopyDependencyConfigTO> getCopyDependencyPatterns(String site,String contentType) {
        if(contentType==null){
             return Collections.emptyList();
        }
		ContentTypeConfigTO contentTypeConfig = contentTypesConfig.getContentTypeConfig(site, contentType);
		if (contentTypeConfig != null) {
			return contentTypeConfig.getCopyDepedencyPattern();
		}
        return Collections.emptyList();
	}

	public List<String> getComponentPatterns(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getComponentPatterns();
		}
		return null;
	}

	public List<String> getPagePatterns(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getPagePatterns();
		}
		return null;
	}

    public List<String> getRenderingTemplatePatterns(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getRenderingTemplatePatterns();
        }
        return null;
    }

    public List<String> getLevelDescriptorPatterns(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getLevelDescriptorPatterns();
        }
        return null;
    }

	public List<String> getDocumentPatterns(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDocumentPatterns();
		}
		return null;
	}

	public String getLevelDescriptorName(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getLevelDescriptorName();
		}
		return null;
	}

	public List<String> getDisplayInWidgetPathPatterns(String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDisplayPatterns();
		}
		return null;
	}

	public String getDefaultTimezone(String site) {
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
         String siteConfigPath = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, site);

         Document document = null;
         SiteConfigTO siteConfig = null;
         try {
             document = contentService.getContentAsDocument(site, siteConfigPath + "/" + getConfigFileName());
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
        loadFolderConfiguration(siteConfig, repoConfigTO, node.selectNodes("folders/folder"));
        loadPatterns(siteConfig, repoConfigTO, node.selectNodes("patterns/pattern-group"));
        List<String> displayPatterns = getStringList(node.selectNodes("display-in-widget-patterns/display-in-widget-pattern"));
        repoConfigTO.setDisplayPatterns(displayPatterns);
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
			items = new ArrayList<String>(nodes.size());
			for (Node node : nodes) {
				items.add(node.getText());
			}
		} else {
			items = new ArrayList<String>(0);
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
                        List<String> patterns = new ArrayList<String>(patternNodes.size());
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
            List<DmFolderConfigTO> folders = new ArrayList<DmFolderConfigTO>(folderNodes.size());
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

    public List<String> getPreviewableMimetypesPaterns(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getPreviewableMimetypesPaterns();
        }
        return null;
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
    }

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME);
    }

    @Override
    public void reloadConfiguration(String site) {
        SiteConfigTO config = loadConfiguration(site);
    }

    public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}



	public ContentTypesConfig getContentTypesConfig() {
		return contentTypesConfig;
	}

	public void setContentTypesConfig(ContentTypesConfig contentTypesConfig) {
		this.contentTypesConfig = contentTypesConfig;
	}

	public ContentRepository getContentRepository() { return contentRepository; }
	public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;
}
