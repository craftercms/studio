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

import java.time.ZonedDateTime;
import java.util.Set;

@SuppressWarnings("unchecked")
public class PreScheduleOperation extends SubmitLifeCycleOperation{
    protected ZonedDateTime launchDate;

    public PreScheduleOperation(WorkflowService workflowService, Set<String> uris, ZonedDateTime launchDate, GoLiveContext context, Set<String> rescheduledUris) {
        super(workflowService, uris, context, rescheduledUris);
        this.launchDate = launchDate;
    }

    @Override
    public Object execute() throws ServiceLayerException {
        workflowService.preSchedule(uris, launchDate, context, rescheduledUris);
        return null;
    }
}
