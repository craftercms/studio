/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.api.service.content;

import java.io.InputStream;
import java.net.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.craftercms.cstudio.api.to.ContentItemTO;

/**
 * Content Services that other services may use
 * @author russdanner
 */
public interface ContentService  {


    /**
     * @return true if site has content object at path
     */
    public boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    public InputStream getContent(String path);

    /**
     * get from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    public String getContentAsString(String path);

    /**
     * get document from wcm content
     * @param path
     * @return document
     * @throws ServiceException
     */
    Document getContentAsDocument(String path) throws DocumentException;

	// /**
	//  * get the tree of content items (metadata) beginning at a root
	//  * @param site - the project ID
	//  * @param path - the path to root at
	//  */
	// public getContentItemTree(String site, String path);

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
	 */
	public ContentItemTO getContentItem(String site,  String path);

// 	/** 
// 	 * get the version history for an item
// 	 * @param site - the project ID
// 	 * @param path - the path of the item 
// 	 */
// 	String void getContentItemVersionHistory(site, path);

}
