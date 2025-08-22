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

import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.InputStream;

/**
 * Interface for loading content from the repository.
 */
public interface ContentLoader {

	/**
	 * Get the content at the given path in the given site.
	 * Note: The caller is responsible for closing this input stream
	 *
	 * @param siteId the site id
	 * @param path   the path to the content
	 * @return the content as an InputStream,
	 * or null if the content does not exist
	 */
	InputStream getContentRaw(String siteId, String path);

	/**
	 * Get the content as a Document.
	 *
	 * @param siteId the site id
	 * @param path   the path to the content
	 * @return the Document represented by the content item
	 * @throws DocumentException if the content cannot be converted to a Document
	 */
	default Document getContent(String siteId, String path) throws DocumentException {
		return ContentUtils.convertStreamToXml(getContentRaw(siteId, path));
	}
}
