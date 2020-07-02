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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENTRIES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ITEM_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LEVEL_DESCRIPTOR_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LEVEL_DESCRIPTOR_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCALE_CODE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORDER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PARENT_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PARENT_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ROOT_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATES_BIT_MAP;

public interface ItemDAO {

    /**
     * Get total number of children for given path
     *
     * @param siteId site identifier
     * @param path path to get children for
     * @param ldName level descriptor name
     * @param localeCode local code
     *
     * @return total number of children
     */
    int getChildrenByPathTotal(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                               @Param(LEVEL_DESCRIPTOR_NAME) String ldName, @Param(LOCALE_CODE) String localeCode);

    /**
     * Get children for given path from database
     *
     * @param siteId site identifier
     * @param parentPath parent path
     * @param ldPath level descriptor path
     * @param path path to get children for
     * @param localeCode locale code
     * @param sortStrategy sort strategy
     * @param order order of children
     * @param offset offset of the first record to return
     * @param limit number of children to return
     *
     * @return list of items (parent, level descriptor, children)
     */

    List<Item> getChildrenByPath(@Param(SITE_ID) Long siteId, @Param(PARENT_PATH) String parentPath,
                                 @Param(LEVEL_DESCRIPTOR_PATH) String ldPath,
                                 @Param(LEVEL_DESCRIPTOR_NAME) String ldName, @Param(PATH) String path,
                                 @Param(LOCALE_CODE) String localeCode, @Param(SORT) String sortStrategy,
                                 @Param(ORDER) String order, @Param(OFFSET) int offset, @Param(LIMIT) int limit);



    /**
     * Get total number of children for given path
     *
     * @param siteId site identifier
     * @param parentId item id to get children for
     * @param ldName level descriptor name
     * @param localeCode local code
     *
     * @return total number of children
     */
    int getChildrenByIdTotal(@Param(SITE_ID) String siteId, @Param(PARENT_ID) String parentId,
                             @Param(LEVEL_DESCRIPTOR_NAME) String ldName, @Param(LOCALE_CODE) String localeCode);
    /**
     * Get children for given id from database
     * @param siteId site identifier
     * @param parentId parent identifier
     * @param localeCode locale code
     * @param sortStrategy sort strategy
     * @param order order of children
     * @param offset offset of the first record to return
     *      * @param limit number of children to return
     * @return list of items (parent, level descriptor, children)
     */
    List<Item> getChildrenById(@Param(SITE_ID) String siteId, @Param(PARENT_ID) String parentId,
                                      @Param(LEVEL_DESCRIPTOR_NAME) String ldName,
                                      @Param(LOCALE_CODE) String localeCode, @Param(SORT) String sortStrategy,
                                      @Param(ORDER) String order, @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Update parent ID for site
     * @param siteId site identifier
     */
    void updateParentIdForSite(@Param(SITE_ID) long siteId, @Param(ROOT_PATH) String rootPath);

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
     * @return item identified by given id
     */
    Item getItemById(@Param(ID) long id);

    /**
     * Get item for given site and path
     * @param siteId site identifier
     * @param path path of the item
     * @return item for given site and path
     */
    Item getItemBySiteIdAndPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

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
}
