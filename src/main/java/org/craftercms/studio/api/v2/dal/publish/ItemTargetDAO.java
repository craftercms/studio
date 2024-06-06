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

package org.craftercms.studio.api.v2.dal.publish;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * Provide access to the item_target data.
 */
public interface ItemTargetDAO {

    String SITE_ID = "siteId";
    String PATH = "path";
    String ITEM_ID = "itemId";
    String ITEM_IDS = "itemIds";
    String TARGET = "target";

    /**
     * Get the item target records by item path
     *
     * @param siteId the site id
     * @param path   the item path
     * @return the item target record
     */
    Collection<ItemTarget> getByItemPath(@Param(SITE_ID) long siteId, @Param(PATH) String path);

    /**
     * Clear all records for the given item
     *
     * @param itemId the item id
     */
    void clearForItem(@Param(ITEM_ID) long itemId);

    /**
     * Clear the record (if exists) for the given item and target
     *
     * @param itemId the item id
     * @param target the target
     */
    void clearForItemAndTarget(@Param(ITEM_ID) long itemId, @Param(TARGET) String target);

    /**
     * Clear all records for the given site and target
     *
     * @param siteId the site id
     * @param target the target
     */
    void clearForSiteAndTarget(@Param(SITE_ID) long siteId, @Param(TARGET) String target);

    /**
     * Clear item_target record for the given ids
     *
     * @param itemIds the item ids
     * @param target  the target
     */
    void clearForItemIds(@Param(ITEM_IDS) Collection<Long> itemIds, @Param(TARGET) String target);
}
