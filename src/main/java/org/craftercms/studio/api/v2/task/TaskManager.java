/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.task;

import org.craftercms.studio.model.task.Task;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * Keeps track of running tasks and provides functionality to monitor their progress
 */
public interface TaskManager {

	/**
	 * Register a new task and create a {@link TaskProgress} object for it
	 *
	 * @param task the task to register
	 * @param <K>  the type of the task id
	 * @return a {@link TaskProgress} for the task
	 */
	<K extends TaskId, R> TaskProgress<K, R> registerTask(Task<K> task);

	/**
	 * Get the running tasks for a site
	 *
	 * @param siteId the site id
	 * @return a collection of {@link TaskProgress} items for the site
	 */
	Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> getSiteTasks(String siteId);

	/**
	 * Get the running tasks for a site and a specific type
	 *
	 * @param siteId the site id
	 * @param type   the type of the task
	 * @return a collection of {@link TaskProgress} items for the site and type
	 */
	@NonNull
	<K extends TaskId.SiteTaskId, R> List<TaskProgress<K, R>> getSiteTasksByType(String siteId, String type);

	/**
	 * Get a task by its id
	 *
	 * @param taskId the task id
	 * @param <K>    the type of the task id
	 * @return the {@link TaskProgress} for the task, if it exists
	 */
	<K extends TaskId, R> TaskProgress<K, R> getTask(final K taskId);

	/**
	 * Remove a task from the manager
	 *
	 * @param taskId the task id
	 * @param <K>    the type of the task id
	 */
	<K extends TaskId> void removeTask(K taskId);

}
