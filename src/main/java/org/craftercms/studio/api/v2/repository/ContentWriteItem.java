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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an item to be written to the repository.
 */
public interface ContentWriteItem {
	/**
	 * The path to the item in the repository.
	 *
	 * @return the path to the item in the repository
	 */
	String repoPath();

	/**
	 * The actual content to be written to the repository.
	 *
	 * @return stream to read the content
	 * @throws IOException if an error occurs while reading the content
	 */
	InputStream content() throws IOException;

	/**
	 * Get the content as a Document.
	 *
	 * @return the content parsed as a {@link Document}
	 * @throws DocumentException if the content cannot be converted to a Document
	 */
	default Document contentAsDocument() throws DocumentException, IOException {
		try (InputStream in = content()) {
			return ContentUtils.convertStreamToXml(in);
		}
	}

}
