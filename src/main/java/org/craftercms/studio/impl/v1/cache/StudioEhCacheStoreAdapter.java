/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.impl.v1.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.jgroups.JGroupsCacheReplicatorFactory;
import org.craftercms.core.cache.CacheItem;
import org.craftercms.core.cache.impl.CacheStoreAdapter;
import org.craftercms.core.cache.impl.store.EhCacheStoreAdapter;
import org.craftercms.core.exception.InvalidScopeException;

import javax.annotation.PreDestroy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StudioEhCacheStoreAdapter implements CacheStoreAdapter {

    public static final String SCOPE_MANAGER_NAME = "crafter.scopeManager";

    /**
     * Holds all the cache scopes.
     */
    private CacheManager scopeManager;

    /**
     * Default no-args constructor. Creates the {@code scopeManager} by calling the factory method
     * {@link net.sf.ehcache.CacheManager#newInstance(net.sf.ehcache.config.Configuration)}, because we want a
     * separate {@code CacheManager}, not the singleton one, since we don't want to the tick() method of a cache
     * implementation to be called on a CacheManager that doesn't have {@link CacheItem}s.
     */
    public StudioEhCacheStoreAdapter() {

        Configuration scopeManagerConfig = ConfigurationFactory.parseConfiguration();
        scopeManagerConfig.setName(SCOPE_MANAGER_NAME);

        if (clusterEnabled) {
            FactoryConfiguration factoryConfiguration = new FactoryConfiguration();
            factoryConfiguration.setClass("net.sf.ehcache.distribution.jgroups.JGroupsCacheManagerPeerProviderFactory");
            scopeManagerConfig.addCacheManagerPeerProviderFactory(factoryConfiguration);
        }
        scopeManager = CacheManager.newInstance(scopeManagerConfig);

    }

    /**
     * Constructor that receives a list of scope configurations. New scopes are created with the provided
     * configurations.
     */
    public StudioEhCacheStoreAdapter(List<CacheConfiguration> scopeConfigs) {
        this();

        for (CacheConfiguration scopeConfig : scopeConfigs) {
            scopeManager.addCache(new Cache(scopeConfig));
        }
    }

    /**
     * Destroy method, called by the Spring container. Calls {@link CacheManager#shutdown()}.
     */
    @PreDestroy
    public void destroy() {
        scopeManager.shutdown();
    }

    @Override
    public boolean hasScope(String scope) throws Exception {
        return scopeManager.cacheExists(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getScopes() throws Exception {
        return Arrays.asList(scopeManager.getCacheNames());
    }

    /**
     * Adds a new scope. The scope is an instance of EhCache's {@link Cache}.
     *
     * @param scope            the name of the scope
     * @param maxItemsInMemory the maximum number of items in memory, before they are evicted
     * @throws Exception
     */
    @Override
    public void addScope(String scope, int maxItemsInMemory) throws Exception {
        CacheConfiguration scopeConfig = new CacheConfiguration(scope, maxItemsInMemory);
        if (clusterEnabled) {
            CacheConfiguration.CacheEventListenerFactoryConfiguration cacheEventListenerFactoryConfiguration = new CacheConfiguration.CacheEventListenerFactoryConfiguration();
            cacheEventListenerFactoryConfiguration.setClass("net.sf.ehcache.distribution.jgroups.JGroupsCacheReplicatorFactory");
            cacheEventListenerFactoryConfiguration.setProperties("replicateAsynchronously=true,replicatePuts=true,replicateUpdates=true,replicateUpdatesViaCopy=false,replicateRemovals=true");
            scopeConfig.addCacheEventListenerFactory(cacheEventListenerFactoryConfiguration);
        }
        if (!scopeManager.cacheExists(scope)) {
            scopeManager.addCache(new Cache(scopeConfig));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeScope(String scope) throws Exception {
        scopeManager.removeCache(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize(String scope) throws Exception {
        return getScopeCache(scope).getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> getKeys(String scope) throws Exception {
        return getScopeCache(scope).getKeys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKey(String scope, Object key) throws Exception {
        return getScopeCache(scope).isKeyInCache(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheItem get(String scope, Object key) throws Exception {
        Element element = getScopeCache(scope).get(key);
        if (element != null) {
            return (CacheItem)element.getObjectValue();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(CacheItem item) throws Exception {
        getScopeCache(item.getScope()).put(new Element(item.getKey(), item));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(String scope, Object key) throws Exception {
        Cache cache = getScopeCache(scope);
        if (cache != null) {
            return cache.remove(key);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAll() throws Exception {
        scopeManager.clearAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearScope(String scope) throws Exception {
        getScopeCache(scope).removeAll();
    }

    /**
     * Returns the {@link Cache} object associated with the given scope.
     *
     * @throws InvalidScopeException if the specified scope isn't a registered one
     */
    private Cache getScopeCache(String scope) throws InvalidScopeException {
        Cache scopeCache = scopeManager.getCache(scope);
        if (scopeCache == null) {
            throw new InvalidScopeException("The scope " + scope + " doesn't exist");
        } else {
            return scopeCache;
        }
    }

    public boolean isClusterEnabled() { return clusterEnabled; }
    public void setClusterEnabled(boolean clusterEnabled) { this.clusterEnabled = clusterEnabled; }

    protected boolean clusterEnabled = false;
}
