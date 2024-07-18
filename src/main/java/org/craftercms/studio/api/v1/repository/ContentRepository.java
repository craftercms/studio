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

package org.craftercms.studio.api.v1.repository;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;

import java.io.InputStream;
import java.util.Map;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend
 * Store must go through this interface.
 *
 * @author russdanner
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param site site id where the operation will be executed
     * @param path path to check if content exists
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
     * @param site site id where the operation will be executed
     * @param path path of the content
     * @return document
     *
     * @throws ContentNotFoundException content not found at given path
     */
    default InputStream getContent(String site, String path) throws ContentNotFoundException {
        return getContent(site, path, false);
    }

    /**
     * Get content from the repository
     * @param site the site id
     * @param path the path of the content
     * @param shallow if true, it will load the file from disk directly, instead of retrieving it from git repository
     * @return InputStream to read the content
     * @throws ContentNotFoundException if the content is not found
     */
    InputStream getContent(String site, String path, boolean shallow) throws ContentNotFoundException;

    /**
     * write content
     *
     * @param site    site id where the operation will be executed
     * @param path    path to content
     * @param content stream of content to write
     * @return Commit Id if successful, null otherwise
     *
     * @throws ServiceLayerException if error happens during write
     */
    String writeContent(String site, String path, InputStream content) throws ServiceLayerException;

    /**
     * create a folder
     *
     * @param site site id where the operation will be executed
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return Commit Id if successful, null otherwise
     */
    String createFolder(String site, String path, String name) throws ServiceLayerException;

    /**
     * delete content
     *
     * @param site     site id where the operation will be executed
     * @param path     path to content
     * @param approver user that approves delete content
     * @return Commit ID if successful, null otherwise
     */
    String deleteContent(String site, String path, String approver) throws ServiceLayerException;

    /**
     * move content from PathA to pathB
     *
     * @param site     site id where the operation will be executed
     * @param fromPath source content
     * @param toPath   target path
     * @return Commit ID if successful, null otherwise
     */
    default String moveContent(String site, String fromPath, String toPath) throws ServiceLayerException {
        return moveContent(site, fromPath, toPath, null);
    }

    /**
     * move content from PathA to pathB
     *
     * @param site     site id where the operation will be executed
     * @param fromPath source content
     * @param toPath   target path
     * @param newName  new file name for rename
     * @return Commit ID if successful, empty string otherwise
     */
    // TODO: SJ: Should refactor to be from path to path without the newName param
    String moveContent(String site, String fromPath, String toPath, String newName) throws ServiceLayerException;

}
