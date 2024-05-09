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

package org.craftercms.studio.impl.v2.service.workflow;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflowOrScheduled;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

@RequireSiteReady
public class WorkflowServiceImpl implements WorkflowService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private ItemServiceInternal itemServiceInternal;
    private ContentServiceInternal contentServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;
    private WorkflowService workflowServiceInternal;
    private UserServiceInternal userServiceInternal;
    private NotificationService notificationService;
    private DependencyService dependencyService;
    private PublishService publishServiceInternal;
    private ServicesConfig servicesConfig;
    private StudioConfiguration studioConfiguration;
    private ApplicationContext applicationContext;
    private ActivityStreamServiceInternal activityStreamServiceInternal;

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getItemStatesTotal(@SiteId String siteId,
                                  @ProtectedResourceId(PATH_RESOURCE_ID) String path, Long states) throws SiteNotFoundException {
        return itemServiceInternal.getItemStatesTotal(siteId, path, states, null);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getItemStates(@SiteId String siteId,
                                           @ProtectedResourceId(PATH_RESOURCE_ID) String path, Long states,
                                           int offset, int limit) throws SiteNotFoundException {
        return itemServiceInternal.getItemStates(siteId, path, states, null, null, offset, limit).stream()
                .map(SandboxItem::getInstance)
                .collect(toList());
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = CompositePermission.class, action = PERMISSION_SET_ITEM_STATES)
    public void updateItemStates(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_SET_ITEM_STATES)
    public void updateItemStatesByQuery(@SiteId String siteId, @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                        Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged, isNew, modified);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getWorkflowAffectedPaths(@SiteId String siteId,
                                                      @ProtectedResourceId(PATH_RESOURCE_ID)
                                                      String path) throws UserNotFoundException, ServiceLayerException {
        List<String> affectedPaths = new LinkedList<>();
        List<SandboxItem> result = new LinkedList<>();
        List<SandboxItem> sandboxItems = contentServiceInternal.getSandboxItemsByPath(siteId, List.of(path), false);
        if (CollectionUtils.isEmpty(sandboxItems)) {
            throw new ContentNotFoundException(path, siteId,
                    "Content not found for site " + siteId + " and path " + path);
        }
        SandboxItem sandboxItem = sandboxItems.get(0);
        if (isInWorkflowOrScheduled(sandboxItem.getState())) {
            affectedPaths.add(path);
            boolean isNew = isNew(sandboxItem.getState());
            // TODO: implement for the new publishing system
            boolean isRenamed = false;
//            boolean isRenamed = isNotEmpty(sandboxItem.getPreviousPath());
            if (isNew || isRenamed) {
                affectedPaths.addAll(getMandatoryDescendants(siteId, path));
            }
            List<String> dependencyPaths = new LinkedList<>(dependencyServiceInternal.getHardDependencies(siteId, affectedPaths));
            affectedPaths.addAll(dependencyPaths);
            List<String> candidates = new LinkedList<>();
            for (String p : affectedPaths) {
                if (!candidates.contains(p)) {
                    candidates.add(p);
                }
            }

            List<SandboxItem> candidateItems = contentServiceInternal.getSandboxItemsByPath(siteId, candidates, true);
            result = candidateItems.stream().filter(i -> isInWorkflowOrScheduled(i.getState())).collect(toList());
        }
        return result;
    }

    private List<String> getMandatoryDescendants(String site, String path)
            throws UserNotFoundException, ServiceLayerException {
        List<String> descendants = new LinkedList<>();
        GetChildrenResult result = contentServiceInternal.getChildrenByPath(site, path, null, null, null, null, null,
                null, 0, Integer.MAX_VALUE);
        if (result != null) {
            if (Objects.nonNull(result.getLevelDescriptor())) {
                descendants.add(result.getLevelDescriptor().getPath());
            }
            if (CollectionUtils.isNotEmpty(result.getChildren())) {
                for (SandboxItem item : result.getChildren()) {
                    descendants.add(item.getPath());
                    descendants.addAll(getMandatoryDescendants(site, item.getPath()));
                }
            }
        }
        return descendants;
    }


    private void notifyRejection(String siteId, List<String> pathsToCancelWorkflow, String rejectedBy, String reason,
                                 List<String> submitterList) {
        notificationService.notifyContentRejection(siteId, submitterList, pathsToCancelWorkflow, reason, rejectedBy);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public void delete(@SiteId String siteId,
                       @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                       List<String> optionalDependencies, String comment)
            throws ServiceLayerException, UserNotFoundException {

        // create submission package (aad folders and children if pages)
        List<String> pathsToDelete = calculateDeleteSubmissionPackage(siteId, paths, optionalDependencies);
        String deletedBy = securityService.getCurrentUser();
        try {
            // set system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToDelete, true);
            // cancel existing workflow
            // TODO: implement for the new system
//            cancelExistingWorkflowEntries(siteId, pathsToDelete);
            // add to publishing queue
            // TODO: implement for the new system
//            deploymentService.delete(siteId, pathsToDelete, deletedBy, getCurrentTime(), comment);
            // send notification email
            // TODO: We don't have notifications on delete now. Fix this ???
            // trigger event
            applicationContext.publishEvent(new WorkflowEvent(securityService.getAuthentication(), siteId));
        } finally {
            // clear system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToDelete, false);
        }
    }

    private List<String> calculateDeleteSubmissionPackage(String siteId, List<String> paths,
                                                          List<String> optionalDependencies)
            throws UserNotFoundException, ServiceLayerException {
        List<String> deletePackage = new LinkedList<>(paths);
        if (CollectionUtils.isNotEmpty(optionalDependencies)) {
            deletePackage.addAll(optionalDependencies);
        }
        List<SandboxItem> items = contentServiceInternal.getSandboxItemsByPath(siteId, paths, false);
        items.forEach(item -> {
            if (StringUtils.equals(item.getSystemType(), StudioConstants.CONTENT_TYPE_FOLDER)) {
                deletePackage.addAll(itemServiceInternal.getSubtreeForDelete(siteId, item.getPath()));
            } else if (StringUtils.equals(item.getSystemType(), StudioConstants.CONTENT_TYPE_PAGE)) {
                deletePackage.addAll(itemServiceInternal.getSubtreeForDelete(siteId,
                        item.getPath().replace(FILE_SEPARATOR + INDEX_FILE, "")));
            }
        });
        Set<String> dependencies = dependencyService.getDeleteDependencies(siteId, deletePackage);
        deletePackage.addAll(dependencies);
        deletePackage.sort((lhs, rhs) -> {
            if (StringUtils.startsWith(rhs.replace(FILE_SEPARATOR + INDEX_FILE, ""),
                    lhs.replace(FILE_SEPARATOR + INDEX_FILE, ""))) {
                return 1;
            } else if (StringUtils.startsWith(lhs.replace(FILE_SEPARATOR + INDEX_FILE, ""),
                    rhs.replace(FILE_SEPARATOR + INDEX_FILE, ""))) {
                return -1;
            }
            return lhs.compareTo(rhs);
        });
        return deletePackage;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setWorkflowServiceInternal(WorkflowService workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setPublishServiceInternal(PublishService publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }
}
