/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflow;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflowOrScheduled;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class WorkflowServiceImpl implements WorkflowService {

    private ItemServiceInternal itemServiceInternal;
    private ContentServiceInternal contentServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemServiceInternal.getItemStatesTotal(siteId, path, states);
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) {
        return itemServiceInternal.getItemStates(siteId, path, states, offset, limit)
                .stream().map(item -> SandboxItem.getInstance(item)).collect(Collectors.toList());
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged) {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged);
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged) {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getWorkflowAffectedPaths(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                      @ProtectedResourceId(PATH_RESOURCE_ID)
                                                      String path) throws UserNotFoundException, ServiceLayerException {
        List<String> affectedPaths = new ArrayList<String>();
        List<SandboxItem> result = new ArrayList<>();
        Item item = itemServiceInternal.getItem(siteId, path);
        if (isInWorkflowOrScheduled(item.getState())) {
            affectedPaths.add(path);
            boolean isNew = isNew(item.getState());
            boolean isRenamed = StringUtils.isNotEmpty(item.getPreviousPath());
            if (isNew || isRenamed) {
                affectedPaths.addAll(getMandatoryDescendents(siteId, path));
            }
            List<String> dependencyPaths = new ArrayList<String>();
            dependencyPaths.addAll(dependencyServiceInternal.getHardDependencies(siteId, affectedPaths));
            affectedPaths.addAll(dependencyPaths);
            List<String> candidates = new ArrayList<String>();
            for (String p : affectedPaths) {
                if (!candidates.contains(p)) {
                    candidates.add(p);
                }
            }

            List<SandboxItem> candidateItems = contentServiceInternal.getSandboxItemsByPath(siteId, candidates, true);
            result = candidateItems.stream().filter(i -> isInWorkflowOrScheduled(i.getState())).collect(Collectors.toList());
        }
        return result;
    }

    private List<String> getMandatoryDescendents(String site, String path)
            throws UserNotFoundException, ServiceLayerException {
        List<String> descendents = new ArrayList<String>();
        GetChildrenResult result = contentServiceInternal.getChildrenByPath(site, path, null, null, null, null,
                null, 0, Integer.MAX_VALUE);
        if (result != null) {
            if (Objects.nonNull(result.getLevelDescriptor())) {
                descendents.add(result.getLevelDescriptor().getPath());
            }
            if (CollectionUtils.isNotEmpty(result.getChildren())) {
                for (SandboxItem item : result.getChildren()) {
                    descendents.add(item.getPath());
                    descendents.addAll(getMandatoryDescendents(site, item.getPath()));
                }
            }
        }
        return descendents;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }
}
