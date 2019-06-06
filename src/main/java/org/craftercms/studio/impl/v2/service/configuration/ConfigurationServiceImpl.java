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
package org.craftercms.studio.impl.v2.service.configuration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_MODULE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;


public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private ContentService contentService;
    private StudioConfiguration studioConfiguration;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> geRoleMappings(String siteId) throws ConfigurationException {
        // TODO: Refactor this to use Apache's Commons Configuration
        Map<String, List<String>> roleMappings = new HashMap<>();
        String roleMappingsConfigPath = getSiteRoleMappingsConfigPath(siteId);
        Document document;

        try {
            document = contentService.getContentAsDocument(siteId, roleMappingsConfigPath);
            Element root = document.getRootElement();
            if (root.getName().equals(DOCUMENT_ROLE_MAPPINGS)) {
                List<Node> groupNodes = root.selectNodes(DOCUMENT_ELM_GROUPS_NODE);
                for (Node node : groupNodes) {
                    String name = node.valueOf(DOCUMENT_ATTR_PERMISSIONS_NAME);
                    if (StringUtils.isNotEmpty(name)) {
                        List<Node> roleNodes = node.selectNodes(DOCUMENT_ELM_PERMISSION_ROLE);
                        List<String> roles = new ArrayList<>();

                        for (Node roleNode : roleNodes) {
                            roles.add(roleNode.getText());
                        }

                        roleMappings.put(name, roles);
                    }
                }
            }
        } catch (DocumentException e) {
            throw new ConfigurationException("Error while reading role mappings file for site " + siteId + " @ " +
                                             roleMappingsConfigPath);
        }

        return roleMappings;
    }


    private String getSiteRoleMappingsConfigPath(String siteId) {
        return UrlUtils.concat(getSiteConfigPath(siteId), getSiteRoleMappingsConfigFileName());
    }

    private String getSiteConfigPath(String siteId) {
        String siteConfigPath = StringUtils.EMPTY;
        if (!StringUtils.isEmpty(studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE))) {
            siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH)
                    .replaceAll(PATTERN_ENVIRONMENT, studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            if (!contentService.contentExists(siteId,siteConfigPath + FILE_SEPARATOR + getSiteRoleMappingsConfigFileName())) {
                siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH).replaceFirst(PATTERN_SITE, siteId);
            }
        } else {
            studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH).replaceFirst(PATTERN_SITE, siteId);
        }
        return siteConfigPath;
    }

    private String getSiteRoleMappingsConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
    }

    @Override
    public String loadConfiguration(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                    String location, String environment) {
        return loadEnvironmentConfiguration(siteId, module, location, environment);
    }

    @Override
    public Document loadConfigurationDocument(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                              String location, String environment)
            throws DocumentException, IOException {
        String content = loadEnvironmentConfiguration(siteId, module, location, environment);
        Document retDocument = null;
        SAXReader saxReader = new SAXReader();
        try {
            saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        }catch (SAXException ex){
            logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
        }
        try (InputStream is = IOUtils.toInputStream(content)) {
            retDocument = saxReader.read(is);
        }
        return retDocument;
    }

    private String loadDefaultConfiguration(String siteId, String module, String location) {
        String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                .replaceAll(PATTERN_MODULE, module);
        String configPath = Paths.get(configBasePath, location).toString();
        return contentService.getContentAsString(siteId, configPath);
    }

    private String loadEnvironmentConfiguration(String siteId, String module, String location, String environment) {
        if (!StringUtils.isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            String configPath =
                    Paths.get(configBasePath, location).toString();
            if (contentService.contentExists(siteId, configPath)) {
                return contentService.getContentAsString(siteId, configPath);
            }
        }
        return loadDefaultConfiguration(siteId, module, location);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "write_configuration")
    public void writeConfiguration(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                   String location, String environment, InputStream content)
            throws ServiceLayerException {
        writeEnvironmentConfiguration(siteId, module, location, environment, content);
    }

    private void writeDefaultConfiguration(String siteId, String module, String location, InputStream content)
            throws ServiceLayerException {
        String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                .replaceAll(PATTERN_MODULE, module);
        String configPath = Paths.get(configBasePath, location).toString();
        contentService.writeContent(siteId, configPath, content);
    }

    private void writeEnvironmentConfiguration(String siteId, String module, String location, String environment,
                                               InputStream content) throws ServiceLayerException {
        if (!StringUtils.isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            if (contentService.contentExists(siteId, configBasePath)) {
                String configPath = Paths.get(configBasePath, location).toString();
                contentService.writeContent(siteId, configPath, content);
            } else {
                writeDefaultConfiguration(siteId, module, location, content);
            }
        } else {
            writeDefaultConfiguration(siteId, module, location, content);
        }
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Required
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

}
