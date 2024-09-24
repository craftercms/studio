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
package org.craftercms.studio.impl.v1.service.workflow;

import jakarta.validation.Valid;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.ZonedDateTime;
import java.util.*;

import static org.craftercms.studio.api.v2.dal.ItemState.*;

/**
 * workflow service implementation
 */
public class WorkflowServiceImpl implements WorkflowService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    protected enum Operation {
        GO_LIVE, DELETE,
        SUBMIT_TO_GO_LIVE,
        REJECT,
    }

    protected ServicesConfig servicesConfig;
    protected DeploymentService deploymentService;
    protected ContentService contentService;
    protected DependencyService dependencyService;
    protected DmPublishService dmPublishService;
    protected SecurityService securityService;
    protected SiteService siteService;
    protected WorkflowProcessor workflowProcessor;
    protected NotificationService notificationService;
    protected StudioConfiguration studioConfiguration;
    protected AuditServiceInternal auditServiceInternal;
    protected ItemServiceInternal itemServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected ContentServiceInternal contentServiceInternal;
    protected PublishServiceInternal publishServiceInternal;
    protected ApplicationContext applicationContext;
    protected ActivityStreamServiceInternal activityStreamServiceInternal;

    private List<String> getDeploymentPaths(final List<DmDependencyTO> submittedItems) {
        List<String> paths=new ArrayList<>(submittedItems.size());
        for (DmDependencyTO submittedItem : submittedItems) {
            paths.add(submittedItem.getUri());
        }
        return paths;
    }

    @Override
    @Valid
    public boolean removeFromWorkflow(@ValidateStringParam String site,
                                      @ValidateSecurePathParam String path, boolean cancelWorkflow)
            throws ServiceLayerException, UserNotFoundException {
        Set<String> processedPaths = new HashSet<>();
        return removeFromWorkflow(site, path, processedPaths, cancelWorkflow);
    }

    protected boolean removeFromWorkflow(String site,  String path, Set<String> processedPaths, boolean cancelWorkflow)
            throws ServiceLayerException, UserNotFoundException {
        // remove submitted aspects from all dependent items
        if (!processedPaths.contains(path)) {
            processedPaths.add(path);
            // cancel workflow if anything is pending
            if (cancelWorkflow) {
                _cancelWorkflow(site, path);
            }
        }
        return false;
    }

    protected void _cancelWorkflow(String site, String path) throws ServiceLayerException, UserNotFoundException {
        List<String> allItemsToCancel = getWorkflowAffectedPathsInternal(site, path);
        List<String> paths = new ArrayList<>();
        for (String affectedItem : allItemsToCancel) {
            try {
                deploymentService.cancelWorkflow(site, affectedItem);
                paths.add(affectedItem);
            } catch (DeploymentException e) {
                // TODO: SJ: This can get excessive since it's in a loop. Refactor.
                logger.trace("Failed to cancel workflow in site '{}' path '{}'", site, affectedItem, e);
            }
        }
        if (CollectionUtils.isNotEmpty(paths)) {
            workflowServiceInternal.deleteWorkflowEntries(site, paths);
            itemServiceInternal.updateStateBitsBulk(site, paths, CANCEL_WORKFLOW_ON_MASK, CANCEL_WORKFLOW_OFF_MASK);
            applicationContext.publishEvent(new WorkflowEvent(securityService.getAuthentication(), site));
        }
    }

    protected List<String> getWorkflowAffectedPathsInternal(String site, String path)
            throws ServiceLayerException, UserNotFoundException {
        List<String> affectedPaths = new ArrayList<>();
        List<String> filteredPaths = new ArrayList<>();
        Item item = itemServiceInternal.getItem(site, path);
        if (isInWorkflowOrScheduled(item.getState())) {
            affectedPaths.add(path);
            boolean isNew = isNew(item.getState());
            boolean isRenamed = StringUtils.isNotEmpty(item.getPreviousPath());
            if (isNew || isRenamed) {
                getMandatoryChildren(site, path, affectedPaths);
            }
            List<String> dependencyPaths = new ArrayList<>(dependencyService.getPublishingDependencies(site, affectedPaths));
            affectedPaths.addAll(dependencyPaths);
            List<String> candidates = new ArrayList<>();
            for (String p : affectedPaths) {
                if (!candidates.contains(p)) {
                    candidates.add(p);
                }
            }

            List<SandboxItem> candidateItems = contentServiceInternal.getSandboxItemsByPath(site, candidates, true);
            for (SandboxItem cp : candidateItems) {
                if (isInWorkflowOrScheduled(cp.getState())) {
                    filteredPaths.add(cp.getPath());
                }
            }
        }
        return filteredPaths;
    }

    private void getMandatoryChildren(String site, String path, List<String> affectedPaths)
            throws UserNotFoundException, ServiceLayerException {
        GetChildrenResult result = contentServiceInternal.getChildrenByPath(site, path, null, null, null, null, null,
                null, 0, Integer.MAX_VALUE);
        if (result != null) {
            if (Objects.nonNull(result.getLevelDescriptor())) {
                affectedPaths.add(result.getLevelDescriptor().getPath());
            }
            if (CollectionUtils.isNotEmpty(result.getChildren())) {
                for (SandboxItem item : result.getChildren()) {
                    affectedPaths.add(item.getPath());
                    getMandatoryChildren(site, item.getPath(), affectedPaths);
                }
            }
        }
    }

    @Override
    public void preScheduleDelete(Set<String> urisToDelete, final ZonedDateTime scheduleDate,
                                  final GoLiveContext context) {
        final String site = context.getSite();
        final List<String> itemsToDelete = new ArrayList<>(urisToDelete);
        dmPublishService.unpublish(site, itemsToDelete, context.getApprover(), scheduleDate);
    }

    @Override
    public List<String> preDelete(Set<String> urisToDelete, GoLiveContext context, Set<String> rescheduledUris)
            throws ServiceLayerException, UserNotFoundException {
        cleanUrisFromWorkflow(urisToDelete, context.getSite());
        cleanUrisFromWorkflow(rescheduledUris, context.getSite());
        return deleteInTransaction(context.getSite(), new ArrayList<>(urisToDelete),
                context.getApprover());
    }

    protected List<String> deleteInTransaction(final String site, final List<String> itemsToDelete,
                                               final String approver) {
        dmPublishService.unpublish(site, itemsToDelete, approver);
        return null;
        //return contentService.deleteContents(site, itemsToDelete, generateActivity, approver);
    }

    protected void cleanUrisFromWorkflow(final Set<String> uris, final String site)
            throws ServiceLayerException, UserNotFoundException {
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                cleanWorkflow(uri, site);
            }
        }
    }

    @Override
    @Valid
    public boolean cleanWorkflow(@ValidateSecurePathParam final String url,
                                 @ValidateStringParam final String site)
            throws ServiceLayerException, UserNotFoundException {
        _cancelWorkflow(site, url);
        return true;
    }

    protected void reject(String site, List<DmDependencyTO> submittedItems, String reason, String approver)
            throws ServiceLayerException, UserNotFoundException {
        if (submittedItems != null) {
            // for each top level items submitted
            // add its children and dependencies that must go with the top level
            // item to the submitted aspect
            // and only submit the top level items to workflow
            for (DmDependencyTO dmDependencyTO : submittedItems) {
                _cancelWorkflow(site, dmDependencyTO.getUri());
            }
            if(!submittedItems.isEmpty()) {
                // for some reason ,  submittedItems.get(0).getSubmittedBy() returns empty and
                // metadata for the same value is also empty , using last modify to blame the rejection.
                final WorkflowItem workflowItem =
                        workflowServiceInternal.getWorkflowEntry(site, submittedItems.get(0).getUri());
                String whoToBlame = "admin"; //worst case, we need someone to blame.
                if (workflowItem != null) {
                    User user = userServiceInternal.getUserByIdOrUsername(workflowItem.getSubmitterId(), null);
                    if (user != null) {
                        whoToBlame = user.getUsername();
                    }
                }
                notificationService.notifyContentRejection(site, Collections.singletonList(whoToBlame),
                        getDeploymentPaths(submittedItems), reason, approver);
            }
        }

        // TODO: send the reason to the user
    }

    // End Rename Service Methods
     /* ================ */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setDmPublishService(DmPublishService dmPublishService) {
        this.dmPublishService = dmPublishService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) {
        this.workflowProcessor = workflowProcessor;
    }

    public void setNotificationService(
            final org.craftercms.studio.api.v2.service.notification.NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }
}
