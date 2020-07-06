/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.item.internal;

import org.craftercms.studio.api.v2.dal.Item;

import java.util.List;

public interface ItemServiceInternal {

    /**
     * Insert record for item if does not exist, otherwise update it
     *
     * @param siteId site identifier
     * @param item item to add or update
     */
    void upsertEntry(String siteId, Item item);

    /**
     * Insert records for list of item if they do not exist, otherwise update them
     *
     * @param siteId site identifier
     * @param items list of items to add or update
     */
    void upsertEntries(String siteId, List<Item> items);

    /**
     * Update parent ids for items in subtree for given root path
     * @param siteId site identifier
     * @param rootPath root path
     */
    void updateParentIds(String siteId, String rootPath);

    /**
     * Get item by given id
     * @param id item id
     * @return item
     */
    Item getItem(long id);

    /**
     * Get item fir given site and path
     * @param siteId site identifier
     * @param path item path
     * @return item
     */
    Item getItem(String siteId, String path);

    /**
     * Update item
     * @param item item to update
     */
    void updateItem(Item item);

    /**
     * Delete item
     * @param itemId item id
     */
    void deleteItem(long itemId);

    /**
     * Delete item
     * @param siteId siteIdentifier
     * @param path path of item to be deleted
     */
    void deleteItem(String siteId, String path);

    /**
     * Set system processing for item
     * @param siteId site identifier
     * @param path path of the item
     * @param isSystemProcessing true if item is being processed by system, otherwise false
     */
    void setSystemProcessing(String siteId, String path, boolean isSystemProcessing);

    /**
     * Set system processing for items
     * @param siteId site identifier
     * @param paths paths of items
     * @param isSystemProcessing true if item is being processed by system, otherwise false
     */
    void setSystemProcessingBulk(String siteId, List<String> paths, boolean isSystemProcessing);

    /**
     * Update states bitmap for item by setting bits to 1
     * @param siteId site identifier
     * @param path path of item
     * @param statesBitMask bit mask for states to be updated
     */
    void setStateBits(String siteId, String path, long statesBitMask);

    /**
     * Update states bitmap for item by setting bits to 0
     * @param siteId site identifier
     * @param path path of item
     * @param statesBitMask bit mask for states to be updated
     */
    void resetStateBits(String siteId, String path, long statesBitMask);

    /**
     * Update states bitmap for item by setting bits to 1
     * @param itemId item identifier
     * @param statesBitMask bit mask for states to be updated
     */
    void setStateBits(long itemId, long statesBitMask);

    /**
     * Update states bitmap for item by setting bits to 0
     * @param itemId item identifier
     * @param statesBitMask bit mask for states to be updated
     */
    void resetStateBits(long itemId, long statesBitMask);
}
