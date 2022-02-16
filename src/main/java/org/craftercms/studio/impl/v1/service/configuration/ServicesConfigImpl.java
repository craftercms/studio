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
package org.craftercms.studio.impl.v1.service.configuration;

import com.google.common.cache.Cache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DmFolderConfigTO;
import org.craftercms.studio.api.v1.to.FacetRangeTO;
import org.craftercms.studio.api.v1.to.FacetTO;
import org.craftercms.studio.api.v1.to.RepositoryConfigTO;
import org.craftercms.studio.api.v1.to.SiteConfigTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_CONFIG_URL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_ADMIN_EMAIL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_AUTHORING_URL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_LIVE_URL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_PLUGIN_FOLDER_PATTERN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_SITE_URLS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_STAGING_URL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_DATE_TIME_FORMAT_OPTIONS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_ENABLE_STAGING_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_LOCALE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_PROTECTED_FOLDER_PATTERNS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_PUBLISHER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_REQUIRE_PEER_REVIEW;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_ELEMENT_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_TIME_ZONE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_CONFIG_XML_ELEMENT_WORKFLOW;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_TIME_ZONE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_LIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_STAGING;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;

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
    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;
    protected ConfigurationService configurationService;
    protected Cache<String, SiteConfigTO> configurationCache;

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
	public ContentTypeConfigTO getContentTypeConfig(@ValidateStringParam(name = "site") String site,
                                                    @ValidateStringParam(name = "name") String name) {
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
	public List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(@ValidateStringParam(name = "site") String site,
                                                                      @ValidateStringParam(name = "contentType") String contentType) {
        if (contentType == null ) {
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
	public List<CopyDependencyConfigTO> getCopyDependencyPatterns(@ValidateStringParam(name = "site") String site,
                                                                  @ValidateStringParam(name = "contentType") String contentType) {
        if (contentType == null ) {
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
        String timeZone = null;
		if (config != null) {
			timeZone = config.getTimezone();
		}
        if (StringUtils.isEmpty(timeZone)) {
            timeZone = studioConfiguration.getProperty(CONFIGURATION_DEFAULT_TIME_ZONE);
            if (StringUtils.isEmpty(timeZone)) {
                timeZone = TimeZone.getDefault().getID();

            }
        }
        return timeZone;
	}

    @Override
    @ValidateParams
    public String getPluginFolderPattern(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null) {
            return config.getPluginFolderPattern();
        }
        return null;
    }

    /**
	 * load services configuration
	 *
	 */
     protected SiteConfigTO loadConfiguration(String site) {
         String environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
         String configFilename = getConfigFileName();
         String cacheKey =
                 configurationService.getCacheKey(site, MODULE_STUDIO, getConfigFileName(), environment, "object");

         SiteConfigTO siteConfig = configurationCache.getIfPresent(cacheKey);
         if (siteConfig == null) {
             try {
                 Document document = configurationService
                         .getConfigurationAsDocument(site, MODULE_STUDIO, configFilename, environment);
                 if (document != null) {
                     Element root = document.getRootElement();
                     Node configNode = root.selectSingleNode("/site-config");
                     String name = configNode.valueOf("display-name");
                     siteConfig = new SiteConfigTO();
                     siteConfig.setName(name);
                     siteConfig.setWemProject(configNode.valueOf("wem-project"));
                     siteConfig.setTimezone(configNode.valueOf(SITE_CONFIG_XML_ELEMENT_LOCALE + "/" +
                             SITE_CONFIG_XML_ELEMENT_DATE_TIME_FORMAT_OPTIONS + "/" +
                             SITE_CONFIG_XML_ELEMENT_TIME_ZONE));
                     String sandboxBranch = configNode.valueOf(SITE_CONFIG_ELEMENT_SANDBOX_BRANCH);
                     if (StringUtils.isEmpty(sandboxBranch)) {
                         sandboxBranch = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
                     }
                     siteConfig.setSandboxBranch(sandboxBranch);
                     String stagingEnvironmentEnabledValue =
                             configNode.valueOf(SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY +
                                     "/" + SITE_CONFIG_XML_ELEMENT_ENABLE_STAGING_ENVIRONMENT);
                     if (StringUtils.isEmpty(stagingEnvironmentEnabledValue)) {
                         siteConfig.setStagingEnvironmentEnabled(false);
                     } else {
                         siteConfig.setStagingEnvironmentEnabled(Boolean.parseBoolean(stagingEnvironmentEnabledValue));
                     }

                     String stagingEnvironment =
                             configNode.valueOf(SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY + "/" +
                             SITE_CONFIG_XML_ELEMENT_STAGING_ENVIRONMENT);
                     if (StringUtils.isEmpty(stagingEnvironment)) {
                         stagingEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_STAGING);
                     }
                     siteConfig.setStagingEnvironment(stagingEnvironment);
                     String liveEnvironment =
                             configNode.valueOf(SITE_CONFIG_XML_ELEMENT_PUBLISHED_REPOSITORY + "/" +
                             SITE_CONFIG_XML_ELEMENT_LIVE_ENVIRONMENT);
                     if (StringUtils.isEmpty(liveEnvironment)) {
                         liveEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_LIVE);
                     }
                     siteConfig.setLiveEnvironment(liveEnvironment);

                     loadSiteUrlsConfiguration(siteConfig, configNode.selectSingleNode(SITE_CONFIG_ELEMENT_SITE_URLS));

                     String adminEmailAddressValue = configNode.valueOf(SITE_CONFIG_ELEMENT_ADMIN_EMAIL_ADDRESS);
                     siteConfig.setAdminEmailAddress(adminEmailAddressValue);

                     loadSiteRepositoryConfiguration(siteConfig, configNode.selectSingleNode("repository"));
                     // set the last updated date
                     siteConfig.setLastUpdated(DateUtils.getCurrentTime());

                     loadSearchFields(configNode, siteConfig);
                     loadFacetConfiguration(configNode, siteConfig);

                     siteConfig.setPluginFolderPattern(configNode.valueOf(SITE_CONFIG_ELEMENT_PLUGIN_FOLDER_PATTERN));

                     String requirePeerReviewValue =
                             configNode.valueOf(SITE_CONFIG_XML_ELEMENT_WORKFLOW + "/" +
                                     SITE_CONFIG_XML_ELEMENT_PUBLISHER + "/" +
                                     SITE_CONFIG_XML_ELEMENT_REQUIRE_PEER_REVIEW);
                     if (StringUtils.isEmpty(requirePeerReviewValue)) {
                         siteConfig.setRequirePeerReview(false);
                     } else {
                         siteConfig.setRequirePeerReview(Boolean.parseBoolean(requirePeerReviewValue));
                     }

                     List<String> protectedFolderPatterns =
                             getStringList(configNode.selectNodes(SITE_CONFIG_XML_ELEMENT_PROTECTED_FOLDER_PATTERNS));
                     siteConfig.setProtectedFolderPatterns(protectedFolderPatterns);

                     configurationCache.put(cacheKey, siteConfig);
                 }
             } catch(ServiceLayerException e) {
                 LOGGER.error("No site configuration found for " + site + " at " + getConfigFileName());
             }
         }
         return siteConfig;
     }

     protected void loadSiteUrlsConfiguration(SiteConfigTO siteConfig, Node configNode) {
         if (Objects.nonNull(configNode)) {
             String authoringUrlValue = configNode.valueOf(SITE_CONFIG_ELEMENT_AUTHORING_URL);
             siteConfig.setAuthoringUrl(authoringUrlValue);

             String stagingUrlValue = configNode.valueOf(SITE_CONFIG_ELEMENT_STAGING_URL);
             siteConfig.setStagingUrl(stagingUrlValue);

             String liveUrlValue = configNode.valueOf(SITE_CONFIG_ELEMENT_LIVE_URL);
             siteConfig.setLiveUrl(liveUrlValue);
         }
     }

     protected void loadSearchFields(Node root, SiteConfigTO config) {
         Map<String, Float> fields = new TreeMap<>();
         List<Node> fieldsConfig = root.selectNodes("search/fields/field");
         if(CollectionUtils.isNotEmpty(fieldsConfig)) {
             fieldsConfig.forEach(fieldConfig -> {
                 String name = XmlUtils.selectSingleNodeValue(fieldConfig, "name/text()");
                 String boost = XmlUtils.selectSingleNodeValue(fieldConfig, "boost/text()");
                 fields.put(name, StringUtils.isNotEmpty(boost)? Float.parseFloat(boost) : 1.0f);
             });
         }
         config.setSearchFields(fields);
     }

    /**
     * Loads the search facets configurations
     * @param root configuration to read
     * @param config configuration to update
     */
    protected void loadFacetConfiguration(Node root, SiteConfigTO config) {
        List<Node> facetsConfig = root.selectNodes("search/facets/facet");
        if(CollectionUtils.isNotEmpty(facetsConfig)) {
            Map<String, FacetTO> facets = facetsConfig.stream()
                .map(facetConfig -> {
                    FacetTO facet = new FacetTO();
                    facet.setName(XmlUtils.selectSingleNodeValue(facetConfig, "name/text()"));
                    facet.setField(XmlUtils.selectSingleNodeValue(facetConfig, "field/text()"));
                    facet.setDate(Boolean.parseBoolean(
                        XmlUtils.selectSingleNodeValue(facetConfig, "date/text()")));
                    facet.setMultiple(Boolean.parseBoolean(
                        XmlUtils.selectSingleNodeValue(facetConfig, "multiple/text()")));
                    List<Node> rangesConfig = facetConfig.selectNodes("ranges/range");
                    if(CollectionUtils.isNotEmpty(rangesConfig)) {
                        List<FacetRangeTO> ranges = rangesConfig.stream()
                            .map(rangeConfig -> {
                                FacetRangeTO range = new FacetRangeTO();
                                range.setLabel(XmlUtils.selectSingleNodeValue(rangeConfig, "label/text()"));
                                String from =XmlUtils.selectSingleNodeValue(rangeConfig, "from/text()");
                                if(StringUtils.isNotEmpty(from)) {
                                    range.setFrom(from);
                                }
                                String to = XmlUtils.selectSingleNodeValue(rangeConfig, "to/text()");
                                if(StringUtils.isNotEmpty(to)) {
                                    range.setTo(to);
                                }
                                return range;
                            })
                            .collect(Collectors.toList());
                        facet.setRanges(ranges);
                    }
                    return facet;
                })
                .collect(Collectors.toMap(FacetTO::getName, Function.identity()));
            config.setFacets(facets);
        }
    }

    /**
     * load the web-project configuration
     *
     * @param siteConfig
     * @param node
     */
    protected void loadSiteRepositoryConfiguration(SiteConfigTO siteConfig, Node node) {
        RepositoryConfigTO repoConfigTO = new RepositoryConfigTO();
        repoConfigTO.setRootPrefix(node.valueOf("@rootPrefix"));
        repoConfigTO.setLevelDescriptorName(node.valueOf("level-descriptor"));
        loadFolderConfiguration(siteConfig, repoConfigTO, node.selectNodes("folders/folder"));
        loadPatterns(siteConfig, repoConfigTO, node.selectNodes("patterns/pattern-group"));
        List<String> displayPatterns =
                getStringList(node.selectNodes("display-in-widget-patterns/display-in-widget-pattern"));
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
                    LOGGER.error("no pattern key provided in " + site.getName() +
                            " configuration. Skipping the pattern.");
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
                folderConfig.setReadDirectChildren(
                        ContentFormatUtils.getBooleanValue(folderNode.valueOf(ATTR_READ_DIRECT_CHILDREN)));
                folderConfig.setAttachRootPrefix(
                        ContentFormatUtils.getBooleanValue(folderNode.valueOf(ATTR_ATTACH_ROOT_PREFIX)));
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

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME);
    }

    @Override
    @ValidateParams
    public String getSandboxBranchName(@ValidateStringParam(name = "site") String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null) {
            return config.getSandboxBranch();
        }
        return null;
    }

    @Override
    public boolean isStagingEnvironmentEnabled(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null) {
            return config.isStagingEnvironmentEnabled();
        }
        return false;
    }

    @Override
    public String getStagingEnvironment(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null) {
            return config.getStagingEnvironment();
        }
        return null;
    }

    @Override
    public String getLiveEnvironment(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if (config != null) {
            return config.getLiveEnvironment();
        }
        return null;
    }

    @Override
    public Map<String, Float> getSearchFields(String site) {
        SiteConfigTO config = getSiteConfig(site);
        if(Objects.nonNull(config)) {
            return config.getSearchFields();
        }
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, FacetTO> getFacets(final String site) {
        SiteConfigTO config = getSiteConfig(site);
        if(Objects.nonNull(config)) {
            return config.getFacets();
        }
        return null;
    }

    @Override
    public String getAuthoringUrl(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (Objects.nonNull(config)) {
            if (StringUtils.isEmpty(config.getAuthoringUrl())) {
                return DEFAULT_CONFIG_URL;
            } else {
                return config.getAuthoringUrl();
            }
        }
        return null;
    }

    @Override
    public String getStagingUrl(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (Objects.nonNull(config)) {
            if (StringUtils.isEmpty(config.getStagingUrl())) {
                return DEFAULT_CONFIG_URL;
            } else {
                return config.getStagingUrl();
            }
        }
        return null;
    }

    @Override
    public String getLiveUrl(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (Objects.nonNull(config)) {
            if (StringUtils.isEmpty(config.getLiveUrl())) {
                return DEFAULT_CONFIG_URL;
            } else {
                return config.getLiveUrl();
            }
        }
        return null;
    }

    @Override
    public String getAdminEmailAddress(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (Objects.nonNull(config)) {
            return config.getAdminEmailAddress();
        }
        return null;
    }

    @Override
    public boolean isRequirePeerReview(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (config != null) {
            return config.isRequirePeerReview();
        }
        return false;
    }

    @Override
    public List<String> getProtectedFolderPatterns(String siteId) {
        SiteConfigTO config = getSiteConfig(siteId);
        if (config != null) {
            return config.getProtectedFolderPatterns();
        }
        return null;
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

	public ContentRepository getContentRepository() {
        return contentRepository;
    }

	public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setConfigurationCache(Cache<String, SiteConfigTO> configurationCache) {
        this.configurationCache = configurationCache;
    }

}
