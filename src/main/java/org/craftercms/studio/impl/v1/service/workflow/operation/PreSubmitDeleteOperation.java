/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;

import java.util.List;
import java.util.Set;

public class PreSubmitDeleteOperation extends SubmitLifeCycleOperation<List<String>> {


    public PreSubmitDeleteOperation(WorkflowService workflowService, Set<String> uriToDelete, GoLiveContext context, Set<String> rescheduledUris) {
        super(workflowService, uriToDelete, context,rescheduledUris);
    }

    @Override
    public List<String> execute() throws ServiceLayerException {
        List<String> stringList = workflowService.preDelete(uris, context, rescheduledUris);
        return stringList;       
    }
}
