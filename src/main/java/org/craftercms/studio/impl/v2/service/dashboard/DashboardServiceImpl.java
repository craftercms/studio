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

package org.craftercms.studio.impl.v2.service.dashboard;


import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DashboardServiceImpl implements DashboardService {

    private PublishServiceInternal publishServiceInternal;
    private ContentServiceInternal contentServiceInternal;

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getPublishingScheduledTotal(String siteId, String publishingTarget, ZonedDateTime dateFrom,
                                           ZonedDateTime dateTo) {
        return publishServiceInternal.getPublishingPackagesScheduledTotal(siteId, publishingTarget, dateFrom, dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<DashboardPublishingPackage> getPublishingScheduled(String siteId, String publishingTarget,
                                                                   ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                                   int offset, int limit) {
        return publishServiceInternal.getPublishingPackagesScheduled(siteId, publishingTarget, dateFrom, dateTo,
                offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getPublishingScheduledDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String sitId,
                                                        String publishingPackageId) throws UserNotFoundException, ServiceLayerException {
        var publishingPackageDetails =
                publishServiceInternal.getPublishingPackageDetails(sitId, publishingPackageId);
        var paths = publishingPackageDetails.getItems().stream()
                .map(PublishingPackageDetails.PublishingPackageItem::getPath)
                .collect(Collectors.toList());
        return contentServiceInternal.getSandboxItemsByPath(sitId, paths, true);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getPublishingHistoryTotal(String siteId, String publishingTarget, String approver,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return publishServiceInternal.getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, dateFrom,
                dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<DashboardPublishingPackage> getPublishingHistory(String siteId, String publishingTarget,
                                                                 String approver, ZonedDateTime dateFrom,
                                                                 ZonedDateTime dateTo, int offset, int limit) {
        return publishServiceInternal.getPublishingPackagesHistory(siteId, publishingTarget, approver, dateFrom,
                dateTo, offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getPublishingHistoryDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String sitId,
                                                        String publishingPackageId) throws UserNotFoundException, ServiceLayerException {
        var publishingPackageDetails =
                publishServiceInternal.getPublishingPackageDetails(sitId, publishingPackageId);
        var paths = publishingPackageDetails.getItems().stream()
                .map(PublishingPackageDetails.PublishingPackageItem::getPath)
                .collect(Collectors.toList());
        return contentServiceInternal.getSandboxItemsByPath(sitId, paths, true);
    }

    @Override
    public void getPublishingStats() {

    }

    public PublishServiceInternal getPublishServiceInternal() {
        return publishServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }
}
