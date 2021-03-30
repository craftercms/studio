/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.marketplace.internal;

import org.craftercms.commons.plugin.model.Version;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRecord;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;

import java.util.List;
import java.util.Map;

/**
 * Provides access to all available Marketplace operations
 *
 * @author joseross
 * @since 3.1.2
 */
public interface MarketplaceServiceInternal {

    String HEADER_STUDIO_ID = "x-studio-id";
    String HEADER_STUDIO_BUILD = "x-studio-build";
    String HEADER_STUDIO_VERSION = "x-studio-version";
    String HEADER_JAVA_VERSION = "x-java-version";
    String HEADER_OS_NAME = "x-os-name";
    String HEADER_OS_VERSION = "x-os-version";
    String HEADER_OS_ARCH = "x-os-arch";

    /**
     * Performs a search for all available plugins that match the given filters
     * @param type the type of plugins to search
     * @param keywords the keywords to filter plugins
     * @param showIncompatible indicates if incompatible plugins should be returned
     * @param offset the offset for pagination
     * @param limit the limit for pagination
     * @return the result from the Marketplace
     * @throws MarketplaceException if there is any error performing the search
     */
    Map<String, Object> searchPlugins(String type, String keywords, boolean showIncompatible, long offset, long limit)
        throws MarketplaceException;

    /**
     * Creates a site using the given blueprint
     * @param request the site information
     * @throws RemoteRepositoryNotFoundException if there is an error with the remote repository
     * @throws InvalidRemoteRepositoryException if there is an error with the remote repository
     * @throws InvalidRemoteUrlException if there is an error with the remote repository
     * @throws ServiceLayerException if there is any unexpected error
     * @throws InvalidRemoteRepositoryCredentialsException if there is any error with the credentials
     */
    void createSite(CreateSiteRequest request) throws RemoteRepositoryNotFoundException,
        InvalidRemoteRepositoryException, InvalidRemoteUrlException,
        ServiceLayerException, InvalidRemoteRepositoryCredentialsException;


    /**
     * Returns a list of the plugins currently installed in the given site
     * @param siteId the id of the site
     * @return the list of plugins
     * @throws MarketplaceException if there is any error reading the plugin registry
     */
    List<PluginRecord> getInstalledPlugins(String siteId) throws MarketplaceException;

    /**
     * Install a plugin in a site
     * @param siteId the id of the site
     * @param pluginId the id of the plugin
     * @param pluginVersion the version of the plugin
     * @throws MarketplaceException if there is any error installing the plugin
     */
    void installPlugin(String siteId, String pluginId, Version pluginVersion) throws MarketplaceException;

    /**
     * Copy a plugin in a site
     * @param siteId the id of the site
     * @param path the path of the plugin folder
     * @throws MarketplaceException if there is any error copying the plugin
     */
    void copyPlugin(String siteId, String path) throws MarketplaceException;

}
