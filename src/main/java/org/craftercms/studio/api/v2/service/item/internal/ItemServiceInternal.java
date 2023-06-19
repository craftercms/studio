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

package org.craftercms.studio.api.v2.service.item.internal;

import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.DetailedItem;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public interface ItemServiceInternal {

    /**
     * Insert record for item if it does not exist, otherwise update it
     *
     * @param item item to add or update
     */
    boolean upsertEntry(Item item);

    /**
     * Get item by given id
     * @param siteId site identifier
     * @param id item id
     * @return item
     */
    DetailedItem getItem(String siteId, long id);

    /**
     * Get item fir given site and path
     * @param siteId site identifier
     * @param path item path
     * @return item
     */
    Item getItem(String siteId, String path);

    /**
     * Get item fir given site and path
     * @param siteId site identifier
     * @param path item path
     * @param preferContent if true return content item if available
     * @return item
     */
    Item getItem(String siteId, String path, boolean preferContent);

    /**
     * Get items for given site and paths
     * @param siteId site identifier
     * @param path item paths
     * @return list of items
     */
    List<Item> getItems(String siteId, List<String> path);

    /**
     * Get items for given site and paths
     * @param siteId site identifier
     * @param paths item paths
     * @param preferContent if true return content item if available
     * @return list of items
     */
    List<Item> getItems(String siteId, List<String> paths, boolean preferContent);

    /**
     * Update item
     * @param item item to update
     */
    void updateItem(Item item);

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
     * Update states to flip on list off states and flip off another list of states for item
     *
     * @param siteId site identifier
     * @param path path of item
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBits(String siteId, String path, long onStateBitMap, long offStateBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param paths list of paths of items
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBitsBulk(String siteId, Collection<String> paths, long onStateBitMap, long offStateBitMap);

    Item.Builder instantiateItem(String siteName, String path);

    /**
     * Delete all items for site
     * @param siteId site id
     */
    void deleteItemsForSite(long siteId);

    /**
     * Get browser url for given repository item
     * @param site site identifier
     * @param path path of the content
     * @return browser url
     */
    String getBrowserUrl(String site, String path);

    /**
     * Persist item metadata after create
     * @param siteId site identifier
     * @param path path of the content
     * @param username user that executed write operation
     * @param commitId commit id of the write operation
     * @param unlock Indicates if content needs to be unlocked after write (save &amp; close)
     * @param parentId id of parent item
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterCreate(String siteId, String path, String username, String commitId,
                                boolean unlock, Long parentId)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after write
     * @param siteId site identifier
     * @param path path of the content
     * @param username user that executed write operation
     * @param commitId commit id of the write operation
     * @param unlock Indicates if content needs to be unlocked after write (save &amp; close)
     */
    void persistItemAfterWrite(String siteId, String path, String username, String commitId,
                               boolean unlock) throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after create folder
     * @param siteId site identifier
     * @param folderPath folder path
     * @param folderName folder name
     * @param username user that executed create folder operation
     * @param commitId commit id of the create folder operation
     * @param parentId id of parent item
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, String username,
                                      String commitId, Long parentId)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after rename folder
     * @param siteId site identifier
     * @param path file path
     * @param name file name
     * @param username user that executed create folder operation
     * @param commitId commit id of the create folder operation
     * @param contentType content type
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterRenameContent(String siteId, String path, String name, String username,
                                      String commitId, String contentType)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Move item
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     * @param parentId new parent ID
     * @param label new label
     */
    void moveItem(String siteId, String oldPath, String newPath, Long parentId, String label);

    /**
     * Check if item is new
     * @param siteId site identifier
     * @param path path of the item
     * @return true if NEW flag is set otherwise false
     */
    boolean isNew(String siteId, String path);

    /**
     * Count all content items
     * @return number of content items in the system
     */
    int countAllContentItems();

    /**
     * Clear previous path of the content
     * @param siteId site identifier
     * @param path path of the content;
     */
    void clearPreviousPath(String siteId, String path);

    /**
     * Convert Publishing History Item to Publishing Dashboard Item
     * @param historyItem publishing history item
     * @return publishing dashboard item
     */
    PublishingDashboardItem convertHistoryItemToDashboardItem(PublishingHistoryItem historyItem);

    /**
     * Get in progress items for given site
     * @param siteId site identifier
     * @return list of items
     */
    List<Item> getInProgressItems(String siteId);

    /**
     * Check if item is update or new
     * @param site site identifier
     * @param path item path
     * @return true if item is new or modified
     */
    boolean isUpdatedOrNew(String site, String path);

    /**
     * Delete items for site and paths
     * @param siteId site id
     * @param folderPath folder path to delete
     */
    void deleteItemForFolder(long siteId, String folderPath);

    /**
     * Check if item is in system processing
     * @param site site identifier
     * @param path item path
     * @return true if item is in system processing
     */
    boolean isSystemProcessing(String site, String path);

    /**
     * Check if path exists as previous path
     * @param siteId site identifier
     * @param path path to check
     * @return true if item exists with previous path as given path
     */
    boolean previousPathExists(String siteId, String path);

    /**
     * Update commit id for item
     * @param siteId site identifier
     * @param path path of the item
     * @param commitId commit id
     */
    void updateCommitId(String siteId, String path, String commitId);

    /**
     * Get mandatory parents for publishing for given site and list of paths
     * @param siteId site identifier
     * @param paths list of paths
     * @return list of mandatory parents paths
     */
    List<String> getMandatoryParentsForPublishing(String siteId, List<String> paths);

    /**
     * Get existing renamed children of mandatory parents for publishing
     * @param siteId site identifier
     * @param parents list of parents paths
     * @return list of children paths
     */
    List<String> getExistingRenamedChildrenOfMandatoryParentsForPublishing(String siteId, List<String> parents);

    /**
     * Get change set for subtree
     * @param siteId site identifier
     * @param path root path of the subtree
     * @return list of items
     */
    List<String> getChangeSetForSubtree(String siteId, String path);

    /**
     * Get items edited on same commit id for given item
     * @param siteId site identifier
     * @param path path of content item
     * @return list of items paths
     */
    List<String> getSameCommitItems(String siteId, String path);

    /**
     * Update last published date for item
     * @param siteId site identifier
     * @param path path of the item
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOn(String siteId, String path, ZonedDateTime lastPublishedOn);

    /**
     * Lock item for given lock owner
     * @param siteId site identifier
     * @param path item path
     * @param username user that owns the lock
     */
    void lockItemByPath(String siteId, String path, String username)
            throws UserNotFoundException, ServiceLayerException;

    /**
     * Unlock item
     * @param siteId site identifier
     * @param path item path
     */
    void unlockItemByPath(String siteId, String path);

    /**
     * Get total number of item states records for given filters by path regex and states mask
     * @param siteId site identifier
     * @param path path regex to filter items
     * @param states states mask to filter items by state
     * @return number of records
     */
    int getItemStatesTotal(String siteId, String path, Long states, List<String> systemTypes);

    /**
     * Get item states for given filters by path regex and states mask
     *
     * @param siteId      site identifier
     * @param path        path regex to filter items
     * @param states      states mask to filter items by state
     * @param systemTypes system types to filter items
     * @param sortFields sort fields
     * @param offset      offset for the first record in result set
     * @param limit       number of item states records to return
     * @return list of sandbox items
     */
    List<Item> getItemStates(String siteId, String path, Long states, List<String> systemTypes, List<SortField> sortFields, int offset, int limit);

    /**
     * Update item state flags for given items
     * @param siteId site identifier
     * @param paths item paths
     * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
     * @param clearUserLocked if true clear user locked flag, otherwise ignore
     * @param live if true set live flag, otherwise reset it
     * @param staged if true set staged flag, otherwise reset it
     * @param isNew value to set the 'new' flag to, or null if the flag should not change
     * @param modified value to set the 'modified' flag to, or null if the flag should not change
     */
    void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked,
                          Boolean live, Boolean staged, Boolean isNew, Boolean modified);

    /**
     * Update item state flags for given path query
     * @param siteId site identifier
     * @param path path regex to identify items
     * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
     * @param clearUserLocked if true clear user locked flag, otherwise ignore
     * @param live if true set live flag, otherwise reset it
     * @param staged if true set staged flag, otherwise reset it
     * @param isNew value to set the 'new' flag to, or null if the flag should not change
     * @param modified value to set the 'modified' flag to, or null if the flag should not change
     */
    void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified);

    /**
     * Get subtree for delete
     * @param siteId site identifier
     * @param path root path of the subtree
     * @return list of items
     */
    List<String> getSubtreeForDelete(String siteId, String path);

    /**
     * Update states for all content in the given site
     * @param siteId site identifier
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap states bitmap to flip off
     */
    void updateStatesForSite(String siteId, long onStateBitMap, long offStateBitMap);

    /**
     * Updates a new page's children (in case the folder existed before the page was created)
     * to point to the new page.
     *
     * @param site site identifier
     * @param path path of the folder where the new index.xml has been added
     */
    void updateNewPageChildren(String site, String path);
}
