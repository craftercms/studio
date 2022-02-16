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
package org.craftercms.studio.impl.v1.service.workflow.operation;


import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;

import java.util.Set;

public abstract class SubmitLifeCycleOperation<Result> {

    protected WorkflowService workflowService;
    protected GoLiveContext context;
    protected Set<String> uris;
    protected boolean needsTransaction;
    protected Set<String> rescheduledUris;

    protected SubmitLifeCycleOperation(WorkflowService workflowService, Set<String> uris, boolean needsTransaction, GoLiveContext context) {
        this.workflowService = workflowService;
        this.context = context;
        this.uris = uris;
        this.needsTransaction = needsTransaction;
    }

    protected SubmitLifeCycleOperation(WorkflowService dmWorkflowService, Set<String> uris, GoLiveContext context) {
        this(dmWorkflowService, uris, true, context);
    }

    protected SubmitLifeCycleOperation(WorkflowService dmWorkflowService, Set<String> uris, GoLiveContext context,Set<String> rescheduledUris) {
        this(dmWorkflowService, uris, true, context);
        this.rescheduledUris = rescheduledUris;
    }

    public abstract Result execute() throws ServiceLayerException, UserNotFoundException;

    public boolean needsTransaction() {
        return needsTransaction;
    }
}
