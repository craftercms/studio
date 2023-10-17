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

package org.craftercms.studio.api.v2.service.content.internal;

import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface ContentServiceInternal {

    /**
     * Check the existent of a content path
     * @param siteId site identifier
     * @param path content path
     * @return true if the content exists, false otherwise
     */
    boolean contentExists(String siteId, String path);

    /**
     * Get subtree items for given path.
     *
     * @param siteId site identifier
     * @param path   path to get subtree items for
     * @return list of paths of subtree items
     */
    List<String> getSubtreeItems(String siteId, String path);

    /**
     * Get subtree items for given paths.
     *
     * @param siteId site identifier
     * @param path   list of paths to get subtree items for
     * @return list of paths of subtree items
     */
    List<String> getSubtreeItems(String siteId, List<String> path);

    /**
     * Get list of children for given path
     *
     * @param siteId       site identifier
     * @param path         item path to children for
     * @param locale       filter children by locale
     * @param keyword      filter children by keyword
     * @param systemTypes  filter children by type
     * @param excludes     exclude items by path
     * @param sortStrategy sort order
     * @param order        ascending or descending
     * @param offset       offset of the first child in the result
     * @param limit        number of children to return
     * @return list of children
     */
    GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String keyword,
                                        List<String> systemTypes, List<String> excludes, String sortStrategy,
                                        String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException;

    Item getItem(String siteId, String path, boolean flatten);

    /**
     * Get content size
     *
     * @param siteId site identifier
     * @param path   content path
     * @return size in bytes
     */
    long getContentSize(String siteId, String path);

    /**
     * Get detailed for given path
     *
     * @param siteId        site identifier
     * @param path          item for path
     * @param preferContent if true return content item if available
     * @return detailed item
     */
    DetailedItem getItemByPath(String siteId, String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get sandbox items for given list of paths
     *
     * @param siteId        site identifier
     * @param paths         list of paths to get sandbox items
     * @param preferContent if true return content items if available
     * @return list of sandbox items
     */
    List<SandboxItem> getSandboxItemsByPath(String siteId, List<String> paths, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get sandbox items for given list of paths
     *
     * @param siteId        site identifier
     * @param ids           list of ids to get sandbox items
     * @param sortFields
     * @param preferContent if true return content items if available
     * @return list of sandbox items
     */
    List<SandboxItem> getSandboxItemsById(String siteId, List<Long> ids, List<SortField> sortFields, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Check if item is editable
     *
     * @param itemPath     item path
     * @param itemMimeType item mime type
     * @return true if item is editable
     */
    boolean isEditable(String itemPath, String itemMimeType);

    /**
     * Lock item by path for given site
     *
     * @param siteId site identifier
     * @param path   item path to lock
     */
    void itemLockByPath(String siteId, String path);

    /**
     * Unlock item by path for given site
     *
     * @param siteId site identifier
     * @param path   item path
     */
    void itemUnlockByPath(String siteId, String path);

    /**
     * Get content for commit id
     *
     * @param siteId   site identifier
     * @param path     path of the content
     * @param commitId commit id of the content version
     * @return the resource if available
     */
    Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
            throws ContentNotFoundException;

    /**
     * Get a list of items by state and system types.
     * @param siteId site identifier
     * @param statesBitMap mask of the states to filter by
     * @param systemTypes list of system types to filter by
     * @param sortFields list of sort fields
     * @param offset number of items to skip
     * @param limit number of items to return
     * @return list of items
     * @throws UserNotFoundException
     * @throws ServiceLayerException
     */
    List<DetailedItem> getItemsByStates(String siteId, long statesBitMap,
                                        List<String> systemTypes, List<SortField> sortFields,
                                        int offset, int limit) throws UserNotFoundException, ServiceLayerException;

    /**
     * Get the version history for a given content item.
     *
     * @param siteId the site id
     * @param path   the content path
     * @return the list of versions
     * @throws ServiceLayerException if an error occurs while create the list of {@link ItemVersion}s
     */
    List<ItemVersion> getContentVersionHistory(String siteId, String path) throws ServiceLayerException;

    /*
     * Create new folder
     * @param siteId site identifier
     * @param path parent path to create new folder into
     * @param name new folder name
     * @return true if success, otherwise false
     *
     * @throws UserNotFoundException user not found error
     * @throws ServiceLayerException general service error
     */
    boolean createFolder(String siteId, String path, String name) throws ServiceLayerException, UserNotFoundException;
}
