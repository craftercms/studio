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

package org.craftercms.studio.model.task;

import org.craftercms.studio.api.v2.task.TaskId;
import org.springframework.lang.NonNull;

/**
 * Base class for tasks
 *
 * @param <K> type of the task id
 */
public abstract class Task<K extends TaskId> {
	private final String type;
	private final K taskId;

	public Task(@NonNull final String type, K taskId) {
		this.type = type;
		this.taskId = taskId;
	}

	@NonNull
	public String getType() {
		return type;
	}

	/**
	 * Get the task id
	 *
	 * @return the task id
	 */
	public K getTaskId() {
		return taskId;
	}
}
