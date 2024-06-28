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

package org.craftercms.studio.impl.v2.service.workflow.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.content.SandboxItem;

import java.util.List;

public class WorkflowServiceInternalImpl implements WorkflowService {

    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    // TODO: implement all methods from WorkflowServiceInternal for the new publishing system
    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) throws SiteNotFoundException {
        return 0;
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) throws SiteNotFoundException {
        return null;
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {

    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) throws SiteNotFoundException {

    }

    @Override
    public List<SandboxItem> getWorkflowAffectedPaths(String siteId, String path) throws UserNotFoundException, ServiceLayerException {
        return null;
    }

    @Override
    public void delete(String siteId, List<String> paths, List<String> optionalDependencies, String comment) throws ServiceLayerException, UserNotFoundException {

    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
