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

import org.springframework.util.function.ThrowingSupplier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.craftercms.studio.api.v2.utils.StudioUtils.createTempFile;

/**
 * The {@link LifecycleContentProvider} presents a facade for retrieving
 * content for content lifecycle operations. It can be used to provide content
 * either from a file path or an InputStream.
 */
public class LifecycleContentProvider implements AutoCloseable {
	private final ThrowingSupplier<Path> pathSupplier;
	private Path filePath;

	private LifecycleContentProvider(ThrowingSupplier<Path> pathSupplier) {
		this.pathSupplier = pathSupplier;
	}

	/**
	 * Gets the content as an InputStream. Callers are responsible for closing this stream.
	 *
	 * @return an InputStream for the content
	 * @throws IOException if there is any error obtaining the content
	 */
	public InputStream getContent() throws IOException {
		return new FileInputStream(filePath().toFile());
	}

	/**
	 * Gets the path to the content file.
	 *
	 * @return the path to the content file
	 * @throws IOException if there is any error obtaining the content file path
	 */
	public Path filePath() throws IOException {
		if (filePath == null) {
			try {
				filePath = pathSupplier.getWithException();
			} catch (Exception e) {
				throw new IOException("Failed to get content file path", e);
			}
		}
		return filePath;
	}

	@Override
	public void close() {
		if (filePath != null) {
			deleteQuietly(filePath.toFile());
		}
	}

	/**
	 * Creates a {@link LifecycleContentProvider} that provides content from a file path.
	 *
	 * @param pathSupplier a supplier that provides the path to the content file
	 * @return a {@link LifecycleContentProvider} that provides content from the specified path
	 */
	public static LifecycleContentProvider ofPath(ThrowingSupplier<Path> pathSupplier) {
		return new LifecycleContentProvider(pathSupplier);
	}

	/**
	 * Creates a {@link LifecycleContentProvider} that provides content from an InputStream.
	 *
	 * @param supplier a supplier that provides the InputStream for the content
	 * @return a {@link LifecycleContentProvider} that provides content from the specified InputStream
	 */
	public static LifecycleContentProvider ofStream(String fileName, ThrowingSupplier<InputStream> supplier) {
		return ofPath(() -> createTempFile(fileName, supplier.getWithException()));
	}

}
