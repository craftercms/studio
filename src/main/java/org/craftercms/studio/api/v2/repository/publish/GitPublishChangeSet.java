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

package org.craftercms.studio.api.v2.repository.publish;

import org.craftercms.studio.api.v2.repository.PublishItemTO;
import org.springframework.util.ObjectUtils;

import java.util.Collection;

/**
 * Store the result of a publish operation
 *
 * @param successfulItems the paths that were updated
 * @param failedItems     the paths that failed to publish, mapped to the error message
 * @param <T>             the actual type of the {@link PublishItemTO} objects
 */
public record GitPublishChangeSet<T extends PublishItemTO>(String commitId,
													Collection<T> successfulItems,
													Collection<T> failedItems) {

	/**
	 * Check if there are successfully published changes
	 *
	 * @return true if the package had successful changes and a commit was created, false otherwise
	 */
	public boolean completed() {
		return !ObjectUtils.isEmpty(commitId);
	}

	/**
	 * Check if there are failed items
	 *
	 * @return true if failed items list contains items, false otherwise
	 */
	public boolean hasFailedItems() {
		return !ObjectUtils.isEmpty(failedItems);
	}
}
