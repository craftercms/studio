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
package org.craftercms.studio.impl.v1.service.deployment;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v2.service.site.SitesService;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_START_STOP_PUBLISHER;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.SITE_ID_RESOURCE_ID;

/**
 *
 */
public class DeploymentServiceImpl implements DeploymentService {

    private SitesService siteService;

    // TODO: remove. Replace with V2
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_START_STOP_PUBLISHER)
    public void enablePublishing(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, boolean enabled)
            throws SiteNotFoundException, AuthenticationException {
        siteService.enablePublishing(siteId, enabled);
    }

    public void setSiteService(final SitesService siteService) {
        this.siteService = siteService;
    }
}
