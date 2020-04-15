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

package org.craftercms.studio.api.v2.service.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.util.List;

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
    List<String> getChildItems(String siteId, List<String> paths);

    /**
     * Delete content for given path. Following content will be deleted:
     *  - given path
     *  - child items for given path
     * @param siteId site identifier
     * @param path content to be deleted
     * @return true if success, otherwise false
     */
    boolean deleteContent(String siteId, String path) throws ServiceLayerException, AuthenticationException, DeploymentException;

    /**
     * Delete content for given paths. Following content will be deleted:
     *  - given paths
     *  - child items for given paths
     * @param siteId site identifier
     * @param paths content to be deleted
     * @return true if success, otherwise false
     */
    boolean deleteContent(String siteId, List<String> paths) throws ServiceLayerException, AuthenticationException, DeploymentException;

    /**
     * Get list of children for given path
     *
     * @param siteId site identifier
     * @param path item path to children for
     * @param locale filter children by locale
     * @param sortStrategy sort order
     * @param order ascending or descending
     * @param offset offset of the first child in the result
     * @param limit number of children to return
     * @return list of children
     */
    GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String sortStrategy, String order,
                                        int offset, int limit);
}
