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

package org.craftercms.studio.api.v2.content;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Container for content items to be passed to the content lifecycle controller
 */
public class LifecycleContent {

	private final String repoPath;
	private final LifeCycleOperation operation;
	private final String contentType;
	private final Map<String, ContentLifecycleItem> items;

	public LifecycleContent(String repoPath, String contentType, Path content, LifeCycleOperation operation) {
		this.items = new HashMap<>();
		this.repoPath = repoPath;
		this.operation = operation;
		this.contentType = contentType;
		this.items.put(repoPath, new ContentLifecycleItem(repoPath, content));
	}

	public void write(String path, Path content) {
		// TODO: Implement
		// Remove the temporary file if it exists
		// Add a new entry with amended=<path is the same as the original repoPath>
		this.items.put(path, new ContentLifecycleItem(path, content, repoPath.equals(path)));
	}

	/**
	 * Exclude the given path from the write operation.
	 * Do not delete the content from repo if it exists.
	 *
	 * @param path the path to exclude
	 */
	public void exclude(final String path) {
		// TODO: Implement
		// Remove the temporary file if it exists
		items.remove(path);
	}

	/**
	 * Mark the given path to be deleted from the repository.
	 *
	 * @param path the path to delete
	 */
	public void delete(final String path) {
		// TODO: Implement
		// Remove the temporary file if it exists
		// Add a new entry with delete=true
		this.items.put(path, new ContentLifecycleItem(path, null, false, true));

	}

	public Map<String, ContentLifecycleItem> getItems() {
		return unmodifiableMap(items);
	}

	public LifeCycleOperation getOperation() {
		return operation;
	}

	public String getRepoPath() {
		return repoPath;
	}

	public String getContentType() {
		return contentType;
	}

	/**
	 * Represents a content lifecycle item.
	 *
	 * @param repoPath the path in the repository where the content will be stored (or deleted from)
	 * @param filePath the path to the temporary file currently storing the content to be written
	 */
	public record ContentLifecycleItem(String repoPath, Path filePath, boolean amended, boolean delete) {

		public ContentLifecycleItem(String repoPath, Path filePath) {
			this(repoPath, filePath, false, false);
		}

		public ContentLifecycleItem(String repoPath, Path filePath, boolean amended) {
			this(repoPath, filePath, amended, false);
		}
	}

	/**
	 * The lifecycle operation to be performed on the content.
	 */
	public enum LifeCycleOperation {
		COPY,
		DELETE,
		DUPLICATE,
		NEW,
		RENAME,
		REVERT,
		UPDATE;
	}
}
