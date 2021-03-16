/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LAST_PUBLISHED_ON;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

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
     * Update states bitmap for list of items by setting bits to 1
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMask bit mask for states to be updated
     */
    void setStateBitsBulk(String siteId, List<String> paths, long statesBitMask);

    /**
     * Update states bitmap for item by setting bits to 0
     * @param siteId site identifier
     * @param path path of item
     * @param statesBitMask bit mask for states to be updated
     */
    void resetStateBits(String siteId, String path, long statesBitMask);

    /**
     * Update states bitmap for list of items by setting bits to 0
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMask bit mask for states to be updated
     */
    void resetStateBitsBulk(String siteId, List<String> paths, long statesBitMask);


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
     * Update states to flip on list off states and flip off another list of states for item
     *
     * @param itemId item identifier
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBits(long itemId, long onStateBitMap, long offStateBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param paths list of paths of items
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBitsBulk(String siteId, List<String> paths, long onStateBitMap, long offStateBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param itemIds list of item ids
     * @param onStateBitMap states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBitsBulk(List<Long> itemIds, long onStateBitMap, long offStateBitMap);

    Item.Builder instantiateItem(String siteName, String path);

    /**
     * Instantiate item by getting it from DB and setting properties to values
     *
     * @param siteId site identifier
     * @param siteName site name
     * @param path path of the item
     * @param previewUrl preview URL
     * @param state state of the item (bitmap)
     * @param ownedBy owner id
     * @param owner owner username
     * @param createdBy creator id
     * @param creator creator username
     * @param createdOn created date
     * @param lastModifiedBy modifier id
     * @param modifier modifier username
     * @param lastModifiedOn modified date
     * @param label label for the item
     * @param contentTypeId content type id
     * @param systemType system type
     * @param mimeType mime type
     * @param disabledAsInt disabled as integer 0 = enabled, 1 = disabled
     * @param disabled disabled as boolean
     * @param localeCode locale code
     * @param translationSourceId translation source item id
     * @param size size of the file
     * @param parentId parent id
     * @param commitId commit id
     * @return Item object
     */
    Item instantiateItem(long siteId, String siteName, String path, String previewUrl, long state, Long ownedBy,
                         String owner, Long createdBy, String creator, ZonedDateTime createdOn, Long lastModifiedBy,
                         String modifier, ZonedDateTime lastModifiedOn, String label, String contentTypeId,
                         String systemType, String mimeType, int disabledAsInt,  boolean disabled, String localeCode,
                         Long translationSourceId, long size, Long parentId, String commitId);

    /**
     * Instantiate item after write or update
     *
     * @param siteId site identifier
     * @param path path of the item
     * @param username user name of modifier
     * @param lastModifiedOn modified date
     * @param label label for the item
     * @param contentTypeId content type id
     * @param localeCode locale code
     * @param commitId commit id obtained with write operation
     * @param size file size in bytes
     * @param unlock Optional unlocking of item, if true unlock, otherwise lock. If not present item will be unlocked
     * @return item object
     * @throws ServiceLayerException General service error
     * @throws UserNotFoundException If given username does not exist
     */
    Item instantiateItemAfterWrite(String siteId, String path, String username, ZonedDateTime lastModifiedOn,
                                   String label, String contentTypeId, String localeCode, String commitId, long size,
                                   Optional<Boolean> unlock)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Delete all items for site
     * @param siteId site id
     */
    void deleteItemsForSite(long siteId);

    /**
     * Delete items by ids
     * @param itemIds ids of items to delete
     */
    void deleteItemsById(List<Long> itemIds);

    /**
     * Delete items for site and paths
     * @param siteId site id
     * @param paths list of item paths to delete
     */
    void deleteItemsForSiteAndPaths(long siteId, List<String> paths);

    /**
     * Get total number of records for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter for result set
     * @param modifier filter results by user
     * @param contentType filter results by content type
     * @param state filter results by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @return total number of records in result set
     */
    int getContentDashboardTotal(String siteId, String path, String modifier, String contentType,
                                 long state, ZonedDateTime dateFrom, ZonedDateTime dateTo);

    /**
     * Get result set for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter for result set
     * @param modifier filter results by user
     * @param contentType filter results by content type
     * @param state filter results by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @param sortBy sort results by column
     * @param order order of results
     * @param offset offset of the first record in result set
     * @param limit number of records to return
     * @return list of item metadata records
     */
    List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier, String contentType,
                                                   long state, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                   String sortBy, String order, int offset, int limit);

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
     * @param unlock true if content needs to be unlocked after write (save & close), otherwise false
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterCreate(String siteId, String path, String username, String commitId,
                                Optional<Boolean> unlock)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after write
     * @param siteId site identifier
     * @param path path of the content
     * @param username user that executed write operation
     * @param commitId commit id of the write operation
     * @param unlock true if content needs to be unlocked after write (save & close), otherwise false
     */
    void persistItemAfterWrite(String siteId, String path, String username, String commitId,
                               Optional<Boolean> unlock) throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after create folder
     * @param siteId site identifier
     * @param folderPath folder path
     * @param folderName folder name
     * @param username user that executed create folder operation
     * @param commitId commit id of the create folder operation
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, String username,
                                      String commitId)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Persist item metadata after rename folder
     * @param siteId site identifier
     * @param folderPath folder path
     * @param folderName folder name
     * @param username user that executed create folder operation
     * @param commitId commit id of the create folder operation
     * @throws ServiceLayerException
     * @throws UserNotFoundException
     */
    void persistItemAfterRenameFolder(String siteId, String folderPath, String folderName, String username,
                                      String commitId)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Move item
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     */
    void moveItem(String siteId, String oldPath, String newPath);

    /**
     * Move items
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     */
    void moveItems(String siteId, String oldPath, String newPath);

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
     * Convert Item to Content Dashboard Item
     * @param siteId site identifier
     * @param item item
     * @return content dashboard item
     */
    ContentDashboardItem convertItemToContentDashboardItem(String siteId, Item item);

    /**
     * Get in progress items for given site
     * @param siteId site identifier
     * @return list of items
     */
    List<Item> getInProgressItems(String siteId);

    /**
     * Get submitted items for given site
     * @param site site identifier
     * @return list of items
     */
    List<Item> getSubmittedItems(String site);

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
     * Upadte commit id for item
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
     * Update last published date for item
     * @param siteId site identifier
     * @param paths list of paths
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOnBulk(String siteId, List<String> paths, ZonedDateTime lastPublishedOn);
}
