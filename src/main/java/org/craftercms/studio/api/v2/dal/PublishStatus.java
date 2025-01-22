/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal;

import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.model.task.PublishTask;

public class PublishStatus {

	// TODO: remove this and publishing_status column when implementing the
	// UM for the publisher refactor
	public static final String READY = "ready";

	private boolean enabled;
	private boolean published;
	private TaskProgress<PublishTask.PublishTaskId, Long> currentTask;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public void setCurrentTask(final TaskProgress<PublishTask.PublishTaskId, Long> currentTask) {
		this.currentTask = currentTask;
	}

	public TaskProgress<PublishTask.PublishTaskId, Long> getCurrentTask() {
		return currentTask;
	}
}
