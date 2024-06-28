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
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v2.annotation.*;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.model.rest.content.DependencyItem;
import org.craftercms.studio.permissions.CompositePermission;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DependencyServiceImpl implements DependencyService {

    private final DependencyService dependencyServiceInternal;

    @ConstructorProperties({"dependencyServiceInternal"})
    public DependencyServiceImpl(final DependencyService dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Collection<String> getSoftDependencies(@SiteId String siteId,
                                                  @ProtectedResourceId(PATH_LIST_RESOURCE_ID) Collection<String> paths) {
        return dependencyServiceInternal.getSoftDependencies(siteId, paths);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getDependentPaths(@SiteId String siteId,
                                          @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws SiteNotFoundException {
        return dependencyServiceInternal.getDependentPaths(siteId, paths);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Collection<String> getHardDependencies(@SiteId String site, String publishingTarget,
                                            @ProtectedResourceId(PATH_LIST_RESOURCE_ID) Collection<String> paths) throws ServiceLayerException {
        return dependencyServiceInternal.getHardDependencies(site, publishingTarget, paths);
    }

    @Override
    public Collection<String> getHardDependencies(String site, Collection<String> paths) {
        return dependencyServiceInternal.getHardDependencies(site, paths);
    }


    @RequireContentExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<DependencyItem> getDependentItems(@SiteId String siteId,
                                                  @ContentPath String path) {
        return dependencyServiceInternal.getDependentItems(siteId, path);
    }

    @Override
    @RequireContentExists
    public void upsertDependencies(@SiteId String site, @ContentPath String path) throws ServiceLayerException {
        dependencyServiceInternal.upsertDependencies(site, path);
    }

    @Override
    @RequireContentExists
    public void deleteItemDependencies(@SiteId String site, @ContentPath String sourcePath) throws ServiceLayerException {
        dependencyServiceInternal.deleteItemDependencies(site, sourcePath);
    }

    @Override
    @RequireSiteExists
    public void invalidateDependencies(@SiteId String siteId, String targetPath) throws ServiceLayerException {
        dependencyServiceInternal.invalidateDependencies(siteId, targetPath);
    }

    @Override
    @RequireSiteExists
    public void validateDependencies(@SiteId String siteId, String targetPath) throws ServiceLayerException {
        dependencyServiceInternal.validateDependencies(siteId, targetPath);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getItemSpecificDependencies(@SiteId String siteId, List<String> paths) {
        return dependencyServiceInternal.getItemSpecificDependencies(siteId, paths);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Map<String, Set<DependencyResolver.ResolvedDependency>> resolveDependencies(@SiteId String site, String sourcePath) {
        return dependencyServiceInternal.resolveDependencies(site, sourcePath);
    }
}
