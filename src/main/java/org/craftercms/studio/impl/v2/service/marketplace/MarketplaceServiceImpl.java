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

package org.craftercms.studio.impl.v2.service.marketplace;

import java.util.List;
import java.util.Map;

import org.craftercms.commons.plugin.model.Version;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.marketplace.internal.MarketplaceServiceInternal;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRecord;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_INSTALL_PLUGINS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_LIST_PLUGINS;

/**
 * Default implementation of {@link MarketplaceService} that proxies all request to the configured Marketplace
 *
 * @author joseross
 * @since 3.1.2
 */
public class MarketplaceServiceImpl implements MarketplaceService {

    protected MarketplaceServiceInternal marketplaceServiceInternal;

    public MarketplaceServiceImpl(MarketplaceServiceInternal marketplaceServiceInternal) {
        this.marketplaceServiceInternal = marketplaceServiceInternal;
    }

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = "create-site")
    public Map<String, Object> searchPlugins(@ValidateStringParam(name = "type") String type,
                                             @ValidateStringParam(name = "keywords") String keywords,
                                             boolean showIncompatible, long offset, long limit)
        throws MarketplaceException {
        return marketplaceServiceInternal.searchPlugins(type, keywords, showIncompatible, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create-site")
    public void createSite(CreateSiteRequest request) throws RemoteRepositoryNotFoundException,
        InvalidRemoteRepositoryException, RemoteRepositoryNotBareException, InvalidRemoteUrlException,
        ServiceLayerException, InvalidRemoteRepositoryCredentialsException {
        marketplaceServiceInternal.createSite(request);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_LIST_PLUGINS)
    public List<PluginRecord> getInstalledPlugins(String siteId)
            throws MarketplaceException {
        return marketplaceServiceInternal.getInstalledPlugins(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_INSTALL_PLUGINS)
    public void installPlugin(String siteId, String pluginId, Version pluginVersion) throws MarketplaceException {
        marketplaceServiceInternal.installPlugin(siteId, pluginId, pluginVersion);
    }

}
