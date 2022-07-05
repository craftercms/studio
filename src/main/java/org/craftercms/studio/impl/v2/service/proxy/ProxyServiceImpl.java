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

package org.craftercms.studio.impl.v2.service.proxy;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.proxy.ProxyService;
import org.craftercms.studio.permissions.StudioPermissionsConstants;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.net.URISyntaxException;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

/**
 * Default implementation for {@link ProxyService}.
 */
public class ProxyServiceImpl implements ProxyService {

    protected final SiteService siteService;

    protected final ProxyService proxyServiceInternal;

    @ConstructorProperties({"siteService", "proxyServiceInternal"})
    public ProxyServiceImpl(final SiteService siteService, final ProxyService proxyServiceInternal) {
        this.siteService = siteService;
        this.proxyServiceInternal = proxyServiceInternal;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = StudioPermissionsConstants.PERMISSION_VIEW_LOGS)
    public ResponseEntity<Object> getSiteLogEvents(final String body,
                                                   @ProtectedResourceId(SITE_ID_RESOURCE_ID) final String siteId,
                                                   final HttpServletRequest request) throws URISyntaxException, SiteNotFoundException {
        return proxyEngine(body, siteId, request);
    }

    @Override
    @ValidateParams
    public ResponseEntity<Object> proxyEngine(final String body, @ValidateStringParam(name = "siteId", notEmpty = true) final String siteId,
                                              final HttpServletRequest request) throws URISyntaxException, SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(String.format("Site '%s' not found", siteId));
        }

        return proxyServiceInternal.proxyEngine(body, siteId, request);
    }
}
