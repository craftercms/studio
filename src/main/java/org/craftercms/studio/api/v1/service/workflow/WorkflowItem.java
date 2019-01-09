/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.service.workflow;

/**
 * Item in an inflight workflow item
 * @author rdanner
 */
public class WorkflowItem {

	/** id property getter */
	public String getId() { return _id; }
	/** id property setter */
	public void setId(String id) { _id = id; }

	/** job id property getter */
	public String getJobId() { return _jobId; }
	/** job id property setter */
	public void setJobId(String id) { _jobId = id; }
	
	/** path property getter */
	public String getPath() { return _path; }
	/** path property setter */
	public void setPath(String path) { _path = path; }

	/** path property getter */
	public int getPercentComplete() { return _percentComplete; }
	/** path property setter */
	public void setPercentComplete(int percentComplete) { 
		if(percentComplete < 0) 
			percentComplete = 0;
		else if(percentComplete > 100) 
			percentComplete = 100;
		
		_percentComplete = percentComplete; 
	}

	protected String _id;
	protected String _jobId;
	protected String _path;
	protected int _percentComplete;
}
