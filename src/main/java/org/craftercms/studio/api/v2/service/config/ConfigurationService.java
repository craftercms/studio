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
package org.craftercms.studio.api.v2.service.config;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.core.io.Resource;

import java.io.IOException;
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
     * @throws ConfigurationException
     */
    Map<String, List<String>> geRoleMappings(String siteId) throws ConfigurationException;

    /**
     * Get configuration as string for given parameters
     *
     * @param siteId site id to use
     * @param module crafter CMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return String content of configuration file
     */
    String getConfigurationAsString(String siteId, String module, String path, String environment);

    /**
     * Get configuration as DOM document for given parameters
     *
     * @param siteId site id to use
     * @param module crafter CMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return DOM document representing configuration file
     * @throws DocumentException
     * @throws IOException
     */
    Document getConfigurationAsDocument(String siteId, String module, String path, String environment)
            throws DocumentException, IOException;

    /**
     * Write configuration file for given parameters
     *
     * @param siteId site id to use
     * @param module crafter CMS module
     * @param path path where to store configuration file
     * @param environment environment to use. if empty using default
     * @param content content of configuration file
     * @throws ServiceLayerException
     */
    void writeConfiguration(String siteId, String module, String path, String environment, InputStream content)
        throws ServiceLayerException;

    /**
     * Get a a file from a plugin
     * @param siteId the id of site
     * @param type the type of plugin
     * @param name the name of the plugin
     * @param filename the path and name of the file
     * @return the file as a resource
     * @throws ContentNotFoundException if there is any issue reading the file from the repository
     */
    Resource getPluginFile(String siteId, String type, String name, String filename)
        throws ContentNotFoundException;

    /**
     * Get configuration history for given parameters
     *
     * @param siteId site id to use
     * @param module crafter CMS module
     * @param path path of configuration file
     * @param environment environment to use. if empty using default
     * @return configuration history
     */
    ConfigurationHistory getConfigurationHistory(String siteId, String module, String path, String environment);

    void writeGlobalConfiguration(String path, InputStream content) throws ServiceLayerException;
}
