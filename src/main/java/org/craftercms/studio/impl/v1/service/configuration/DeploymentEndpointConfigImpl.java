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
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.DeploymentConfigTO;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DEPLOYMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DEPLOYMENT_CONFIG_FILE_NAME;

public class DeploymentEndpointConfigImpl implements DeploymentEndpointConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentEndpointConfigImpl.class);

    protected DeploymentConfigTO loadConfiguration(String key) {
        String siteConfigPath = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, key);
        siteConfigPath = siteConfigPath + "/" + getConfigFileName();
        Document document = null;
        DeploymentConfigTO config = null;
        try {
            document = contentService.getContentAsDocument(key, siteConfigPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (document != null) {
            Element root = document.getRootElement();
            config = new DeploymentConfigTO();
            List<Element> endpoints = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_ROOT);
            for (Element endpointElm : endpoints) {
                DeploymentEndpointConfigTO endpointConfig = new DeploymentEndpointConfigTO();

                String name = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_NAME);
                endpointConfig.setName(name);

                String type = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_TYPE);
                endpointConfig.setType(type);

                String serverUrl = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SERVER_URL);
                endpointConfig.setServerUrl(serverUrl);

                String versionUrl = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_VERSION_URL);
                endpointConfig.setVersionUrl(versionUrl);

                String password = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PASSWORD);
                endpointConfig.setPassword(password);

                String target = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_TARGET);
                endpointConfig.setTarget(target);

                String siteId = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SITE_ID);
                endpointConfig.setSiteId(siteId);

                String sendMetadataStr = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_SEND_METADATA);
                boolean sendMetadataVal = (StringUtils.isNotEmpty(sendMetadataStr)) && Boolean.parseBoolean(sendMetadataStr);
                endpointConfig.setSendMetadata(sendMetadataVal);

                List<Element> excludePatternElms = endpointElm.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_EXCLUDE_PATTERN + "/" + StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PATTERN);
                List<String> excludePatternStrs = new ArrayList<>();
                for (Element patternElem : excludePatternElms) {
                    excludePatternStrs.add(patternElem.getText());
                }
                endpointConfig.setExcludePattern(excludePatternStrs);

                List<Element> includePatternElms = endpointElm.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_INCLUDE_PATTERN + "/" + StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PATTERN);
                List<String> includePatternStrs = new ArrayList<>();
                for (Element patternElem : includePatternElms) {
                    includePatternStrs.add(patternElem.getText());
                }
                endpointConfig.setIncludePattern(includePatternStrs);

                String bucketSizeStr = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_BUCKET_SIZE);
                if (StringUtils.isNotEmpty(bucketSizeStr)) {
                    try {
                        int bucketSizeVal = Integer.parseInt(bucketSizeStr);
                        endpointConfig.setBucketSize(bucketSizeVal);
                    } catch (NumberFormatException exc) {
                        LOGGER.info(String.format("Illegal number format for buckets size in deployment endpoint %s config [path: %s]", name, siteConfigPath));
                        LOGGER.info(String.format("Default bucket size %d will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                    }

                } else {
                    LOGGER.info(String.format("Buckets size not defined in deployment endpoint (%s) config [path: %s]", name, siteConfigPath));
                    LOGGER.info(String.format("Default bucket size (%d) will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                }

                String statusUrl = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_STATUS_URL);
                endpointConfig.setStatusUrl(statusUrl);

                String orderStr = endpointElm.valueOf(StudioXmlConstants.DOCUMENT_ELM_ENDPOINT_ORDER);
                if (StringUtils.isNotEmpty(orderStr)) {
                    try {
                        int orderVal = Integer.parseInt(orderStr);
                        endpointConfig.setOrder(orderVal);
                    } catch (NumberFormatException exc) {
                        LOGGER.info(String.format("Order not defined in deployment endpoint (%s) config [path: %s]", name, siteConfigPath));
                        LOGGER.info(String.format("Default order value (%d) will be used for endpoint [%s]", endpointConfig.getOrder(), name));
                    }
                }

                config.addEndpoint(endpointConfig);
            }

            config.setLastUpdated(new Date());
        }
        return config;
    }

    @Override
    public DeploymentEndpointConfigTO getDeploymentConfig(final String site, final String endpoint) {
        DeploymentConfigTO config = loadConfiguration(site);
        if (config != null) {
            return config.getEndpoint(endpoint);
        }
        return null;
    }


    @Override
    public DeploymentConfigTO getSiteDeploymentConfig(final String site) {
        DeploymentConfigTO config = loadConfiguration(site);
        return config;
    }

    @Override
    public void reloadConfiguration(String site) {
        DeploymentConfigTO config = loadConfiguration(site);
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPLOYMENT_CONFIG_BASE_PATH);
    }

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPLOYMENT_CONFIG_FILE_NAME);
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected ContentService contentService;
    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;
}
