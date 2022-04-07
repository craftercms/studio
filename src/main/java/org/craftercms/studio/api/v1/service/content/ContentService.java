/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.core.io.Resource;

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
     * @param site site identifier
     * @param path path of the content
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
     * Returns content wrapped as a {@link Resource} instance
     * @param site the site id
     * @param path the path of the content
     * @return the resource object
     * @throws ContentNotFoundException if there is no content at the given path
     * @since 3.1.1
     */
    Resource getContentAsResource(String site, String path) throws ContentNotFoundException;

    /**
     * write content
     *
     * @param site    - the project ID
     * @param path    path to content
     * @param content stream of content to write
     * @return return true if successful
     *
     * @throws ServiceLayerException general service error
     */
    boolean writeContent(String site, String path, InputStream content) throws ServiceLayerException;

    /**
     * create a folder
     *
     * @param site - the project ID
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return return the reference to the folder created
     *
     * @throws SiteNotFoundException site not found
     */
    boolean createFolder(String site, String path, String name)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * delete content at the path
     *
     * @param site - the project ID
     * @param path path to content
     * @return return true if successful
     *
     * @throws SiteNotFoundException site not found
     */
    boolean deleteContent(String site, String path, String approver) throws ServiceLayerException, UserNotFoundException;

    boolean deleteContent(String site, String path, boolean generateActivity, String approver)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * copy content fromPath to toPath
     *
     * @param site     - the project ID
     * @param fromPath the source path
     * @param toPath   the target path to copy content to
     * @return final path if successful, null otherwise
     */
    String copyContent(String site, String fromPath, String toPath) throws ServiceLayerException, UserNotFoundException;

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
     *
     * @return content item with children tree
     */
    ContentItemTO getContentItemTree(String site, String path, int depth);

    /**
     * get the content item (metadata) at a specific path
     *
     * @param site - the project ID
     * @param path - the path of the content item
     *
     * @return content item representation
     */
    ContentItemTO getContentItem(String site, String path);

    /**
     * get the content item (metadata) at a specific path
     *
     * @param site - the project ID
     * @param path - the path of the content item
     * @param depth - depth to get desendents
     *
     * @return content item representation
     */
    ContentItemTO getContentItem(String site, String path, int depth);

    /**
     * get the version history for an item
     *
     * @param site - the project ID
     * @param path - the path of the item
     *
     * @return version history
     */
    VersionTO[] getContentItemVersionHistory(String site, String path);

    /**
     * revert a version (create a new version based on an old version)
     *
     * @param site    - the project ID
     * @param path    - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @param major major version
     * @param comment comment for revert action
     *
     * @return true if success otherwise false
     *
     * @throws SiteNotFoundException site not found
     */
    boolean revertContentItem(String site, String path, String version, boolean major, String comment)
            throws ServiceLayerException, UserNotFoundException;

	/**
     * return the content for a given version
     *
     * @param site    - the project ID
     * @param path    - the path item
     * @param version - version
     *
     * @return content
     *
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
     *
     * @throws ContentNotFoundException content not found
     */
 	String getContentVersionAsString(String site, String path, String version)	throws ContentNotFoundException;

    /**
     * write content
     *
     * @param site site identifier
     * @param path path
     * @param fileName file name
     * @param contentType content type
     * @param input content
     * @param createFolders
     * 			create missing folders in path?
     * @param edit edit
     * @param unlock
     * 			unlock the content upon edit?
     * @throws ServiceLayerException general service error
     */
    void writeContent(String site, String path, String fileName, String contentType, InputStream input,
                      String createFolders, String edit, String unlock)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * write content
     *
     * @param site site identifier
     * @param path path
     * @param fileName file name
     * @param contentType content type
     * @param input content
     * @param createFolders
     * 			create missing folders in path?
     * @param edit edit
     * @param unlock
     * 			unlock the content upon edit?
     * @param skipAuditLogInsert if true do not insert audit log row, otherwise false
     * @throws ServiceLayerException general service error
     */
    void writeContent(String site, String path, String fileName, String contentType, InputStream input,
                      String createFolders, String edit, String unlock, boolean skipAuditLogInsert)
            throws ServiceLayerException, UserNotFoundException;

    void writeContentAndRename(final String site, final String path, final String targetPath, final String fileName,
                               final String contentType, final InputStream input, final String createFolders,
                               final String edit, final String unlock, final boolean createFolder)
                                throws ServiceLayerException;

    Map<String, Object> writeContentAsset(String site, String path, String assetName, InputStream in,
                                          String isImage, String allowedWidth, String allowedHeight,
                                          String allowLessSize, String draft, String unlock, String systemAsset)
                                            throws ServiceLayerException;

    /**
     * get the next available of the given content name at the given path (used for paste/duplicate)
     *
     * @param site site identifier
     * @param path path of the item
     * @return next available name that avoids a name conflict
     */
    String getNextAvailableName(String site, String path);

/* THESE ARE NOT PUBLIC METHODS, DO NOT USE THE THEM */
/* DEJAN TO CLEAN UP WHAT IS NOT TRULY PUBLIC */

    ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath);

    String getContentTypeClass(String site, String uri);

    ResultTO processContent(String id, InputStream input, boolean isXml, Map<String, String> params,
                            String contentChainForm) throws ServiceLayerException, UserNotFoundException;

    GoLiveDeleteCandidates getDeleteCandidates(String site, String uri) throws ServiceLayerException;

    void lockContent(String site, String path) throws UserNotFoundException, ServiceLayerException;

    List<DmOrderTO> getItemOrders(String site, String path) throws ContentNotFoundException;

    double reorderItems(String site, String relativePath, String before, String after, String orderName)
        throws ServiceLayerException;

    /**
     * rename a folder
     *
     * @param site - the project ID
     * @param path path to a folder to rename
     * @param name a new folder name
     * @return return the reference to the folder renamed
     *
     * @throws ServiceLayerException general service error
     */
    boolean renameFolder(String site, String path, String name) throws ServiceLayerException, UserNotFoundException;

    /**
     * Push content to remote repository
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteBranch remote branch
     * @return true if operation was successful
     *
     * @throws ServiceLayerException general service error
     * @throws InvalidRemoteUrlException invalid remote url
     * @throws AuthenticationException authentication error
     * @throws CryptoException git repository helper error
     */
    boolean pushToRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, AuthenticationException, CryptoException;

    /**
     * Pull from remote repository
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteBranch remote branch
     * @return true if operation was successful
     *
     * @throws ServiceLayerException general service error
     * @throws InvalidRemoteUrlException invalid remote url
     * @throws AuthenticationException authentication error
     * @throws CryptoException git repository helper error
     */
    boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, AuthenticationException, CryptoException;
}
