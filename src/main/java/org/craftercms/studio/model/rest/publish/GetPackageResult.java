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

package org.craftercms.studio.model.rest.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.task.PublishTask;

/**
 * Represents the response of a get package operation.
 * It includes the package and the progress of
 * the operation (will be null if the package is not being published at the time).
 */
public class GetPackageResult extends Result {
	private final PublishPackage publishPackage;
	private final TaskProgress<PublishTask.PublishTaskId, Long> progress;

	public GetPackageResult(TaskProgress<PublishTask.PublishTaskId, Long> progress, PublishPackage publishPackage) {
		this.progress = progress;
		this.publishPackage = publishPackage;
	}

	public TaskProgress<PublishTask.PublishTaskId, Long> getProgress() {
		return progress;
	}

	@JsonProperty("package")
	public PublishPackage getPublishPackage() {
		return publishPackage;
	}
}

