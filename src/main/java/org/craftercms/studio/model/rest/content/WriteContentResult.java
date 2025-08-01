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

package org.craftercms.studio.model.rest.content;

import org.craftercms.studio.api.v2.content.LifecycleContent.LifecycleOperation;

import java.util.List;

/**
 * Result of a write content operation.
 */
public class WriteContentResult {
	private final List<WriteContentResultItem> items;
	private final String commitId;

	public WriteContentResult(String commitId, final List<WriteContentResultItem> items) {
		this.commitId = commitId;
		this.items = items;
	}

	public String getCommitId() {
		return commitId;
	}

	public List<WriteContentResultItem> getItems() {
		return items;
	}

	/**
	 * Each of the items in the result of a write content operation.
	 *
	 * @param path      the path of the content item
	 * @param operation the operation performed on the content item
	 * @param amended   true if the content was updated (lifecycle controller or asset pipeline),
	 *                  false otherwise
	 */
	public record WriteContentResultItem(String path,
										 LifecycleOperation operation,
										 boolean amended) {
	}
}
