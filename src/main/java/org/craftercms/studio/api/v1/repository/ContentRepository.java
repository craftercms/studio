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
package org.craftercms.studio.api.v1.repository;


import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.VersionTO;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend
 * Store must go through this interface.
 * @author russdanner
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return document
     */
    InputStream getContent(String site, String path) throws ContentNotFoundException;

    /**
     * write content
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @param content stream of content to write
     * @return Commit Id if successful, null otherwise
     */
    String writeContent(String site, String path, InputStream content) throws ServiceException;

    /**
     * create a folder
     *
     * @param site site id where the operation will be executed
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return Commit Id if successful, null otherwise
     */
    String createFolder(String site, String path, String name);

    /**
     * delete content
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return Commit ID if successful, null otherwise
     */
    String deleteContent(String site, String path);

    /**
     * move content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath source content
     * @param toPath target path
     * @return Commit ID if successful, null otherwise
     */
    String moveContent(String site, String fromPath, String toPath);

    /**
     * move content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath source content
     * @param toPath target path
     * @param newName new file name for rename
     * @return Commit ID if successful, empty string otherwise
     */
    // TODO: SJ: Should refactor to be from path to path without the newName param
    String moveContent(String site, String fromPath, String toPath, String newName);

    /**
     * copy content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath paths to content
     * @param toPath target path
     * @return Commit ID if successful, empty string otherwise
     */
    String copyContent(String site, String fromPath, String toPath);

    /**
     * get immediate children for path
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return a list of children
     */
    RepositoryItem[] getContentChildren(String site, String path);

    /**
     * get the version history for an item
     *
     * @param site site id where the operation will be executed
     * @param site - the project ID
     * @param path - the path of the item
     * @return a list of versions
     */
    VersionTO[] getContentVersionHistory(String site, String path);

    /**
     * create a version
     *
     * @param site site id where the operation will be executed
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, boolean majorVersion);

    /**
     * create a version
     *
     * @param site site id where the operation will be executed
     * @param path location of content
     * @param comment version history comment
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, String comment, boolean majorVersion);

    /**
     * revert a version (create a new version based on an old version)
     *
     * @param site site id where the operation will be executed
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @return Commit ID if successful, empty string otherwise
     */
    String revertContent(String site, String path, String version, boolean major, String comment);

    /**
     * return a specific version of the content
     *
     * @param site site id where the operation will be executed
     * @param path path of the content
     * @param version version to return
     * @return input stream
     */
    InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException;
    /**
     * get the modified date for an oject at path
     * NOTE: THis should be move to a get metadata of some sort that returns a structure with additional data
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return document
     */
    Date getModifiedDate(String site, String path);

    /**
     * lock an item
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void lockItem(String site, String path); // TODO: SJ: Change to have a return

    /**
     * unlock an item
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void unLockItem(String site, String path); // TODO: SJ: Change to have a return

    /**
     * Create a new site based on a blueprint
     *
     * @param blueprintName
     * @param siteId
     * @return true if successful, false otherwise
     */
    boolean createSiteFromBlueprint(String blueprintName, String siteId);

    /**
     * Deletes an existing site.
     *
     * @param siteId site to delete
     * @return true if successful, false otherwise
     */
    boolean deleteSite(String siteId);

    /**
     * Publish content to specified environment.
     *
     * @param commitIds
     * @param environment
     * @param author
     * @param comment
     */
    void publish(String site, List<String> commitIds, String environment, String author, String comment);
}