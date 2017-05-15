/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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


import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.configuration.SiteEnvironmentConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME;

public class SiteEnvironmentConfigImpl implements SiteEnvironmentConfig {

    private static final Logger logger = LoggerFactory.getLogger(SiteEnvironmentConfigImpl.class);

	/** environment key (e.g. dev, qa, staging..) **/
	protected ServicesConfig servicesConfig;
	protected ContentService contentService;

	public ServicesConfig getServicesConfig() { return servicesConfig; }
	public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

	public ContentService getContentService() { return contentService; }
	public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getConfigPath() {
	    return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_BASE_PATH);
	}

    public String getConfigFileName() {
	    return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_FILE_NAME);
	}

	@Override
	public EnvironmentConfigTO getEnvironmentConfig(final String site) {
        return loadConfiguration(site);
	}

	public String getPreviewServerUrl(String site) {
		//checkForUpdate(site);
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			String previewServerUrl = config.getPreviewServerUrl();
			if (!StringUtils.isEmpty(previewServerUrl)) {
				String sandbox = null;//_servicesConfig.getSandbox(site);
				String webProject = servicesConfig.getWemProject(site);
				return previewServerUrl.replaceAll(StudioConstants.PATTERN_WEB_PROJECT, webProject)
									.replaceAll(StudioConstants.PATTERN_SANDBOX, sandbox);
			}
		}
		return "";
	}

	public String getLiveServerUrl(String site) {
		//checkForUpdate(site);
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getLiveServerUrl();
		}
		return "";
	}

	public String getAdminEmailAddress(String site) {
		//checkForUpdate(site);
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getAdminEmailAddress();
		}
		return "";
	}

	public String getAuthoringServerUrl(String site) {
		//checkForUpdate(site);
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getAuthoringServerUrl();
		}
		return "";
	}

	protected EnvironmentConfigTO loadConfiguration(String key) {
		String configLocation = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, key)
				.replaceFirst(StudioConstants.PATTERN_ENVIRONMENT, getEnvironment());
		configLocation = configLocation + "/" + getConfigFileName();
        EnvironmentConfigTO config = null;
		Document document = null;
		try {
			document = contentService.getContentAsDocument(key, configLocation);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		if (document != null) {
            Element root = document.getRootElement();
			config = new EnvironmentConfigTO();
			String previewServerUrl = root.valueOf("preview-server-url");
			config.setPreviewServerUrl(previewServerUrl);

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
                            logger.info(String.format("Order not defined for publishing group (%s) config [path: %s]", targetTO.getDisplayLabel(), configLocation));
                            logger.info(String.format("Default order value (%d) will be used for publishing group [%s]", targetTO.getOrder(), targetTO.getDisplayLabel()));
                        }
                    }
                }
                config.getPublishingTargets().add(targetTO);
            }

            String previewDeploymentEndpoint = root.valueOf("preview-deployment-endpoint");
            config.setPreviewDeploymentEndpoint(previewDeploymentEndpoint);

			config.setLastUpdated(new Date());
		}
        return config;
	}

    @Override
    public void reloadConfiguration(String site) {
        EnvironmentConfigTO config = loadConfiguration(site);
    }

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT);
	}

    @Override
    public boolean exists(String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        return config != null;
    }

    @Override
    public String getPreviewDeploymentEndpoint(String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPreviewDeploymentEndpoint();
        }
        return null;
    }

    @Override
    public List<PublishingTargetTO> getPublishingTargetsForSite(String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPublishingTargets();
        } else {
            return new ArrayList<PublishingTargetTO>();
        }

    }

    protected boolean checkEndpointConfigured(String site, String endpointName) {
        DeploymentEndpointConfigTO endpointConfigTO = deploymentEndpointConfig.getDeploymentConfig(site, endpointName);
        return (endpointConfigTO != null);
    }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public DeploymentEndpointConfig getDeploymentEndpointConfig() { return deploymentEndpointConfig; }
    public void setDeploymentEndpointConfig(DeploymentEndpointConfig deploymentEndpointConfig) { this.deploymentEndpointConfig = deploymentEndpointConfig; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected GeneralLockService generalLockService;
    protected DeploymentEndpointConfig deploymentEndpointConfig;
    protected StudioConfiguration studioConfiguration;
}
