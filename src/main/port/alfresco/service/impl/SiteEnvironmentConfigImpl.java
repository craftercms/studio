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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioXmlConstants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.api.SiteEnvironmentConfig;
import org.craftercms.cstudio.alfresco.to.EnvironmentConfigTO;
import org.craftercms.cstudio.alfresco.to.PublishingChannelConfigTO;
import org.craftercms.cstudio.alfresco.to.PublishingChannelGroupConfigTO;
import org.craftercms.cstudio.alfresco.to.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SiteEnvironmentConfigImpl extends ConfigurableServiceBase implements SiteEnvironmentConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteEnvironmentConfigImpl.class);

            /** sites and environments mapping **/
    protected Map<String, EnvironmentConfigTO> _siteMapping = new HashMap<String, EnvironmentConfigTO>();
	/** environment key (e.g. dev, qa, staging..) **/
	protected String _environment;

    @Override
    public void register() {
        this._servicesManager.registerService(SiteEnvironmentConfig.class, this);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.SiteEnvironmentConfig#getEnvironmentConfig(java.lang.String)
      */
	@Override
	public EnvironmentConfigTO getEnvironmentConfig(String site) {
		checkForUpdate(site);
		return _siteMapping.get(site);
	}


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SiteEnvironmentConfig#getPreviewServerUrl(java.lang.String)
	 */
	public String getPreviewServerUrl(String site) {
		checkForUpdate(site);
		EnvironmentConfigTO config = _siteMapping.get(site);
		if (config != null) {
			String previewServerUrl = config.getPreviewServerUrl();
			if (!StringUtils.isEmpty(previewServerUrl)) {
				String sandbox = null;//_servicesConfig.getSandbox(site);
                ServicesConfig servicesConfig = getService(ServicesConfig.class);
				String webProject = servicesConfig.getWemProject(site);
				return previewServerUrl.replaceAll(CStudioConstants.PATTERN_WEB_PROJECT, webProject)
									.replaceAll(CStudioConstants.PATTERN_SANDBOX, sandbox);
			}
		}
		return "";
	}
	
	public String getLiveServerUrl(String site) {
		checkForUpdate(site);
		EnvironmentConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getLiveServerUrl();
		}
		return "";
	}
	
	public String getAdminEmailAddress(String site) {
		checkForUpdate(site);
		EnvironmentConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getAdminEmailAddress();
		}
		return "";
	}


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SiteEnvironmentConfig#getPreviewServerUrl(java.lang.String)
	 */
	public String getAuthoringServerUrl(String site) {
		checkForUpdate(site);
		EnvironmentConfigTO config = _siteMapping.get(site);
		if (config != null) {
			return config.getAuthoringServerUrl();
		}
		return "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SiteEnvironmentConfig#getFormServerUrl(java.lang.String)
	 */
	public String getFormServerUrl(String site) {
		checkForUpdate(site);
		EnvironmentConfigTO config = _siteMapping.get(site);
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

	@Override
	public boolean isUpdated(String site) {
		return super.isConfigUpdated(site);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfigRef(java.lang.String)
	 */
	protected NodeRef getConfigRef(String key) {
		String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key)
										.replaceFirst(CStudioConstants.PATTERN_ENVIRONMENT, _environment);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfiguration(java.lang.String)
	 */
	protected TimeStamped getConfiguration(String key) {
		return _siteMapping.get(key);
	}

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            _siteMapping.remove(key);
        }
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#loadConfiguration(java.lang.String)
      */
	protected void loadConfiguration(String key) {
		NodeRef configRef = getConfigRef(key);
        PersistenceManagerService persistenceManagerService =getService(PersistenceManagerService.class);
        if (configRef != null) {
            String configPath = persistenceManagerService.getNodePath(configRef);
            LOGGER.info("Loading environment configuration for " + key + "; Path: " + configPath);
        }

		Document document = persistenceManagerService.loadXml(configRef);
		if (document != null) {
            Element root = document.getRootElement();
			EnvironmentConfigTO config = new EnvironmentConfigTO();
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
                            LOGGER.warn("Multiple publishing groups assigned as live environment. Only one publishing group can be live environment. " + config.getLiveEnvironmentPublishingGroup().getName() + " is already set as live environment.");
                        }
                    }
                }
                config.getPublishingChannelGroupConfigs().put(pcgConfigTo.getName(), pcgConfigTo);
            }

            String previewDeploymentEndpoint = root.valueOf("preview-deployment-endpoint");
            config.setPreviewDeploymentEndpoint(previewDeploymentEndpoint);
            
			config.setLastUpdated(new Date());
			
			_siteMapping.put(key, config);
		}
	}

	/**
	 * @param environment the environment to set
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

    @Override
    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site) {
        checkForUpdate(site);
        EnvironmentConfigTO config = _siteMapping.get(site);
        if (config != null) {
            return config.getPublishingChannelGroupConfigs();
        }
        return Collections.emptyMap();
    }

    @Override
    public PublishingChannelGroupConfigTO getLiveEnvironmentPublishingGroup(String site) {
        checkForUpdate(site);
        EnvironmentConfigTO config = _siteMapping.get(site);
        if (config != null) {
            return config.getLiveEnvironmentPublishingGroup();
        }
        return null;
    }

    @Override
    public boolean exists(String site) {
        if (_siteMapping == null) return false;
        return _siteMapping.get(site) != null;
    }

    @Override
    public String getPreviewDeploymentEndpoint(String site) {
        checkForUpdate(site);
        EnvironmentConfigTO config = _siteMapping.get(site);
        if (config != null) {
            return config.getPreviewDeploymentEndpoint();
        }
        return null;
    }
}
