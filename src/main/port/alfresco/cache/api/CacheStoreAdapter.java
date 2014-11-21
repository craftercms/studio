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

import java.util.Collection;

/**
 * Adapter to the real data structure used to store the items of a cache.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public interface CacheStoreAdapter {

    /**
     * Returns true if the specified scope exists in the cache.
     *
     * @throws Exception
     */
    boolean hasScope(String scope) throws Exception;

    /**
     * Returns the scopes managed by the underlying store.
     *
     * @throws Exception
     */
    Collection<String> getScopes() throws Exception;

    /**
     * Adds a new scope to the underlying store.
     *
     * @param scope
     *          the name of the scope
     * @param maxItemsInMemory
     *          the maximum number of items in memory, before they are evicted
     * @throws Exception
     */
    void addScope(String scope, int maxItemsInMemory) throws Exception;

    /**
     * Removes an existing scope and all the items that are in it.
     *
     * @param scope
     *          scope to remove
     * @throws Exception
     */
    void removeScope(String scope) throws Exception;

    /**
     * Returns the quantity of items present in the specified scope.
     *
     * @return the size of the scope (quantity of items)
     * @throws Exception
     */
    int getSize(String scope) throws Exception;

    /**
     * Returns a list of the keys of the items present in the specified scope.
     *
     * @return a list of the keys of the available items in the scope
     * @throws Exception
     */
    Collection<Object> getKeys(String scope) throws Exception;

    /**
     * Returns true if there's and item with the specified key in the specified scope.
     *
     * @return true if the key exists in the scope, false otherwise
     * @throws Exception
     */
    boolean hasKey(String scope, Object key) throws Exception;

    /**
     * Retrieves an item from a scope.
     *
     * @param scope
     *            scope to get the item from
     * @param key
     *            unique key for the item within this scope
     * @return the requested item if found, null otherwise
     * @throws Exception
     */
    org.craftercms.cstudio.alfresco.cache.api.CacheItem get(String scope, Object key) throws Exception;

    /**
     * Puts an item in a scope. The scope is obtained through {@link CacheItem#getScope()}.
     *
     * @param item
     *          the item to put
     * @throws Exception
     */
    void put(org.craftercms.cstudio.alfresco.cache.api.CacheItem item) throws Exception;

    /**
     * Removes an item from a scope.
     *
     * @param scope
     *            scope to remove the item from
     * @param key
     *            unique key for the item within this scope
     * @return true if the removal was successful, false otherwise
     * @throws Exception
     */
    boolean remove(String scope, Object key) throws Exception;

    /**
     * Clears the contents of the entire store.
     *
     * @throws Exception
     */
    void clearAll() throws Exception;

    /**
     * Clears the contents of the specified scope in the underlying store.
     *
     * @param scope
     *            scope to clear
     * @throws Exception
     */
    void clearScope(String scope) throws Exception;
}
