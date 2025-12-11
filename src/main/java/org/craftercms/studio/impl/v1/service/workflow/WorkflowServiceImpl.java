/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.collections4.ListUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.DalUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

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

    protected SecurityService securityService;
    protected NotificationService notificationService;
    protected ItemServiceInternal itemServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected PublishServiceInternal publishServiceInternal;
    protected ApplicationContext applicationContext;

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

    protected void _cancelWorkflow(String site, String path) throws ServiceLayerException {
        Set<String> affectedPaths = new HashSet<>();
        Collection<String> affectedPackages = publishServiceInternal.getWorkflowAffectedPackages(site, path);
        if (CollectionUtils.isNotEmpty(affectedPackages)) {
            publishServiceInternal.cancelPublishingPackages(site, affectedPackages);
            workflowServiceInternal.deleteWorkflowPackages(site, affectedPackages);
            affectedPaths.addAll(publishServiceInternal.getPackagePaths(site, affectedPackages));
        }

        Collection<String> workflowHardDeps = new HashSet<>(workflowServiceInternal.getWorkflowHardDeps(site, path));
        workflowHardDeps.add(path);
        workflowServiceInternal.deleteWorkflowEntries(site, workflowHardDeps, Workflow.STATE_OPENED);
        affectedPaths.addAll(workflowHardDeps);

        if (isNotEmpty(affectedPaths)) {
            recalculateItemStates(site, affectedPaths.stream().toList());
            applicationContext.publishEvent(new WorkflowEvent(securityService.getAuthentication(), site));
        }
    }

    // Recalculate state bits based on the current publish_request and workflow tables
    private void recalculateItemStates(final String siteId, final List<String> affectedPaths) {
        for (List<String> batch : ListUtils.partition(affectedPaths, DalUtils.MY_BATIS_QUERY_BATCH_SIZE)) {
            itemServiceInternal.recalculateItemStates(siteId, batch);
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
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setNotificationService(
            final org.craftercms.studio.api.v2.service.notification.NotificationService notificationService) {
        this.notificationService = notificationService;
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

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }
}
