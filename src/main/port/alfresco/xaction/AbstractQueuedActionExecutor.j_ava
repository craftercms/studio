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
package org.craftercms.cstudio.alfresco.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javolution.util.FastMap;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.task.AbstractTask;

/**
 * This is a base class of actions that only queue two tasks, one running and
 * one pending and drops any request while a task is pending
 * 
 * @author hyanghee
 * 
 */
public abstract class AbstractQueuedActionExecutor extends ActionExecuterAbstractBase {

	protected class Target {
		public AbstractTask _current = null;
		public AbstractTask _pending = null;
		/** are we already running **/
		protected boolean _running = false;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQueuedActionExecutor.class);

	// Two tasks, current and pending per target
	protected Map<Object, Target> _targets = null;

	/**
	 * task executor
	 */
	protected ExecutorService _executor = Executors.newFixedThreadPool(1);

	/**
	 * initialize the targets map 
	 */
	public void init() {
		_targets = new FastMap<Object, Target>();
	}

	/**
	 * initialize target by creating current and pending tasks
	 * 
	 * @param target
	 * 			key to find the target tasks
	 */
	public synchronized void initializeTarget(Object target) {
		Target actionTarget = _targets.get(target);
		if (actionTarget == null) {
			actionTarget = new Target();
		}
		actionTarget._current = initializeTask();
		actionTarget._pending = initializeTask();
		_targets.put(target, actionTarget);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl
	 * (org.alfresco.service.cmr.action.Action,
	 * org.alfresco.service.cmr.repository.NodeRef)
	 */
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		Object target = getTarget(action, actionedUponNodeRef);

		Target actionTarget = _targets.get(target);
		synchronized (this) {
			if (actionTarget == null) {
				initializeTarget(target);
				actionTarget = _targets.get(target);
			}
			if (actionTarget._running) {
				if (actionTarget._pending.isActive()) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(">>>>>>>>>>>> [Request for " + target + ", Status: DROPPED]");
					}
				} else {
					setTaskParameters(action, actionedUponNodeRef, actionTarget._pending);
					actionTarget._pending.setActive(true);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(">>>>>>>>>>>> [" + actionTarget._pending + " Status: ADDED]");
					}
				}
				return;
			} else {
				setTaskParameters(action, actionedUponNodeRef, actionTarget._current);
				actionTarget._current.setActive(true); // task is now active
				actionTarget._running = true;
			}
		}

		while (actionTarget._current.isActive()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(">>>>>>>>>>>> [" + actionTarget._current + " Status: RUNNING]");
			}

			FutureTask<Object> taskToRun = new FutureTask<Object>(actionTarget._current);
			Future future = _executor.submit(taskToRun);
			try {
				future.get();
			} catch (InterruptedException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Failed to execute task" + taskToRun.toString(), e);
				}
			} catch (ExecutionException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Failed to execute task" + taskToRun.toString(), e);
				}
			} // Wait for the task to finish
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(">>>>>>>>>>>> [" + actionTarget._current + " Status: DONE]");
			}

			actionTarget._current.setActive(false); // task is now inactive
			prepareNextTask(target);
		}

		synchronized (this) {
			actionTarget._running = false;
		}
	}

	/**
	 * Prepare next task
	 * 
	 * @param target
	 * 			key to get tasks
	 * 				
	 */
	protected synchronized void prepareNextTask(Object target) {
		Target actionTarget = _targets.get(target);
		AbstractTask tmp = actionTarget._current;
		actionTarget._current = actionTarget._pending;
		actionTarget._pending = tmp;
	}

	/**
	 * initialize future task array
	 * 
	 * @return future task
	 */
	protected abstract AbstractTask initializeTask();

	/**
	 * set task parameters
	 * 
	 * @param action
	 * @param actionedUponNodeRef
	 * @param task
	 * 			the task to set parameters in
	 */
	protected abstract void setTaskParameters(Action action, NodeRef actionedUponNodeRef, AbstractTask task);

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.alfresco.repo.action.ParameterizedItemAbstractBase#
	 * addParameterDefinitions(java.util.List)
	 */
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {}

	/**
	 * get the target key to find action target
	 * 
	 * @param action
	 * @param actionedUponNodeRef
	 * @return target
	 */
	protected abstract Object getTarget(Action action, NodeRef actionedUponNodeRef);

}
