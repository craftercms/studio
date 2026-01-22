/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Content Services that other services may use
 *
 * @author russdanner
 */
public interface ContentService {

	/**
	 * Check if content exists
	 *
	 * @param site site identifier
	 * @param path path of the content
	 * @return true if site has content object at path
	 */
	boolean contentExists(String site, String path);

	/**
	 * This is a faster, but less accurate, version of contentExists. This prioritizes
	 * performance over checking the actual underlying repository if the content is actually in the store
	 * or we simply hold a reference to the object in the actual store.
	 *
	 * @return true if site has content object at path
	 */
	boolean shallowContentExists(String site, String path);

	/**
	 * get document from wcm content
	 *
	 * @param site site identifier
	 * @param path path of the content
	 * @return document
	 */
	InputStream getContent(String site, String path) throws ContentNotFoundException;

	/**
	 * get file size
	 *
	 * @param site site id where the operation will be executed
	 * @param path path to content
	 * @return Size in bytes
	 */
	long getContentSize(String site, String path);

	/**
	 * get content as string from repository
	 *
	 * @param site site identifier
	 * @param path path of the content
	 * @return document
	 */
	String getContentAsString(String site, String path);

	/**
	 * get content as string from repository
	 *
	 * @param site     site identifier
	 * @param path     path of the content
	 * @param encoding file encoding
	 * @return document
	 */
	String getContentAsString(String site, String path, String encoding);

	/**
	 * get document from wcm content
	 *
	 * @param site site identifier
	 * @param path content path
	 * @return document
	 * @throws DocumentException XML document error
	 */
	Document getContentAsDocument(String site, String path) throws DocumentException;

	/**
	 * get the tree of content items (metadata) beginning at a root
	 *
	 * @param site - the project ID
	 * @param path - the path to root at
	 * @return content item with children tree
	 */
	ContentItemTO getContentItemTree(String site, String path, int depth);

	/**
	 * get the content item (metadata) at a specific path
	 *
	 * @param site - the project ID
	 * @param path - the path of the content item
	 * @return content item representation
	 */
	ContentItemTO getContentItem(String site, String path);

	/**
	 * get the content item (metadata) at a specific path
	 *
	 * @param site  - the project ID
	 * @param path  - the path of the content item
	 * @param depth - depth to get desendents
	 * @return content item representation
	 */
	ContentItemTO getContentItem(String site, String path, int depth);

	/**
	 * Retrieves the content type for a given path
	 *
	 * @param site the site id
	 * @param path the content path
	 * @return content type
	 * @throws DocumentException on failure to retrieve the content type from xml (when applicable)
	 */
	String getItemContentType(String site, String path) throws DocumentException, SiteNotFoundException;

	/**
	 * return the content for a given version
	 *
	 * @param site    - the project ID
	 * @param path    - the path item
	 * @param version - version
	 * @return content
	 * @throws ContentNotFoundException content not found
	 */
	Optional<Resource> getContentVersion(String site, String path, String version) throws ContentNotFoundException;

	/**
	 * return the content for a given version
	 *
	 * @param site    - the project ID
	 * @param path    - the path item
	 * @param version - version
	 * @return version number
	 * @throws ContentNotFoundException content not found
	 */
	String getContentVersionAsString(String site, String path, String version) throws ContentNotFoundException;

	/* THESE ARE NOT PUBLIC METHODS, DO NOT USE THE THEM */
	/* DEJAN TO CLEAN UP WHAT IS NOT TRULY PUBLIC */

	ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath) throws SiteNotFoundException;

	String getContentTypeClass(String site, String uri) throws SiteNotFoundException;

	List<DmOrderTO> getItemOrders(String site, String path) throws ContentNotFoundException;

	double reorderItems(String site, String relativePath, String before, String after, String orderName)
		throws ServiceLayerException;

}
