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
package org.craftercms.studio.impl.v1.service.workflow.dal;

import org.craftercms.studio.api.v1.service.workflow.WorkflowItem;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;

import java.util.*;


/**
 * abstract workflow job DAL takes care of common operations that are the same across all persistance 
 *
 * @author russdanner
 *
 */
public abstract class AbstractWorkflowJobDAL implements WorkflowJobDAL {

	/**
	 * create a workflow job
	 * @param site the site which owns the workflow
	 * @param paths the paths in-flight
	 * @param processName the name of the workflow process
	 * @param properties the properties for the flow
	 */
	public WorkflowJob createJob(String site, List<String> srcPaths, String processName, Map<String, String> properties) {
		WorkflowJob retJob = newJob(site, processName, properties);

		for(String path : srcPaths) {
			WorkflowItem item = createItem(retJob.getId(), path);
			retJob.getItems().add(item);
		}

		writeNewJob(retJob);

		return retJob;
	}

	/**
	 * create a job item for a given job
	 * @param jobId the job for the item
	 * @param path the path of the content in the job
	 */
	public WorkflowItem createItem(String jobId, String path) {
		return newItem(jobId, path);
	}
	
	/**
	 * write the item to the store
	 * @param item the item to write
	 */
	protected abstract void writeNewJob(WorkflowJob job);
	
	/**
	 * write the item to the store
	 * @param item the item to write
	 */
	protected abstract void writeNewItem(WorkflowItem item);

	/**
	 * create a new job
	 * @param site the site which owns the workflow
	 * @param processName the name of the workflow process
	 * @param properties the properties for the flow
	 */
	protected WorkflowJob newJob(String site, String processName, Map<String, String> properties) {
		WorkflowJob retJob = new WorkflowJob();
		Date now = new Date();
		List<WorkflowItem> items = new ArrayList<WorkflowItem>();
		
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		
		retJob.setId(generateJobId());
		retJob.setCreateDate(now);
		retJob.setModifiedDate(now);
		retJob.setSite(site);
		retJob.setItems(items);
		retJob.setProcessName(processName);
		retJob.setProperties(properties);
		retJob.setCurrentStatus(WorkflowService.STATE_CREATED);

		return retJob;
	}

	/**
	 * create a new item
	 * @param jobId the id of the job that owns the item
	 * @parm path the path of the content
	 */
	protected WorkflowItem newItem(String jobId, String path) {
		WorkflowItem retItem = new WorkflowItem();
		retItem.setId(generateItemId());
		retItem.setJobId(jobId);
		retItem.setPath(path);
		retItem.setPercentComplete(0);
		
		return retItem;
	}

	/**
	 * generate a job id
	 * @return a ID for the job
	 */
	protected String generateJobId() {
		String retId = "";
		UUID uuid = UUID.randomUUID();
		retId = JOB_ID_PREFIX + uuid.toString();
		return retId;
	}

	/**
	 * generate a item id
	 * @return a ID for the item
	 */
	protected String generateItemId() {
		String retId = "";
		UUID uuid = UUID.randomUUID();
		retId = ITM_ID_PREFIX + uuid.toString();
		return retId;
	}		

	public static final String JOB_ID_PREFIX = "job-";
	public static final String ITM_ID_PREFIX = "itm-";
}