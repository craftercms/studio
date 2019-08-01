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
import org.apache.commons.lang3.text.StrSubstitutor;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIGURATION;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_ENVIRONMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_MODULE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS;
import static org.craftercms.studio.api.v1.dal.ItemMetadata.PROP_LOCK_OWNER;
import static org.craftercms.studio.api.v1.dal.ItemMetadata.PROP_MODIFIED;
import static org.craftercms.studio.api.v1.dal.ItemMetadata.PROP_MODIFIER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;


public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    public static final String PLACEHOLDER_MODULE = "module";
    public static final String PLACEHOLDER_TYPE = "type";
    public static final String PLACEHOLDER_NAME = "name";

    private ContentService contentService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;
    private SecurityService securityService;
    private ObjectMetadataManager objectMetadataManager;
    private ServicesConfig servicesConfig;

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
                siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH)
                        .replaceFirst(PATTERN_SITE, siteId);
            }
        } else {
            siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH)
                    .replaceFirst(PATTERN_SITE, siteId);
        }
        return siteConfigPath;
    }

    private String getSiteRoleMappingsConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
    }

    @Override
    public String getConfigurationAsString(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                    String path, String environment) {
        return getEnvironmentConfiguration(siteId, module, path, environment);
    }

    @Override
    public Document getConfigurationAsDocument(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                               String path, String environment)
            throws DocumentException, IOException {
        String content = getEnvironmentConfiguration(siteId, module, path, environment);
        Document retDocument = null;
        SAXReader saxReader = new SAXReader();
        try {
            saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        }catch (SAXException ex){
            logger.error("Unable to turn off external entity loading, this could be a security risk.", ex);
        }
        try (InputStream is = IOUtils.toInputStream(content)) {
            retDocument = saxReader.read(is);
        }
        return retDocument;
    }

    private String getDefaultConfiguration(String siteId, String module, String path) {
        String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                .replaceAll(PATTERN_MODULE, module);
        String configPath = Paths.get(configBasePath, path).toString();
        return contentService.getContentAsString(siteId, configPath);
    }

    private String getEnvironmentConfiguration(String siteId, String module, String path, String environment) {
        if (!StringUtils.isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            String configPath =
                    Paths.get(configBasePath, path).toString();
            if (contentService.contentExists(siteId, configPath)) {
                return contentService.getContentAsString(siteId, configPath);
            }
        }
        return getDefaultConfiguration(siteId, module, path);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "write_configuration")
    public void writeConfiguration(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                   String path, String environment, InputStream content)
            throws ServiceLayerException {
        writeEnvironmentConfiguration(siteId, module, path, environment, content);
    }

    @Override
    public Resource getExtensionFile(String siteId, String module,
                                     String type, String name, String filename) throws ContentNotFoundException {

        String basePath = servicesConfig.getExtensionFolderPattern(siteId);
        if (StringUtils.isEmpty(basePath)) {
            throw new IllegalStateException(
                String.format("Site '%s' does not have an extension folder pattern", siteId));
        } else if (!StringUtils.contains(basePath, PLACEHOLDER_MODULE) ||
            !StringUtils.contains(basePath, PLACEHOLDER_TYPE) ||
            !StringUtils.contains(basePath, PLACEHOLDER_NAME)) {
            throw new IllegalStateException(
                String.format("Extension folder pattern for site '%s' does not contain all required placeholders"));
        }

        Map<String, String> values = new HashMap<>();
        values.put(PLACEHOLDER_MODULE, module);
        values.put(PLACEHOLDER_TYPE, type);
        values.put(PLACEHOLDER_NAME, name);
        basePath = StrSubstitutor.replace(basePath, values);

        String filePath = UrlUtils.concat(basePath, filename);

        return contentService.getContentAsResource(siteId, filePath);
    }

    private void writeDefaultConfiguration(String siteId, String module, String path, InputStream content)
            throws ServiceLayerException {
        String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                .replaceAll(PATTERN_MODULE, module);
        String configPath = Paths.get(configBasePath, path).toString();
        contentService.writeContent(siteId, configPath, content);
        String currentUser = securityService.getCurrentUser();
        updateMetadata(siteId, configPath, currentUser);
        generateAuditLog(siteId, configPath, currentUser);
    }

    private void writeEnvironmentConfiguration(String siteId, String module, String path, String environment,
                                               InputStream content) throws ServiceLayerException {
        if (!StringUtils.isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            if (contentService.contentExists(siteId, configBasePath)) {
                String configPath = Paths.get(configBasePath, path).toString();
                contentService.writeContent(siteId, configPath, content);
                String currentUser = securityService.getCurrentUser();
                updateMetadata(siteId, configPath, currentUser);
                generateAuditLog(siteId, configPath, currentUser);
            } else {
                writeDefaultConfiguration(siteId, module, path, content);
            }
        } else {
            writeDefaultConfiguration(siteId, module, path, content);
        }
    }

    private void updateMetadata(String siteId, String path, String user) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_MODIFIER, user);
        properties.put(PROP_MODIFIED, ZonedDateTime.now(ZoneOffset.UTC));
        properties.put(PROP_LOCK_OWNER, StringUtils.EMPTY);
        if (!objectMetadataManager.metadataExist(siteId, path)) {
            objectMetadataManager.insertNewObjectMetadata(siteId, path);
        }
        objectMetadataManager.setObjectMetadata(siteId, path, properties);
    }

    private void generateAuditLog(String siteId, String path, String user) throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_UPDATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(siteId + ":" + path);
        auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
        auditLog.setPrimaryTargetValue(path);
        auditLog.setPrimaryTargetSubtype(CONTENT_TYPE_CONFIGURATION);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Required
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }
}
