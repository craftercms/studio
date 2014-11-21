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
package org.craftercms.cstudio.alfresco.cache.impl.store;

import org.craftercms.cstudio.alfresco.cache.api.CacheItem;
import org.craftercms.cstudio.alfresco.cache.api.CacheStoreAdapter;
import org.craftercms.cstudio.alfresco.exception.InvalidScopeException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a {@link CacheStoreAdapter} using an underlying {@link java.util.Map}.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public class MapCacheStoreAdapter implements CacheStoreAdapter {

    /**
     * Holds all the cache scopes.
     */
    private Map<String, Map<Object, CacheItem>> scopeCaches;

    /**
     * Default constructor. Creates {@code scopeCaches} as a {@link java.util.concurrent.ConcurrentHashMap}
     */
    public MapCacheStoreAdapter() {
        scopeCaches = new ConcurrentHashMap<String, Map<Object, CacheItem>>();
    }

    /**
     * Constructor that receives a list of scopes that need to created. Each scope is an instance of
     * {@link ConcurrentHashMap}.
     */
    public MapCacheStoreAdapter(List<String> scopes) {
        this();

        for (String scope : scopes) {
            scopeCaches.put(scope, new ConcurrentHashMap<Object, CacheItem>());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasScope(String scope) throws Exception {
        return scopeCaches.containsKey(scope);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getScopes() throws Exception {
        return scopeCaches.keySet();
    }

    /**
     * Adds a new scope. The scope is an instance of {@link ConcurrentHashMap}.
     *
     * @param scope
     *          the name of the scope
     * @param maxItemsInMemory
     *          the maximum number of items in memory, before they are evicted
     * @throws Exception
     */
    public void addScope(String scope, int maxItemsInMemory) throws Exception {
        // Ignore maxItemsInMemory, since we don't run an eviction algorithm.
        scopeCaches.put(scope, new ConcurrentHashMap<Object, CacheItem>());
    }

    /**
     * {@inheritDoc}
     */
    public void removeScope(String scope) throws Exception {
        scopeCaches.remove(scope);
    }

    /**
     * {@inheritDoc}
     */
    public int getSize(String scope) throws Exception {
        return getScopeCache(scope).size();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Object> getKeys(String scope) throws Exception {
        return getScopeCache(scope).keySet();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasKey(String scope, Object key) throws Exception {
        return getScopeCache(scope).containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public CacheItem get(String scope, Object key) throws Exception {
        return getScopeCache(scope).get(key);
    }

    /**
     * {@inheritDoc}
     */
    public void put(CacheItem item) throws Exception {
        getScopeCache(item.getScope()).put(item.getKey(), item);
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(String scope, Object key) throws Exception {
        return getScopeCache(scope).remove(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    public void clearAll() throws Exception {
        scopeCaches.clear();
    }

    /**
     * {@inheritDoc}
     */
    public void clearScope(String scope) throws Exception {
        getScopeCache(scope).clear();
    }

    /**
     * Returns the {@link Map} object associated with the given scope.
     *
     * @throws InvalidScopeException
     *          if the specified scope isn't a registered one
     */
    private Map<Object, CacheItem> getScopeCache(String scope) throws InvalidScopeException {
        Map<Object, CacheItem> scopeCache = scopeCaches.get(scope);
        if (scopeCache == null) {
            throw new InvalidScopeException("The scope " + scope + " doesn't exist");
        } else {
            return scopeCache;
        }
    }
}
