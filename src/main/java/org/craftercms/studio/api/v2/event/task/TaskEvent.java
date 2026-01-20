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

package org.craftercms.studio.api.v2.event.task;

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.StudioEvent;
import org.craftercms.studio.api.v2.task.TaskProgress;

/**
 * Event triggered when a task is state changes
 */
public class TaskEvent extends StudioEvent implements BroadcastEvent {
	public static final String EVENT_TYPE_TASK_COMPLETED = "TASK_COMPLETED";
	public static final String EVENT_TYPE_TASK_STARTED = "TASK_STARTED";
	public static final String EVENT_TYPE_TASK_PROGRESS = "TASK_PROGRESS";

	private final TaskProgress<?, ?> progress;
	private final String eventType;

	public TaskEvent(final TaskProgress<?, ?> progress, final String eventType) {
		this.progress = progress;
		this.eventType = eventType;
	}

	/**
	 * Get the {@link TaskProgress} associated with this event
	 *
	 * @return the task progress
	 */
	public TaskProgress<?, ?> getProgress() {
		return progress;
	}

	@Override
	public String getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return """
			TaskEvent {taskId=%s, timestamp=%s, eventType=%s, progress=%s}
			""".formatted(progress.getTask().getTaskId(),
			timestamp,
			getEventType(),
			progress);
	}
}
