/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.model.config.TranslationConfiguration;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.dom4j.Document;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;


public class ConfigurationServiceImpl implements ConfigurationService {

    private ConfigurationService configurationServiceInternal;

    public void setConfigurationServiceInternal(ConfigurationService configurationServiceInternal) {
        this.configurationServiceInternal = configurationServiceInternal;
    }

    @Override
    public Map<String, List<String>> getRoleMappings(String siteId) throws ServiceLayerException {
        return configurationServiceInternal.getRoleMappings(siteId);
    }

    @Override
    public Map<String, List<String>> getGlobalRoleMappings() throws ServiceLayerException {
        return configurationServiceInternal.getGlobalRoleMappings();
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_CONFIGURATION)
    public String getConfigurationAsString(@SiteId String siteId,
                                           String module,
                                           @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                           String environment) throws ContentNotFoundException {
        return configurationServiceInternal.getConfigurationAsString(siteId, module, path, environment);
    }

    @Override
    public Document getConfigurationAsDocument(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String module,
                                               String path, String environment) throws ServiceLayerException {
        return configurationServiceInternal.getConfigurationAsDocument(siteId, module, path, environment);
    }

    @Override
    public HierarchicalConfiguration<?> getXmlConfiguration(String siteId, String path) throws ConfigurationException {
        return configurationServiceInternal.getXmlConfiguration(siteId, path);
    }

    @Override
    public HierarchicalConfiguration<?> getXmlConfiguration(String siteId, String module, String path) throws ConfigurationException {
        return configurationServiceInternal.getXmlConfiguration(siteId, module, path);
    }

    @Override
    public HierarchicalConfiguration<?> getGlobalXmlConfiguration(String path) throws ConfigurationException {
        return configurationServiceInternal.getGlobalXmlConfiguration(path);
    }

    @Override
    public Document getGlobalConfigurationAsDocument(String path) throws ServiceLayerException {
        return configurationServiceInternal.getGlobalConfigurationAsDocument(path);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "write_global_configuration")
    public String getGlobalConfigurationAsString(@ProtectedResourceId(PATH_RESOURCE_ID) String path) throws ContentNotFoundException {
        return configurationServiceInternal.getGlobalConfigurationAsString(path);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_CONFIGURATION)
    public void writeConfiguration(@SiteId String siteId,
                                   String module,
                                   @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                   String environment,
                                   InputStream content)
            throws ServiceLayerException, UserNotFoundException {
        configurationServiceInternal.writeConfiguration(siteId, module, path, environment, content);
    }

    @Override
    public String getCacheKey(String siteId, String module, String path, String environment, String suffix) {
        return configurationServiceInternal.getCacheKey(siteId, module, path, environment, suffix);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Resource getPluginFile(@SiteId String siteId,
                                  String pluginId,
                                  String type,
                                  String name,
                                  String filename)
            throws ContentNotFoundException {
        return configurationServiceInternal.getPluginFile(siteId, pluginId, type, name, filename);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_CONFIGURATION)
    public ConfigurationHistory getConfigurationHistory(@SiteId String siteId,
                                                        String module,
                                                        @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                                        String environment)
            throws ServiceLayerException {
        return configurationServiceInternal.getConfigurationHistory(siteId, module, path, environment);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_GLOBAL_CONFIGURATION)
    public void writeGlobalConfiguration(@ProtectedResourceId(PATH_RESOURCE_ID) String path, InputStream content)
            throws ServiceLayerException {
        configurationServiceInternal.writeGlobalConfiguration(path, content);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_CONFIGURATION)
    public TranslationConfiguration getTranslationConfiguration(@SiteId String siteId) throws ServiceLayerException {
        return configurationServiceInternal.getTranslationConfiguration(siteId);
    }

    @Override
    public void invalidateConfiguration(String siteId, String path) {
        configurationServiceInternal.invalidateConfiguration(siteId, path);
    }

    @Override
    public void invalidateConfiguration(String siteId, String module, String path, String environment) {
        configurationServiceInternal.invalidateConfiguration(siteId, module, path, environment);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_CONFIGURATION)
    public void invalidateConfiguration(@SiteId String siteId) {
        configurationServiceInternal.invalidateConfiguration(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_CONFIGURATION)
    public void makeBlobStoresReadOnly(final String siteId) throws ServiceLayerException {
        configurationServiceInternal.makeBlobStoresReadOnly(siteId);
    }

    // Moved from SiteServiceImpl to be able to properly cache the object
    // TODO: JM: Remove unused method?
    @Override
    public Map<String, Object> legacyGetConfiguration(String site, String path) throws ServiceLayerException {
        return configurationServiceInternal.legacyGetConfiguration(site, path);
    }
}
