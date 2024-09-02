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

package org.craftercms.studio.impl.v2.service.publish;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.PublishingPackageNotFoundException;
import org.craftercms.studio.api.v2.security.HasAnyPermissions;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.permissions.CompositePermission;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;
import static org.springframework.util.CollectionUtils.isEmpty;

@RequireSiteReady
public class PublishServiceImpl implements PublishService {

    private PublishService publishServiceInternal;

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public int getPublishingPackagesTotal(@SiteId String siteId, String environment,
                                          String path, List<String> states) throws SiteNotFoundException {
        return publishServiceInternal.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public List<PublishingPackage> getPublishingPackages(@SiteId String siteId,
                                                         String environment, String path, List<String> states,
                                                         int offset, int limit) throws SiteNotFoundException {
        return publishServiceInternal.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public PublishingPackageDetails getPublishingPackageDetails(@SiteId String siteId,
                                                                String packageId) throws SiteNotFoundException, PublishingPackageNotFoundException {
        PublishingPackageDetails publishingPackageDetails = publishServiceInternal.getPublishingPackageDetails(siteId, packageId);
        if (isEmpty(publishingPackageDetails.getItems())) {
            throw new PublishingPackageNotFoundException(siteId, packageId);
        }

        return publishingPackageDetails;
    }

    @Override
    @RequireSiteExists
    public List<DeploymentHistoryGroup> getDeploymentHistory(@SiteId String siteId, int daysFromToday, int numberOfItems,
                                                             String filterType) throws ServiceLayerException, UserNotFoundException {
        return publishServiceInternal.getDeploymentHistory(siteId, daysFromToday, numberOfItems, filterType);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_PUBLISH)
    public long publish(@SiteId String siteId, String publishingTarget, List<PublishRequestPath> paths,
                        List<String> commitIds, Instant schedule, String comment, boolean submitAll)
            throws AuthenticationException, ServiceLayerException {
        return publishServiceInternal.publish(siteId, publishingTarget, paths, commitIds, schedule, comment, submitAll);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_READ)
    public long requestPublish(@SiteId String siteId, String publishingTarget, List<PublishRequestPath> paths,
                               List<String> commitIds, Instant schedule, String comment, boolean submitAll)
            throws AuthenticationException, ServiceLayerException {
        return publishServiceInternal.requestPublish(siteId, publishingTarget, paths, commitIds, schedule, comment, submitAll);
    }

    @Override
    public int getPublishingItemsScheduledTotal(String siteId, String publishingTarget, String approver,
                                                ZonedDateTime dateFrom, ZonedDateTime dateTo, List<String> systemTypes) {
        return publishServiceInternal.getPublishingItemsScheduledTotal(siteId, publishingTarget, approver, dateFrom, dateTo, systemTypes);
    }

    @Override
    public int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver,
                                                 ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return publishServiceInternal.getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, dateFrom, dateTo);
    }

    @Override
    public int getPublishingHistoryDetailTotalItems(String siteId, String publishingPackageId) {
        return publishServiceInternal.getPublishingHistoryDetailTotalItems(siteId, publishingPackageId);
    }

    @Override
    public List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget, String approver, ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit) {
        return publishServiceInternal.getPublishingPackagesHistory(siteId, publishingTarget, approver, dateFrom, dateTo, offset, limit);
    }

    @Override
    public int getNumberOfPublishes(String siteId, int days) {
        return publishServiceInternal.getNumberOfPublishes(siteId, days);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public PublishDependenciesResult getPublishDependencies(@SiteId String siteId, String publishingTarget, Collection<PublishRequestPath> paths,
                                                            Collection<String> commitIds)
            throws ServiceLayerException, IOException {
        return publishServiceInternal.getPublishDependencies(siteId, publishingTarget, paths, commitIds);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public PublishPackage getReadyPackageForItem(final String site, final String path, final boolean includeChildren) {
        return publishServiceInternal.getReadyPackageForItem(site, path, includeChildren);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public Collection<PublishPackage> getActivePackagesForItems(final String siteId, final Collection<String> paths,
                                                                final boolean includeChildren) {
        return publishServiceInternal.getActivePackagesForItems(siteId, paths, includeChildren);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public long publishDelete(String siteId, Collection<String> userRequestedPaths, Collection<String> dependencies, String comment) throws ServiceLayerException {
        return publishServiceInternal.publishDelete(siteId, userRequestedPaths, dependencies, comment);
    }

    @Override
    @RequireSiteExists
    @HasAnyPermissions(type = DefaultPermission.class, actions = {PERMISSION_PUBLISH, PERMISSION_CONTENT_READ})
    public List<PublishingTarget> getAvailablePublishingTargets(@SiteId String siteId) throws SiteNotFoundException {
        return publishServiceInternal.getAvailablePublishingTargets(siteId);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public boolean isSitePublished(@SiteId String siteId) throws SiteNotFoundException {
        return publishServiceInternal.isSitePublished(siteId);
    }

    @SuppressWarnings("unused")
    public void setPublishServiceInternal(final PublishService publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }
}
