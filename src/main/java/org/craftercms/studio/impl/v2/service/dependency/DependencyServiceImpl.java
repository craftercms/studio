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

package org.craftercms.studio.impl.v2.service.dependency;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.permissions.CompositePermission;

import java.util.List;

import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;

public class DependencyServiceImpl implements DependencyService {

    private DependencyServiceInternal dependencyServiceInternal;

    @Override
    public List<String> getSoftDependencies(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                            @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ServiceLayerException {
        List<String> toRet = dependencyServiceInternal.getSoftDependencies(siteId, path);
        toRet.remove(path);
        return dependencyServiceInternal.getSoftDependencies(siteId, path);
    }

    @Override
    public List<String> getSoftDependencies(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                            @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths)
            throws ServiceLayerException {
        List<String> toRet = dependencyServiceInternal.getSoftDependencies(siteId, paths);
        toRet.removeAll(paths);
        return toRet;
    }

    @Override
    public List<String> getHardDependencies(String site, String path) throws ServiceLayerException {
        return dependencyServiceInternal.getHardDependencies(site, path);
    }

    @Override
    public List<String> getHardDependencies(String site, List<String> paths) throws ServiceLayerException {
        return dependencyServiceInternal.getHardDependencies(site, paths);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getDependentItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                          @ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        return dependencyServiceInternal.getDependentItems(siteId, path);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getDependentItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                          @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) {
        return dependencyServiceInternal.getDependentItems(siteId, paths);
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }
}
