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

import jakarta.validation.Valid;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.site.SitesService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_START_STOP_PUBLISHER;

/**
 *
 */
public class DeploymentServiceImpl implements DeploymentService {

    private PublishService publishService;

    // TODO: once publisher is refactored, make this class call new methods in PublishService for backwards compatibility
    @Override
    @Valid
    @Deprecated
    public long bulkGoLive(String site,
                           String environment,
                           @ValidateSecurePathParam String path,
                           String comment) throws ServiceLayerException, AuthenticationException {
        return publishService.publish(site, environment,
                List.of(new PublishService.PublishRequestPath(path, true, false)),
                emptyList(), null, comment);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_START_STOP_PUBLISHER)
    public void enablePublishing(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, boolean enabled)
            throws SiteNotFoundException, AuthenticationException {
        // TODO: implement for new publishing system
    }

    @Override
    @Valid
    @Deprecated
    public long publishCommits(String site,
                               String environment,
                               List<String> commitIds, String comment)
            throws ServiceLayerException, AuthenticationException {
        return publishService.publish(site, environment, emptyList(), commitIds, null, comment);
    }

    @Override
    public void resetStagingEnvironment(String siteId) throws ServiceLayerException, CryptoException {
        // TODO: implement for new publishing system
    }

    public void setPublishService(final PublishService publishService) {
        this.publishService = publishService;
    }
}
