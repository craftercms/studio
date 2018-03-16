/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
import java.util.List;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.to.*;
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
    boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     *
     * @param site
     * @param path
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
     * get from wcm content
     *
     * @param path
     * @return document
     */
    String getContentAsString(String site, String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws DocumentException
     */
    Document getContentAsDocument(String site, String path) throws DocumentException;

    /**
     * write content
     *
     * @param site    - the project ID
     * @param path    path to content
     * @param content stream of content to write
     * @return return true if successful
     */
    boolean writeContent(String site, String path, InputStream content) throws ServiceException;

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
    boolean deleteContent(String site, String path, String approver);

    boolean deleteContent(String site, String path, boolean generateActivity, String approver);

    /**
     * copy content fromPath to toPath
     *
     * @param site     - the project ID
     * @param fromPath the source path
     * @param toPath   the target path to copy content to
     * @return final path if successful, null otherwise
     */
    String copyContent(String site, String fromPath, String toPath);

    /**
     * move content fromPath to toPath
     *
     * @param site     - the project ID
     * @param fromPath the source path
     * @param toPath   the target path to copy content to
     * @return final path if successful, null otherwise
     */
    String moveContent(String site, String fromPath, String toPath);

    /**
     * get the tree of content items (metadata) beginning at a root
     *
     * @param site - the project ID
     * @param path - the path to root at
     */
    ContentItemTO getContentItemTree(String site, String path, int depth);

    /**
     * get the content item (metadata) at a specific path
     *
     * @param site - the project ID
     * @param path - the path of the content item
     */
    ContentItemTO getContentItem(String site, String path);

    /**
     * get the content item (metadata) at a specific path
     *
     * @param site - the project ID
     * @param path - the path of the content item
     * @param depth - depth to get desendents
     */
    ContentItemTO getContentItem(String site, String path, int depth);

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

	/**
     * return the content for a given version
     *
     * @param site    - the project ID
     * @param path    - the path item
     * @param version - version
     */
 	InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException;

	/**
     * return the content for a given version
     *
     * @param site    - the project ID
     * @param path    - the path item
     * @param version - version
     */
 	String getContentVersionAsString(String site, String path, String version)	throws ContentNotFoundException;

    /**
     * write content
     *
     * @param site
     * @param path
     * @param fileName
     * @param contentType
     * @param input
     * @param createFolders
     * 			create missing folders in path?
     * @param edit
     * @param unlock
     * 			unlock the content upon edit?
     * @throws ServiceException
     */
    void writeContent(String site, String path, String fileName, String contentType, InputStream input,
                      String createFolders, String edit, String unlock) throws ServiceException;

    void writeContentAndRename(final String site, final String path, final String targetPath, final String fileName, final String contentType, final InputStream input,
                               final String createFolders, final  String edit, final String unlock, final boolean createFolder) throws ServiceException;

    Map<String, Object> writeContentAsset(String site, String path, String assetName, InputStream in,
                                          String isImage, String allowedWidth, String allowedHeight, String allowLessSize, String draft, String unlock, String systemAsset) throws ServiceException;

    /**
     * get the next available of the given content name at the given path (used for paste/duplicate)
     *
     * @param site
     * @param path
     * @return next available name that avoids a name conflict
     */
    String getNextAvailableName(String site, String path);

/* THESE ARE NOT PUBLIC METHODS, DO NOT USE THE THEM */
/* DEJAN TO CLEAN UP WHAT IS NOT TRULY PUBLIC */

    ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath);

    String getContentTypeClass(String site, String uri);

    ResultTO processContent(String id, InputStream input, boolean isXml, Map<String, String> params, String contentChainForm) throws ServiceException;

    GoLiveDeleteCandidates getDeleteCandidates(String site, String uri) throws ServiceException;

    void lockContent(String site, String path);

    void unLockContent(String site, String path);

    List<DmOrderTO> getItemOrders(String site, String path) throws ContentNotFoundException;

    double reorderItems(String site, String relativePath, String before, String after, String orderName) throws ServiceException;

    /**
     * rename a folder
     *
     * @param site - the project ID
     * @param path path to a folder to rename
     * @param name a new folder name
     * @return return the reference to the folder renamed
     */
    boolean renameFolder(String site, String path, String name) throws ServiceException;

    /**
     * Add remote repository for site content repository
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteUrl remote url
     * @param authenticationType authentication type
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @param remoteToken remote token
     * @param remotePrivateKey remote private key
     * @return true if operation was successful
     */
    boolean addRemote(String siteId, String remoteName, String remoteUrl, String authenticationType, String remoteUsername, String remotePassword, String remoteToken, String remotePrivateKey) throws InvalidRemoteUrlException, ServiceException;

    /**
     * Remove remote with given name for site
     * @param siteId site identifier
     * @param remoteName remote name
     * @return true if operation was successful
     */
    boolean removeRemote(String siteId, String remoteName) throws SiteNotFoundException;
}
