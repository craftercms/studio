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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public interface ItemDAO {

    /**
     * Get total number of children for given path
     *
     * @param siteId      site identifier
     * @param path        path to get children for
     * @param localeCode  local code
     * @param keyword     filter by keyword
     * @param systemTypes filter by type
     * @param excludes    exclude items by path
     * @return total number of children
     */
    int getChildrenByPathTotal(@Param(SITE_ID) Long siteId,
                               @Param(PATH) String path,
                               @Param(LOCALE_CODE) String localeCode,
                               @Param(KEYWORD) String keyword,
                               @Param(SYSTEM_TYPES) List<String> systemTypes,
                               @Param(EXCLUDES) List<String> excludes);

    /**
     * Get children for given path from database
     *
     * @param siteId           site identifier
     * @param path             path to get children for
     * @param systemTypeFolder system type value for folder
     * @param localeCode       locale code
     * @param keyword          filter by keyword
     * @param systemTypes      filter by type
     * @param excludes         exclude items by path
     * @param sortStrategy     sort strategy
     * @param order            order of children
     * @param offset           offset of the first record to return
     * @param limit            number of children to return
     * @return list of items (parent, level descriptor, children)
     */

    List<Item> getChildrenByPath(@Param(SITE_ID) Long siteId,
                                 @Param(PATH) String path,
                                 @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                 @Param(LOCALE_CODE) String localeCode,
                                 @Param(KEYWORD) String keyword,
                                 @Param(SYSTEM_TYPES) List<String> systemTypes,
                                 @Param(EXCLUDES) List<String> excludes,
                                 @Param(SORT_STRATEGY) String sortStrategy,
                                 @Param(ORDER) String order,
                                 @Param(OFFSET) int offset,
                                 @Param(LIMIT) int limit);

    /**
     * insert or update item
     *
     * @param item item to insert/update
     */
    void upsertEntry(Item item);

    /**
     * Insert or update items
     *
     * @param entries list of items to insert
     */
    void upsertEntries(@Param(ENTRIES) List<Item> entries);

    /**
     * Get item by id
     *
     * @param id item id
     * @param siteId site identifier
     * @param systemTypeFolder value for system type folder
     * @param completedState completed state
     * @param liveEnvironment live environment
     * @param stagingEnvironment staging environment
     * @return item identified by given id
     */
    DetailedItem getItemById(@Param(ID) long id,
                             @Param(SITE_ID) String siteId,
                             @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                             @Param(COMPLETED_STATE) String completedState,
                             @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                             @Param(LIVE_ENVIRONMENT) String liveEnvironment);

    /**
     * Get item for given site and path
     * @param siteId site identifier
     * @param path path of the item
     * @param systemTypeFolder value for system type folder
     * @param completedState completed state
     * @param liveEnvironment live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */
    DetailedItem getItemBySiteIdAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                        @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                        @Param(COMPLETED_STATE) String completedState,
                                        @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                        @Param(LIVE_ENVIRONMENT) String liveEnvironment);

    /**
     * Get item with prefer content option for given site and path
     * @param siteId site identifier
     * @param path path of the item
     * @param systemTypeFolder value for system type folder
     * @param completedState completed state
     * @param liveEnvironment live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */
    DetailedItem getItemBySiteIdAndPathPreferContent(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                                     @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                                     @Param(COMPLETED_STATE) String completedState,
                                                     @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                     @Param(LIVE_ENVIRONMENT) String liveEnvironment);

    /**
     * Update item
     * @param item item to update
     */
    void updateItem(Item item);

    /**
     * Delete item
     * @param id id of the item to delete
     */
    void deleteById(@Param(ID) long id);

    /**
     * Delete item
     * @param siteId site identifier
     * @param path path of item to delete
     */
    void deleteBySiteAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

    /**
     * Set items state
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMap states bit map to be set
     */
    void setStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) List<String> paths,
                                    @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Set items state
     * @param itemIds ids of items
     * @param statesBitMap states bit map to be set
     */
    void setStatesByIdBulk(@Param(ITEM_IDS) List<Long> itemIds, @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Reset items state
     * @param siteId site identifier
     * @param paths paths of items
     * @param statesBitMap states bit map to be reset
     */
    void resetStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) List<String> paths,
                                    @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Reset items state
     * @param itemIds ids of items
     * @param statesBitMap states bit map to be reset
     */
    void resetStatesByIdBulk(@Param(ITEM_IDS) List<Long> itemIds, @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param paths list of paths to update states for
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) Collection<String> paths,
                                   @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                                   @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param itemIds list of item identifiers
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesByIdBulk(@Param(ITEM_IDS) List<Long> itemIds,
                                   @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                                   @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId site identifier
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesForSite(@Param(SITE_ID) long siteId, @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                             @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Delete all items for site
     * @param siteId site id
     */
    void deleteItemsForSite(@Param(SITE_ID) long siteId);

    /**
     * Delete items by id
     * @param itemIds item ids
     */
    void deleteItemsById(@Param(ITEM_IDS) List<Long> itemIds);

    /**
     * Delete items for site and paths
     * @param siteId site id
     * @param paths paths of the items
     */
    void deleteItemsForSiteAndPath(@Param(SITE_ID) long siteId, @Param(PATHS) List<String> paths);

    /**
     * Delete items for site and folder path
     * @param siteId site id
     * @param path path of the folder
     */
    void deleteBySiteAndPathForFolder(@Param(SITE_ID) long siteId, @Param(FOLDER_PATH) String path);

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
    int getContentDashboardTotal(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                                 @Param(MODIFIER) String modifier, @Param(CONTENT_TYPE) String contentType,
                                 @Param(STATE) long state, @Param(DATE_FROM) ZonedDateTime dateFrom,
                                 @Param(DATE_TO) ZonedDateTime dateTo);

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
     * @param sort sort results by column
     * @param order order of results
     * @param offset offset of the first record in result set
     * @param limit number of records to return
     * @return list of item metadata records
     */
    List<Item> getContentDashboard(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                                   @Param(MODIFIER) String modifier, @Param(CONTENT_TYPE) String contentType,
                                   @Param(STATE) long state, @Param(DATE_FROM) ZonedDateTime dateFrom,
                                   @Param(DATE_TO) ZonedDateTime dateTo, @Param(SORT_STRATEGY) String sort,
                                   @Param(ORDER) String order, @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Move item
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     */
    void moveItem(@Param(SITE_ID) String siteId, @Param(OLD_PATH) String oldPath, @Param(NEW_PATH) String newPath);

    /**
     * Get item for given path from database
     *
     * @param siteId site identifier
     * @param path path of the item
     * @param systemTypeFolder value for system type folder
     * @param completedState completed state
     * @param liveEnvironment live environment
     * @param stagingEnvironment staging environment
     * @return item
     */

    DetailedItem getItemByPath(@Param(SITE_ID) Long siteId, @Param(PATH) String path,
                               @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                               @Param(COMPLETED_STATE) String completedState,
                               @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                               @Param(LIVE_ENVIRONMENT) String liveEnvironment);
    /**
     * Get item with prefer content option for given path from database
     *
     * @param siteId site identifier
     * @param path path of the item
     * @param systemTypeFolder value for system type folder
     * @param completedState completed state
     * @param liveEnvironment live environment
     * @param stagingEnvironment staging environment
     * @return item
     */

    DetailedItem getItemByPathPreferContent(@Param(SITE_ID) Long siteId, @Param(PATH) String path,
                                            @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                            @Param(COMPLETED_STATE) String completedState,
                                            @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                            @Param(LIVE_ENVIRONMENT) String liveEnvironment);


    /**
     * Move items
     * @param siteId site identifier
     * @param oldPath old path
     * @param newPath new path
     * @param parentId new parent id, if null parent will not be changed
     * @param oldPreviewUrl old preview url
     * @param newPreviewUrl new preview url
     * @param onStatesBitMap state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void moveItems(@Param(SITE_ID) String siteId, @Param(OLD_PATH) String oldPath, @Param(NEW_PATH) String newPath,
                   @Param(PARENT_ID) Long parentId, @Param(OLD_PREVIEW_URL) String oldPreviewUrl,
                   @Param(NEW_PREVIEW_URL) String newPreviewUrl, @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                   @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Get sandbox items for given paths
     * @param siteId site identifier
     * @param paths paths to get items for
     * @param systemTypeFolder value for system type folder
     * @param preferContent indicates if pages should be returned instead of folders when available
     * @return list of items
     */
    List<Item> getSandboxItemsByPath(@Param(SITE_ID) Long siteId, @Param(PATHS) List<String> paths,
                                     @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                     @Param(PREFER_CONTENT) boolean preferContent);

    /**
     * Get sandbox items for given ids with prefer content option
     * @param itemIds item ids
     * @param systemTypeFolder value for system type folder
     * @return list of items
     */
    List<Item> getSandboxItemsByIdPreferContent(@Param(ITEM_IDS) List<Long> itemIds,
                                                @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder);

    /**
     * Get sandbox items for given ids
     * @param itemIds item ids
     * @param systemTypeFolder value for system type folder
     * @return list of items
     */
    List<Item> getSandboxItemsById(@Param(ITEM_IDS) List<Long> itemIds,
                                   @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder);

    /**
     * Get mandatory parents for publishing
     * @param siteId site identifier
     * @param possibleParents possible parents
     * @param newMask states mask for detecting new items
     * @param modifiedMask states mask for detecting modified items
     * @return list of mandatory parents
     */
    List<String> getMandatoryParentsForPublishing(@Param(SITE_ID) String siteId,
                                                  @Param(POSSIBLE_PARENTS) List<String> possibleParents,
                                                  @Param(NEW_MASK) long newMask,
                                                  @Param(MODIFIED_MASK) long modifiedMask);

    List<String> getExistingRenamedChildrenOfMandatoryParentsForPublishing(@Param(SITE_ID) String siteId,
                                                                           @Param(PARENTS) List<String> parents,
                                                                           @Param(NEW_MASK) long newMask,
                                                                           @Param(MODIFIED_MASK) long modifiedMask);

    /**
     * Count all content items in the system
     * @return number of content items in the system
     */
    int countAllContentItems(@Param(NON_CONTENT_ITEM_TYPES) List<String> nonContentItemTypes);

    /**
     * Clear previous path of the content
     * @param siteId site identifier
     * @param path path of the content
     */
    void clearPreviousPath(@Param(SITE_ID) String siteId, @Param(PATH) String path);

    /**
     * Get in progress items from DB
     * @param siteId site identifier
     * @param inProgressMask in progress states mask
     * @return list of items
     */
    List<Item> getInProgressItems(@Param(SITE_ID) String siteId, @Param(IN_PROGRESS_MASK) long inProgressMask);

    /**
     * Get submitted items from DB
     * @param siteId site identifier
     * @param submittedMask mask with submitted states turned on
     * @return list of items
     */
    List<Item> getSubmittedItems(@Param(SITE_ID) String siteId, @Param(SUBMITTED_MASK) long submittedMask);

    /**
     * Count items having previous path property set to given path
     * @param siteId site identifier
     * @param previousPath path to check
     * @return number of items
     */
    int countPreviousPaths(@Param(SITE_ID) String siteId, @Param(PREVIOUS_PATH) String previousPath);

    /**
     * Update commit id for item
     * @param siteId site identifier
     * @param path path of the item
     * @param commitId commit id
     */
    void updateCommitId(@Param(SITE_ID) String siteId, @Param(PATH) String path, @Param(COMMIT_ID) String commitId);

    /**
     * Get change set for subtree
     * @param siteId site identifier
     * @param path path of subtree root
     * @param likePath like path for query
     * @param nonContentItemTypes non content item types
     * @param inProgressMask in progress state mask
     * @return list of items
     */
    List<String> getChangeSetForSubtree(@Param(SITE_ID) String siteId,
                                        @Param(PATH) String path,
                                        @Param(LIKE_PATH) String likePath,
                                        @Param(NON_CONTENT_ITEM_TYPES) List<String> nonContentItemTypes,
                                        @Param(IN_PROGRESS_MASK) long inProgressMask);

    /**
     * Get items edited on same commit id for given item
     * @param siteId site identifier
     * @param path path of content item
     * @return list of items paths
     */
    List<String> getSameCommitItems(@Param(SITE_ID) String siteId, @Param(PATH) String path);

    /**
     * Update last published date for item
     * @param siteId site identifier
     * @param path path of the item
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOn(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                               @Param(LAST_PUBLISHED_ON) ZonedDateTime lastPublishedOn);

    /**
     * Update last published date for item
     * @param siteId site identifier
     * @param paths list of paths
     * @param lastPublishedOn published date
     */
    void updateLastPublishedOnBulk(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths,
                                   @Param(LAST_PUBLISHED_ON) ZonedDateTime lastPublishedOn);

    /**
     * Finds all items related to a given content-type
     *
     * @param siteId the id of the site
     * @param contentType the id of the content-type
     * @param scriptPath the path of the controller script
     * @return the list of items
     */
    List<Item> getContentTypeUsages(@Param(SITE_ID) String siteId, @Param(CONTENT_TYPE) String contentType,
                                    @Param(SCRIPT_PATH) String scriptPath);

    /**
     * Lock item
     * @param siteId site identifier
     * @param path path of the item
     * @param lockOwnerId lock owner
     * @param lockedBitOn state bit mask with LOCKED bit on
     * @param systemTypeFolder value for system type folder
     */
    void lockItemByPath(@Param(SITE_ID) String siteId, @Param(PATH) String path, @Param(LOCK_OWNER_ID) long lockOwnerId,
                        @Param(LOCKED_BIT_ON) long lockedBitOn, @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder);

    /**
     * Lock items
     * @param siteId site identifier
     * @param paths list of item paths
     * @param lockOwnerId lock owner
     * @param lockedBitOn state bit mask with LOCKED bit on
     * @param systemTypeFolder value for system type folder
     */
    void lockItemsByPath(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths,
                        @Param(LOCK_OWNER_ID) long lockOwnerId, @Param(LOCKED_BIT_ON) long lockedBitOn,
                        @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder);

    /**
     * Lock item
     * @param siteId site identifier
     * @param path path of the item
     * @param lockedBitOff state bit mask with LOCKED bit off
     */
    void unlockItemByPath(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                          @Param(LOCKED_BIT_OFF) long lockedBitOff);

    /**
     * Get total number of item states records for given filters by path regex and states mask
     * @param siteId site identifier
     * @param path path regex to filter items
     * @param states states mask to filter items by state
     * @return number of records
     */
    int getItemStatesTotal(@Param(SITE_ID) String siteId, @Param(PATH) String path, @Param(STATES_BIT_MAP) Long states);

    /**
     * Get item states for given filters by path regex and states mask
     * @param siteId site identifier
     * @param path path regex to filter items
     * @param states states mask to filter items by state
     * @param offset offset for the first record in result set
     * @param limit number of item states records to return
     * @return list of sandbox items
     */
    List<Item> getItemStates(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                             @Param(STATES_BIT_MAP) Long states, @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Update item state by query
     * @param siteId site identifier
     * @param path path regex to filter items to be updated
     * @param states states bitmap mask to filter items to be updated
     * @param setStatesMask states bitmap mask to set states on
     * @param resetStatesMask states bitmap mask to set states off
     */
    void updateStatesByQuery(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                             @Param(STATES_BIT_MAP) Long states,
                                 @Param(ON_STATES_BIT_MAP) long setStatesMask,
                                 @Param(OFF_STATES_BIT_MAP) long resetStatesMask);

    /**
     * Get subtree for delete
     * @param siteId site identifier
     * @param likePath like path for query
     * @return list of items
     */
    List<String> getSubtreeForDelete(@Param(SITE_ID) String siteId,
                                     @Param(LIKE_PATH) String likePath);
}
