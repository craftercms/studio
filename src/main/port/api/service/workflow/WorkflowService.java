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
package org.craftercms.cstudio.api.service.workflow;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.craftercms.cstudio.alfresco.service.api.NotificationService;

public interface WorkflowService {

	public static final String STATE_CREATED = "created";
	public static final String STATE_STARTED = "started";
	public static final String STATE_IN_PROGRESS = "in-progress";
	public static final String STATE_COMPLETE = "complete";
	public static final String STATE_ENDED = "ended";
	
	/**
	 * create a workflow job
	 * @param site the site which owns the workflow
	 * @param paths the paths in-flight
	 * @param processName the name of the workflow process
	 * @param properties the properties for the flow
	 */
	WorkflowJob createJob(String site, List<String> paths,  String processName, Map<String, String> properties);
	
	/**
	 * get a list of active jobs
	 */
	List<WorkflowJob> getActiveJobs();
	
	/**
	 * get a list of jobs in a particular set of states
	 * @param states the list of states to filter for (null is all states)
	 */
	List<WorkflowJob> getJobsInState(Set<String> states);

	/**
	 * for a given job ID return the job object
	 * @jobId the id of the job to return
	 */
	WorkflowJob getJob(String jobId);
	
	/**
	 * given a workflow job transfer object, update the workflow in the system
	 * @param job the job to update
	 */
	WorkflowJob updateJob(WorkflowJob job);
	
	/**
	 * given a job ID, delete the job
	 * @jobId the job to delete
	 */
	boolean deleteJob(String jobId);
	
	/**
	 * given a jobID move it to the start phase
	 * @param jobId the ID of the job to start
	 */
	boolean startJob(String jobId);

	/**
	 * Set the state of a job to a given state
	 * @param jobId the id of the job to transition
	 * @parm the state to transition to
	 */
	boolean transitionJobState(String jobId, String state);
	
	/**
	 * end a job
	 * @param jobId the id of the job to end
	 */
	boolean endJob(String jobId);
	
	/**
	 * submit content to go-live
	 * - convienience method for workflow that puts items in the approval queue for go-live
	 * - may result in items (and related dependencies) being put in several workflows (depending on rules)
	 * @param site the site
	 * @param paths the paths of the content to be submitted
	 * @param scheduledDate A suggested launch date if appropriate.  Null for no date
	 * @param sendApprovedNotice true triggers email to submitter on approval
	 * @param submitter the one submitted the job.
	 */
	void submitToGoLive(String site, List<String> paths, Date scheduledDate, boolean sendApprovedNotice, String submitter);

	/**
	 * Get notification service.
	 */
	NotificationService getNotificationService();
}
