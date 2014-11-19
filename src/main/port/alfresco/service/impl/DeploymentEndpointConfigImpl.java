/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.service.impl;

import javolution.util.FastList;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioXmlConstants;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.service.api.DeploymentEndpointConfig;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.to.DeploymentConfigTO;
import org.craftercms.cstudio.alfresco.to.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentEndpointConfigImpl extends ConfigurableServiceBase implements DeploymentEndpointConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(DeploymentEndpointConfigImpl.class);

    /** sites and environments mapping **/
    protected Map<String, DeploymentConfigTO> siteMapping = new HashMap<String, DeploymentConfigTO>();


    @Override
    protected void loadConfiguration(String key) {
        NodeRef configRef = getConfigRef(key);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        Document document = persistenceManagerService.loadXml(configRef);
        if (document != null) {
            Element root = document.getRootElement();
            DeploymentConfigTO config = new DeploymentConfigTO();
            List<Element> endpoints = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_ROOT);
            for (Element endpointElm : endpoints) {
                DeploymentEndpointConfigTO endpointConfig = new DeploymentEndpointConfigTO();

                String name = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_NAME);
                endpointConfig.setName(name);

                String type = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_TYPE);
                endpointConfig.setType(type);

                String serverUrl = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SERVER_URL);
                endpointConfig.setServerUrl(serverUrl);

                String versionUrl = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_VERSION_URL);
                endpointConfig.setVersionUrl(versionUrl);

                String password = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PASSWORD);
                endpointConfig.setPassword(password);

                String target = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_TARGET);
                endpointConfig.setTarget(target);

                String siteId = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SITE_ID);
                endpointConfig.setSiteId(siteId);

                String sendMetadataStr = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SEND_METADATA);
                boolean sendMetadataVal = (StringUtils.isNotEmpty(sendMetadataStr)) && Boolean.parseBoolean(sendMetadataStr);
                endpointConfig.setSendMetadata(sendMetadataVal);

                List<Element> excludePatternElms = endpointElm.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_EXCLUDE_PATTERN + "/" + CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PATTERN);
                List<String> excludePatternStrs = new FastList<String>();
                for (Element patternElem : excludePatternElms) {
                    excludePatternStrs.add(patternElem.getText());
                }
                endpointConfig.setExcludePattern(excludePatternStrs);

                List<Element> includePatternElms = endpointElm.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_INCLUDE_PATTERN + "/" + CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PATTERN);
                List<String> includePatternStrs = new FastList<String>();
                for (Element patternElem : includePatternElms) {
                    includePatternStrs.add(patternElem.getText());
                }
                endpointConfig.setIncludePattern(includePatternStrs);

                String bucketSizeStr = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_BUCKET_SIZE);
                if (StringUtils.isNotEmpty(bucketSizeStr)) {
                    try {
                        int bucketSizeVal = Integer.parseInt(bucketSizeStr);
                        endpointConfig.setBucketSize(bucketSizeVal);
                    } catch (NumberFormatException exc) {
                        if (LOGGER.isInfoEnabled()) {
                            String configPath = persistenceManagerService.getNodePath(configRef);
                            LOGGER.info(String.format("Illegal number format for buckets size in deployment endpoint %s config [path: %s]", name, configPath));
                            LOGGER.info(String.format("Default bucket size %d will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                        }
                    }

                } else {
                    if (LOGGER.isInfoEnabled()) {
                        String configPath = persistenceManagerService.getNodePath(configRef);
                        LOGGER.info(String.format("Buckets size not defined in deployment endpoint (%s) config [path: %s]", name, configPath));
                        LOGGER.info(String.format("Default bucket size (%d) will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                    }
                }

                String statusUrl = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_STATUS_URL);
                endpointConfig.setStatusUrl(statusUrl);

                config.addEndpoint(endpointConfig);
            }

            config.setLastUpdated(new Date());
            siteMapping.put(key, config);
        }
    }

    @Override
    protected TimeStamped getConfiguration(String key) {
        return siteMapping.get(key);
    }

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            siteMapping.remove(key);
        }
    }

    @Override
    protected NodeRef getConfigRef(String key) {
        String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
    }

    @Override
    public void register() {
        getServicesManager().registerService(DeploymentEndpointConfig.class, this);
    }

    @Override
    public DeploymentEndpointConfigTO getDeploymentConfig(String site, String endpoint) {
        checkForUpdate(site);
        if (siteMapping.containsKey(site)) {
            DeploymentConfigTO config = siteMapping.get(site);
            if (config != null) {
                return config.getEndpoint(endpoint);
            }
        }
        return null;
    }

    @Override
    public boolean isUpdated(String site) {
        return super.isConfigUpdated(site);
    }

    @Override
    public boolean exists(String site) {
        if (siteMapping == null) return false;
        return siteMapping.get(site) != null;
    }

    @Override
    public DeploymentConfigTO getSiteDeploymentConfig(String site) {
        checkForUpdate(site);
        return siteMapping.get(site);
    }
}
