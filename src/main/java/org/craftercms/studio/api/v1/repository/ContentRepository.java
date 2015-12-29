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
package org.craftercms.studio.api.v1.repository;


import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.to.VersionTO;

import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend 
 * Store must go through this interface.
 * @author russdanner
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     */
    InputStream getContent(String path) throws ContentNotFoundException;

    /**
     * write content
     * @param path path to content
     * @param content stream of content to write
     * @return true if successful
     */
    boolean writeContent(String path, InputStream content);

    /**
     * create a folder
     *
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return true if successful
     */
    boolean createFolder(String path, String name);

    /**
     * delete content
     * @param path path to content
     */
    boolean deleteContent(String path);

    /**
     * move content from PathA to pathB
     *
     * @param fromPath source content
     * @param toPath target path
     * @return true if successful
     */
    boolean moveContent(String fromPath, String toPath);

    /**
     * move content from PathA to pathB
     *
     * @param fromPath source content
     * @param toPath target path
     * @param newName new file name for rename
     * @return true if successful
     */
    boolean moveContent(String fromPath, String toPath, String newName);

    /**
     * copy content from PathA to pathB
     *
     * @param fromPath paths to content
     * @param toPath target path
     * @return true if successful
     */
    boolean copyContent(String fromPath, String toPath);

    /**
     * get immediate children for path
     * @param path path to content
     * @return a list of children
     */
    RepositoryItem[] getContentChildren(String path);

    /** 
     * get the version history for an item
     * @param site - the project ID
     * @param path - the path of the item
     * @return a list of versions
     */
    VersionTO[] getContentVersionHistory(String path);

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String path, boolean majorVersion);

    /**
     * create a version
     * @param path location of content
     * @param comment version history comment
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String path, String comment, boolean majorVersion);

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @return true if successful
     */
    boolean revertContent(String path, String version, boolean major, String comment);

    /** 
     * return a specific version of the content
     * @param path path of the content
     * @param version version to return
     * @return input stream
     */
    InputStream getContentVersion(String path, String version) throws ContentNotFoundException;  
    /**
     * get the modified date for an oject at path
     * NOTE: THis should be move to a get metadata of some sort that returns a structure with additional data
     * @param path
     * @return document
     */
    Date getModifiedDate(String path);

    /**
     * lock an item
     * NOTE: site will be removed from this interface
     * @param path
     */
    void lockItem(String site, String path);

    /**
     * unlock an item
     * NOTE: site will be removed from this interface
     * @param path
     */
    void unLockItem(String site, String path);







}
