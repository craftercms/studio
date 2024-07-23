/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import jakarta.validation.Valid;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.annotation.*;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenBulkRequest.PathParams;
import org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;
import org.craftercms.studio.permissions.PermissionOrOwnership;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class ContentServiceImpl implements ContentService {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private ContentServiceInternal contentServiceInternal;

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public boolean contentExists(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_RESOURCE_ID) String path) throws SiteNotFoundException {
        return contentServiceInternal.contentExists(siteId, path);
    }

    @Override
    @RequireSiteExists
    public boolean shallowContentExists(@SiteId String site, String path) throws SiteNotFoundException {
        return contentServiceInternal.shallowContentExists(site, path);
    }

    @Override
    @RequireSiteExists
    // TODO: JM: Should we have a "is member of site" validation here?
    public List<QuickCreateItem> getQuickCreatableContentTypes(@SiteId String siteId) throws SiteNotFoundException {
        return contentServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getChildItems(@SiteId String siteId,
                                      @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws SiteNotFoundException {
        return contentServiceInternal.getChildItems(siteId, paths);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public long deleteContent(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                                 String submissionComment)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException {
        return contentServiceInternal.deleteContent(siteId, paths, submissionComment);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_CHILDREN)
    public GetChildrenResult getChildrenByPath(@SiteId String siteId,
                                               @ProtectedResourceId(PATH_RESOURCE_ID) String path, String locale,
                                               String keyword, List<String> systemTypes, List<String> excludes,
                                               String sortStrategy, String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getChildrenByPath(siteId, path, locale, keyword, systemTypes, excludes,
                                                        sortStrategy, order, offset, limit);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_GET_CHILDREN)
    public GetChildrenByPathsBulkResult getChildrenByPaths(@SiteId String siteId,
                                                           @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                                                           Map<String, PathParams> pathParams)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getChildrenByPaths(siteId, paths, pathParams);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Item getItem(@SiteId String siteId,
                        @ProtectedResourceId(PATH_RESOURCE_ID)  String path, boolean flatten)
            throws SiteNotFoundException, ContentNotFoundException {
        try {
            return contentServiceInternal.getItem(siteId, path, flatten);
        } catch (PathNotFoundException e) {
            logger.error("Content not found for site '{}' at path '{}'", siteId, path, e);
            throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'", siteId, path));
        }
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Document getItemDescriptor(@SiteId String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path, boolean flatten)
            throws SiteNotFoundException, ContentNotFoundException {
        return contentServiceInternal.getItemDescriptor(siteId, path, flatten);
    }

    @Override
    @RequireSiteReady
    @RequireContentExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_CHILDREN)
    public DetailedItem getItemByPath(@SiteId String siteId, @ContentPath String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getItemByPath(siteId, path, preferContent);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_GET_CHILDREN)
    public List<SandboxItem> getSandboxItemsByPath(@SiteId String siteId,
                                                   @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                                                   boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, preferContent);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_WRITE)
    public void lockContent(@SiteId String siteId,
                            @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws UserNotFoundException, ServiceLayerException {
        contentServiceInternal.lockContent(siteId, path);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = PermissionOrOwnership.class, action = PERMISSION_ITEM_UNLOCK)
    public void unlockContent(@SiteId String siteId,
                              @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ContentNotFoundException, ContentAlreadyUnlockedException, SiteNotFoundException {
        contentServiceInternal.unlockContent(siteId, path);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public Optional<Resource> getContentByCommitId(@SiteId String siteId,
                                                   @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                                   String commitId) throws ContentNotFoundException {
        return contentServiceInternal.getContentByCommitId(siteId, path, commitId);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_WRITE)
    public boolean renameContent(@SiteId String site,
                                 @ProtectedResourceId(PATH_RESOURCE_ID) String path, String name)
     throws ServiceLayerException, UserNotFoundException{
        return contentServiceInternal.renameContent(site, path, name);
    }

    @Override
    @Valid
    public Resource getContentAsResource(@ValidateStringParam String site,
                                         @ValidateSecurePathParam String path)
        throws ContentNotFoundException {
        return contentServiceInternal.getContentAsResource(site, path);
    }

    @Override
    @RequireSiteReady
    @RequireContentExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<ItemVersion> getContentVersionHistory(@SiteId String siteId, @ContentPath String path) throws ServiceLayerException {
        return contentServiceInternal.getContentVersionHistory(siteId, path);
    }

    public void setContentServiceInternal(final ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }
}
