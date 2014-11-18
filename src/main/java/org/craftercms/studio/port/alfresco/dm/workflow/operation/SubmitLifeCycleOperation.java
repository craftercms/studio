/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.workflow.operation;

import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.workflow.GoLiveContext;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

import java.util.Set;

public abstract class SubmitLifeCycleOperation<Result> {

    protected DmWorkflowService _dmWorkflowService;
    protected GoLiveContext _context;
    protected Set<String> _uris;
    protected boolean _needsTransaction;
    protected Set<String> _rescheduledUris;

    protected SubmitLifeCycleOperation(DmWorkflowService dmWorkflowService, Set<String> uris, boolean needsTransaction, GoLiveContext context) {
        this._dmWorkflowService = dmWorkflowService;
        this._context = context;
        this._uris = uris;
        this._needsTransaction = needsTransaction;
    }

    protected SubmitLifeCycleOperation(DmWorkflowService dmWorkflowService, Set<String> uris, GoLiveContext context) {
        this(dmWorkflowService, uris, true, context);
    }

    protected SubmitLifeCycleOperation(DmWorkflowService dmWorkflowService, Set<String> uris, GoLiveContext context,Set<String> rescheduledUris) {
        this(dmWorkflowService, uris, true, context);
        this._rescheduledUris = rescheduledUris;
    }

    public abstract Result execute() throws ServiceException;

    public boolean needsTransaction() {
        return _needsTransaction;
    }
}
