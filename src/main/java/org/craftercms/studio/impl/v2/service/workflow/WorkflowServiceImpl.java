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

package org.craftercms.studio.impl.v2.service.workflow;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;

import java.time.Instant;
import java.util.List;

import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

@RequireSiteReady
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowService workflowServiceInternal;

    public WorkflowServiceImpl(final WorkflowService workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getItemStatesTotal(@SiteId String siteId,
                                  @ProtectedResourceId(PATH_RESOURCE_ID) String path, Long states) throws SiteNotFoundException {
        return workflowServiceInternal.getItemStatesTotal(siteId, path, states);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getItemStates(@SiteId String siteId,
                                           @ProtectedResourceId(PATH_RESOURCE_ID) String path, Long states,
                                           int offset, int limit) throws SiteNotFoundException {
       return workflowServiceInternal.getItemStates(siteId, path, states, offset, limit);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = CompositePermission.class, action = PERMISSION_SET_ITEM_STATES)
    public void updateItemStates(@SiteId String siteId,
                                 @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {
        workflowServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_SET_ITEM_STATES)
    public void updateItemStatesByQuery(@SiteId String siteId, @ProtectedResourceId(PATH_RESOURCE_ID) String path,
                                        Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {
        workflowServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged, isNew, modified);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getWorkflowAffectedPaths(@SiteId String siteId,
                                                      @ProtectedResourceId(PATH_RESOURCE_ID)
                                                      String path) throws UserNotFoundException, ServiceLayerException {
        return workflowServiceInternal.getWorkflowAffectedPaths(siteId, path);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUBLISH)
    public void approvePackage(@SiteId String siteId, long packageId, Instant schedule, String comment)
            throws AuthenticationException, ServiceLayerException {
        workflowServiceInternal.approvePackage(siteId, packageId, schedule, comment);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CANCEL_PUBLISH)
    public void rejectPackage(@SiteId String siteId, long packageId, String comment) throws ServiceLayerException, AuthenticationException {
        workflowServiceInternal.rejectPackage(siteId, packageId, comment);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CANCEL_PUBLISH)
    public void cancelPackage(@SiteId String siteId, long packageId, String comment) throws ServiceLayerException, AuthenticationException {
        workflowServiceInternal.cancelPackage(siteId, packageId, comment);
    }
}
