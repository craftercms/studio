/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.cache.api;

import java.util.List;

/**
 * Refreshes a list of {@link CacheItem}s in a {@link Cache}. New values for the items will usually be obtained through
 * {@link CacheLoader}s.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */

public interface CacheRefresher {

    /**
     * Refreshes the specified list of {@link CacheItem}s.
     *
     * @param itemsToRefresh
     * @param cache
     *          the cache where the new item values should be put
     */
    void refreshItems(List<CacheItem> itemsToRefresh, Cache cache);
}
