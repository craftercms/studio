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

package org.craftercms.studio.impl.v2.service.workflow.internal;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflowOrScheduled;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;

public class WorkflowServiceInternalImpl implements WorkflowService, ApplicationEventPublisherAware {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowServiceInternalImpl.class);

    private NotificationService notificationService;
    private ItemServiceInternal itemServiceInternal;
    private ContentServiceInternal contentServiceInternal;
    private org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceInternal;
    private ApplicationEventPublisher eventPublisher;

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemServiceInternal.getItemStatesTotal(siteId, path, states, null);
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) throws SiteNotFoundException {
        return itemServiceInternal.getItemStates(siteId, path, states, null, null, offset, limit).stream()
                .map(SandboxItem::getInstance)
                .collect(toList());
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged, isNew, modified);
    }

    @Override
    public List<SandboxItem> getWorkflowAffectedPaths(String siteId, String path) throws UserNotFoundException, ServiceLayerException {
        List<String> affectedPaths = new LinkedList<>();
        List<SandboxItem> result = new LinkedList<>();
        List<SandboxItem> sandboxItems = contentServiceInternal.getSandboxItemsByPath(siteId, List.of(path), false);
        if (CollectionUtils.isEmpty(sandboxItems)) {
            throw new ContentNotFoundException(path, siteId,
                    "Content not found for site " + siteId + " and path " + path);
        }
        SandboxItem sandboxItem = sandboxItems.getFirst();
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

    public void setItemServiceInternal(final ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    @SuppressWarnings("unused")
    public void setContentServiceInternal(final ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    @SuppressWarnings("unused")
    public void setDependencyServiceInternal(final org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    @SuppressWarnings("unused")
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}
