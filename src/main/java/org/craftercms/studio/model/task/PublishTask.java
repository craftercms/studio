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

/**
 * Task to publish a package
 */
public class PublishTask extends SiteTask<PublishTask.PublishTaskId> {
	public static final String PUBLISH_TASK_TYPE = "publish";

	public PublishTask(String siteId, long packageId) {
		super(PUBLISH_TASK_TYPE, getTaskId(siteId, packageId));
	}

	/**
	 * Create a PublishTaskId for the given site and package
	 *
	 * @param siteId    the site id
	 * @param packageId the package id
	 * @return the task id
	 */
	public static PublishTaskId getTaskId(final String siteId, final long packageId) {
		return new PublishTaskId(siteId, packageId);
	}

	public record PublishTaskId(String siteId, long packageId) implements TaskId.SiteTaskId {
		@Override
		public String getSiteId() {
			return siteId;
		}
	}

	@Override
	public String toString() {
		return """
			PublishTask {id="%s", type="%s"}"""
			.formatted(getTaskId(), getType());
	}
}
