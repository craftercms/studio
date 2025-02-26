/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public interface ItemDAO {

	String STATES = "states";

	String SITE_ID = "siteId";
	String NON_CONTENT_ITEM_TYPES = "nonContentItemTypes";

	String ON_STATES_BIT_MAP = "onStatesBitMap";

	String OFF_STATES_BIT_MAP = "offStatesBitMap";
	String TIMESTAMP = "timestamp";
	String ITEM_STATE_MASK = "itemStateMask";

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
	List<ContentItem> getChildrenByPath(@Param(SITE_ID) Long siteId,
										@Param(PATH) String path,
										@Param(LOCALE_CODE) String localeCode,
										@Param(KEYWORD) String keyword,
										@Param(SYSTEM_TYPES) List<String> systemTypes,
										@Param(EXCLUDE_SYSTEM_TYPES) List<String> excludeSystemTypes,
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
	 * Get item for given site and path
	 *
	 * @param siteId site identifier
	 * @param path   path of the item
	 * @return item for given site and path
	 */
	ContentItem getContentItemByPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

	/**
	 * Get item with prefer content option for given site and path
	 *
	 * @param siteId site identifier
	 * @param path   path of the item
	 * @return item for given site and path
	 */
	ContentItem getContentItemByPathPreferContent(@Param(SITE_ID) long siteId, @Param(PATH) String path);

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
	void deleteBySiteAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

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
	 * Get content items for given paths
	 *
	 * @param siteId        site identifier
	 * @param paths         paths to get items for
	 * @param preferContent indicates if pages should be returned instead of folders when available
	 * @return list of items
	 */
	List<ContentItem> getContentItemsByPath(@Param(SITE_ID) Long siteId, @Param(PATHS) Collection<String> paths,
											@Param(PREFER_CONTENT) boolean preferContent);

	/**
	 * Get an item by site and path
	 *
	 * @param siteId        site identifier
	 * @param path          path of the item
	 * @param preferContent if true return pages (if exist) instead of their containing folders
	 * @return list of items
	 */
	default Item getItemByPath(long siteId, String path, boolean preferContent) {
		List<Item> items = getItemsByPath(siteId, List.of(path), preferContent);
		if (items.isEmpty()) {
			return null;
		}
		return items.getFirst();
	}

	/**
	 * Get items by site and path
	 *
	 * @param siteId        site identifier
	 * @param paths         paths of the items
	 * @param preferContent if true return pages (if exist) instead of their containing folders
	 * @return list of items
	 */
	List<Item> getItemsByPath(@Param(SITE_ID) long siteId,
							  @Param(PATHS) Collection<String> paths,
							  @Param(PREFER_CONTENT) boolean preferContent);

	/**
	 * Count all content items in the system
	 *
	 * @return number of content items in the system
	 */
	int countAllContentItems(@Param(NON_CONTENT_ITEM_TYPES) List<String> nonContentItemTypes);

	/**
	 * Finds all items related to a given content-type
	 *
	 * @param siteId      the id of the site
	 * @param contentType the id of the content-type
	 * @param scriptPath  the path of the controller script
	 * @return the list of items
	 */
	List<LightItem> getContentTypeUsages(@Param(SITE_ID) String siteId, @Param(CONTENT_TYPE) String contentType,
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
	int getItemByStatesTotal(@Param(SITE_ID) String siteId, @Param(PATH) String path,
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
	 * @return list of items
	 */
	List<ContentItem> getContentItemsByStates(@Param(SITE_ID) String siteId,
											  @Param(PATH) String path,
											  @Param(STATES_BIT_MAP) Long states,
											  @Param(SYSTEM_TYPES) List<String> systemTypes,
											  @Param(SORT_FIELDS) List<SortField> sortFields,
											  @Param(OFFSET) int offset,
											  @Param(LIMIT) int limit);

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

	/**
	 * Get a list of the unpublished paths for the given site
	 *
	 * @param siteId the site id
	 * @return list of unpublished paths
	 */
	default Collection<String> getUnpublishedPaths(final long siteId) {
		return getUnpublishedPathsInternal(siteId, UNPUBLISHED_MASK);
	}

	/**
	 * Get a list of the unpublished paths for the given site
	 *
	 * @param siteId the site id
	 * @param states unpublished state mask
	 * @return list of unpublished paths
	 */
	Collection<String> getUnpublishedPathsInternal(@Param(SITE_ID) long siteId,
												   @Param(STATES) long states);

	/**
	 * Get all the non-folder children of the given paths, recursively.
	 *
	 * @param siteId the site id
	 * @param paths  the paths to get children for
	 * @return list of children as {@link LightItem}
	 */
	Collection<LightItem> getSubtreeItems(@Param(SITE_ID) String siteId,
										  @Param(PATHS) Collection<String> paths);

	/**
	 * Get {@link ItemPathAndState} records for the given paths in a map by path
	 *
	 * @param siteId the site id
	 * @param paths  the collection of paths to retrieve states for
	 * @return Map of path -> {@link ItemPathAndState}
	 */
	@MapKey(PATH)
	Map<String, ItemPathAndState> getItemStates(@Param(SITE_ID) String siteId, @Param(PATHS) Collection<String> paths);

	/*
	 * Recalculate the parent id for all the items in the site
	 *
	 * @param siteId the site id
	 */
	void updateParentIdForSite(@Param(SITE_ID) long siteId);

	/**
	 * Check if any of the items in the given paths match the given state mask
	 *
	 * @param siteId        the site id
	 * @param paths         the paths to match states for
	 * @param itemStateMask the state mask to match
	 * @return true if any of the items match the state mask, false otherwise
	 */
	boolean matchItemState(@Param(SITE_ID) String siteId,
						   @Param(PATHS) Collection<String> paths,
						   @Param(ITEM_STATE_MASK) long itemStateMask);

	/**
	 * Recalculate the parent id for the given paths
	 *
	 * @param siteId the site id
	 * @param paths  the paths to update
	 */
	void updateParentId(@Param(SITE_ID) long siteId, @Param(PATHS) Collection<String> paths);
}
