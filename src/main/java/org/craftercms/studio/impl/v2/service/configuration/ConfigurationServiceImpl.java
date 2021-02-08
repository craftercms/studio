/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.service.configuration;

import com.google.common.cache.Cache;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.craftercms.commons.config.DisableClassLoadingConstructor;
import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.ContentItemVersion;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.exception.configuration.InvalidConfigurationException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.config.TranslationConfiguration;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIGURATION;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIG_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
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
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_ON_MASK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PLUGIN_BASE_PATTERN;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;


public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    public static final String PLACEHOLDER_TYPE = "type";
    public static final String PLACEHOLDER_NAME = "name";
    public static final String PLACEHOLDER_ID = "id";

    /* Translation Config */
    public static final String CONFIG_KEY_TRANSLATION_DEFAULT_LOCALE = "defaultLocaleCode";
    public static final String CONFIG_KEY_TRANSLATION_LOCALES = "localeCodes.localeCode";

    private ContentService contentService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;
    private SecurityService securityService;
    private ObjectMetadataManager objectMetadataManager;
    private ServicesConfig servicesConfig;
    private ObjectStateService objectStateService;
    private EventService eventService;
    private EncryptionAwareConfigurationReader configurationReader;
    private ItemServiceInternal itemServiceInternal;
    private org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2;

    private String translationConfig;
    private Cache<String, Object> configurationCache;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> geRoleMappings(String siteId) throws ServiceLayerException {
        // The write seems to always send env = null
        var cacheKey = getCacheKey(siteId, MODULE_STUDIO, getSiteRoleMappingsConfigFileName(), null);
        try {
            return (Map<String, List<String>>) configurationCache.get(cacheKey, () -> {
                logger.debug("CACHE MISS: {0}", cacheKey);
                // TODO: Refactor this to use Apache's Commons Configuration
                Map<String, List<String>> roleMappings = new HashMap<>();
                String roleMappingsConfigPath = getSiteRoleMappingsConfigPath(siteId);
                Document document;

                try {
                    document = contentService.getContentAsDocument(siteId, roleMappingsConfigPath);
                    if (document != null) {
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
                    }
                } catch (DocumentException e) {
                    throw new ConfigurationException("Error while reading role mappings file for site " + siteId +
                            " @ " + roleMappingsConfigPath);
                }

                return roleMappings;
            });
        } catch (ExecutionException e) {
            throw new ServiceLayerException("Error loading configuration", e);
        }
    }


    private String getSiteRoleMappingsConfigPath(String siteId) {
        return UrlUtils.concat(getSiteConfigPath(siteId), getSiteRoleMappingsConfigFileName());
    }

    private String getSiteConfigPath(String siteId) {
        String siteConfigPath = StringUtils.EMPTY;
        if (!isEmpty(studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE))) {
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
                                    String path, String environment) throws ServiceLayerException {
        return getEnvironmentConfiguration(siteId, module, path, environment);
    }

    @Override
    public Document getConfigurationAsDocument(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                               String path, String environment) throws ServiceLayerException {
        var cacheKey = getCacheKey(siteId,module,path, environment);
        try {
            return (Document) configurationCache.get(cacheKey, () -> {
                logger.debug("CACHE MISS: {0}", cacheKey);
                String content = getEnvironmentConfiguration(siteId, module, path, environment);
                Document retDocument = null;
                if (StringUtils.isNotEmpty(content)) {
                    SAXReader saxReader = new SAXReader();
                    try {
                        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                        saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    } catch (SAXException ex) {
                        logger.error("Unable to turn off external entity loading, this could be a security risk.", ex);
                    }
                    try (InputStream is = IOUtils.toInputStream(content, UTF_8)) {
                        retDocument = saxReader.read(is);
                    }
                }
                return retDocument;
            });
        } catch (ExecutionException e) {
            throw new ServiceLayerException("Error loading configuration", e);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "write_global_configuration")
    public String getGlobalConfiguration(@ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        return contentService.getContentAsString(StringUtils.EMPTY, path);
    }

    private String getDefaultConfiguration(String siteId, String module, String path) {
        String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                .replaceAll(PATTERN_MODULE, module);
        String configPath = Paths.get(configBasePath, path).toString();
        return contentService.getContentAsString(siteId, configPath);
    }

    private String getEnvironmentConfiguration(String siteId, String module, String path, String environment) {
        if (!isEmpty(environment)) {
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
        if (StringUtils.endsWithAny(path, FILE_SEPARATOR +
                        studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME), FILE_SEPARATOR +
                studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME))) {
            securityServiceV2.invalidateAvailableActions(siteId);
        }
        var cacheKey = getCacheKey(siteId,module,path,environment);
        logger.debug("INVALIDATING CACHE: {0}", cacheKey);
        configurationCache.invalidate(cacheKey);
    }

    protected String getCacheKey(String siteId, String module, String path, String environment) {
        return format("%s:%s:%s:%s", siteId, module, path, environment);
    }

    @Override
    public Resource getPluginFile(String siteId, String pluginId, String type, String name, String filename)
        throws ContentNotFoundException {

        String basePath;
        if (isEmpty(pluginId)) {
            basePath = servicesConfig.getPluginFolderPattern(siteId);
        } else {
            basePath = studioConfiguration.getProperty(PLUGIN_BASE_PATTERN);
        }

        if (isEmpty(basePath)) {
            throw new IllegalStateException(
                format("Site '%s' does not have an plugin folder pattern configured", siteId));
        } else if (!StringUtils.contains(basePath, PLACEHOLDER_TYPE) ||
            !StringUtils.contains(basePath, PLACEHOLDER_NAME)) {
            throw new IllegalStateException(format(
                "Plugin folder pattern for site '%s' does not contain all required placeholders", basePath));
        }

        Map<String, String> values = new HashMap<>();
        values.put(PLACEHOLDER_TYPE, type);
        values.put(PLACEHOLDER_NAME, name);
        values.put(PLACEHOLDER_ID, isEmpty(pluginId)? pluginId : pluginId.replace('.', '/'));
        basePath = StringSubstitutor.replace(basePath, values);

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
        objectStateService.transition(siteId, configPath, TransitionEvent.SAVE);
        updateMetadata(siteId, configPath, currentUser);
        generateAuditLog(siteId, configPath, currentUser);

        PreviewEventContext context = new PreviewEventContext();
        context.setSite(siteId);
        eventService.publish(EVENT_PREVIEW_SYNC, context);
    }

    @SuppressWarnings("unchecked")
    protected InputStream validate(InputStream content, String filename) throws ServiceLayerException {
        // Check the filename to see if it needs to be validated
        String extension = getExtension(filename);
        if (isEmpty(extension)) {
            // without extension there is no way to know
            return content;
        }
        try {
            // Copy the contents of the stream
            byte[] bytes;
            bytes = IOUtils.toByteArray(content);

            // Perform the validation
            switch (extension.toLowerCase()) {
                case "xml":
                    try {
                        DocumentHelper.parseText(new String(bytes));
                    } catch (Exception e) {
                        throw new InvalidConfigurationException("Invalid XML file", e);
                    }
                    break;
                case "yaml":
                case "yml":
                    try {
                        Yaml yaml = new Yaml(new DisableClassLoadingConstructor());
                        Map<String, Object> map = (Map<String, Object>) yaml.load(new ByteArrayInputStream(bytes));
                    } catch (Exception e) {
                        throw new InvalidConfigurationException("Invalid YAML file", e);
                    }
            }

            // Return a new stream
            return new ByteArrayInputStream(bytes);

        } catch (IOException e) {
            throw new ServiceLayerException("Error validating configuration", e);
        }
    }

    private void writeEnvironmentConfiguration(String siteId, String module, String path, String environment,
                                               InputStream content) throws ServiceLayerException {
        if (!isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            if (contentService.contentExists(siteId, configBasePath)) {
                String configPath = Paths.get(configBasePath, path).toString();
                contentService.writeContent(siteId, configPath, content);
                String currentUser = securityService.getCurrentUser();
                objectStateService.transition(siteId, configPath, TransitionEvent.SAVE);
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

        itemServiceInternal.updateStateBits(siteId, path, SAVE_AND_CLOSE_ON_MASK, SAVE_AND_CLOSE_OFF_MASK);
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

    @Override
    public ConfigurationHistory getConfigurationHistory(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                        String module, String path, String environment)
            throws SiteNotFoundException, ContentNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException("Site " + siteId + " not found");
        }
        String configPath = StringUtils.EMPTY;
        if (!isEmpty(environment)) {
            String configBasePath =
                    studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH_PATTERN)
                            .replaceAll(PATTERN_MODULE, module)
                            .replaceAll(PATTERN_ENVIRONMENT, environment);
            configPath = Paths.get(configBasePath, path).toString();
            if (!contentService.contentExists(siteId, configPath)) {
                configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                        .replaceAll(PATTERN_MODULE, module);
                configPath = Paths.get(configBasePath, path).toString();
            }
        } else {
            String configBasePath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN)
                    .replaceAll(PATTERN_MODULE, module);
            configPath = Paths.get(configBasePath, path).toString();
        }
        if (!contentService.contentExists(siteId, configPath)) {
            throw new ContentNotFoundException(path, siteId,
                    "Content not found at path " + configPath + " site " + siteId);
        }
        ConfigurationHistory configurationHistory = new ConfigurationHistory();
        configurationHistory.setItem(contentService.getContentItem(siteId, configPath));
        List<ContentItemVersion> versions = new ArrayList<ContentItemVersion>();
        VersionTO[] versionTOS = contentService.getContentItemVersionHistory(siteId, configPath);
        for (VersionTO v : versionTOS) {
            ContentItemVersion civ = new ContentItemVersion();
            civ.setVersionNumber(v.getVersionNumber());
            civ.setComment(v.getComment());
            civ.setLastModifiedDate(v.getLastModifiedDate());
            civ.setLastModifier(v.getLastModifier());
            versions.add(civ);
        }
        configurationHistory.setVersions(versions);
        return configurationHistory;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "write_global_configuration")
    public void writeGlobalConfiguration(@ProtectedResourceId(PATH_RESOURCE_ID) String path, InputStream content)
            throws ServiceLayerException {
        contentService.writeContent(StringUtils.EMPTY, path, validate(content, path));
        if (StringUtils.endsWithAny(path, FILE_SEPARATOR +
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME) +
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME))) {
            securityServiceV2.invalidateAvailableActions();
        }
        String currentUser = securityService.getCurrentUser();
        generateAuditLog(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE), path, currentUser);
        configurationCache.invalidate(path);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public TranslationConfiguration getTranslationConfiguration(String siteId) throws ServiceLayerException {
        TranslationConfiguration translationConfiguration = new TranslationConfiguration();
        if (contentService.contentExists(siteId, translationConfig)) {
            try (InputStream is = contentService.getContent(siteId, translationConfig)) {
                HierarchicalConfiguration config = configurationReader.readXmlConfiguration(is);
                if (config != null) {
                    translationConfiguration.setDefaultLocaleCode(
                            config.getString(CONFIG_KEY_TRANSLATION_DEFAULT_LOCALE));
                    translationConfiguration.setLocaleCodes(
                            config.getList(String.class, CONFIG_KEY_TRANSLATION_LOCALES));
                }
            } catch (Exception e) {
                throw new ServiceLayerException("Error getting translation config for site " + siteId, e);
            }
        }
        return translationConfiguration;
    }

    // Moved from SiteServiceImpl to be able to properly cache the object
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> legacyGetConfiguration(String site, String path) throws ServiceLayerException {
        // anonymous object needed to access the non-final configContent variable in the lambda
        var ref = new Object() {
            String configContent;
        };
        String configPath = null;
        String cacheKey;
        String env = null;
        var useContentService = true;
        if (StringUtils.isEmpty(site)) {
            configPath = getGlobalConfigRoot() + path;
            cacheKey = configPath;
        } else {
            if (path.startsWith(FILE_SEPARATOR + CONTENT_TYPE_CONFIG_FOLDER + FILE_SEPARATOR)) {
                configPath = getSitesConfigPath() + path;
                // the write config is receiving env = null so this needs to match
                cacheKey = getCacheKey(site, MODULE_STUDIO, path, null);
            } else {
                useContentService = false;
                env = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
                cacheKey = getCacheKey(site,MODULE_STUDIO, path, env);
            }
        }

        try {
            // final variables needed to use the lambda as a cache loader
            boolean finalUseContentService = useContentService;
            String finalConfigPath = configPath;
            String finalEnv = env;

            return (Map<String, Object>) configurationCache.get(cacheKey, () -> {
                logger.debug("CACHE MISS: {0}", cacheKey);

                if (finalUseContentService) {
                    ref.configContent = contentService.getContentAsString(site, finalConfigPath);
                } else {
                    ref.configContent = getConfigurationAsString(site, MODULE_STUDIO, path, finalEnv);
                }
                ref.configContent = ref.configContent.replaceAll("\"\\n([\\s]+)?+", "\" ");
                ref.configContent = ref.configContent.replaceAll("\\n([\\s]+)?+", "");
                ref.configContent = ref.configContent.replaceAll("<!--(.*?)-->", "");

                return convertNodesFromXml(ref.configContent);
            });
        } catch (ExecutionException e) {
            throw new ServiceLayerException("Error loading configuration", e);
        }
    }

    private Map<String, Object> convertNodesFromXml(String xml) throws ServiceLayerException {
        try {
            Document document = DocumentHelper.parseText(xml);
            return createMap(document.getRootElement());
        } catch (DocumentException e) {
            throw new ServiceLayerException("Error reading xml string:\n" + xml, e);
        }
    }

    @SuppressWarnings("rawtypes,unchecked")
    private Map<String, Object> createMap(Element element) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node currentNode = element.node(i);
            if (currentNode instanceof Element) {
                Element currentElement = (Element) currentNode;
                String key = currentElement.getName();
                Object toAdd = null;
                if (currentElement.isTextOnly()) {
                    toAdd = currentElement.getStringValue();
                } else {
                    toAdd = createMap(currentElement);
                }
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    List listOfValues = new ArrayList<Object>();
                    if (value instanceof List) {
                        listOfValues = (List<Object>) value;
                    } else {
                        listOfValues.add(value);
                    }
                    listOfValues.add(toAdd);
                    map.put(key, listOfValues);
                } else {
                    map.put(key, toAdd);
                }
            }
        }
        return map;
    }

    private String getGlobalConfigRoot() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    private String getSitesConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
    }
    // --- end of copied code ---

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setObjectStateService(ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setConfigurationReader(EncryptionAwareConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
    }

    public void setTranslationConfig(String translationConfig) {
        this.translationConfig = translationConfig;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setConfigurationCache(Cache<String, Object> configurationCache) {
        this.configurationCache = configurationCache;
    }

}
