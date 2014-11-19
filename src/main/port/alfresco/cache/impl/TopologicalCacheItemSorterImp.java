/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.alfresco.cache.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.cstudio.alfresco.cache.api.Cache;
import org.craftercms.cstudio.alfresco.cache.api.CacheItem;
import org.craftercms.cstudio.alfresco.cache.api.TopologicalCacheItemSorter;

import java.util.*;

public class TopologicalCacheItemSorterImp implements TopologicalCacheItemSorter {

    private static final Log logger = LogFactory.getLog(TopologicalCacheItemSorterImp.class);

    /**
     * Sorts the given {@link org.craftercms.cstudio.alfresco.cache.api.CacheItem}s through the following depth-first search based topological sorting
     * algorithm:
     *
     * L <- Empty list that will contain the sorted nodes
     * S <- Set of all nodes with no incoming edges
     * for each node n in S do
     *     visit(n)
     * function visit(node n)
     *     if n has not been visited yet then
     *         mark n as visited
     *         for each node m with an edge from n to m do
     *             visit(m)
     *         add n to L
     *
     * @param items
     *          the items to sort
     * @return the sorted items
     *
     * @see <a href="http://en.wikipedia.org/wiki/Topological_sorting">Topological sorting</a>
     */
    public List<CacheItem> sortTopologically(List<CacheItem> items, Cache cache) {
        // List of sorted nodes, or L.
        List<CacheItem> sortedItems = new ArrayList<CacheItem>();
        // Nodes with no incoming edges, or S.
        List<CacheItem> itemsWithNoDependants = getItemsWithNoDependants(items, cache);
        // Visited nodes.
        Set<CacheItem> visitedItems = new HashSet<CacheItem>();

        // for each node n in S do
        for (CacheItem item : itemsWithNoDependants) {
            visitItem(item, visitedItems, sortedItems, cache);
        }

        // Keep only items that are contained in the original list.
        for (Iterator<CacheItem> i = sortedItems.iterator(); i.hasNext();) {
            CacheItem item = i.next();
            if (!items.contains(item)) {
                i.remove();
            }
        }

        return sortedItems;
    }

    /**
     * The visit(n) function of the sorting algorithm. Adds the items to the sorted list by depth-first.
     */
    private void visitItem(CacheItem item, Set<CacheItem> visitedItems, List<CacheItem> sortedItems, Cache cache) {
        // if n has not been visited yet then
        if (!visitedItems.contains(item)) {
            // mark n as visited
            visitedItems.add(item);

            // for each node m with an edge from n to m do
            List<Object> dependencyKeys = item.getDependencyKeys();
            if (CollectionUtils.isNotEmpty(dependencyKeys)) {
                for (Object dependencyKey : dependencyKeys) {
                    CacheItem dependency = getCacheItem(cache, item.getScope(), dependencyKey);
                    if (dependency != null) {
                        visitItem(dependency, visitedItems, sortedItems, cache);
                    }
                }
            }

            // add n to L
            sortedItems.add(item);
        }
    }

    /**
     * Returns the list of items with no dependants (in the algorithm: S <- Set of all nodes with no incoming edges).
     */
    private List<CacheItem> getItemsWithNoDependants(List<CacheItem> items, Cache cache) {
        List<CacheItem> itemsWithNoDependants = new ArrayList<CacheItem>(items);

        for (Iterator<CacheItem> i = items.iterator(); i.hasNext();) {
            boolean isDependency = false;
            CacheItem possibleDependency = i.next();

            for (Iterator<CacheItem> j = items.iterator(); j.hasNext() && !isDependency;) {
                CacheItem item = j.next();

                if (!item.equals(possibleDependency)) {
                    if (isDependency(item, possibleDependency, cache)) {
                        itemsWithNoDependants.remove(possibleDependency);

                        isDependency = true;
                    }
                }
            }
        }

        return itemsWithNoDependants;
    }

    /**
     * Returns true if {@code possibleDependency} is a direct or indirect dependency of {@code item}.
     */
    private boolean isDependency(CacheItem item, CacheItem possibleDependency, Cache cache) {
        List<Object> dependencyKeys = item.getDependencyKeys();
        if (CollectionUtils.isNotEmpty(dependencyKeys)) {
            for (Object dependencyKey : dependencyKeys) {
                if (dependencyKey.equals(possibleDependency.getKey())) {
                    return true;
                } else {
                    CacheItem dependency = getCacheItem(cache, item.getScope(), dependencyKey);
                    if (dependency != null && isDependency(dependency, possibleDependency, cache)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns the {@link CacheItem} for the given scope and key, or null if there's no item or an exception
     * was thrown (the exception is logged).
     */
    private CacheItem getCacheItem(Cache cache, String scope, Object key) {
        try {
            return cache.get(scope, key);
        } catch (Exception e) {
            logger.error("Unable to retrieve object with key " + key + " at cache scope " + scope, e);

            return null;
        }
    }
}

