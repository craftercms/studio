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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.LIVE_SUCCESS;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.STAGING_SUCCESS;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.SUBMITTED;

public interface ItemDAO {

    String STATES = "states";

    String SITE_ID = "siteId";
    String NEW_MASK = "newMask";
    String MODIFIED_MASK = "modifiedMask";
    String NON_CONTENT_ITEM_TYPES = "nonContentItemTypes";
    String IN_PROGRESS_MASK = "inProgressMask";
    String ITEM_IDS = "itemIds";

    String ON_STATES_BIT_MAP = "onStatesBitMap";
    String SUCCESS_ON_BIT_MAP = "successOnStatesBitMap";
    String SUCCESS_OFF_BIT_MAP = "successOffStatesBitMap";
    String FAILURE_ON_BIT_MAP = "failureOnStatesBitMap";
    String FAILURE_OFF_BIT_MAP = "failureOffStatesBitMap";

    String OFF_STATES_BIT_MAP = "offStatesBitMap";
    String TIMESTAMP = "timestamp";
    String STAGING_PUBLISHED_STATE = "stagingPublishedState";
    String LIVE_PUBLISHED_STATE = "livePublishedState";
    String PUBLISH_PACKAGE_STATE = "packageState";
    String PUBLISH_PACKAGE_APPROVAL_STATES = "approvalStates";

    String SYSTEM_TYPE_PAGE = "systemTypePage";

    Map<String, String> SORT_FIELD_MAP = Map.of(
            "id", "id",
            "dateModified", "last_modified_on",
            "label", "label");

    Map<String, String> DETAILED_ITEM_SORT_FIELD_MAP = Map.of(
            "id", "id",
            "dateModified", "last_modified_on",
            "dateScheduled", "IFNULL(live_scheduleddate,staging_scheduleddate)",
            "label", "label");

    /**
     * Get total number of children for given path
     *
     * @param siteId             site identifier
     * @param path               path to get children for
     * @param localeCode         local code
     * @param keyword            filter by keyword
     * @param systemTypes        filter by type
     * @param excludeSystemTypes system types to exclude
     * @param excludes           exclude items by path
     * @return total number of children
     */
    int getChildrenByPathTotal(@Param(SITE_ID) Long siteId,
                               @Param(PATH) String path,
                               @Param(LOCALE_CODE) String localeCode,
                               @Param(KEYWORD) String keyword,
                               @Param(SYSTEM_TYPES) List<String> systemTypes,
                               @Param(EXCLUDE_SYSTEM_TYPES) List<String> excludeSystemTypes,
                               @Param(EXCLUDES) List<String> excludes);


    /**
     * Get children for given path from database
     *
     * @param siteId             site identifier
     * @param path               path to get children for
     * @param localeCode         locale code
     * @param keyword            filter by keyword
     * @param systemTypes        filter by type
     * @param excludeSystemTypes system types to exclude
     * @param excludes           exclude items by path
     * @param sortStrategy       sort strategy
     * @param order              order of children
     * @param offset             offset of the first record to return
     * @param limit              number of children to return
     * @return list of items (parent, level descriptor, children)
     */
    default List<Item> getChildrenByPath(@Param(SITE_ID) Long siteId,
                                         @Param(PATH) String path,
                                         @Param(LOCALE_CODE) String localeCode,
                                         @Param(KEYWORD) String keyword,
                                         @Param(SYSTEM_TYPES) List<String> systemTypes,
                                         @Param(EXCLUDE_SYSTEM_TYPES) List<String> excludeSystemTypes,
                                         @Param(EXCLUDES) List<String> excludes,
                                         @Param(SORT_STRATEGY) String sortStrategy,
                                         @Param(ORDER) String order,
                                         @Param(OFFSET) int offset,
                                         @Param(LIMIT) int limit) {
        return getChildrenByPath(siteId, path, CONTENT_TYPE_FOLDER, localeCode,
                keyword, systemTypes, excludeSystemTypes,
                excludes, sortStrategy, order,
                PublishPackage.PackageState.READY.value, List.of(APPROVED, SUBMITTED),
                offset, limit);
    }

    /**
     * Get children for given path from database
     *
     * @param siteId             site identifier
     * @param path               path to get children for
     * @param systemTypeFolder   system type value for folder
     * @param localeCode         locale code
     * @param keyword            filter by keyword
     * @param systemTypes        filter by type
     * @param excludeSystemTypes system types to exclude
     * @param excludes           exclude items by path
     * @param sortStrategy       sort strategy
     * @param order              order of children
     * @param packageState       package state mask
     * @param approvalStates     package approval states to filter
     * @param offset             offset of the first record to return
     * @param limit              number of children to return
     * @return list of items (parent, level descriptor, children)
     */
    List<Item> getChildrenByPath(@Param(SITE_ID) Long siteId,
                                 @Param(PATH) String path,
                                 @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                 @Param(LOCALE_CODE) String localeCode,
                                 @Param(KEYWORD) String keyword,
                                 @Param(SYSTEM_TYPES) List<String> systemTypes,
                                 @Param(EXCLUDE_SYSTEM_TYPES) List<String> excludeSystemTypes,
                                 @Param(EXCLUDES) List<String> excludes,
                                 @Param(SORT_STRATEGY) String sortStrategy,
                                 @Param(ORDER) String order,
                                 @Param(PUBLISH_PACKAGE_STATE) long packageState,
                                 @Param(PUBLISH_PACKAGE_APPROVAL_STATES) Collection<ApprovalState> approvalStates,
                                 @Param(OFFSET) int offset,
                                 @Param(LIMIT) int limit);

    /**
     * insert or update item
     *
     * @param item item to insert/update
     */
    void upsertEntry(Item item);

    /**
     * Get item by id
     *
     * @param id                 item id
     * @param siteId             site identifier
     * @param systemTypeFolder   value for system type folder
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item identified by given id
     */
    default DetailedItem getItemById(long id,
                                     String siteId,
                                     String systemTypeFolder,
                                     String stagingEnvironment,
                                     String liveEnvironment) {
        return getItemById(id, siteId, systemTypeFolder, stagingEnvironment, liveEnvironment, LIVE_SUCCESS.value, STAGING_SUCCESS.value);
    }

    /**
     * Get item by id
     *
     * @param id                 item id
     * @param siteId             site identifier
     * @param systemTypeFolder   value for system type folder
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item identified by given id
     */
    DetailedItem getItemById(@Param(ID) long id,
                             @Param(SITE_ID) String siteId,
                             @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                             @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                             @Param(LIVE_ENVIRONMENT) String liveEnvironment,
                             @Param(LIVE_PUBLISHED_STATE) long livePublishedState,
                             @Param(STAGING_PUBLISHED_STATE) long stagingPublishedState);

    /**
     * Get item for given site and path
     *
     * @param siteId             site identifier
     * @param path               path of the item
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */
    default DetailedItem getItemBySiteIdAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                                @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                @Param(LIVE_ENVIRONMENT) String liveEnvironment) {
        return getItemBySiteIdAndPath(siteId, path, CONTENT_TYPE_FOLDER, stagingEnvironment, liveEnvironment,
                LIVE_SUCCESS.value, STAGING_SUCCESS.value);
    }

    /**
     * Get item for given site and path
     *
     * @param siteId             site identifier
     * @param path               path of the item
     * @param systemTypeFolder   value for system type folder
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */
    DetailedItem getItemBySiteIdAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                        @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                        @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                        @Param(LIVE_ENVIRONMENT) String liveEnvironment,
                                        @Param(LIVE_PUBLISHED_STATE) long livePublishedState,
                                        @Param(STAGING_PUBLISHED_STATE) long stagingPublishedState);

    /**
     * Get item with prefer content option for given site and path
     *
     * @param siteId             site identifier
     * @param path               path of the item
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */

    default DetailedItem getItemBySiteIdAndPathPreferContent(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                                             @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                             @Param(LIVE_ENVIRONMENT) String liveEnvironment) {
        return getItemBySiteIdAndPathPreferContent(siteId, path, CONTENT_TYPE_FOLDER, stagingEnvironment, liveEnvironment,
                LIVE_SUCCESS.value, STAGING_SUCCESS.value);
    }

    /**
     * Get item with prefer content option for given site and path
     *
     * @param siteId             site identifier
     * @param path               path of the item
     * @param systemTypeFolder   value for system type folder
     * @param liveEnvironment    live environment
     * @param stagingEnvironment staging environment
     * @return item for given site and path
     */
    DetailedItem getItemBySiteIdAndPathPreferContent(@Param(SITE_ID) long siteId, @Param(PATH) String path,
                                                     @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                                     @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                     @Param(LIVE_ENVIRONMENT) String liveEnvironment,
                                                     @Param(LIVE_PUBLISHED_STATE) long livePublishedState,
                                                     @Param(STAGING_PUBLISHED_STATE) long stagingPublishedState);

    /**
     * Update item
     *
     * @param item item to update
     */
    void updateItem(Item item);

    /**
     * Delete an item by path.
     * Notice that the parent folder will be deleted if path corresponds to a page.
     *
     * @param siteId the site id
     * @param path   the item path
     */
    default void deleteBySiteAndPath(final long siteId, final String path) {
        deleteBySiteAndPath(siteId, path, CONTENT_TYPE_PAGE);
    }

    /**
     * Delete item
     * Notice that the parent folder will be deleted if path corresponds to a page.
     *
     * @param siteId         site identifier
     * @param path           path of item to delete
     * @param systemTypePage system type page. In case the path corresponds to a page, the parent folder will be deleted as well.
     */
    void deleteBySiteAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path, @Param(SYSTEM_TYPE_PAGE) String systemTypePage);

    /**
     * Set items state
     *
     * @param siteId       site identifier
     * @param paths        paths of items
     * @param statesBitMap states bit map to be set
     */
    void setStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) Collection<String> paths,
                                    @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Reset items state
     *
     * @param siteId       site identifier
     * @param paths        paths of items
     * @param statesBitMap states bit map to be reset
     */
    void resetStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) Collection<String> paths,
                                      @Param(STATES_BIT_MAP) long statesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId          site identifier
     * @param paths           list of paths to update states for
     * @param onStatesBitMap  state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesBySiteAndPathBulk(@Param(SITE_ID) long siteId, @Param(PATHS) Collection<String> paths,
                                       @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                                       @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Update states to flip on list off states and flip off another list of states for items
     *
     * @param siteId          site identifier
     * @param onStatesBitMap  state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void updateStatesForSite(@Param(SITE_ID) long siteId, @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                             @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Delete all items for site
     *
     * @param siteId site id
     */
    void deleteItemsForSite(@Param(SITE_ID) long siteId);

    /**
     * Delete items for site and folder path
     *
     * @param siteId site id
     * @param path   path of the folder
     */
    void deleteBySiteAndPathForFolder(@Param(SITE_ID) long siteId, @Param(FOLDER_PATH) String path);

    /**
     * Move item. Update item and item_target table to reflect an item moved from previousPath to newPath.
     *
     * @param siteId        the site id
     * @param previousPath  the previous path
     * @param newPath       the new path
     * @param parentId      the new parent id
     * @param oldPreviewUrl the old preview url
     * @param newPreviewUrl the new preview url
     * @param label         the new label
     */
    @Transactional
    default void moveItem(String siteId, String previousPath, String newPath,
                          Long parentId, String oldPreviewUrl,
                          String newPreviewUrl, String label) {
        moveItemInternal(siteId, previousPath, newPath, parentId, oldPreviewUrl, newPreviewUrl, label, SAVE_AND_CLOSE_ON_MASK, SAVE_AND_CLOSE_OFF_MASK);
        updatePreviousPath(siteId, previousPath, newPath);
    }

    /**
     * Update item_target table previous path if null.
     * If previous_path is not null, it means the item has not been
     * published after a move operation, so we don't need to update the previous path.
     *
     * @param siteId       the site id
     * @param previousPath the previous path
     * @param newPath      the new (current) path
     */
    void updatePreviousPath(@Param(SITE_ID) String siteId, @Param(PREVIOUS_PATH) String previousPath, @Param(NEW_PATH) String newPath);

    /**
     * Move item.
     * DO NOT USE THIS METHOD DIRECTLY. USE moveItem INSTEAD.
     *
     * @param siteId          site identifier
     * @param previousPath    previous path
     * @param newPath         new path
     * @param parentId        new parent ID
     * @param oldPreviewUrl   old preview url
     * @param newPreviewUrl   new preview url
     * @param label           the new label
     * @param onStatesBitMap  state bitmap to flip on
     * @param offStatesBitMap state bitmap to flip off
     */
    void moveItemInternal(@Param(SITE_ID) String siteId, @Param(PREVIOUS_PATH) String previousPath, @Param(NEW_PATH) String newPath,
                          @Param(PARENT_ID) Long parentId, @Param(OLD_PREVIEW_URL) String oldPreviewUrl,
                          @Param(NEW_PREVIEW_URL) String newPreviewUrl, @Param(LABEL) String label,
                          @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                          @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Get a list of {@link DetailedItem} for given site and filters (system_types, states)
     *
     * @param siteId             site identifier
     * @param statesBitMap       states bit map to filter by
     * @param systemTypes        system types to filter by
     * @param sortFields         sort fields
     * @param stagingEnvironment staging environment
     * @param liveEnvironment    live environment
     * @param offset             offset for the first record in result set
     * @param limit              number of records to return
     * @return list of filtered {@link DetailedItem}s
     */
    default List<DetailedItem> getDetailedItemsByStates(@Param(SITE_ID) long siteId,
                                                        @Param(STATES_BIT_MAP) long statesBitMap,
                                                        @Param(SYSTEM_TYPES) List<String> systemTypes,
                                                        @Param(SORT_FIELDS) List<SortField> sortFields,
                                                        @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                        @Param(LIVE_ENVIRONMENT) String liveEnvironment,
                                                        @Param(OFFSET) int offset, @Param(LIMIT) int limit) {
        return getDetailedItemsByStates(siteId, statesBitMap, CONTENT_TYPE_FOLDER, null, systemTypes, sortFields,
                stagingEnvironment, liveEnvironment, LIVE_SUCCESS.value, STAGING_SUCCESS.value, offset, limit);
    }

    /**
     * Get a list of {@link DetailedItem} for given site and filters (system_types, states)
     *
     * @param siteId             site identifier
     * @param statesBitMap       states bit map to filter by
     * @param systemTypeFolder   value for system type folder
     * @param completedState     completed state
     * @param systemTypes        system types to filter by
     * @param sortFields         sort fields
     * @param stagingEnvironment staging environment
     * @param liveEnvironment    live environment
     * @param offset             offset for the first record in result set
     * @param limit              number of records to return
     * @return list of filtered {@link DetailedItem}s
     */
    List<DetailedItem> getDetailedItemsByStates(@Param(SITE_ID) long siteId,
                                                @Param(STATES_BIT_MAP) long statesBitMap,
                                                @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                                @Param(COMPLETED_STATE) String completedState,
                                                @Param(SYSTEM_TYPES) List<String> systemTypes,
                                                @Param(SORT_FIELDS) List<SortField> sortFields,
                                                @Param(STAGING_ENVIRONMENT) String stagingEnvironment,
                                                @Param(LIVE_ENVIRONMENT) String liveEnvironment,
                                                @Param(LIVE_PUBLISHED_STATE) long livePublishedState,
                                                @Param(STAGING_PUBLISHED_STATE) long stagingPublishedState,
                                                @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Get sandbox items for given paths
     *
     * @param siteId        site identifier
     * @param paths         paths to get items for
     * @param preferContent indicates if pages should be returned instead of folders when available
     * @return list of items
     */
    default List<Item> getSandboxItemsByPath(@Param(SITE_ID) Long siteId, @Param(PATHS) List<String> paths,
                                             @Param(PREFER_CONTENT) boolean preferContent) {
        return getSandboxItemsByPath(siteId, paths, CONTENT_TYPE_FOLDER, preferContent,
                PublishPackage.PackageState.READY.value,
                List.of(APPROVED, SUBMITTED));
    }

    /**
     * Get sandbox items for given paths
     *
     * @param siteId           site identifier
     * @param paths            paths to get items for
     * @param systemTypeFolder value for system type folder
     * @param preferContent    indicates if pages should be returned instead of folders when available
     * @param packageState     package state mask
     * @param approvalStates   package approval states to filter
     * @return list of items
     */
    List<Item> getSandboxItemsByPath(@Param(SITE_ID) Long siteId, @Param(PATHS) List<String> paths,
                                     @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                     @Param(PREFER_CONTENT) boolean preferContent,
                                     @Param(PUBLISH_PACKAGE_STATE) long packageState,
                                     @Param(PUBLISH_PACKAGE_APPROVAL_STATES) Collection<ApprovalState> approvalStates);


    /**
     * Get sandbox items for given ids with prefer content option
     *
     * @param itemIds    item ids
     * @param sortFields sort fields
     * @return list of items
     */
    default List<Item> getSandboxItemsByIdPreferContent(@Param(ITEM_IDS) List<Long> itemIds,
                                                        @Param(SORT_FIELDS) List<SortField> sortFields) {
        return getSandboxItemsByIdPreferContent(itemIds, CONTENT_TYPE_FOLDER, sortFields,
                PublishPackage.PackageState.READY.value,
                List.of(APPROVED, SUBMITTED));
    }

    /**
     * Get sandbox items for given ids with prefer content option
     *
     * @param itemIds          item ids
     * @param sortFields       sort fields
     * @param systemTypeFolder value for system type folder
     * @return list of items
     */
    List<Item> getSandboxItemsByIdPreferContent(@Param(ITEM_IDS) List<Long> itemIds,
                                                @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                                @Param(SORT_FIELDS) List<SortField> sortFields,
                                                @Param(PUBLISH_PACKAGE_STATE) long packageState,
                                                @Param(PUBLISH_PACKAGE_APPROVAL_STATES) Collection<ApprovalState> approvalStates);

    /**
     * Get sandbox items for given ids
     *
     * @param itemIds    item ids
     * @param sortFields sort fields
     * @return list of items
     */
    default List<Item> getSandboxItemsById(@Param(ITEM_IDS) List<Long> itemIds,
                                           @Param(SORT_FIELDS) List<SortField> sortFields) {
        return getSandboxItemsById(itemIds, CONTENT_TYPE_FOLDER, sortFields,
                PublishPackage.PackageState.READY.value,
                List.of(APPROVED, SUBMITTED));
    }

    /**
     * Get sandbox items for given ids
     *
     * @param itemIds          item ids
     * @param systemTypeFolder value for system type folder
     * @param sortFields       sort fields
     * @return list of items
     */
    List<Item> getSandboxItemsById(@Param(ITEM_IDS) List<Long> itemIds,
                                   @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                   @Param(SORT_FIELDS) List<SortField> sortFields,
                                   @Param(PUBLISH_PACKAGE_STATE) long packageState,
                                   @Param(PUBLISH_PACKAGE_APPROVAL_STATES) Collection<ApprovalState> approvalStates);

    /**
     * Get mandatory parents for publishing
     *
     * @param siteId          site identifier
     * @param possibleParents possible parents
     * @param newMask         states mask for detecting new items
     * @param modifiedMask    states mask for detecting modified items
     * @return list of mandatory parents
     */
    List<String> getMandatoryParentsForPublishing(@Param(SITE_ID) String siteId,
                                                  @Param(POSSIBLE_PARENTS) List<String> possibleParents,
                                                  @Param(NEW_MASK) long newMask,
                                                  @Param(MODIFIED_MASK) long modifiedMask);

    /**
     * Count all content items in the system
     *
     * @return number of content items in the system
     */
    int countAllContentItems(@Param(NON_CONTENT_ITEM_TYPES) List<String> nonContentItemTypes);

    /**
     * Get in progress items from DB
     *
     * @param siteId         site identifier
     * @param inProgressMask in progress states mask
     * @return list of items
     */
    List<Item> getInProgressItems(@Param(SITE_ID) String siteId, @Param(IN_PROGRESS_MASK) long inProgressMask);

    /**
     * Get change set for subtree
     *
     * @param siteId              site identifier
     * @param path                path of subtree root
     * @param likePath            like path for query
     * @param nonContentItemTypes non content item types
     * @param inProgressMask      in progress state mask
     * @return list of items
     */
    List<String> getChangeSetForSubtree(@Param(SITE_ID) String siteId,
                                        @Param(PATH) String path,
                                        @Param(LIKE_PATH) String likePath,
                                        @Param(NON_CONTENT_ITEM_TYPES) List<String> nonContentItemTypes,
                                        @Param(IN_PROGRESS_MASK) long inProgressMask);

    /**
     * Finds all items related to a given content-type
     *
     * @param siteId      the id of the site
     * @param contentType the id of the content-type
     * @param scriptPath  the path of the controller script
     * @return the list of items
     */
    List<Item> getContentTypeUsages(@Param(SITE_ID) String siteId, @Param(CONTENT_TYPE) String contentType,
                                    @Param(SCRIPT_PATH) String scriptPath);

    /**
     * Lock item
     *
     * @param siteId           site identifier
     * @param path             path of the item
     * @param lockOwnerId      lock owner
     * @param lockedBitOn      state bit mask with LOCKED bit on
     * @param systemTypeFolder value for system type folder
     */
    void lockItemByPath(@Param(SITE_ID) String siteId, @Param(PATH) String path, @Param(LOCK_OWNER_ID) long lockOwnerId,
                        @Param(LOCKED_BIT_ON) long lockedBitOn, @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder);

    /**
     * Lock item
     *
     * @param siteId       site identifier
     * @param path         path of the item
     * @param lockedBitOff state bit mask with LOCKED bit off
     */
    void unlockItemByPath(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                          @Param(LOCKED_BIT_OFF) long lockedBitOff);

    /**
     * Get total number of item states records for given filters by path regex and states mask
     *
     * @param siteId      site identifier
     * @param path        path regex to filter items
     * @param states      states mask to filter items by state
     * @param systemTypes system types to filter items
     * @return number of records
     */
    int getItemStatesTotal(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                           @Param(STATES_BIT_MAP) Long states, @Param(SYSTEM_TYPES) List<String> systemTypes);

    /**
     * Get item states for given filters by path regex and states mask
     *
     * @param siteId      site identifier
     * @param path        path regex to filter items
     * @param states      states mask to filter items by state
     * @param systemTypes system types to filter items
     * @param sortFields  list of sort fields
     * @param offset      offset for the first record in result set
     * @param limit       number of item states records to return
     * @return list of sandbox items
     */
    List<Item> getItemStates(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                             @Param(STATES_BIT_MAP) Long states, @Param(SYSTEM_TYPES) List<String> systemTypes,
                             @Param(SORT_FIELDS) List<SortField> sortFields,
                             @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Update item state by query
     *
     * @param siteId          site identifier
     * @param path            path regex to filter items to be updated
     * @param states          states bitmap mask to filter items to be updated
     * @param setStatesMask   states bitmap mask to set states on
     * @param resetStatesMask states bitmap mask to set states off
     */
    void updateStatesByQuery(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                             @Param(STATES_BIT_MAP) Long states,
                             @Param(ON_STATES_BIT_MAP) long setStatesMask,
                             @Param(OFF_STATES_BIT_MAP) long resetStatesMask);

    /**
     * Get subtree for delete
     *
     * @param siteId   site identifier
     * @param likePath like path for query
     * @return list of items
     */
    List<String> getSubtreeForDelete(@Param(SITE_ID) String siteId,
                                     @Param(LIKE_PATH) String likePath);

    /**
     * When creating a new page in an already existing folder, we need to update the children of the folder
     * to become the children of the page.
     * This method will find the direct children of the given folder, and update
     * their parent id to be the id the new page (the item with path = folderPath + /index.xml).
     *
     * @param siteId     site identifier
     * @param folderPath path of the folder
     */
    void updateNewPageChildren(@Param(SITE_ID) String siteId, @Param(PATH) String folderPath);

    /**
     * Get the non-folder paths of the children of the item at the given path, recursively.
     *
     * @param siteId the site id
     * @param path   the path to get children for
     * @return list of children paths
     */
    Collection<String> getChildrenPaths(@Param(SITE_ID) long siteId, @Param(PATH) String path);

    default Collection<String> getUnpublishedPaths(final long siteId) {
        return getUnpublishedPaths(siteId, UNPUBLISHED_MASK);
    }

    Collection<String> getUnpublishedPaths(@Param(SITE_ID) long siteId,
                                           @Param(STATES) long states);

    /**
     * Update states for items with given ids.
     *
     * @param ids            ids of items
     * @param onStateBitMap  states bitmap to flip on
     * @param offStateBitMap stats bitmap to flip off
     */
    void updateStateBitsByIds(@Param(ITEM_IDS) Collection<Long> ids,
                              @Param(ON_STATES_BIT_MAP) long onStateBitMap,
                              @Param(OFF_STATES_BIT_MAP) long offStateBitMap);

    /**
     * Update states for successful publish items in the package.
     *
     * @param packageId        the package id
     * @param successOnMask    states to flip on for successful items
     * @param successOffMask   states to flip off for successful items
     * @param failureOnMask    states to flip on for failed items
     * @param failureOffMask   states to flip off for failed items
     * @param itemSuccessState the state of the successful items to filter
     */
    void updateForCompletePackage(@Param(PACKAGE_ID) long packageId,
                                  @Param(SUCCESS_ON_BIT_MAP) long successOnMask,
                                  @Param(SUCCESS_OFF_BIT_MAP) long successOffMask,
                                  @Param(FAILURE_ON_BIT_MAP) long failureOnMask,
                                  @Param(FAILURE_OFF_BIT_MAP) long failureOffMask,
                                  @Param(PublishDAO.ITEM_SUCCESS_STATE) long itemSuccessState);
}
