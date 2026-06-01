/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.item;

import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemPathAndState;
import org.craftercms.studio.api.v2.dal.item.ContentItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemService {

	/**
	 * Get item fir given site and path
	 *
	 * @param siteId site identifier
	 * @param path   item path
	 * @return item
	 */
	Item getItem(String siteId, String path);

	/**
	 * Get item fir given site and path
	 *
	 * @param siteId        site identifier
	 * @param path          item path
	 * @param preferContent if true return content item if available
	 * @return item
	 */
	Item getItem(String siteId, String path, boolean preferContent);

	/**
	 * Delete item with the given path.
	 *
	 * @param siteId siteIdentifier
	 * @param path   path of item to be deleted
	 * @param removePageParentFolder flag to indicate that parent folder for page should be removed if path corresponds to a page
	 */
	void deleteItem(long siteId, String path, boolean removePageParentFolder);

	/**
	 * Set system processing for item
	 *
	 * @param siteId             site identifier
	 * @param path               path of the item
	 * @param isSystemProcessing true if item is being processed by system, otherwise false
	 */
	default void setSystemProcessing(String siteId, String path, boolean isSystemProcessing) {
		setSystemProcessingBulk(siteId, List.of(path), isSystemProcessing);
	}

	/**
	 * Set system processing for items
	 *
	 * @param siteId             site identifier
	 * @param paths              paths of items
	 * @param isSystemProcessing true if item is being processed by system, otherwise false
	 */
	void setSystemProcessingBulk(String siteId, Collection<String> paths, boolean isSystemProcessing);

	Item.Builder instantiateItem(String siteName, String path);

	/**
	 * Get browser url for given repository item
	 *
	 * @param site site identifier
	 * @param path path of the content
	 * @return browser url
	 */
	String getBrowserUrl(String site, String path) throws SiteNotFoundException;

	/**
	 * Persist item metadata after create
	 *
	 * @param siteId   site identifier
	 * @param path     path of the content
	 * @param parentId id of parent item
	 * @throws ServiceLayerException if there is an error persisting the item
	 * @throws UserNotFoundException if the user is not found
	 */
	void persistItemAfterCreate(String siteId, String path, Long parentId)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException;

	/**
	 * Persist item metadata after write
	 *
	 * @param siteId site identifier
	 * @param path   path of the content
	 */
	void persistItemAfterWrite(String siteId, String path) throws ServiceLayerException, UserNotFoundException, AuthenticationException;

	/**
	 * Persist item metadata after create folder
	 *
	 * @param siteId     site identifier
	 * @param folderPath folder path
	 * @param folderName folder name
	 * @param parentId   id of parent item
	 * @throws ServiceLayerException if there is an error persisting the item
	 * @throws UserNotFoundException if the user is not found
	 */
	void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, Long parentId)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException;

	/**
	 * Move item
	 *
	 * @param siteId   site identifier
	 * @param oldPath  old path
	 * @param newPath  new path
	 * @param parentId new parent ID
	 * @param label    new label
	 * @param userId   ID of the user performing the move operation
	 */
	void moveItem(String siteId, String oldPath, String newPath, Long parentId, String label, long userId)
			throws SiteNotFoundException;

	/**
	 * Copy matching items from one path to another.
	 *
	 * @param siteId     the site id
	 * @param sourcePath the source path of the item to copy
	 * @param targetPath the target path of the item to copy
	 * @param parentId   the new  parent ID
	 * @param label      the new label
	 * @param userId     the ID of the user performing the copy operation
	 */
	void copyItem(String siteId, String sourcePath, String targetPath, long parentId, String label, long userId) throws SiteNotFoundException;

	/**
	 * Check if item is new
	 *
	 * @param siteId site identifier
	 * @param path   path of the item
	 * @return true if NEW flag is set otherwise false
	 */
	boolean isNew(String siteId, String path);

	/**
	 * Count all content items
	 *
	 * @return number of content items in the system
	 */
	int countAllContentItems();

	/**
	 * Get the paths for items that are not published
	 *
	 * @param siteId the site id
	 * @return list of paths for non-folder unpublished items
	 */
	Collection<String> getUnpublishedPaths(long siteId);

	/**
	 * Check if any of the items is in system processing
	 *
	 * @param site  the site id
	 * @param paths the paths to check
	 * @return true if any of the given items is in system processing
	 */
	boolean isSystemProcessing(String site, Collection<String> paths);

	/**
	 * Check if item is in system processing
	 *
	 * @param site site identifier
	 * @param path item path
	 * @return true if item is in system processing
	 */
	default boolean isSystemProcessing(String site, String path) {
		return isSystemProcessing(site, List.of(path));
	}

	/**
	 * Lock item for given lock owner
	 *
	 * @param siteId   site identifier
	 * @param path     item path
	 * @param username user that owns the lock
	 */
	void lockItemByPath(String siteId, String path, String username)
		throws UserNotFoundException, ServiceLayerException;

	/**
	 * Unlock item
	 *
	 * @param siteId site identifier
	 * @param path   item path
	 */
	void unlockItemByPath(String siteId, String path);

	/**
	 * Get total number of item states records for given filters by path regex and states mask
	 *
	 * @param siteId site identifier
	 * @param path   path regex to filter items
	 * @param states states mask to filter items by state
	 * @return number of records
	 */
	int getItemByStatesTotal(String siteId, String path, Long states, List<String> systemTypes);

	/**
	 * Get item states for given paths
	 *
	 * @param siteId the site id
	 * @param paths  the collection of paths to retrieve the states for
	 * @return Map of path -> {@link ItemPathAndState}
	 */
	Map<String, ItemPathAndState> getItemStates(String siteId, Collection<String> paths);

	/**
	 * Get item states for given filters by path regex and states mask
	 *
	 * @param siteId      site identifier
	 * @param path        path regex to filter items
	 * @param states      states mask to filter items by state
	 * @param systemTypes system types to filter items
	 * @param sortFields  sort fields
	 * @param offset      offset for the first record in result set
	 * @param limit       number of item states records to return
	 * @return list of sandbox items
	 */
	List<ContentItem> getItemsByStates(String siteId, String path, Long states, List<String> systemTypes, List<SortField> sortFields, int offset, int limit);

	/**
	 * Update item state flags for given items
	 *
	 * @param siteId                site identifier
	 * @param paths                 item paths
	 * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
	 * @param clearUserLocked       if true clear user locked flag, otherwise ignore
	 * @param live                  if true set live flag, otherwise reset it
	 * @param staged                if true set staged flag, otherwise reset it
	 * @param isNew                 value to set the 'new' flag to, or null if the flag should not change
	 * @param modified              value to set the 'modified' flag to, or null if the flag should not change
	 */
	void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked,
			      Boolean live, Boolean staged, Boolean isNew, Boolean modified);

	/**
	 * Update item state flags for given path query
	 *
	 * @param siteId                site identifier
	 * @param path                  path regex to identify items
	 * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
	 * @param clearUserLocked       if true clear user locked flag, otherwise ignore
	 * @param live                  if true set live flag, otherwise reset it
	 * @param staged                if true set staged flag, otherwise reset it
	 * @param isNew                 value to set the 'new' flag to, or null if the flag should not change
	 * @param modified              value to set the 'modified' flag to, or null if the flag should not change
	 */
	void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
				     boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified);

	/**
	 * Updates a new page's children (in case the folder existed before the page was created)
	 * to point to the new page.
	 *
	 * @param site site identifier
	 * @param path path of the folder where the new index.xml has been added
	 */
	void updateNewPageChildren(String site, String path);

	/**
	 * Get the non-folder paths of the children of the item at the given path, recursively.
	 *
	 * @param siteId the site id
	 * @param path   the path to get children for
	 * @return list of children paths
	 */
	Collection<String> getChildrenPaths(long siteId, String path);

	/**
	 * Recalculate the parent id for the given paths
	 *
	 * @param siteId the site id
	 * @param paths  the paths to update
	 */
	void updateParentId(long siteId, Collection<String> paths);
}
