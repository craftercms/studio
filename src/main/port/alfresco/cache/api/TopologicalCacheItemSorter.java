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
 * {@link CacheItem} sorter that sorts the items according to their dependency relationships. The dependency
 * relationships in a {@link Cache} basically form a directed acyclic graph, so using a topological sort algorithm is
 * a perfect solution for rearranging the items so that items are refreshed only after their dependencies in the list
 * are refreshed.
 *
 * @author Alfonso Vásquez
 *
 * @see <a href="http://en.wikipedia.org/wiki/Topological_sorting">Topological sorting</a>
 */
public interface TopologicalCacheItemSorter {

    /**
     * Sorts the given {@link CacheItem}s through a topological sorting algorithm.
     *
     * @param items
     *          the items to sort
     * @param cache
     *          the cache, used to get the dependencies when needed for the sorting.
     * @return the sorted items
     */
    List<CacheItem> sortTopologically(List<CacheItem> items, Cache cache);
}
