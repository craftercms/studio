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
package org.craftercms.studio.impl.v2.service.clipboard;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.clipboard.internal.ClipboardServiceInternal;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;

/**
 * Default implementation of {@link ClipboardService}
 *
 * @author joseross
 * @since 3.2
 */
public class ClipboardServiceImpl implements ClipboardService {

    protected final ClipboardServiceInternal clipboardServiceInternal;
    protected ContentService contentService;

    @ConstructorProperties({"clipboardServiceInternal", "contentService"})
    public ClipboardServiceImpl(ClipboardServiceInternal clipboardServiceInternal, ContentService contentService) {
        this.clipboardServiceInternal = clipboardServiceInternal;
        this.contentService = contentService;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_WRITE)
    public List<String> pasteItems(@SiteId String siteId,
                                   Operation operation,
                                   @ProtectedResourceId(PATH_RESOURCE_ID) String targetPath,
                                   PasteItem item) throws ServiceLayerException, UserNotFoundException {
        return clipboardServiceInternal.pasteItems(siteId, operation, targetPath, item);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_WRITE)
    public String duplicateItem(@SiteId String siteId,
                                @ProtectedResourceId(PATH_RESOURCE_ID)
                                String path)
            throws ServiceLayerException, UserNotFoundException {
        contentService.checkContentExists(siteId, path);
        return clipboardServiceInternal.duplicateItem(siteId, path);
    }
}
