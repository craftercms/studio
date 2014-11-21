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
package org.craftercms.cstudio.alfresco.task;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.to.ResultTO;

/**
 * a task base for any tasks that need to be queued and wait 
 * 
 * @author hyanghee
 *
 */
public abstract class AbstractTask implements Callable<Object> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);
	
	/** is this task active **/
	protected boolean _isActive = false;
	
	/** task id **/
	protected long _id = 0L; 
	
	/**
	 * constructor
	 */
	public AbstractTask() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	public Object call() throws Exception {
		// no params being passed since this task should retrieve params upon execution
		return executeInternal(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.executor.AbstractTransactionalExecutor#executeInternal(java.util.Map)
	 */
	protected ResultTO executeInternal(Map<String, Serializable> params) {
		Object result = executeInternal();
		return createResult(result);
	}
	
	/**
	 * create result object
	 * 
	 * @param result
	 * @return result
	 */
	protected abstract ResultTO createResult(Object result);

	/**
	 * actual task to be executed
	 * 
	 * @return task result
	 */
	protected abstract Object executeInternal();

	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this._isActive = isActive;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return _isActive;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this._id = id;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return _id;
	}
}
