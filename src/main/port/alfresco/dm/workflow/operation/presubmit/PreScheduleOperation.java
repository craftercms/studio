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
package org.craftercms.cstudio.alfresco.dm.workflow.operation.presubmit;

import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.workflow.GoLiveContext;
import org.craftercms.cstudio.alfresco.dm.workflow.operation.SubmitLifeCycleOperation;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

import java.util.Date;
import java.util.Set;

public class PreScheduleOperation extends SubmitLifeCycleOperation{
    protected Date _launchDate;

    public PreScheduleOperation(DmWorkflowService dmWorkflowService, Set<String> uris, Date launchDate, GoLiveContext context, Set<String> rescheduledUris) {
        super(dmWorkflowService, uris, context, rescheduledUris);
        this._launchDate = launchDate;
    }

    @Override
    public Object execute() throws ServiceException {
        _dmWorkflowService.preSchedule(_uris, _launchDate, _context, _rescheduledUris);
        return null;
    }
}
