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
import org.craftercms.cstudio.alfresco.cache.api.*;
import org.craftercms.cstudio.alfresco.exception.InternalCacheEngineException;
import org.craftercms.cstudio.alfresco.exception.InvalidScopeException;
import org.craftercms.cstudio.alfresco.util.api.CachingAwareObject;
import org.craftercms.cstudio.alfresco.util.api.TimestampGenerator;
import org.craftercms.cstudio.alfresco.util.impl.IncrementalTimestampGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Cache} that implements common functionality, such as logging, expiration/refresh check every tick and
 * dependency management, and uses and underlying {@link CacheStoreAdapter} to store the items.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public class CacheImpl implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(CacheImpl.class);

    /**
     * Holds the current number of ticks.
     */
    protected volatile long ticks;

    /**
     * Adapter for the cache data structure.
     */
    protected CacheStoreAdapter cacheStoreAdapter;

    /**
     * Used to refresh a list of items.
     */
    protected CacheRefresher cacheRefresher;

    /**
     * The timestamp generator. Timestamps are used to check when dependencies have changed.
     */
    protected TimestampGenerator timestampGenerator;

    /**
     * Default constructor. Sets <code>timestampGenerator</code> to {@link IncrementalTimestampGenerator}.
     */
    public CacheImpl() {
        ticks = 0;
        timestampGenerator = new IncrementalTimestampGenerator();
    }

    /**
     * Sets the underlying {@link CacheStoreAdapter}
     *
     * @param cacheStoreAdapter
     *          the adapter for the cache data structure
     */
    @Required
    public void setCacheStoreAdapter(CacheStoreAdapter cacheStoreAdapter) {
        this.cacheStoreAdapter = cacheStoreAdapter;
    }

    /**
     * Sets the {@link CacheRefresher}, used to refresh the items of the cache.
     */
    public void setCacheRefresher(CacheRefresher cacheRefresher) {
        this.cacheRefresher = cacheRefresher;
    }

    /**
     * Sets the timestamp generator. Timestamps are used to check when dependencies have changed.
     */
    public void setTimestampGenerator(TimestampGenerator timestampGenerator) {
        this.timestampGenerator = timestampGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasScope(String scope) throws InternalCacheEngineException {
        try {
            return cacheStoreAdapter.hasScope(scope);
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while checking if the scope " + scope + " exists in the cache", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getScopes() throws InternalCacheEngineException {
        try {
            return cacheStoreAdapter.getScopes();
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while getting the list of available scopes", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScope(String scope, int maxItemsInMemory) throws InternalCacheEngineException {
        try {
            cacheStoreAdapter.addScope(scope, maxItemsInMemory);
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while adding scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeScope(String scope) throws InvalidScopeException, InternalCacheEngineException {
        try {
            if (hasScope(scope)) {
                cacheStoreAdapter.removeScope(scope);
            }
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while removing scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize(String scope) throws InvalidScopeException, InternalCacheEngineException {
        try {
            return cacheStoreAdapter.getSize(scope);
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while getting size of scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getKeys(String scope) throws InvalidScopeException, InternalCacheEngineException {
        try {
            return cacheStoreAdapter.getKeys(scope);
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while getting keys of items within scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKey(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException {
        try {
            return cacheStoreAdapter.hasKey(scope, key);
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while checking if the scope " + scope + " has key " + key, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheItem get(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException {
        try {
            CacheItem item = cacheStoreAdapter.get(scope, key);
            if (item != null && logger.isDebugEnabled()) {
                logger.debug("Cache hit: found " + item);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Cache miss: item with key " + key + " not found in scope " + scope);
            }

            return item;
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while getting item with key " + key + " from scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheItem getWithDependencyCheck(String scope, Object key) throws InvalidScopeException,
            InternalCacheEngineException {
        try {
            CacheItem item = cacheStoreAdapter.get(scope, key);
            if (item != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cache hit: found " + item);
                }

                if (haveDependenciesChanged(item)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Dependencies have changed for " + item + ". Removing it from the cache.");
                    }

                    cacheStoreAdapter.remove(scope, key);

                    return null;
                } else {
                    return item;
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cache miss: item with key " + key + " not found in scope " + scope);
                }

                return null;
            }
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while getting item with key " + key + " from scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String scope, Object key, Object value) throws InvalidScopeException,
            InternalCacheEngineException {
        put(scope, key, value, null, CacheItem.NEVER_EXPIRE, CacheItem.NEVER_REFRESH, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String scope, Object key, Object value, long expireAfter, long refreshFrequency,
                    CacheLoader loader, Object... loaderParams) throws InvalidScopeException,
            InternalCacheEngineException {
        put(scope, key, value, null, expireAfter, refreshFrequency, loader, loaderParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String scope, Object key, Object value, List<Object> dependencyKeys) throws InvalidScopeException,
            InternalCacheEngineException {
        put(scope, key, value, dependencyKeys, CacheItem.NEVER_EXPIRE, CacheItem.NEVER_REFRESH, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String scope, Object key, Object value, List<Object> dependencyKeys, long expireAfter,
                    long refreshFrequency, CacheLoader loader, Object... loaderParams) throws InvalidScopeException,
            InternalCacheEngineException {
        if (expireAfter < 0) {
            throw new IllegalArgumentException("The expireAfter argument should be 0 or positive");
        }

        if (refreshFrequency < 0) {
            throw new IllegalArgumentException("The refreshFrequency argument should be 0 or positive");
        }

        if (value instanceof CachingAwareObject) {
            CachingAwareObject cachingAwareObj = (CachingAwareObject) value;

            if (CollectionUtils.isEmpty(dependencyKeys)) {
                dependencyKeys = cachingAwareObj.getDependencyKeys();
            }

            cachingAwareObj.setCachingTime(System.currentTimeMillis());
            cachingAwareObj.setScope(scope);
            cachingAwareObj.setKey(key);
        }

        try {
            CacheItem item = new CacheItemImpl(scope, ticks, key, value, expireAfter, refreshFrequency, timestampGenerator.generate(),
                    dependencyKeys, loader, loaderParams);

            cacheStoreAdapter.put(item);

            if (logger.isDebugEnabled()) {
                logger.debug("Put into cache: " + item);
            }
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while putting item with key " + key +
                    " into scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(String scope, Object key) throws InvalidScopeException, InternalCacheEngineException {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing item with key " + key + " from scope " + scope);
        }

        try {
            if (hasScope(scope)) {
                return cacheStoreAdapter.remove(scope, key);
            } else {
                return false;
            }
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while removing item with key " +
                    key + " from scope " + scope, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAll() throws InternalCacheEngineException {
        try {
            cacheStoreAdapter.clearAll();
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while clearing all items from the cache", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearScope(String scope) throws InvalidScopeException, InternalCacheEngineException {
        try {
            cacheStoreAdapter.clearScope(scope);
        } catch (InvalidScopeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalCacheEngineException("Exception while clearing all items from scope " + scope, ex);
        }
    }

    /**
     * Called when a tick occurs. A tick is a logical unit of time. It basically symbolizes the time span between
     * calls to this method (i.e. 15 mins) by a job scheduler like Quartz.
     *
     * Checks if some of the {@link CacheItem}s in the cache have expired or need to be refreshed. If the item
     * has expired, it is removed from the cache. If it needs to be refreshed, it is added to a list of items that
     * need to be refreshed that is later passed to the {@link CacheRefresher}.
     */
    public void tick() {
        ticks++;

        if (logger.isDebugEnabled()) {
            logger.debug("Tick!");
        }

        List<CacheItem> itemsToRefresh = new ArrayList<CacheItem>();

        try {
            Collection<String> scopes = cacheStoreAdapter.getScopes();
            if (CollectionUtils.isNotEmpty(scopes)) {
                for (String scope : scopes) {
                    Collection<Object> keys = cacheStoreAdapter.getKeys(scope);
                    if (CollectionUtils.isNotEmpty(keys)) {
                        for (Object key : keys) {
                            CacheItem item = cacheStoreAdapter.get(scope, key);
                            if (item != null) {
                                doChecks(item, itemsToRefresh);
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(item + " was removed before it could be checked for expiration" +
                                            "/refresh");
                                }
                            }
                        }
                    }
                }
            }

            if (cacheRefresher != null && CollectionUtils.isNotEmpty(itemsToRefresh)) {
                cacheRefresher.refreshItems(itemsToRefresh, this);
            }
        } catch (Exception ex) {
            logger.warn("Exception while checking items for expiration/refresh", ex);
        }
    }

    /**
     * Checks if the given {@link CacheItem} has expired or needs to be refreshed.
     *
     * @param item
     * @param itemsToRefresh
     *          the list of items where to put the specified item if it needs to be refreshed
     */
    protected void doChecks(CacheItem item, List<CacheItem> itemsToRefresh) {
        boolean expired;
        boolean willBeRefreshed;

        try {
            expired = checkForExpiration(item);
            if (expired) {
                if (logger.isDebugEnabled()) {
                    logger.debug(item + " was removed because it expired");
                }
            } else {
                willBeRefreshed = checkForRefresh(item, itemsToRefresh);
                if (willBeRefreshed) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(item + " will be refreshed");
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Exception while checking " + item + " for expiration/refresh", ex);
        }
    }

    /**
     * Checks if the given {@link CacheItem} has expired. If so, it is removed from its scope in the
     * {@link CacheStoreAdapter}.
     *
     * @return true if the item has expired and was removed from the scope, false otherwise
     * @throws Exception
     */
    protected boolean checkForExpiration(CacheItem item) throws Exception {
        if (item.isExpired(ticks)) {
            cacheStoreAdapter.remove(item.getScope(), item.getKey());

            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a given {@link CacheItem} needs to be refreshed. If so, then it is added to the listen of items to
     * refresh.
     *
     * @param item
     * @param itemsToRefresh
     *          the list of items where to put the specified item if it needs to be refreshed
     * @return true if the item will be refreshed, false otherwise
     */
    protected boolean checkForRefresh(CacheItem item, List<CacheItem> itemsToRefresh) {
        if (item.getLoader() != null && item.needsRefresh(ticks)) {
            itemsToRefresh.add(item);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if some of the dependencies of the given {@link CacheItem} have changed, that means, they're no longer
     * in the cache, their timestamp is greater than the item's timestamp (which means that they were put in the
     * cache after the item was put) or their own dependencies have changed (recursive). If the dependencies have
     * changed, the item needs to be taken out of the cache.
     *
     * @return true if some of the dependencies have changed, false if none of them have changed.
     * @throws Exception
     */
    protected boolean haveDependenciesChanged(CacheItem item) throws Exception {
        List<Object> dependencyKeys = item.getDependencyKeys();
        if (CollectionUtils.isNotEmpty(dependencyKeys)) {
            for (Object dependencyKey : dependencyKeys) {
                CacheItem dependency = cacheStoreAdapter.get(item.getScope(), dependencyKey);
                if (dependency == null ||
                        item.getTimestamp() < dependency.getTimestamp() ||
                        haveDependenciesChanged(dependency)) {
                    return true;
                }
            }
        }

        return false;
    }
}
