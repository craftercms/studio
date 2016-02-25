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

import org.craftercms.studio.api.v1.service.workflow.WorkflowItem;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;

import java.util.*;

/**
 * The in memory workflow job DAL is a simple map based implementation
 * This implementation is good for testing.
 * Note that jobs are not persisted across restarts and there are no limits impossed by
 * this class to keep you from using all of your heap.
 * @author russdanner
 *
 */
public class InMemoryWorkflowJobDAL extends AbstractWorkflowJobDAL {

	/**
	 * constructor
	 */
	public InMemoryWorkflowJobDAL() {
		_jobs = new HashMap<String, WorkflowJob>();
		_items = new HashMap<String, WorkflowItem>();
	}

	
	/**
	 * given a job id return the job
	 * @param id the id of the job
	 */
	public WorkflowJob getJob(String id) {
		return _jobs.get(id);
	}

	/**
	 * get a list of jobs in a particular set of states
	 * @param states the list of states to filter for (null is all states)
	 */
	public List<WorkflowJob> getJobsByState(Set<String> states) {
		List<WorkflowJob> retJobs = new ArrayList<WorkflowJob>();
		Set<String> ids = _jobs.keySet();
		
		for(String id : ids) {
			WorkflowJob job = _jobs.get(id);

			if (states == null || states.contains(job.getCurrentStatus())) {
				retJobs.add(job);
			}
		}
		return retJobs;
	}
	
	/**
	 * given a workflow job transfer object, update the workflow in the system
	 * @param job the job to update
	 */
	public WorkflowJob updateJob(WorkflowJob job) {
		_jobs.put(job.getId(), job);
		return job;
	}
	
	/**
	 * given a job ID, delete the job
	 * @jobId the job to delete
	 */
	public boolean deleteJob(String id) {
		_jobs.remove(id);
		return true;
	}
	
	/**
	 * given an item id return the item
	 * @param id the id of the item
	 */
	public WorkflowItem getItem(String id) {
		return _items.get(id);
	}

	/**
	 * given a job id return all the items
	 * @param jobId the id of the job
	 */
	public List<WorkflowItem> getItemsByJob(String jobId) {
		List<WorkflowItem> retItems = new ArrayList<WorkflowItem>();
		Set<String> ids = _items.keySet();
		
		for(String id : ids) {
			WorkflowItem item = _items.get(id);
			
			if(jobId == item.getJobId()) {
				retItems.add(item);
			}
		}
		
		return retItems;
	}
	
	/**
	 * given a workflow item, update it in the store
	 * @param item the item to update
	 */
	public WorkflowItem updateItem(WorkflowItem item) {
		_items.put(item.getId(), item);
		return item;
	}
	
	/**
	 * given the id of an item remove it from the store
	 * @param id the id of the item to remove
	 */
	public boolean deleteItem(String id) {
		_items.remove(id);
		return true;
	}

	/**
	 * write the job to the store
	 * @param job the job to write
	 */
	protected void writeNewJob(WorkflowJob job) {
		_jobs.put(job.getId(), job);
	}
	
	/**
	 * write the item to the store
	 * @param item the item to write
	 */
	protected void writeNewItem(WorkflowItem item) {
		_items.put(item.getId(), item);
	}
	
	protected Map<String, WorkflowJob> _jobs;
	protected Map<String, WorkflowItem> _items;
}
