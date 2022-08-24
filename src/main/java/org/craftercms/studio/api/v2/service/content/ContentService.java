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

package org.craftercms.studio.api.v2.service.content;

import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public interface ContentService {

    /**
     * Get list of content types marked as quick creatable for given site
     *
     * @param siteId site id to use
     * @return list of content types
     */
    List<QuickCreateItem> getQuickCreatableContentTypes(String siteId);

    /**
     * Get child items for given path. Child item is
     *  - belongs to item subtree
     *  - is item specific dependency
     *
     * @param siteId site identifier
     * @param path path to get child items for
     * @return list of paths of child items
     */
    List<String> getChildItems(String siteId, String path);

    /**
     * Get child items for given paths. Child item is
     *  - belongs to item subtree
     *  - is item specific dependency
     *
     * @param siteId site identifier
     * @param paths list of paths to get child items for
     * @return list of paths of child items
     */
    List<String> getChildItems(String siteId, List<String> paths) throws ServiceLayerException;

    /**
     * Get SandboxItems for each the child items for the given paths.Child item is
     * - belongs to item subtree
     * - is item specific dependency
     *
     * @param siteId site identifier
     * @param paths  paths to get child items for
     * @return list of {@link SandboxItem}
     * @throws UserNotFoundException if current user cannot be found to calculate the available actions for the user
     * @throws ServiceLayerException general service error
     */
    List<SandboxItem> getChildSandboxItems(String siteId, List<String> paths) throws ServiceLayerException, UserNotFoundException;

    /**
     * Get SandboxItems for dependents of each of the given paths
     *
     * @param siteId site identifier
     * @param paths  paths to get dependent items for
     * @return list of {@link SandboxItem}
     * @throws UserNotFoundException if current user cannot be found to calculate the available actions for the user
     * @throws ServiceLayerException general service error
     */
    List<SandboxItem> getDependentSandboxItems(String siteId, List<String> paths) throws UserNotFoundException, ServiceLayerException;

    /**
     * Delete content for given path. Following content will be deleted:
     *  - given path
     *  - child items for given path
     * @param siteId site identifier
     * @param path content to be deleted
     * @param submissionComment  submission comment
     * @return true if success, otherwise false
     *
     * @throws ServiceLayerException general service error
     * @throws AuthenticationException authentication error
     * @throws DeploymentException deployment error caused by delete
     */
    boolean deleteContent(String siteId, String path, String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException;

    /**
     * Delete content for given paths. Following content will be deleted:
     *  - given paths
     *  - child items for given paths
     * @param siteId site identifier
     * @param paths content to be deleted
     * @param submissionComment submission comment
     * @return true if success, otherwise false
     *
     * @throws ServiceLayerException general service error
     * @throws AuthenticationException authentication error
     * @throws DeploymentException deployment error caused by delete
     */
    boolean deleteContent(String siteId, List<String> paths, String submissionComment)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException;

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


    Item getItem(String siteId, String path, boolean flatten) throws SiteNotFoundException, ContentNotFoundException;

    /**
     * Get detailed item for given path
     *
     * @param siteId site identifier
     * @param path item path
     * @param preferContent if true return content item if available
     * @return detailed item
     */
    DetailedItem getItemByPath(String siteId, String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get sandbox items for given list of paths
     * @param siteId site identifier
     * @param paths list of paths to get sandbox items
     * @param preferContent if true return content items if available
     * @return list of sandbox items
     */
    List<SandboxItem> getSandboxItemsByPath(String siteId, List<String> paths, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Lock item by path for given site
     * @param siteId site identifier
     * @param path path to lock
     */
    void lockContent(String siteId, String path) throws UserNotFoundException, ServiceLayerException;

    /**
     * Unlock item by path for given site
     * @param siteId site identifier
     * @param path item path
     */
    void unlockContent(String siteId, String path) throws ContentNotFoundException, ContentAlreadyUnlockedException;

    /**
     * Get content for commit id
     * @param siteId  site identifier
     * @param path path of the content
     * @param commitId commit id of the content version
     * @return the content if available
     */
    Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
            throws ContentNotFoundException;

    /**
    * Rename content for given path
    * @param site  site identifier
    * @param path path of the content
    * @param name new name of the content
    * @return true if success, otherwise false
    *
    * @throws ServiceLayerException general service error
    * @throws UserNotFoundException user not found error
    */
    boolean renameContent( String site, String path, String name)
         throws ServiceLayerException, UserNotFoundException;
}
