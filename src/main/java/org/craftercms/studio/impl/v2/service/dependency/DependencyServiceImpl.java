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
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.model.rest.content.DependencyItem;
import org.craftercms.studio.permissions.CompositePermission;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.List;

import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DependencyServiceImpl implements DependencyService {

    private final DependencyServiceInternal dependencyServiceInternal;
    private final SiteService siteService;
    private final ContentRepository contentRepository;

    @ConstructorProperties({"dependencyServiceInternal", "siteService", "contentRepository"})
    public DependencyServiceImpl(final DependencyServiceInternal dependencyServiceInternal,
                                 final SiteService siteService, final ContentRepository contentRepository) {
        this.dependencyServiceInternal = dependencyServiceInternal;
        this.siteService = siteService;
        this.contentRepository = contentRepository;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Collection<String> getSoftDependencies(@SiteId String siteId,
                                                  @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths)
            throws ServiceLayerException {
        siteService.checkSiteExists(siteId);
        return dependencyServiceInternal.getSoftDependencies(siteId, paths);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getDependentPaths(@SiteId String siteId,
                                          @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return dependencyServiceInternal.getDependentPaths(siteId, paths);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getHardDependencies(@SiteId String site,
                                            @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws ServiceLayerException {
        siteService.checkSiteExists(site);
        return dependencyServiceInternal.getHardDependencies(site, paths);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<DependencyItem> getDependentItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                  @ProtectedResourceId(PATH_RESOURCE_ID) String path) throws ServiceLayerException {
        siteService.checkSiteExists(siteId);
        contentRepository.checkContentExists(siteId, path);
        return dependencyServiceInternal.getDependentItems(siteId, path);
    }
}
