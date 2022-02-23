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
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;
import org.craftercms.studio.permissions.PermissionOrOwnership;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_APPROVE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_ITEM_UNLOCK;

public class ContentServiceImpl implements ContentService {

    private ContentServiceInternal contentServiceInternal;
    private ContentTypeServiceInternal contentTypeServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;
    private DeploymentService deploymentService;
    private UserService userService;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private ItemServiceInternal itemServiceInternal;
    private SecurityService securityService;

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_DELETE)
    public List<String> getChildItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      @ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, path);
        List<String> childItems = new ArrayList<String>();
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
        List<String> childItems = new ArrayList<String>();
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
        List<String> contentToDelete = new ArrayList<String>();
        contentToDelete.addAll(getChildItems(siteId, path));
        contentToDelete.add(path);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);

        AuthenticatedUser currentUser = userService.getCurrentUser();
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
        List<String> contentToDelete = new ArrayList<String>();
        contentToDelete.addAll(getChildItems(siteId, paths));
        contentToDelete.addAll(paths);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);
        AuthenticatedUser currentUser = userService.getCurrentUser();
        deploymentService.delete(siteId, contentToDelete, currentUser.getUsername(),
                DateUtils.getCurrentTime(), submissionComment);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, false);
        insertDeleteContentApprovedActivity(siteId, currentUser.getUsername(), contentToDelete);
        return true;
    }

    private void insertDeleteContentApprovedActivity(String siteId, String aprover, List<String> contentToDelete)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_APPROVE);
        auditLog.setActorId(aprover);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        List<AuditLogParameter> auditLogParameters = new ArrayList<AuditLogParameter>();
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
            throws ServiceLayerException, UserNotFoundException, ContentNotFoundException {
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
    public void itemsLockByPath(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                               @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths)
            throws UserNotFoundException, ServiceLayerException {
        contentServiceInternal.itemsLockByPath(siteId, paths);
        itemServiceInternal.lockItemsByPath(siteId, paths, securityService.getCurrentUser());
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_WRITE)
    public void itemsLockById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<Long> itemIds)
            throws UserNotFoundException, ServiceLayerException {
        contentServiceInternal.itemsLockById(siteId, itemIds);
        itemServiceInternal.lockItemsById(itemIds, securityService.getCurrentUser());
    }

    @Override
    @HasPermission(type = PermissionOrOwnership.class, action = PERMISSION_ITEM_UNLOCK)
    public void itemUnlockByPath(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                 @ProtectedResourceId(PATH_RESOURCE_ID) String path) {
        contentServiceInternal.itemUnlockByPath(siteId, path);
        itemServiceInternal.unlockItemByPath(siteId, path);
    }

    @Override
    @HasPermission(type = PermissionOrOwnership.class, action = PERMISSION_ITEM_UNLOCK)
    public void itemUnlockById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, long itemId) {
        contentServiceInternal.itemUnlockById(siteId, itemId);
        itemServiceInternal.unlockItemById(itemId);
    }

    @Override
    public InputStream getContentByCommitId(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path,
                                            String commitId) throws ContentNotFoundException, IOException {
        return contentServiceInternal.getContentByCommitId(siteId, path, commitId);
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public ContentTypeServiceInternal getContentTypeServiceInternal() {
        return contentTypeServiceInternal;
    }

    public void setContentTypeServiceInternal(ContentTypeServiceInternal contentTypeServiceInternal) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
