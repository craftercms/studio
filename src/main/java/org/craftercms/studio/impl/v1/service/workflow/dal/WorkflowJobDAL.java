/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.service.workflow.dal;

import java.util.Map;
import java.util.Set;
import java.util.List;

import org.craftercms.studio.api.v1.service.workflow.WorkflowItem;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;

public interface WorkflowJobDAL {

	/**
	 * create a workflow job
	 * @param site the site which owns the workflow
	 * @param paths the paths in-flight
	 * @param processName the name of the workflow process
	 * @param properties the properties for the flow
	 */
	WorkflowJob createJob(String site, List<String> srcPaths, String processName, Map<String, String> properties);

	/**
	 * given a job id return the job
	 * @param id the id of the job
	 */
	WorkflowJob getJob(String id);

	/**
	 * get a list of jobs in a particular set of states
	 * @param states the list of states to filter for (null is all states)
	 */
	List<WorkflowJob> getJobsByState(Set<String> states);

	/**
	 * given a workflow job transfer object, update the workflow in the system
	 * @param job the job to update
	 */
	WorkflowJob updateJob(WorkflowJob job);
	
	/**
	 * given a job ID, delete the job
	 * @jobId the job to delete
	 */
	boolean deleteJob(String id);

	/**
	 * create a job item for a given job
	 * @param jobId the job for the item
	 * @param path the path of the content in the job
	 */
	WorkflowItem createItem(String jobId, String path);		

	/**
	 * given an item id return the item
	 * @param id the id of the item
	 */
	WorkflowItem getItem(String id);

	/**
	 * given a job id return all the items
	 * @param jobId the id of the job
	 */
	List<WorkflowItem> getItemsByJob(String jobId);
	
	/**
	 * given a workflow item, update it in the store
	 * @param item the item to update
	 */
	WorkflowItem updateItem(WorkflowItem item);
	
	/**
	 * given the id of an item remove it from the store
	 * @param id the id of the item to remove
	 */
	boolean deleteItem(String id);
}
