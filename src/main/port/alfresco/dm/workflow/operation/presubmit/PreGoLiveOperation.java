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


import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dejan
 * Date: 12/26/11
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreGoLiveOperation extends SubmitLifeCycleOperation {

    public PreGoLiveOperation(DmWorkflowService dmWorkflowService, Set<String> paths, GoLiveContext context, Set<String> rescheduledUris) {
        super(dmWorkflowService, paths, context, rescheduledUris);
        this._context = context;
    }

    @Override
    public Object execute() throws ServiceException {
        _dmWorkflowService.preGoLive(_uris, _context, _rescheduledUris);
        return null;
    }
}
