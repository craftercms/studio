/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.CommitNotFoundException;
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;

import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_START_STOP_PUBLISHER;

/**
 *
 */
public class DeploymentServiceImpl implements DeploymentService {

    // TODO: once publisher is refactored, make this class call new methods in PublishService for backwards compatibility
    @Override
    @Valid
    public void bulkGoLive(String site,
                           String environment,
                           @ValidateSecurePathParam String path,
                           String comment) throws ServiceLayerException {
        // TODO: implement for new publishing system
    }

    @Override
    @Valid
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_START_STOP_PUBLISHER)
    public boolean enablePublishing(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site, boolean enabled)
            throws SiteNotFoundException, AuthenticationException {
        // TODO: implement for new publishing system
        return false;
    }

    @Override
    @Valid
    public void publishCommits(@ValidateStringParam String site,
                               @ValidateStringParam String environment,
                               List<String> commitIds, String comment)
            throws SiteNotFoundException, EnvironmentNotFoundException, CommitNotFoundException {
        // TODO: implement for new publishing system
    }

    @Override
    public void resetStagingEnvironment(String siteId) throws ServiceLayerException, CryptoException {
        // TODO: implement for new publishing system
    }
}
