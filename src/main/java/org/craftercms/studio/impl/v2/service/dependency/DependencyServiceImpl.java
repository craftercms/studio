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

package org.craftercms.studio.impl.v2.service.dependency;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.permissions.CompositePermission;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DependencyServiceImpl implements DependencyService {

    private final DependencyServiceInternal dependencyServiceInternal;
    private final SiteService siteService;

    @ConstructorProperties({"dependencyServiceInternal", "siteService"})
    public DependencyServiceImpl(final DependencyServiceInternal dependencyServiceInternal, final SiteService siteService) {
        this.dependencyServiceInternal = dependencyServiceInternal;
        this.siteService = siteService;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getSoftDependencies(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                            @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths)
            throws ServiceLayerException {
        siteService.checkSiteExists(siteId);
        List<String> toRet = dependencyServiceInternal.getSoftDependencies(siteId, paths);
        toRet.removeAll(paths);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getHardDependencies(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site,
                                            @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws ServiceLayerException {
        siteService.checkSiteExists(site);
        return dependencyServiceInternal.getHardDependencies(site, paths);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getDependentItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                          @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return dependencyServiceInternal.getDependentItems(siteId, paths);
    }
}
