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

package org.craftercms.studio.impl.v2.service.task;

import org.craftercms.studio.api.v2.task.TaskId;
import org.craftercms.studio.api.v2.task.TaskManager;
import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.model.task.Task;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link TaskManager} implementation
 */
public class TaskManagerImpl implements TaskManager, ApplicationContextAware {

	// By site and then by task id
	private final Map<String, Map<TaskId.SiteTaskId, TaskProgress<? extends TaskId.SiteTaskId, ?>>> tasks;
	private final Map<TaskId, TaskProgress<? extends TaskId, ?>> globalTasks;
	private ApplicationContext applicationContext;

	public TaskManagerImpl() {
		tasks = new ConcurrentHashMap<>();
		globalTasks = new ConcurrentHashMap<>();
	}

	/**
	 * Get the map of tasks for a site
	 */
	private Map<TaskId.SiteTaskId, TaskProgress<? extends TaskId.SiteTaskId, ?>> bySite(final String site) {
		return tasks.computeIfAbsent(site, k -> new ConcurrentHashMap<>());
	}

	@Override
	public <K extends TaskId, R> TaskProgress<K, R> registerTask(final Task<K> task) {
		TaskProgressImpl<K, R> progress = applicationContext.getBean(TaskProgressImpl.class, task, this);
		switch (task.getTaskId()) {
			case TaskId.SiteTaskId siteTaskId -> bySite(siteTaskId.getSiteId()).put(siteTaskId, (TaskProgress<? extends TaskId.SiteTaskId, ?>) progress);
			case TaskId.GlobalTaskId globalTaskId -> globalTasks.put(globalTaskId, progress);
		}
		return progress;
	}

	@Override
	public <K extends TaskId, R> TaskProgress<K, R> getTask(final K taskId) {
		return switch (taskId) {
			case TaskId.SiteTaskId siteTaskId -> (TaskProgress<K, R>) bySite(siteTaskId.getSiteId()).get(taskId);
			case TaskId.GlobalTaskId __ -> (TaskProgress<K, R>) globalTasks.get(taskId);
		};
	}

	@Override
	@NonNull
	public <K extends TaskId.SiteTaskId, R> List<TaskProgress<K, R>> getSiteTasksByType(final String siteId, final String type) {
		return bySite(siteId).values()
			.stream()
			.filter(task -> task.getTask().getType().equals(type))
			.map(task -> (TaskProgress<K, R>) task)
			.toList();
	}

	@Override
	public Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> getSiteTasks(final String siteId) {
		return bySite(siteId).values();
	}

	@Override
	public <K extends TaskId> void removeTask(final K taskId) {
		switch (taskId) {
			case TaskId.SiteTaskId siteTaskId -> bySite(siteTaskId.getSiteId()).remove(taskId);
			case TaskId.GlobalTaskId __ -> globalTasks.remove(taskId);
		}
	}

	@Override
	public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
