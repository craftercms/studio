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

package org.craftercms.studio.api.v2.service.content.internal;

import org.craftercms.core.service.Item;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.util.List;

public interface ContentServiceInternal {

    /**
     * Get subtree items for given path.
     *
     * @param siteId site identifier
     * @param path path to get subtree items for
     * @return list of paths of subtree items
     */
    List<String> getSubtreeItems(String siteId, String path);

    /**
     * Get subtree items for given paths.
     *
     * @param siteId site identifier
     * @param path list of paths to get subtree items for
     * @return list of paths of subtree items
     */
    List<String> getSubtreeItems(String siteId, List<String> path);

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
     *
     * @return list of children
     */
    GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String sortStrategy, String order,
                                        int offset, int limit);

    /**
     * Get total number of children for given path
     *
     * @param siteId site identifier
     * @param path item path to children for
     * @param locale filter children by locale
     *
     * @return total number of children
     */
    int getChildrenByPathTotal(String siteId, String path, String locale);

    /**
     * Get list of children for given item id
     *
     * @param siteId site identifier
     * @param parentId item id to get children for
     * @param locale filter children by locale
     * @param sortStrategy sort order
     * @param order ascending or descending
     * @param offset offset of the first child in the result
     * @param limit number of children to return
     *
     * @return list of children
     */
    GetChildrenResult getChildrenById(String siteId, String parentId, String locale, String sortStrategy,
                                      String order, int offset, int limit);

    /**
     * Get total number of children for given path
     *
     * @param siteId site identifier
     * @param parentId item id to children for
     * @param locale filter children by locale
     *
     * @return total number of children
     */
    int getChildrenByIdTotal(String siteId, String parentId, String ldName, String locale);

    Item getItem(String siteId, String path);

}
