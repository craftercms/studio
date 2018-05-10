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
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.CStudioXmlConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.ConfigurableServiceBase;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.DeploymentConfigTO;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.to.TimeStamped;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.*;

public class DeploymentEndpointConfigImpl implements DeploymentEndpointConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(DeploymentEndpointConfigImpl.class);


    protected DeploymentConfigTO loadConfiguration(String key) {
        String siteConfigPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key);
        siteConfigPath = siteConfigPath + "/" + configFileName;
        Document document = null;
        DeploymentConfigTO config = null;
        try {
            document = contentService.getContentAsDocument(siteConfigPath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (document != null) {
            Element root = document.getRootElement();
            config = new DeploymentConfigTO();
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
                List<String> excludePatternStrs = new ArrayList<>();
                for (Element patternElem : excludePatternElms) {
                    excludePatternStrs.add(patternElem.getText());
                }
                endpointConfig.setExcludePattern(excludePatternStrs);

                List<Element> includePatternElms = endpointElm.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_INCLUDE_PATTERN + "/" + CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_PATTERN);
                List<String> includePatternStrs = new ArrayList<>();
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
                        LOGGER.info(String.format("Illegal number format for buckets size in deployment endpoint %s config [path: %s]", name, siteConfigPath));
                        LOGGER.info(String.format("Default bucket size %d will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                    }

                } else {
                    LOGGER.info(String.format("Buckets size not defined in deployment endpoint (%s) config [path: %s]", name, siteConfigPath));
                    LOGGER.info(String.format("Default bucket size (%d) will be used for endpoint [%s]", endpointConfig.getBucketSize(), name));
                }

                String statusUrl = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_STATUS_URL);
                endpointConfig.setStatusUrl(statusUrl);

                String orderStr = endpointElm.valueOf(CStudioXmlConstants.DOCUMENT_ELM_ENDPOINT_ORDER);
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
    @ValidateParams
    public DeploymentEndpointConfigTO getDeploymentConfig(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "endpoint") final String endpoint) {
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        CacheService cacheService = cacheTemplate.getCacheService();
        DeploymentConfigTO config = cacheTemplate.getObject(cacheContext, new Callback<DeploymentConfigTO>() {
            @Override
            public DeploymentConfigTO execute() {
                return loadConfiguration(site);
            }
        }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        if (config != null) {
            return config.getEndpoint(endpoint);
        }
        return null;
    }


    @Override
    @ValidateParams
    public DeploymentConfigTO getSiteDeploymentConfig(@ValidateStringParam(name = "site") final String site) {
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        DeploymentConfigTO config = cacheTemplate.getObject(cacheContext, new Callback<DeploymentConfigTO>() {
            @Override
            public DeploymentConfigTO execute() {
                return loadConfiguration(site);
            }
        }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        return config;
    }

    @Override
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        cacheService.remove(cacheContext, cacheKey);
        DeploymentConfigTO config = loadConfiguration(site);
        cacheService.put(cacheContext, cacheKey, config);
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }

    public String getConfigFileName() { return configFileName; }
    public void setConfigFileName(String configFileName) { this.configFileName = configFileName; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    protected ContentService contentService;
    protected CacheTemplate cacheTemplate;
    protected String configPath;
    protected String configFileName;
    protected GeneralLockService generalLockService;
}
