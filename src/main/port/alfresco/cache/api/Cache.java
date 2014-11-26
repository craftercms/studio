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


import org.craftercms.cstudio.alfresco.exception.InternalCacheEngineException;
import org.craftercms.cstudio.alfresco.exception.InvalidScopeException;

import java.util.Collection;
import java.util.List;

/**
 * Crafter's Cache API.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */

public interface Cache {

    /**
     * Returns true if the specified scope exists in the cache.
     *
     * @param scope
     *          the name of the scope
     * @return true if the specified scope exists in the cache
     * @throws org.craftercms.cstudio.alfresco.exception.InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    boolean hasScope(String scope) throws InternalCacheEngineException;

    /**
     * Returns the list of scopes this cache manages.
     *
     * @return the list of available scopes in the cache
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    Collection<String> getScopes() throws InternalCacheEngineException;

    /**
     * Adds a new scope to the cache.
     *
     * @param scope
     *          the name of the scope
     * @param maxItemsInMemory
     *          the maximum number of items in memory, before they are evicted
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void addScope(String scope, int maxItemsInMemory) throws InternalCacheEngineException;

    /**
     * Removes an existing scope and all the items that are in it.
     *
     * @param scope
     *          scope to remove
     * @throws org.craftercms.cstudio.alfresco.exception.InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void removeScope(String scope) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Returns the quantity of items present in the specified scope.
     *
     * @return the size of the scope (quantity of items)
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    int getSize(String scope) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Returns a list of the keys of the items present in the specified scope.
     *
     * @return a list of the keys of the available items in the scope
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    Collection<Object> getKeys(String scope) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Returns true if there's and item with the specified key in the specified scope.
     *
     * @return true if the key exists in the scope, false otherwise
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    boolean hasKey(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Retrieves an item from a scope.
     *
     * @param scope
     *            scope to get the item from
     * @param key
     *            unique key for the item within this scope
     * @return the requested item if found, null otherwise
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    CacheItem get(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Retrieves an item from a scope, checking before for the state of it's dependencies.
     *
     * @param scope
     *            scope to get the item from
     * @param key
     *            unique key for the item within this scope
     * @return the requested item if found, null if not or if the item's dependencies have changed
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    public CacheItem getWithDependencyCheck(String scope, Object key) throws InvalidScopeException,
            InternalCacheEngineException;

    /**
     * Puts an item in a scope.
     *
     * @param scope
     *          scope to add the item to
     * @param key
     *          unique key for the item within this scope
     * @param value
     *          value to store in the cache
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void put(String scope, Object key, Object value) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Puts and item in a scope.
     *
     * @param scope
     *          scope to add the item to
     * @param key
     *          unique key for the item within this scope
     * @param value
     *          value to store in the cache
     * @param expireAfter
     *          the amount of time (in ticks, where the tick time span is defined at runtime) before the item can be
     *          removed from cache. Use {@link CacheItem#NEVER_EXPIRE} to indicate that there's no expiration time.
     * @param refreshFrequency
     *          the amount of time (in ticks, where the tick time span is defined at runtime) before items are reloaded
     *          into the cache. Use {@link CacheItem#NEVER_REFRESH} to indicate that there's no need for the item to
     *          be refreshed. Note that when refreshing, there's almost always an old value for the item in the cache
     *          before the new value is loaded into the cache. The {@code loader} param is also required in order for
     *          the item to be refreshed.
     * @param loader
     *          the cache loader used to load a new value when the item needs to be refreshed. If not specified and
     *          the {@code refreshFrequency} is not {@link CacheItem#NEVER_REFRESH}, the cache will use a default one.
     * @param loaderParams
     *          additional parameters the loader could need
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void put(String scope, Object key, Object value, long expireAfter, long refreshFrequency,
             CacheLoader loader, Object... loaderParams) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Puts an item in a scope.
     *
     * @param scope
     *          scope to add the item to
     * @param key
     *          unique key for the item within this scope
     * @param value
     *          value to store in the cache
     * @param dependencyKeys
     *          the keys of the dependencies defined for this item
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void put(String scope, Object key, Object value, List<Object> dependencyKeys) throws InvalidScopeException,
            InternalCacheEngineException;

    /**
     * Puts an item in a scope.
     *
     * @param scope
     *          scope to add the item to
     * @param key
     *          unique key for the item within this scope
     * @param value
     *          value to store in the cache
     * @param dependencyKeys
     *          the keys of the dependencies defined for this item
     * @param expireAfter
     *          the amount of time (in ticks, where the tick time span is defined at runtime) before the item can be
     *          removed from cache. Use {@link CacheItem#NEVER_EXPIRE} to indicate that there's no expiration time.
     * @param refreshFrequency
     *          the amount of time (in ticks, where the tick time span is defined at runtime) before items are reloaded
     *          into the cache. Use {@link CacheItem#NEVER_REFRESH} to indicate that there's no need for the item to
     *          be refreshed. Note that when refreshing, there's almost always an old value for the item in the cache
     *          before the new value is loaded into the cache. The {@code loader} param is also required in order for
     *          the item to be refreshed.
     * @param loader
     *          the cache loader used to load a new value when the item needs to be refreshed. If not specified and
     *          the {@code refreshFrequency} is not {@link CacheItem#NEVER_REFRESH}, the cache will use a default one.
     * @param loaderParams
     *          additional parameters the loader could need
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void put(String scope, Object key, Object value, List<Object> dependencyKeys, long expireAfter,
             long refreshFrequency, CacheLoader loader, Object... loaderParams) throws InvalidScopeException,
            InternalCacheEngineException;

    /**
     * Removes an item from a scope.
     *
     * @param scope
     *            scope to remove the item from
     * @param key
     *            unique key for the item within this scope
     * @return true if the removal was successful, false otherwise
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    boolean remove(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException;

    /**
     * Clears the contents of the entire cache.
     *
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void clearAll() throws InternalCacheEngineException;

    /**
     * Clears the contents of the specified scope in the cache.
     *
     * @param scope
     *            scope to clear
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     * @throws InternalCacheEngineException
     *          if an error occurred in the underlying cache implementation
     */
    void clearScope(String scope) throws InvalidScopeException, InternalCacheEngineException;
}
