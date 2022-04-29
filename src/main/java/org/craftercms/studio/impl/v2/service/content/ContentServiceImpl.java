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
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
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
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;
import org.craftercms.studio.permissions.PermissionOrOwnership;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_APPROVE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_ITEM_UNLOCK;

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

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getChildItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, path);
        List<String> childItems = new ArrayList<>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, path));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getChildItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, paths);
        List<String> childItems = new ArrayList<>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, paths));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public boolean deleteContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
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
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public boolean deleteContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID)String siteId,
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
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public GetChildrenResult getChildrenByPath(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                               @ProtectedResourceId(PATH_RESOURCE_ID) String path, String locale,
                                               String keyword, List<String> excludes, String sortStrategy, String order,
                                               int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getChildrenByPath(siteId, path, locale, keyword, excludes, sortStrategy, order,
                offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public GetChildrenResult getChildrenById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String id,
                                             String locale, String keyword, List<String> excludes, String sortStrategy,
                                             String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getChildrenById(siteId, id, locale, keyword, excludes, sortStrategy, order,
                offset, limit);
    }

    @Override
    public Item getItem(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                        @ValidateSecurePathParam String path, boolean flatten) {
        return contentServiceInternal.getItem(siteId, path, flatten);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public DetailedItem getItemByPath(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getItemByPath(siteId, path, preferContent);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public DetailedItem getItemById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, long id,
                                    boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getItemById(siteId, id, preferContent);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = "get_children")
    public List<SandboxItem> getSandboxItemsByPath(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                   @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                                                   boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, preferContent);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public List<SandboxItem> getSandboxItemsById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                 List<Long> ids, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        return contentServiceInternal.getSandboxItemsById(siteId, ids, preferContent);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_WRITE)
    public void lockContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                            @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws UserNotFoundException, ServiceLayerException {
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.nonNull(item)) {
                var username = securityService.getCurrentUser();
                if (StringUtils.isEmpty(item.getLockOwner())) {
                    contentServiceInternal.itemLockByPath(siteId, path);
                    itemServiceInternal.lockItemByPath(siteId, path, username);
                    applicationContext.publishEvent(
                            new LockContentEvent(securityService.getAuthentication(), siteId, path, true));
                } else {
                    if (!StringUtils.equals(item.getLockOwner(), username)) {
                        throw new ContentLockedByAnotherUserException(item.getLockOwner());
                    }
                }
            } else {
                throw new ContentNotFoundException();
            }
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
    }

    @Override
    @HasPermission(type = PermissionOrOwnership.class, action = PERMISSION_ITEM_UNLOCK)
    public void unlockContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                              @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ContentNotFoundException, ContentAlreadyUnlockedException {
        logger.debug("Trying to unlock item at {} in site {}", path, siteId);
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.nonNull(item)) {
                if (StringUtils.isEmpty(item.getLockOwner())) {
                    logger.debug("Item at {} in site {} is already unlocked", path, siteId);
                    throw new ContentAlreadyUnlockedException();
                } else {
                    contentServiceInternal.itemUnlockByPath(siteId, path);
                    itemServiceInternal.unlockItemByPath(siteId, path);
                    logger.debug("Item at {} in site {} successfully unlocked", path, siteId);
                    applicationContext.publishEvent(
                            new LockContentEvent(securityService.getAuthentication(), siteId, path, false));
                }
            } else {
                logger.debug("Item not found at {} in site {}", path, siteId);
                throw new ContentNotFoundException();
            }
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
    }

    @Override
    public Optional<Resource> getContentByCommitId(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path,
                                                   String commitId) throws ContentNotFoundException, IOException {
        return contentServiceInternal.getContentByCommitId(siteId, path, commitId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
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
}
