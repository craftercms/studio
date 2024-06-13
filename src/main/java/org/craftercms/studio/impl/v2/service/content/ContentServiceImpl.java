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

package org.craftercms.studio.impl.v2.service.content;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.event.lock.LockContentEvent;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.exception.content.ContentLockedByAnotherUserException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.AuthenticatedUser;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.*;

import static java.lang.String.format;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class ContentServiceImpl implements ContentService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private ContentServiceInternal contentServiceInternal;
    private ContentTypeServiceInternal contentTypeServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;
    private DeploymentService deploymentService;
    private UserServiceInternal userServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private ItemServiceInternal itemServiceInternal;
    private SecurityService securityService;
    private GeneralLockService generalLockService;
    private ApplicationContext applicationContext;
    private org.craftercms.studio.api.v1.service.content.ContentService contentServiceV1;

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
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getChildItems(@SiteId String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, path);
        List<String> childItems = new ArrayList<>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, path));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_READ)
    public List<String> getChildItems(@SiteId String siteId,
                                      @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) throws SiteNotFoundException {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, paths);
        List<String> childItems = new ArrayList<>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, paths));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public boolean deleteContent(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                 String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {
        List<String> contentToDelete = new ArrayList<>(getChildItems(siteId, path));
        contentToDelete.add(path);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);

        AuthenticatedUser currentUser = userServiceInternal.getCurrentUser();
        deploymentService.delete(siteId, contentToDelete, currentUser.getUsername(),
                DateUtils.getCurrentTime(), submissionComment);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, false);
        insertDeleteContentApprovedActivity(siteId, currentUser.getUsername(), contentToDelete);
        return true;
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public boolean deleteContent(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                                 String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {
        List<String> contentToDelete = new ArrayList<>();
        contentToDelete.addAll(getChildItems(siteId, paths));
        contentToDelete.addAll(paths);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);
        AuthenticatedUser currentUser = userServiceInternal.getCurrentUser();
        deploymentService.delete(siteId, contentToDelete, currentUser.getUsername(),
                DateUtils.getCurrentTime(), submissionComment);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, false);
        insertDeleteContentApprovedActivity(siteId, currentUser.getUsername(), contentToDelete);
        return true;
    }

    private void insertDeleteContentApprovedActivity(String siteId, String approver, List<String> contentToDelete)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_APPROVE);
        auditLog.setActorId(approver);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
        for (String itemToDelete : contentToDelete) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + itemToDelete);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(itemToDelete);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
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
        try {
            Item item = contentServiceInternal.getItem(siteId, path, flatten);
            Document descriptor = item.getDescriptorDom();
            if (descriptor == null) {
                throw new ContentNotFoundException(path, siteId, format("No descriptor found for '%s' in site '%s'", path, siteId));
            }
            return descriptor;
        } catch (PathNotFoundException e) {
            logger.error("Content not found for site '{}' at path '{}'", siteId, path, e);
            throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'", siteId, path));
        }
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_CHILDREN)
    public DetailedItem getItemByPath(@SiteId String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        contentServiceV1.checkContentExists(siteId, path);
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
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.isNull(item)) {
                throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'",
                        siteId, path));
            }
            var username = securityService.getCurrentUser();
            if (Objects.isNull(item.getLockOwner())) {
                contentServiceInternal.itemLockByPath(siteId, path);
                itemServiceInternal.lockItemByPath(siteId, path, username);
                applicationContext.publishEvent(
                        new LockContentEvent(securityService.getAuthentication(), siteId, path, true));
            } else {
                if (!StringUtils.equals(item.getLockOwner().getUsername(), username)) {
                    throw new ContentLockedByAnotherUserException(item.getLockOwner().getUsername());
                }
            }
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = PermissionOrOwnership.class, action = PERMISSION_ITEM_UNLOCK)
    public void unlockContent(@SiteId String siteId,
                              @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ContentNotFoundException, ContentAlreadyUnlockedException, SiteNotFoundException {
        logger.debug("Unlock item in site '{}' path '{}'", siteId, path);
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.isNull(item)) {
                logger.debug("Item not found in site '{}' path '{}'", siteId, path);
                throw new ContentNotFoundException(path, siteId, format("Item not found in site '%s' path '%s'", siteId, path));
            }
            if (Objects.isNull(item.getLockOwner())) {
                logger.debug("Item in site '{}' path '{}' is already unlocked", siteId, path);
                throw new ContentAlreadyUnlockedException();
            }
            contentServiceInternal.itemUnlockByPath(siteId, path);
            itemServiceInternal.unlockItemByPath(siteId, path);
            logger.debug("Item in site '{}' path '{}' successfully unlocked", siteId, path);
            applicationContext.publishEvent(
                    new LockContentEvent(securityService.getAuthentication(), siteId, path, false));
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
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
        logger.debug("rename path {} to new name {} for site {}", path, name, site);
        return contentServiceV1.renameContent(site, path, name);
    }

    @Override
    @Valid
    public Resource getContentAsResource(@ValidateStringParam String site,
                                         @ValidateSecurePathParam String path)
        throws ContentNotFoundException {
        return contentServiceV1.getContentAsResource(site, path);
    }

    @Override
    @RequireSiteReady
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<ItemVersion> getContentVersionHistory(@SiteId String siteId, @ProtectedResourceId(PATH_RESOURCE_ID) String path) throws ServiceLayerException {
        contentServiceV1.checkContentExists(siteId, path);
        return contentServiceInternal.getContentVersionHistory(siteId, path);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setContentTypeServiceInternal(ContentTypeServiceInternal contentTypeServiceInternal) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setContentServiceV1(org.craftercms.studio.api.v1.service.content.ContentService contentService) {
        this.contentServiceV1 = contentService;
    }
}
