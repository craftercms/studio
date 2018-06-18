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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_PUBLISHED_LIVE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_PUBLISHED_STAGING;

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
    protected static final String PATTERN_SCRIPTS = "scripts";
    protected static final String PATTERN_LEVEL_DESCRIPTOR = "level-descriptor";
    protected static final String PATTERN_PREVIEWABLE_MIMETYPES = "previewable-mimetypes";

	/** xml element names **/
	protected static final String ELM_PATTERN = "pattern";

	/** xml attribute names **/
	protected static final String ATTR_NAME = "@name";
	protected static final String ATTR_PATH = "@path";
	protected static final String ATTR_READ_DIRECT_CHILDREN = "@read-direct-children";
	protected static final String ATTR_ATTACH_ROOT_PREFIX = "@attach-root-prefix";

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

    @Override
    @ValidateParams
	public String getRootPrefix(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getRootPrefix();
		}
		return null;
	}

	@Override
    @ValidateParams
	public ContentTypeConfigTO getContentTypeConfig(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "name") String name) {
		return contentTypesConfig.getContentTypeConfig(site, name);
	}

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

	@Override
    @ValidateParams
	public List<String> getComponentPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getComponentPatterns();
		}
		return null;
	}

	@Override
    @ValidateParams
	public List<String> getPagePatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getPagePatterns();
		}
		return null;
	}

	@Override
    @ValidateParams
    public List<String> getRenderingTemplatePatterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getRenderingTemplatePatterns();
        }
        return null;
    }

    @Override
    @ValidateParams
    public List<String> getScriptsPatterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getScriptsPatterns();
        }
        return null;
    }

    @Override
    @ValidateParams
    public List<String> getLevelDescriptorPatterns(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null && config.getRepositoryConfig() != null) {
            return config.getRepositoryConfig().getLevelDescriptorPatterns();
        }
        return null;
    }

    @Override
    @ValidateParams
	public List<String> getDocumentPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDocumentPatterns();
		}
		return null;
	}

	@Override
    @ValidateParams
	public String getLevelDescriptorName(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getLevelDescriptorName();
		}
		return null;
	}

	@Override
    @ValidateParams
	public List<String> getDisplayInWidgetPathPatterns(@ValidateStringParam(name = "site") String site) {
		SiteConfigTO config = getSiteConfig(site);
		if (config != null && config.getRepositoryConfig() != null) {
			return config.getRepositoryConfig().getDisplayPatterns();
		}
		return null;
	}

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
         String siteConfigPath = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, site);

         Document document = null;
         SiteConfigTO siteConfig = null;
         try {
             document = contentService.getContentAsDocument(site, siteConfigPath + FILE_SEPARATOR +  getConfigFileName());
         } catch (DocumentException e) {
             LOGGER.error("Error while loading configuration for " + site + " at " + siteConfigPath, e);
         }
         if (document != null) {
             Element root = document.getRootElement();
             Node configNode = root.selectSingleNode("/site-config");
             String name = configNode.valueOf("display-name");
             siteConfig = new SiteConfigTO();
             siteConfig.setName(name);
             siteConfig.setWemProject(configNode.valueOf("wem-project"));
             siteConfig.setTimezone(configNode.valueOf("default-timezone"));
             boolean siteEnvironmentConfigEnabled = Boolean.parseBoolean(
                     studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED));
             if (!siteEnvironmentConfigEnabled) {
                 String stagingEnvironment = configNode.valueOf(SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY + "/" +
                         SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT);
                 if (StringUtils.isEmpty(stagingEnvironment)) {
                     stagingEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_STAGING);
                 }
                 siteConfig.setStagingEnvironment(stagingEnvironment);
                 String liveEnvironment = configNode.valueOf(SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY + "/" +
                         SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT);
                 if (StringUtils.isEmpty(liveEnvironment)) {
                     liveEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_LIVE);
                 }
                 siteConfig.setLiveEnvironment(liveEnvironment);
             }
             loadSiteRepositoryConfiguration(siteConfig, configNode.selectSingleNode("repository"));
             // set the last updated date
             siteConfig.setLastUpdated(ZonedDateTime.now(ZoneOffset.UTC));
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
                        } else if (patternKey.equals(PATTERN_SCRIPTS)) {
                            repo.setScriptsPatterns(patterns);
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

    @Override
    @ValidateParams
    public List<String> getPreviewableMimetypesPaterns(@ValidateStringParam(name = "site") String site) {
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
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = loadConfiguration(site);
    }

    public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

    @Override
    public String getStagingEnvironment(String site) {
        return null;
    }

    @Override
    public String getLiveEnvironment(String site) {
        return null;
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
