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
package org.craftercms.studio.impl.v1.service.workflow;


import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;

import java.util.Map;
import java.util.HashMap;

/**
 * internal workflow manager for Workflow.  Simple manager that maintains a 
 * map of states to state handlers.  Each handler returns an updated state
 * @author rdanner
 */
public class WorkflowManager {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

	protected final String MSG_NO_HANDLERS_FOR_WORKFLOW_STATE = "no_handlers_for_workflow_state";
	protected final String MSG_NO_HANDLERS_FOR_WORKFLOW = "no_handlers_for_workflow";
	
	/**
	 * create a new workflow manager
	 */
	public WorkflowManager() {
		_jobStateHandlers = new HashMap<String, Map<String, JobStateHandler>>();
	}

	/**
	 * given a job, process the current state
	 * @param job the job to process
	 */
	public void handleJobState(WorkflowJob job) {
		if(job != null) {
			Map<String, JobStateHandler> handlers = _jobStateHandlers.get(job.getProcessName());
			
			if(handlers != null) {
				String currentState = job.getCurrentStatus();
				JobStateHandler handler = handlers.get(currentState);
				
				if(handler != null) {
					
					String nextState = handler.handleState(job, _workflowService);
					
					if (nextState != null) {
						if(nextState != currentState) {
							job.setCurrentStatus(nextState);
							_workflowService.updateJob(job);
						}
						// This is the only successful path
						return;
					}
					// No next state defined (job is done)
				}
				else {
					logger.error(MSG_NO_HANDLERS_FOR_WORKFLOW_STATE, job.getProcessName(), job.getCurrentStatus(), job.toString());
				}
			}
			else {
				logger.error(MSG_NO_HANDLERS_FOR_WORKFLOW, job.getProcessName(), job.getCurrentStatus(), job.toString());
			}
			// Clean up after all failure conditions
			_workflowService.deleteJob(job.getId());
		}
	}

	/** getter job state handlers */
	public Map<String, Map<String, JobStateHandler>> getJobStateHandlers() { return _jobStateHandlers; }
	/** setter for job state handlers */
	public void setJobStateHandlers(Map<String, Map<String, JobStateHandler>> map) { _jobStateHandlers = map; }
	
	/** getter WorkflowService */
	public WorkflowService getWorkflowService() { return _workflowService; }
	/** setter for Workflow service */
	public void setWorkflowService(WorkflowService service) { _workflowService = service; }

	protected Map<String, Map<String, JobStateHandler>> _jobStateHandlers;
	protected WorkflowService _workflowService;
}
