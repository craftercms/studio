/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v2.service.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.model.config.TranslationConfiguration;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.dom4j.Document;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service that helps access different Studio configuration.
 *
 * @author avasquez
 */
public interface ConfigurationService {

    /**
     * Get role mappings configuration for given site
     *
     * @param siteId Site id to use
     * @return role mappings configuration
     * @throws ConfigurationException configuration error
     */
    Map<String, List<String>> getRoleMappings(String siteId) throws ServiceLayerException;

    /**
     * Get global role mappings configuration
     *
     * @return role mappings configuration
     * @throws ConfigurationException configuration error
     */
    Map<String, List<String>> getGlobalRoleMappings() throws ServiceLayerException;

    /**
     * Get configuration as string for given parameters
     *
     * @param siteId site id to use
     * @param module CrafterCMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return String content of configuration file
     */
    String getConfigurationAsString(String siteId, String module, String path, String environment);

    /**
     * Get configuration as DOM document for given parameters
     *
     * @param siteId site id to use
     * @param module CrafterCMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return DOM document representing configuration file
     * @throws ServiceLayerException if there is any error loading the configuration
     */
    Document getConfigurationAsDocument(String siteId, String module, String path, String environment)
            throws ServiceLayerException;

    /**
     * Reads a configuration file using Apache Commons Configuration
     * @param siteId the id of the site
     * @param path the path of the file
     * @return the configuration object
     * @throws ConfigurationException if there is any error reading or parsing the file
     */
    HierarchicalConfiguration<?> getXmlConfiguration(String siteId, String path) throws ConfigurationException;

    /**
     * Reads a configuration file using Apache Commons Configuration
     * @param path the path of the file
     * @return the configuration object
     * @throws ConfigurationException if there is any error reading or parsing the file
     */
    HierarchicalConfiguration<?> getGlobalXmlConfiguration(String path) throws ConfigurationException;

    /**
     * Get configuration from global repository as Document
     * @param path path of the configuration file
     * @return the Document
     * @throws ServiceLayerException if there is any error reading the configuration
     */
    Document getGlobalConfigurationAsDocument(String path) throws ServiceLayerException;

    /**
     * Get configuration from global repository as String
     *
     * @param path path of configuration file
     * @return String content of configuration file
     */
    String getGlobalConfigurationAsString(String path);

    /**
     * Write configuration file for given parameters
     *
     * @param siteId site id to use
     * @param module CrafterCMS module
     * @param path path where to store configuration file
     * @param environment environment to use. if empty using default
     * @param content content of configuration file
     * @throws ServiceLayerException general service error
     */
    void writeConfiguration(String siteId, String module, String path, String environment, InputStream content)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get a a file from a plugin
     * @param siteId the id of site
     * @param pluginId the id of the plugin
     * @param type the type of plugin
     * @param name the name of the plugin
     * @param filename the path and name of the file
     * @return the file as a resource
     * @throws ContentNotFoundException if there is any issue reading the file from the repository
     */
    Resource getPluginFile(String siteId, String pluginId, String type, String name, String filename)
        throws ContentNotFoundException;

    /**
     * Get configuration history for given parameters
     *
     * @param siteId site id to use
     * @param module CrafterCMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return configuration history
     */
    ConfigurationHistory getConfigurationHistory(String siteId, String module, String path, String environment)
            throws SiteNotFoundException, ContentNotFoundException;

    /**
     * Write configuration file within global repo
     *
     * @param path path of configuration file
     * @param content content of configuration file
     * @throws ServiceLayerException general service error
     */
    void writeGlobalConfiguration(String path, InputStream content) throws ServiceLayerException;

    /**
     * Get the translation configuration for a given site
     * @param siteId the id of the site
     * @return the translation configuration
     *
     * @throws ServiceLayerException general service error
     */
    TranslationConfiguration getTranslationConfiguration(String siteId) throws ServiceLayerException;

    /**
     * This method holds logic for API 1, can be deleted when API 1 get configuration is removed
     */
    Map<String, Object> legacyGetConfiguration(String site, String path) throws ServiceLayerException;

    /**
     * Builds the key for a given file
     * @param siteId the id of the site
     * @param module the module of the file
     * @param path the path of the file
     * @param environment the environment of the file
     * @return the key for the file
     */
    default String getCacheKey(String siteId, String module, String path, String environment) {
        return getCacheKey(siteId, module, path, environment, null);
    }

    /**
     * Builds the key for a given file
     * @param siteId the id of the site
     * @param module the module of the file
     * @param path the path of the file
     * @param environment the environment of the file
     * @param suffix the suffix for the cache key
     * @return the key for the file
     */
    String getCacheKey(String siteId, String module, String path, String environment, String suffix);

    /**
     * Invalidates the cache for the given file
     * @param siteId the id of the site
     * @param path the path of the file
     */
    void invalidateConfiguration(String siteId, String path);

    /**
     * Invalidates the cache for the given file
     * @param siteId the id of the site
     * @param module the module of the file
     * @param path the path of the file
     * @param environment the environment of the file
     */
    void invalidateConfiguration(String siteId, String module, String path, String environment);

    /**
     * Invalidates all objects for a given site
     * @param siteId the id of the site
     */
    void invalidateConfiguration(String siteId);

}
