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
package org.craftercms.studio.api.v1.service.content;

import java.io.InputStream;

import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 * Content Services that other services may use
 *
 * @author russdanner
 */
public interface ContentService {

    /**
     * @return true if site has content object at path
     */
    public boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @oaram site
     */
    public InputStream getContent(String site, String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     */
    public InputStream getContent(String path);

    /**
     * get from wcm content
     *
     * @param path
     * @return document
     */
    public String getContentAsString(String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws DocumentException
     */
    Document getContentAsDocument(String path) throws DocumentException;

    /**
     * write content
     *
     * @param path    path to content
     * @param content stream of content to write
     * @return return true if successful
     */
    boolean writeContent(String path, InputStream content);

    /**
     * write content
     *
     * @param site    - the project ID
     * @param path    path to content
     * @param content stream of content to write
     * @return return true if successful
     */
    boolean writeContent(String site, String path, InputStream content);

    /**
     * create a folder
     *
     * @param site - the project ID
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return return the reference to the folder created
     */
    boolean createFolder(String site, String path, String name);

    /**
     * delete content at the path
     *
     * @param site - the project ID
     * @param path path to content
     * @return return true if successful
     */
    boolean deleteContent(String site, String path);

    /**
     * copy content fromPath to toPath
     *
     * @param site     - the project ID
     * @param fromPath the source path
     * @param toPath   the target path to copy content to
     * @return true if successful
     */
    public boolean copyContent(String site, String fromPath, String toPath);

    /**
     * move content fromPath to toPath
     *
     * @param site     - the project ID
     * @param fromPath the source path
     * @param toPath   the target path to copy content to
     * @return true if successful
     */
    public boolean moveContent(String site, String fromPath, String toPath);

    /**
     * get the tree of content items (metadata) beginning at a root
     *
     * @param site - the project ID
     * @param path - the path to root at
     */
    public ContentItemTO getContentItemTree(String site, String path, int depth);

    /**
     * get the content item (metadata) at a specific path
     *
     * @param site - the project ID
     * @param path - the path of the content item
     */
    public ContentItemTO getContentItem(String site, String path);

    /**
     * get the version history for an item
     *
     * @param site - the project ID
     * @param path - the path of the item
     */
    VersionTO[] getContentItemVersionHistory(String site, String path);

    /**
     * revert a version (create a new version based on an old version)
     *
     * @param site    - the project ID
     * @param path    - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    boolean revertContentItem(String site, String path, String version, boolean major, String comment);


    ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath);

    String expandRelativeSitePath(String site, String relativePath);

    String getRelativeSitePath(String site, String fullPath);
}
