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

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In progess workflow job
 * @author rdanner
 */
public class WorkflowJob {

	/** id property getter */
	public String getId() { return _id; }
	/** id property setter */
	public void setId(String id) { _id = id; }

	/** process name property getter */
	public String getProcessName() { return _processName; }
	/** process name property setter */
	public void setProcessName(String processName) { _processName = processName; }
	
	/** site property getter */
	public String getSite() { return _site; }
	/** site property setter */
	public void setSite(String site) { _site = site; }

	/** create date property getter */
	public ZonedDateTime getCreateDate() { return _createDate; }
	/** create date property setter */
	public void setCreateDate(ZonedDateTime createDate) { _createDate = createDate; }
	
	/** modified date property getter */
	public ZonedDateTime getModifiedDate() { return _modifiedDate; }
	/** modified date property setter */
	public void setModifiedDate(ZonedDateTime modified) { _modifiedDate = modified; }
	
	/** current status property getter */
	public String getCurrentStatus() { return _currentStatus; }
	/** current status property setter */
	public void setCurrentStatus(String currentStatus) { _currentStatus = currentStatus; }

	/** items property getter */
	public List<WorkflowItem> getItems() { return _items; }
	/** items property setter */
	public void setItems(List<WorkflowItem> items) { _items = items; }

	/**  property getter */
	public Map<String, String> getProperties() { return _properties; }
	/**  property setter */
	public void setProperties(Map<String, String> properties) { _properties = properties; }

	//@Transient
	public void setPropertyList(List<WorkflowJobProperty> list) {
		_properties = new HashMap<String, String>();
		if (list != null) {
			for (WorkflowJobProperty prop : list) {
				_properties.put(prop.getName(), prop.getValue());
			}
		}
	}

	protected String _id;
	protected String _site;
	protected String _processName;
	protected ZonedDateTime _createDate;
	protected ZonedDateTime _modifiedDate;
	protected String _currentStatus;
	protected List<WorkflowItem> _items;
	protected Map<String, String> _properties;
}
