/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
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
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_APPROVE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class ContentServiceImpl implements ContentService {

    private ContentServiceInternal contentServiceInternal;
    private ContentTypeServiceInternal contentTypeServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;
    private DeploymentService deploymentService;
    private ObjectStateService objectStateService;
    private UserService userService;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private ItemServiceInternal itemServiceInternal;

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_content")
    public List<String> getChildItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, path);
        List<String> childItems = new ArrayList<String>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, path));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_content")
    public List<String> getChildItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<String> paths) {
        List<String> subtreeItems = contentServiceInternal.getSubtreeItems(siteId, paths);
        List<String> childItems = new ArrayList<String>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, paths));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_content")
    public boolean deleteContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path,
                                 String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException {
        List<String> contentToDelete = new ArrayList<String>();
        contentToDelete.add(path);
        contentToDelete.addAll(getChildItems(siteId, path));
        objectStateService.setSystemProcessingBulk(siteId, contentToDelete, true);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);

        AuthenticatedUser currentUser = userService.getCurrentUser();
        deploymentService.delete(siteId, contentToDelete, currentUser.getUsername(),
                ZonedDateTime.now(ZoneOffset.UTC), submissionComment);
        objectStateService.setSystemProcessingBulk(siteId, contentToDelete, false);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, false);
        insertDeleteContentApprovedActivity(siteId, currentUser.getUsername(), contentToDelete);
        return true;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_content")
    public boolean deleteContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID)String siteId, List<String> paths,
                                 String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException {
        List<String> contentToDelete = new ArrayList<String>();
        contentToDelete.addAll(getChildItems(siteId, paths));
        contentToDelete.addAll(paths);
        objectStateService.setSystemProcessingBulk(siteId, contentToDelete, true);
        itemServiceInternal.setSystemProcessingBulk(siteId, contentToDelete, true);
        AuthenticatedUser currentUser = userService.getCurrentUser();
        deploymentService.delete(siteId, contentToDelete, currentUser.getUsername(),
                ZonedDateTime.now(ZoneOffset.UTC), submissionComment);
        objectStateService.setSystemProcessingBulk(siteId, contentToDelete, false);
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
                                               String sortStrategy,
                                               String order, int offset, int limit) {
        return contentServiceInternal.getChildrenByPath(siteId, path, locale, sortStrategy, order, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "get_children")
    public GetChildrenResult getChildrenById(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String id,
                                             String locale, String sortStrategy, String order, int offset, int limit) {
        return contentServiceInternal.getChildrenById(siteId, id, locale, sortStrategy, order, offset, limit);
    }

    @Override
    public Item getItem(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                        @ValidateSecurePathParam String path) {
        return contentServiceInternal.getItem(siteId, path);
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

    public ObjectStateService getObjectStateService() {
        return objectStateService;
    }

    public void setObjectStateService(ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
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
}
