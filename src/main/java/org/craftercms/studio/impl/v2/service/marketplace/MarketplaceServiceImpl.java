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

package org.craftercms.studio.impl.v2.service.marketplace;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.plugin.model.Version;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.marketplace.internal.MarketplaceServiceInternal;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRecord;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CREATE_SITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_INSTALL_PLUGINS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_LIST_PLUGINS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_REMOVE_PLUGINS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_SEARCH_PLUGINS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_WRITE_CONFIGURATION;

/**
 * Default implementation of {@link MarketplaceService} that proxies all request to the configured Marketplace
 *
 * @author joseross
 * @since 3.1.2
 */
public class MarketplaceServiceImpl implements MarketplaceService {

    protected final MarketplaceServiceInternal marketplaceServiceInternal;

    @ConstructorProperties({"marketplaceServiceInternal"})
    public MarketplaceServiceImpl(MarketplaceServiceInternal marketplaceServiceInternal) {
        this.marketplaceServiceInternal = marketplaceServiceInternal;
    }

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_SEARCH_PLUGINS)
    public Map<String, Object> searchPlugins(@ValidateStringParam(name = "type") String type,
                                             @ValidateStringParam(name = "keywords") String keywords,
                                             boolean showIncompatible, long offset, long limit)
        throws MarketplaceException {
        return marketplaceServiceInternal.searchPlugins(type, keywords, showIncompatible, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_SITE)
    public void createSite(CreateSiteRequest request) throws RemoteRepositoryNotFoundException,
        InvalidRemoteRepositoryException, InvalidRemoteUrlException,
        ServiceLayerException, InvalidRemoteRepositoryCredentialsException {
        marketplaceServiceInternal.createSite(request);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_LIST_PLUGINS)
    public List<PluginRecord> getInstalledPlugins(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId)
            throws MarketplaceException {
        return marketplaceServiceInternal.getInstalledPlugins(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_INSTALL_PLUGINS)
    public void installPlugin(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                              String pluginId, Version pluginVersion, Map<String, String> parameters)
            throws MarketplaceException {
        marketplaceServiceInternal.installPlugin(siteId, pluginId, pluginVersion, parameters);
    }

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_INSTALL_PLUGINS)
    public void copyPlugin(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                           @ValidateSecurePathParam(name = "path") String path,
                           Map<String, String> parameters) throws MarketplaceException {
        marketplaceServiceInternal.copyPlugin(siteId, path, parameters);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REMOVE_PLUGINS)
    public void removePlugin(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String pluginId, boolean force)
            throws ServiceLayerException {
        marketplaceServiceInternal.removePlugin(siteId, pluginId, force);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REMOVE_PLUGINS)
    public List<String> getPluginUsage(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String pluginId)
            throws ServiceLayerException {
        return marketplaceServiceInternal.getPluginUsage(siteId, pluginId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public HierarchicalConfiguration<?> getPluginConfiguration(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                               String pluginId)
            throws ConfigurationException {
        return marketplaceServiceInternal.getPluginConfiguration(siteId, pluginId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public String getPluginConfigurationAsString(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                 String pluginId) {
        return marketplaceServiceInternal.getPluginConfigurationAsString(siteId, pluginId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_CONFIGURATION)
    public void writePluginConfiguration(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         String pluginId, String content)
            throws UserNotFoundException, ServiceLayerException {
        marketplaceServiceInternal.writePluginConfiguration(siteId, pluginId, content);
    }

}
