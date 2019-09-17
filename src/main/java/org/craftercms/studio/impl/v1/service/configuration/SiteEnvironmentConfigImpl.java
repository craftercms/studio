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
package org.craftercms.studio.impl.v1.service.configuration;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.configuration.SiteEnvironmentConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.EnvironmentConfigTO;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_DEFAULT_AUTHORING_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_DEFAULT_PREVIEW_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME;

public class SiteEnvironmentConfigImpl implements SiteEnvironmentConfig {

    private static final Logger logger = LoggerFactory.getLogger(SiteEnvironmentConfigImpl.class);

	/** environment key (e.g. dev, qa, staging..) **/
	protected ServicesConfig servicesConfig;
	protected ContentService contentService;
	protected ConfigurationService configurationService;
    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;

    @Override
    @ValidateParams
	public EnvironmentConfigTO getEnvironmentConfig(@ValidateStringParam(name = "site") final String site) {
        return loadConfiguration(site);
	}

	@Override
    @ValidateParams
	public String getPreviewServerUrl(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        RequestContext requestContext = RequestContext.getCurrent();
        HttpServletRequest request = null;
        String previewServerUrl = "";
        String currentDomainPreviewUrl = "";
        if (requestContext != null) {
            request = requestContext.getRequest();
            if (request != null) {
                currentDomainPreviewUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            }
        }
        if (config != null) {
            previewServerUrl = config.getPreviewServerUrl();
            if (StringUtils.isNotEmpty(currentDomainPreviewUrl)) {
                previewServerUrl = StringUtils.replaceFirst(previewServerUrl,
                        studioConfiguration.getProperty(CONFIGURATION_SITE_DEFAULT_PREVIEW_URL),
                        currentDomainPreviewUrl);
            }
        }
        return previewServerUrl;
	}

    @Override
    @ValidateParams
    public String getPreviewEngineServerUrl(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPreviewEngineServerUrl();
        }
        return "";
    }

    @Override
    @ValidateParams
    public String getGraphqlServerUrl(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getGraphqlServerUrl();
        }
        return "";
    }

	@Override
    @ValidateParams
	public String getLiveServerUrl(@ValidateStringParam(name = "site") String site) {
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getLiveServerUrl();
		}
		return "";
	}

	@Override
    @ValidateParams
	public String getAdminEmailAddress(@ValidateStringParam(name = "site") String site) {
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getAdminEmailAddress();
		}
		return "";
	}

	@Override
    @ValidateParams
	public String getAuthoringServerUrl(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        RequestContext requestContext = RequestContext.getCurrent();
        HttpServletRequest request = null;
        String authoringServerUrl = "";
        String currentDomainAuthoringUrl = "";
        if (requestContext != null) {
            request = requestContext.getRequest();
            if (request != null) {
                currentDomainAuthoringUrl =
                        request.getRequestURL().toString().replace(request.getPathInfo(), "");
            }
        }
        if (config != null) {
            authoringServerUrl = config.getAuthoringServerUrl();
            if (StringUtils.isNotEmpty(currentDomainAuthoringUrl)) {
                authoringServerUrl = StringUtils.replaceFirst(authoringServerUrl,
                        studioConfiguration.getProperty(CONFIGURATION_SITE_DEFAULT_AUTHORING_URL),
                        currentDomainAuthoringUrl);
            }
        }
        return authoringServerUrl;
	}

    @SuppressWarnings("unchecked")
	protected EnvironmentConfigTO loadConfiguration(String key) {
        EnvironmentConfigTO config = null;
		Document document = null;
		try {
			document = configurationService.getConfigurationAsDocument(key, MODULE_STUDIO, getConfigFileName(),
                    studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
		} catch (DocumentException | IOException e) {
			logger.error("Error reading environment configuration for site " + key + " from path " +
                    getConfigFileName());
		}
		if (document != null) {
            Element root = document.getRootElement();
			config = new EnvironmentConfigTO();
			String previewServerUrl = root.valueOf("preview-server-url");
			config.setPreviewServerUrl(previewServerUrl);

            String previewEngineServerUrl = root.valueOf(XML_TAG_PREVIEW_ENGINE_SERVER_URL);
            config.setPreviewEngineServerUrl(previewEngineServerUrl);

            String graphqlServerUrl = root.valueOf(XML_TAG_GRAPHQL_SERVER_URL);
            config.setGraphqlServerUrl(graphqlServerUrl);

			String openDropdown = root.valueOf("open-sidebar");
			config.setOpenDropdown((openDropdown != null) ? Boolean.valueOf(openDropdown) : false);

			String authoringServerUrl = root.valueOf("authoring-server-url");
			config.setAuthoringServerUrl(authoringServerUrl);

			String liveServerUrl = root.valueOf("live-server-url");
			config.setLiveServerUrl(liveServerUrl);

			String adminEmailAddress = root.valueOf("admin-email-address");
			config.setAdminEmailAddress(adminEmailAddress);

            List<Element> publishingTargetsList = root.selectNodes(PUBLISHING_TARGET_XPATH);
            for (Element element : publishingTargetsList) {
                PublishingTargetTO targetTO = new PublishingTargetTO();
                Node node = element.selectSingleNode(XML_TAG_REPO_BRANCH_NAME);
                if (node != null) {
                    targetTO.setRepoBranchName(node.getText());
                }
                node = element.selectSingleNode(XML_TAG_DISPLAY_LABEL);
                if (node != null) {
                    targetTO.setDisplayLabel(node.getText());
                }
                node = element.selectSingleNode("order");
                if (node != null) {
                    String orderStr = node.getText();
                    if (StringUtils.isNotEmpty(orderStr)) {
                        try {
                            int orderVal = Integer.parseInt(orderStr);
                            targetTO.setOrder(orderVal);
                        } catch (NumberFormatException exc) {
                            logger.info(String.format("Order not defined for publishing group (%s) config [path: %s]",
                                    targetTO.getDisplayLabel(), getConfigFileName()));
                            logger.info(String.format("Default order value (%d) will be used for publishing group [%s]",
                                    targetTO.getOrder(), targetTO.getDisplayLabel()));
                        }
                    }
                }
                config.getPublishingTargets().add(targetTO);
            }

            String previewDeploymentEndpoint = root.valueOf("preview-deployment-endpoint");
            config.setPreviewDeploymentEndpoint(previewDeploymentEndpoint);

			config.setLastUpdated(ZonedDateTime.now(ZoneOffset.UTC));
		}
        return config;
	}

    @Override
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = loadConfiguration(site);
    }

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT);
	}

    @Override
    @ValidateParams
    public boolean exists(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        return config != null;
    }

    @Override
    @ValidateParams
    public String getPreviewDeploymentEndpoint(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPreviewDeploymentEndpoint();
        }
        return null;
    }

    @Override
    @ValidateParams
    public List<PublishingTargetTO> getPublishingTargetsForSite(@ValidateStringParam(name = "site") String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPublishingTargets();
        } else {
            return new ArrayList<PublishingTargetTO>();
        }

    }

    private String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME);
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
