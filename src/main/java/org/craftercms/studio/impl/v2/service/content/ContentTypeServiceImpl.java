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
package org.craftercms.studio.impl.v2.service.content;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v2.service.content.ContentTypeService;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.model.contentType.ContentTypeUsage;
import org.springframework.core.io.Resource;

import java.beans.ConstructorProperties;
import java.util.List;

import static java.lang.String.format;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

/**
 * Default implementation for {@link ContentTypeService}
 *
 * @author joseross
 * @since 4.0
 */
public class ContentTypeServiceImpl implements ContentTypeService {
    protected final ContentTypeServiceInternal contentTypeServiceInternal;
    protected final SiteService siteService;

    @ConstructorProperties({"contentTypeServiceInternal", "siteService"})
    public ContentTypeServiceImpl(ContentTypeServiceInternal contentTypeServiceInternal, SiteService siteService) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
        this.siteService = siteService;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_CONFIGURATION)
    public ContentTypeConfigTO getContentType(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                              String contentTypeId) throws ServiceLayerException {
        siteService.checkSiteExists(siteId);
        if (StringUtils.isEmpty(contentTypeId) || StringUtils.equalsIgnoreCase(contentTypeId, CONTENT_TYPE_UNKNOWN)) {
            throw new ServiceLayerException(format("Invalid content type Id '%s'", contentTypeId));
        }

        return contentTypeServiceInternal.loadContentTypeConfiguration(siteId, contentTypeId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_CONFIGURATION)
    public List<ContentTypeConfigTO> getContentTypes(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path)
            throws ServiceLayerException {
        siteService.checkSiteExists(siteId);
        return contentTypeServiceInternal.getContentTypes(siteId, path);
    }

    /**
     * Finds all items related to a given content-type
     *
     * @param siteId the id of the site
     * @param contentType the id of the content-type
     * @return the usage
     * @throws ServiceLayerException if there is any error finding the items
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public ContentTypeUsage getContentTypeUsage(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String contentType) throws ServiceLayerException {
        return contentTypeServiceInternal.getContentTypeUsage(siteId, contentType);
    }

    /**
     * Finds the preview image for a given content-type
     *
     * @param siteId the id of the site
     * @param contentTypeId the id of the content-type
     * @return the preview image file as a pair of path and resource
     * @throws ServiceLayerException if there is any error finding the items
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public ImmutablePair<String, Resource> getContentTypePreviewImage(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String contentTypeId) throws ServiceLayerException {
        return contentTypeServiceInternal.getContentTypePreviewImage(siteId, contentTypeId);
    }

    /**
     * Deletes all files related to a given content-type
     *
     * @param siteId the id of the site
     * @param contentType the id of the content-type
     * @param deleteDependencies indicates if all dependencies should be deleted
     * @throws ServiceLayerException if there is any error deleting the files
     * @throws AuthenticationException if there is any error authenticating the user
     * @throws DeploymentException if there is any error publishing the changes
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_WRITE_CONFIGURATION)
    public void deleteContentType(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String contentType, boolean deleteDependencies)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {
        contentTypeServiceInternal.deleteContentType(siteId, contentType, deleteDependencies);
    }

}
