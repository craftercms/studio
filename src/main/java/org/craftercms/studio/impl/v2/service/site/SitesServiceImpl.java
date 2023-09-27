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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.InvalidSiteStateException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.security.HasAllPermissions;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressObserver;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class SitesServiceImpl implements SitesService {

    private final SitesService sitesServiceInternal;
    private final PublishingProgressServiceInternal publishingProgressServiceInternal;
    private final ContentRepository contentRepository;
    private final SiteService siteService;

    @ConstructorProperties({"sitesServiceInternal", "publishingProgressServiceInternal", "contentRepository", "siteService"})
    public SitesServiceImpl(final SitesService sitesServiceInternal, final PublishingProgressServiceInternal publishingProgressServiceInternal,
                            final ContentRepository contentRepository, final SiteService siteService) {
        this.sitesServiceInternal = sitesServiceInternal;
        this.publishingProgressServiceInternal = publishingProgressServiceInternal;
        this.contentRepository = contentRepository;
        this.siteService = siteService;
    }

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
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_EDIT_SITE)
    public void updateSite(@SiteId String siteId, String name, String description)
            throws SiteNotFoundException, SiteAlreadyExistsException, InvalidParametersException {
        if (isBlank(name) && isBlank(description)) {
            throw new InvalidParametersException("The request needs to include a name or a description");
        }
        siteService.checkSiteExists(siteId);
        sitesServiceInternal.updateSite(siteId, name, description);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUBLISH_STATUS)
    public PublishStatus getPublishingStatus(@SiteId String siteId) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
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
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUBLISH_CLEAR_LOCK)
    public void clearPublishingLock(@SiteId String siteId) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        sitesServiceInternal.clearPublishingLock(siteId);
    }

    @Override
    public void checkSiteState(final String siteId, final String state) throws InvalidSiteStateException, SiteNotFoundException {
        sitesServiceInternal.checkSiteState(siteId, state);
    }

    @Override
    @RequireSiteReady
    @HasAllPermissions(type = DefaultPermission.class, actions = {PERMISSION_DUPLICATE_SITE, PERMISSION_CONTENT_READ,
            PERMISSION_READ_CONFIGURATION, PERMISSION_CONTENT_SEARCH})
    public void duplicate(@SiteId String sourceSiteId, String siteId, String siteName, String description, String sandboxBranch)
            throws ServiceLayerException {
        siteService.checkSiteExists(sourceSiteId);
        if (siteService.exists(siteId)) {
            throw new SiteAlreadyExistsException(siteId);
        }
        sitesServiceInternal.duplicate(sourceSiteId, siteId, siteName, description, sandboxBranch);
    }

    @Override
    public void addSiteUuidFile(String site, String siteUuid) throws IOException {
        sitesServiceInternal.addSiteUuidFile(site, siteUuid);
    }
}
