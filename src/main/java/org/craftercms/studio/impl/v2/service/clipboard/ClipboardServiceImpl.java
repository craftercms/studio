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
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.clipboard.internal.ClipboardServiceInternal;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;

import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;

/**
 * Default implementation of {@link ClipboardService}
 *
 * @author joseross
 * @since 3.2
 */
public class ClipboardServiceImpl implements ClipboardService {

    protected ClipboardServiceInternal clipboardServiceInternal;

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_WRITE)
    public List<String> pasteItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                   Operation operation,
                                   @ProtectedResourceId(PATH_RESOURCE_ID)
                                       @ValidateSecurePathParam(name = "targetPath") String targetPath,
                                   PasteItem item) throws ServiceLayerException, UserNotFoundException {
        return clipboardServiceInternal.pasteItems(siteId, operation, targetPath, item);
    }

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_WRITE)
    public String duplicateItem(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                @ProtectedResourceId(PATH_RESOURCE_ID)
                                @ValidateSecurePathParam(name = "path") String path)
            throws ServiceLayerException, UserNotFoundException {
        return clipboardServiceInternal.duplicateItem(siteId, path);
    }

    public ClipboardServiceInternal getClipboardServiceInternal() {
        return clipboardServiceInternal;
    }

    public void setClipboardServiceInternal(ClipboardServiceInternal clipboardServiceInternal) {
        this.clipboardServiceInternal = clipboardServiceInternal;
    }
}
