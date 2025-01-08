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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link TaskManager} implementation
 */
public class TaskManagerImpl implements TaskManager, ApplicationContextAware {

    private static final String GLOBAL_SITE_ID = "";
    // By site and then by task id
    private final Map<String, Map<Object, TaskProgress<? extends TaskId, ?>>> tasks;
    private ApplicationContext applicationContext;

    public TaskManagerImpl() {
        tasks = new ConcurrentHashMap<>();
    }

    /**
     * Get the map of tasks for a site
     */
    private Map<Object, TaskProgress<? extends TaskId, ?>> bySite(final String site) {
        return tasks.computeIfAbsent(site, k -> new ConcurrentHashMap<>());
    }

    @Override
    public <K extends TaskId, R> TaskProgress<K, R> registerTask(final Task<K> task) {
        TaskProgressImpl<K, R> progress = applicationContext.getBean(TaskProgressImpl.class, task, this);
        bySite(getTaskSiteId(task.getTaskId())).put(task.getTaskId(), progress);
        return progress;
    }

    @Override
    public <K extends TaskId, R> TaskProgress<K, R> getTask(final K taskId) {
        return (TaskProgress<K, R>) bySite(getTaskSiteId(taskId)).get(taskId);
    }

    @Override
    public <R> Collection<TaskProgress<TaskId.SiteTaskId, R>> getSiteTasksByType(final String siteId, final String type) {
        return bySite(siteId).values()
                .stream()
                .filter(task -> task.getTask().getType().equals(type))
                .map(task -> (TaskProgress<TaskId.SiteTaskId, R>) task)
                .toList();
    }

    @Override
    public Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> getSiteTasks(final String siteId) {
        Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> siteTasks = new LinkedList<>();
        bySite(siteId).values()
                .stream()
                .forEach(task -> siteTasks.add((TaskProgress<? extends TaskId.SiteTaskId, ?>) task));

        return siteTasks;
    }

    @Override
    public <K extends TaskId> void removeTask(final K taskId) {
        bySite(getTaskSiteId(taskId)).remove(taskId);
    }

    /**
     * Get the site id from a {@link TaskId}, or the global site id if it is a global task
     */
    private String getTaskSiteId(final TaskId taskId) {
        return switch (taskId) {
            case TaskId.SiteTaskId siteTaskId -> siteTaskId.getSiteId();
            case TaskId.GlobalTaskId __ -> GLOBAL_SITE_ID;
        };
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
