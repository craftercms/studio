/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Common interface for content repository operations
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return true if site has content object at path
     */
    boolean contentExists(String site, String path);

    /**
     * get file size
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return Size in bytes
     */
    long getContentSize(String site, String path);

    /**
     * Checks if a content exists at a given path and throw an exception if it does not.
     * @param site id of the site
     * @param path the content path
     * @throws ServiceLayerException if no content is found at the given path
     */
    void checkContentExists(String site, String path) throws ServiceLayerException;

    /**
     * This is a faster, but less accurate, version of contentExists. This prioritizes
     * performance over checking the actual underlying repository if the content is actually in the store
     * or we simply hold a reference to the object in the actual store.
     *
     * @return true if site has content object at path
     */
    boolean shallowContentExists(String site, String path);
}
