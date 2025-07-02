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

package org.craftercms.studio.api.v2.service.content;

import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException;
import org.craftercms.studio.api.v2.exception.content.EmptyChangesetException;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.content.*;
import org.craftercms.studio.model.rest.content.GetChildrenBulkRequest.PathParams;
import org.dom4j.Document;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.*;

/**
 * Provide access to content operations
 */
public interface ContentService {

	/**
	 * Check the existent of a content path
	 *
	 * @param siteId site identifier
	 * @param path   content path
	 * @return true if the content exists, false otherwise
	 * @throws SiteNotFoundException if site is not found
	 */
	boolean contentExists(String siteId, String path) throws SiteNotFoundException;

	/**
	 * This is a faster, but less accurate, version of contentExists. This prioritizes
	 * performance over checking the actual underlying repository if the content is actually in the store
	 * or we simply hold a reference to the object in the actual store.
	 *
	 * @return true if site has content object at path
	 */
	boolean shallowContentExists(String site, String path) throws SiteNotFoundException;

	/**
	 * Get child items for given paths. Child item is
	 * - belongs to item subtree
	 * - is item specific dependency
	 *
	 * @param siteId site identifier
	 * @param paths  list of paths to get child items for
	 * @return list of paths of child items
	 */
	List<LightItem> getChildItems(String siteId, List<String> paths) throws SiteNotFoundException;

	/**
	 * Delete content for given paths. Following content will be deleted:
	 * - given paths
	 * - child items for given paths
	 *
	 * @param siteId         site identifier
	 * @param paths          content to be deleted
	 * @param publishTitle   title of the publish package
	 * @param publishComment submitter comment of the publish package
	 * @return id of publish package, or 0 if no package was created (if the site has not been published)
	 * @throws ServiceLayerException   general service error
	 * @throws AuthenticationException authentication error
	 */
	DeleteContentResult deleteContent(String siteId, Set<String> paths, String publishTitle, String publishComment)
			throws ServiceLayerException, AuthenticationException, UserNotFoundException;

	/**
	 * Get list of children for given path
	 *
	 * @param siteId       site identifier
	 * @param path         item path to children for
	 * @param locale       filter children by locale
	 * @param keyword      filter children by keyword
	 * @param types        filter children by type
	 * @param excludes     exclude items by path
	 * @param sortStrategy sort order
	 * @param order        ascending or descending
	 * @param offset       offset of the first child in the result
	 * @param limit        number of children to return
	 * @return list of children
	 */
	GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String keyword, List<String> types,
										List<String> excludes, String sortStrategy, String order, int offset, int limit)
			throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get children for paths bulk.
	 * This method will return children for a list of paths. Result items will also
	 * include a {@link ContentItem} object for the item itself.
	 *
	 * @param siteId     the site id
	 * @param paths      paths to get children for. Notice that this parameter is redundant with the pathParams. This list of paths is used to
	 *                   validate permissions.
	 * @param pathParams Map of extra parameters for each path
	 * @return object containing a list of {@link org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult.ChildrenByPathResult}
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException user not found (when calculating available actions)
	 */
	GetChildrenByPathsBulkResult getChildrenByPaths(String siteId, List<String> paths, Map<String, PathParams> pathParams)
			throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get a content item by path
	 *
	 * @param siteId  site identifier
	 * @param path    item path
	 * @param flatten indicates if descriptors should be flattened
	 * @return content item
	 * @throws SiteNotFoundException    if site is not found
	 * @throws ContentNotFoundException if content is not found
	 */
	Item getItem(String siteId, String path, boolean flatten) throws SiteNotFoundException, ContentNotFoundException;

	/**
	 * Get a content item descriptor by path
	 *
	 * @param siteId  site identifier
	 * @param path    item path
	 * @param flatten indicates if descriptors should be flattened
	 * @return item descriptor
	 * @throws SiteNotFoundException    if site is not found
	 * @throws ContentNotFoundException if content is not found
	 */
	Document getItemDescriptor(String siteId, String path, boolean flatten) throws SiteNotFoundException, ContentNotFoundException;

	/**
	 * Check if the content is part of any ready/processing publish package and fail if it is
	 *
	 * @param siteId          the site id
	 * @param paths           the paths to check
	 * @param includeChildren if true, check if any children of the paths are part of a publish package
	 * @throws ContentInPublishQueueException if the content is part of a publish package
	 */
	void assertNotInWorkflow(String siteId, Collection<String> paths, boolean includeChildren) throws ContentInPublishQueueException;


	/**
	 * Get content size
	 *
	 * @param siteId site identifier
	 * @param path   content path
	 * @return size in bytes
	 */
	long getContentSize(String siteId, String path);

	/**
	 * Check if item is editable
	 *
	 * @param itemPath     item path
	 * @param itemMimeType item mime type
	 * @return true if item is editable
	 */
	boolean isEditable(String itemPath, String itemMimeType);

	/**
	 * Get a list of items by state and system types.
	 *
	 * @param siteId       site identifier
	 * @param statesBitMap mask of the states to filter by
	 * @param systemTypes  list of system types to filter by
	 * @param sortFields   list of sort fields
	 * @param offset       number of items to skip
	 * @param limit        number of items to return
	 * @return list of items
	 * @throws UserNotFoundException if user is not found
	 * @throws ServiceLayerException if an error occurs while getting the items
	 */
	List<ContentItem> getContentItemsByStates(String siteId, long statesBitMap,
											  List<String> systemTypes, List<SortField> sortFields,
											  int offset, int limit) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Get detailed item for given path
	 *
	 * @param siteId        site identifier
	 * @param path          item path
	 * @param preferContent if true return content item if available
	 * @return detailed item
	 */
	ContentItem getItemByPath(String siteId, String path, boolean preferContent)
			throws ServiceLayerException, UserNotFoundException;

	/**
	 * Get sandbox items for given list of paths
	 *
	 * @param siteId        site identifier
	 * @param paths         list of paths to get sandbox items
	 * @param preferContent if true return content items if available
	 * @return list of sandbox items
	 */
	List<ContentItem> getContentItemsByPath(String siteId, List<String> paths, boolean preferContent)
			throws ServiceLayerException, UserNotFoundException;

	/**
	 * Lock item by path for given site
	 *
	 * @param siteId site identifier
	 * @param path   path to lock
	 */
	void lockContent(String siteId, String path) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Unlock item by path for given site
	 *
	 * @param siteId site identifier
	 * @param path   item path
	 */
	void unlockContent(String siteId, String path) throws ContentNotFoundException, SiteNotFoundException;

	/**
	 * Get content for commit id
	 *
	 * @param siteId   site identifier
	 * @param path     path of the content
	 * @param commitId commit id of the content version
	 * @return the content if available
	 */
	Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
			throws ContentNotFoundException;

	/**
	 * Rename content for given path
	 *
	 * @param site site identifier
	 * @param path path of the content
	 * @param name new name of the content
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException user not found error
	 * @throws ValidationException   validation exception
	 */
	void renameContent(String site, String path, String name)
			throws ServiceLayerException, UserNotFoundException, ValidationException, AuthenticationException;

	/**
	 * Move content from sourcePath to targetPath.
	 * Notice that both paths must be full paths. e.g.: A rename would look like /site/website/page1 to /site/website/page2
	 *
	 * @param siteId     the site id
	 * @param sourcePath the source path
	 * @param targetPath the target path
	 * @return {@link PasteContentResult} object containing the affected paths
	 * @throws ServiceLayerException   if there is an error moving the content
	 * @throws UserNotFoundException   if the current user is not found
	 * @throws AuthenticationException if there is an error retrieving the currently authenticated user
	 */
	PasteContentResult move(String siteId, String sourcePath, String targetPath)
			throws ServiceLayerException, UserNotFoundException, AuthenticationException;

	/**
	 * Alternative method to move content from sourcePath to targetParent.
	 * The difference with the {@link #move(String, String, String)} method is that this method will accept a target parent path (vs the full path),
	 * so it will try to calculate the target path based on the source file name and the existing content in the target parent.
	 *
	 * @param siteId       the site id
	 * @param sourcePath   the source path
	 * @param targetParent the target parent path where the content will be moved to
	 * @return {@link PasteContentResult} object containing the affected paths
	 * @throws ServiceLayerException   if there is an error moving the content
	 * @throws UserNotFoundException   if the current user is not found
	 * @throws AuthenticationException if there is an error retrieving the currently authenticated user
	 */
	PasteContentResult moveToParentPath(String siteId, String sourcePath, String targetParent)
			throws ServiceLayerException, UserNotFoundException, AuthenticationException;


	/**
	 * Returns content wrapped as a {@link Resource} instance
	 *
	 * @param site the site id
	 * @param path the path of the content
	 * @return the resource object
	 * @throws ContentNotFoundException if there is no content at the given path
	 */
	Resource getContentAsResource(String site, String path) throws ContentNotFoundException;

	/**
	 * Get the version history for a given content item.
	 *
	 * @param siteId the site id
	 * @param path   the content path
	 * @return the list of versions
	 * @throws ServiceLayerException if an error occurs while create the list of {@link ItemVersion}s
	 */
	List<ItemVersion> getContentVersionHistory(String siteId, String path) throws ServiceLayerException;

	/**
	 * Write content to the given path
	 *
	 * @param siteId  the site id
	 * @param path    the content path
	 * @param content the content to write
	 * @return the result of the write operation, which includes affected paths
	 * @throws EmptyChangesetException if the write operation results in an empty changeset (i.e.: try to write the same content the repository already has)
	 * @throws ServiceLayerException   if an error occurs while writing the content
	 */
	WriteContentResult write(String siteId, String path, InputStream content) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Copy content from sourcePath to targetPath.
	 * This method will not necessarily copy the whole sourcePath, but copy the paths listed in <code>copyPaths</code> parameter, replacing
	 * the targetPath with the sourcePath.
	 *
	 * @param siteId     the site id
	 * @param sourcePath the root source path to copy the content from
	 * @param targetPath the target path to copy the content to
	 * @param copyPaths  the list of paths to copy
	 * @return the result of the copy operation, which includes affected paths
	 */
	PasteContentResult copy(String siteId, String sourcePath, String targetPath, Set<String> copyPaths)
			throws ServiceLayerException, UserNotFoundException;
}
