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

package org.craftercms.studio.impl.v2.service.site;

import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressObserver;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;

import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH_CLEAR_LOCK;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH_STATUS;

public class SitesServiceImpl implements SitesService {

    private SitesServiceInternal sitesServiceInternal;
    private PublishingProgressServiceInternal publishingProgressServiceInternal;
    private ContentRepository contentRepository;

    @Override
    public List<PluginDescriptor> getAvailableBlueprints() {
        return sitesServiceInternal.getAvailableBlueprints();
    }

    @Override
    public PluginDescriptor getBlueprintDescriptor(final String id) {
        return sitesServiceInternal.getBlueprintDescriptor(id);
    }

    @Override
    public String getBlueprintLocation(String blueprintId) {
        return sitesServiceInternal.getBlueprintLocation(blueprintId);
    }

    @Override
    public PluginDescriptor getSiteBlueprintDescriptor(final String id) {
        return sitesServiceInternal.getSiteBlueprintDescriptor(id);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "edit_site")
    public void updateSite(@ProtectedResourceId("siteId") String siteId, String name, String description)
            throws SiteNotFoundException, SiteAlreadyExistsException {
        sitesServiceInternal.updateSite(siteId, name, description);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUBLISH_STATUS)
    public PublishStatus getPublishingStatus(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        PublishStatus publishStatus = sitesServiceInternal.getPublishingStatus(siteId);
        PublishingProgressObserver publishingProgressObserver =
                publishingProgressServiceInternal.getPublishingProgress(siteId);
        if (Objects.nonNull(publishingProgressObserver)) {
            publishStatus.setPublishingTarget(publishingProgressObserver.getPublishingTarget());
            publishStatus.setSubmissionId(publishingProgressObserver.getPackageId());
            publishStatus.setNumberOfItems(publishingProgressObserver.getNumberOfFilesCompleted());
            publishStatus.setTotalItems(publishingProgressObserver.getNumberOfFilesBeingPublished());
        }
        publishStatus.setPublished(contentRepository.publishedRepositoryExists(siteId));
        return publishStatus;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUBLISH_CLEAR_LOCK)
    public void clearPublishingLock(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        sitesServiceInternal.clearPublishingLock(siteId);
    }

    public SitesServiceInternal getSitesServiceInternal() {
        return sitesServiceInternal;
    }

    public void setSitesServiceInternal(SitesServiceInternal sitesServiceInternal) {
        this.sitesServiceInternal = sitesServiceInternal;
    }

    public PublishingProgressServiceInternal getPublishingProgressServiceInternal() {
        return publishingProgressServiceInternal;
    }

    public void setPublishingProgressServiceInternal(PublishingProgressServiceInternal publishingProgressServiceInternal) {
        this.publishingProgressServiceInternal = publishingProgressServiceInternal;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
}
