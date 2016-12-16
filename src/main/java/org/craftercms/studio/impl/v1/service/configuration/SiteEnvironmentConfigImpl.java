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

	public String getFormServerUrl(String site) {
		//checkForUpdate(site);
		EnvironmentConfigTO config = getEnvironmentConfig(site);
		if (config != null) {
			return config.getFormServerUrlPattern();
		}
		return "";
	}

	@Override
	public String getCookieDomain(String site) {
		// TODO Auto-generated method stub
		return null;
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

			String openDropdown = root.valueOf("open-site-dropdown");
			config.setOpenDropdown((openDropdown != null) ? Boolean.valueOf(openDropdown) : false);

			String previewServerUrlPattern = root.valueOf("preview-server-url-pattern");
			config.setPreviewServerUrlPattern(previewServerUrlPattern);

			String orbeonServerUrlPattern = root.valueOf("form-server-url");
			config.setFormServerUrlPattern(orbeonServerUrlPattern);

			String authoringServerUrl = root.valueOf("authoring-server-url");
			config.setAuthoringServerUrl(authoringServerUrl);
			String authoringServerUrlPattern = root.valueOf("authoring-server-url-pattern");
			config.setAuthoringServerUrlPattern(authoringServerUrlPattern);

			String liveServerUrl = root.valueOf("live-server-url");
			config.setLiveServerUrl(liveServerUrl);

			String adminEmailAddress = root.valueOf("admin-email-address");
			config.setAdminEmailAddress(adminEmailAddress);
			String cookieDomain = root.valueOf("cookie-domain");
			config.setCookieDomain(cookieDomain);

            List<Element> channelGroupList = root.selectNodes("publishing-channels/channel-group");
            for (Element element : channelGroupList) {
                PublishingChannelGroupConfigTO pcgConfigTo = new PublishingChannelGroupConfigTO();
                Node node = element.selectSingleNode("label");
                if (node != null) pcgConfigTo.setName(node.getText());
                List<Element> channels = element.selectNodes("channels/channel");
                for (Element channel : channels) {
                    PublishingChannelConfigTO pcConfigTO = new PublishingChannelConfigTO();
                    pcConfigTO.setName(channel.getText());
                    if (!checkEndpointConfigured(key, pcConfigTO.getName())) {
                        logger.error("Deployment endpoint \"" + pcConfigTO.getName() + "\" is not configured for site " + key);
                    }
                    pcgConfigTo.getChannels().add(pcConfigTO);
                }
                node = element.selectSingleNode("live-environment");
                if (node != null) {
                    String isLiveEnvStr = node.getText();
                    boolean isLiveEnvVal = (StringUtils.isNotEmpty(isLiveEnvStr)) && Boolean.valueOf(isLiveEnvStr);
                    pcgConfigTo.setLiveEnvironment(isLiveEnvVal);
                    if (isLiveEnvVal) {
                        if (config.getLiveEnvironmentPublishingGroup() == null) {
                            config.setLiveEnvironmentPublishingGroup(pcgConfigTo);
                        } else {
                            pcgConfigTo.setLiveEnvironment(false);
                            logger.warn("Multiple publishing groups assigned as live environment. Only one publishing group can be live environment. " + config.getLiveEnvironmentPublishingGroup().getName() + " is already set as live environment.");
                        }
                    }
                }
                node = element.selectSingleNode("order");
                if (node != null) {
                    String orderStr = node.getText();
                    if (StringUtils.isNotEmpty(orderStr)) {
                        try {
                            int orderVal = Integer.parseInt(orderStr);
                            pcgConfigTo.setOrder(orderVal);
                        } catch (NumberFormatException exc) {
                            logger.info(String.format("Order not defined for publishing group (%s) config [path: %s]", pcgConfigTo.getName(), configLocation));
                            logger.info(String.format("Default order value (%d) will be used for publishing group [%s]", pcgConfigTo.getOrder(), pcgConfigTo.getName()));
                        }
                    }
                }
                List<Element> roles = element.selectNodes("roles/role");
                Set<String> rolesStr = new HashSet<String>();
                for (Element role : roles) {
                    rolesStr.add(role.getTextTrim());
                }
                pcgConfigTo.setRoles(rolesStr);
                config.getPublishingChannelGroupConfigs().put(pcgConfigTo.getName(), pcgConfigTo);
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
    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getPublishingChannelGroupConfigs();
        }
        return Collections.emptyMap();
    }

    @Override
    public PublishingChannelGroupConfigTO getLiveEnvironmentPublishingGroup(String site) {
        EnvironmentConfigTO config = getEnvironmentConfig(site);
        if (config != null) {
            return config.getLiveEnvironmentPublishingGroup();
        }
        return null;
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
