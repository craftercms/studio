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

package org.craftercms.studio.impl.v2.service.repository;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class RepositoryManagementServiceImpl implements RepositoryManagementService {

    private RepositoryManagementServiceInternal repositoryManagementServiceInternal;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "add_remote")
    public boolean addRemote(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException {
        return repositoryManagementServiceInternal.addRemote(siteId, remoteRepository);
    }

    public RepositoryManagementServiceInternal getRepositoryManagementServiceInternal() {
        return repositoryManagementServiceInternal;
    }

    public void setRepositoryManagementServiceInternal(RepositoryManagementServiceInternal repositoryManagementServiceInternal) {
        this.repositoryManagementServiceInternal = repositoryManagementServiceInternal;
    }
}
