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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.craftercms.studio.api.v2.repository.ContentWriteItem;
import org.dom4j.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.craftercms.studio.api.v2.utils.StudioUtils.createTempFile;

/**
 * Container for content items to be passed to the content life cycle controller
 */
public class LifeCycleContent implements AutoCloseable {

	private final String repoPath;
	private final LifeCycleOperation operation;
	private final String contentType;
	private final Map<String, ContentLifeCycleItem> items;

	/**
	 * Constructor for creating a new LifecycleContent object.
	 *
	 * @param repoPath    the path to the content item in the repository
	 * @param sourcePath  source path for move operations
	 * @param contentType the content type of the item
	 * @param content     the content to be written
	 * @param operation   the life cycle operation to be performed
	 */
	public LifeCycleContent(String repoPath, String sourcePath, String contentType, Path content, LifeCycleOperation operation) {
		this.items = new HashMap<>();
		this.repoPath = repoPath;
		this.operation = operation;
		this.contentType = contentType;
		this.items.put(repoPath, new ContentLifeCycleItem(repoPath, sourcePath, content));
	}

	/**
	 * Constructor for creating a new LifecycleContent object.
	 *
	 * @param repoPath    the path to the content item in the repository
	 * @param contentType the content type of the item
	 * @param content     the content to be written
	 * @param operation   the life cycle operation to be performed
	 */
	public LifeCycleContent(String repoPath, String contentType, Path content, LifeCycleOperation operation) {
		this(repoPath, null, contentType, content, operation);
	}

	/**
	 * Add a new content item to the life cycle operation.
	 *
	 * @param path    the path to the content item
	 * @param content InputStream of the content item
	 * @throws IOException if an error occurs while reading the stream or storing the content
	 */
	public void write(String path, InputStream content) throws IOException {
		Path filePath = createTempFile(path, content);
		write(path, filePath);
	}

	/**
	 * Write the content to the given path in the repository.
	 *
	 * @param path     the path to write the content to
	 * @param document the Document to write
	 * @throws IOException if an error occurs while writing the content
	 */
	public void write(String path, Document document) throws IOException {
		Path filePath = createTempFile(path, document);
		write(path, filePath);
	}

	/**
	 * Write the content to the given path in the repository.
	 *
	 * @param path     the path to write the content to
	 * @param filePath the path containing the content to write
	 */
	public void write(String path, Path filePath) {
		String normalizedPath = FilenameUtils.normalize(path);
		// Remove the temporary file if it exists
		exclude(normalizedPath);
		// Add a new entry with amended=<path is the same as the original repoPath>
		this.items.put(normalizedPath, new ContentLifeCycleItem(normalizedPath, filePath, repoPath.equals(normalizedPath)));
	}

	/**
	 * Exclude the given path from the write operation.
	 * Do not delete the content from repo if it exists.
	 *
	 * @param path the path to exclude
	 */
	private void exclude(final String path) {
		ContentLifeCycleItem removed = items.remove(path);
		if (removed != null && removed.filePath() != null) {
			FileUtils.deleteQuietly(removed.filePath().toFile());
		}
	}

	public Map<String, ContentLifeCycleItem> getItems() {
		return unmodifiableMap(items);
	}

	/**
	 * Get the content life cycle item for the given path.
	 *
	 * @param repoPath the path in the repository
	 * @return the content life cycle item, or null if it does not exist
	 */
	public ContentLifeCycleItem get(String repoPath) {
		return items.get(repoPath);
	}

	public LifeCycleOperation getOperation() {
		return operation;
	}

	public String getRepoPath() {
		return repoPath;
	}

	public String getSourcePath() {
		ContentLifeCycleItem item = items.get(repoPath);
		return item != null ? item.sourcePath() : null;
	}

	public String getContentType() {
		return contentType;
	}

	@Override
	public void close() {
		// Remove the remaining temporary files
		items.values().forEach(ContentLifeCycleItem::close);
	}

	/**
	 * Represents a content life cycle item.
	 *
	 * @param repoPath the path in the repository where the content will be stored (or deleted from)
	 * @param filePath the path to the temporary file currently storing the content to be written
	 * @param amended  true if the content has been amended by the controller, false otherwise
	 */
	public record ContentLifeCycleItem(String repoPath, String sourcePath, Path filePath,
									   boolean amended) implements ContentWriteItem {

		public ContentLifeCycleItem(String repoPath, Path filePath) {
			this(repoPath, filePath, false);
		}

		public ContentLifeCycleItem(String repoPath, Path filePath, boolean amended) {
			this(repoPath, null, filePath, amended);
		}

		public ContentLifeCycleItem(String repoPath, String sourcePath, Path filePath) {
			this(repoPath, sourcePath, filePath, false);
		}

		@Override
		public InputStream content() throws FileNotFoundException {
			if (filePath == null) {
				throw new FileNotFoundException("No content file available for " + repoPath);
			}
			return new FileInputStream(filePath.toFile());
		}

		public void close() {
			if (filePath != null) {
				FileUtils.deleteQuietly(filePath.toFile());
			}
		}
	}

	/**
	 * The life cycle operation to be performed on the content.
	 */
	public enum LifeCycleOperation {
		COPY,
		DELETE,
		DUPLICATE,
		NEW,
		RENAME,
		REVERT,
		UPDATE
	}
}
