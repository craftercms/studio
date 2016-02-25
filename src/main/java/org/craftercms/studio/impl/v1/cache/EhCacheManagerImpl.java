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
 */
package org.craftercms.studio.impl.v1.cache;

import org.craftercms.studio.api.v1.cache.Scope;
import org.craftercms.studio.api.v1.cache.ThreadSafeCacheManager;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.to.DmPathTO;
import org.craftercms.studio.api.v1.to.GoLiveQueue;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 */
public class EhCacheManagerImpl implements ThreadSafeCacheManager {


    protected EhCacheAdapter<CacheKey, Serializable> cacheAdapter;

    protected ReentrantReadWriteLock lock=new ReentrantReadWriteLock();

    public EhCacheManagerImpl() {

    }


    @Override
    public void put(Scope scope, String key, Serializable item) {
        lock.writeLock().lock();
        try {
            CacheKey cacheKey = new CacheKey(scope, key);
            cacheAdapter.put(cacheKey, item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(Scope scope, String key, String site,Serializable item) {
        if(site != null) {
            key = site + ":" + key;
        }
        put(scope,key,item);
    }

    @Override
    public Serializable get(Scope scope, String key) {
        lock.readLock().lock();
        try {
            CacheKey cacheKey = new CacheKey(scope, key);
            return cacheAdapter.get(cacheKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Serializable get(Scope scope,String key,String site) {
        if(site != null) {
            key = site + ":" + key;
        }
        return get(scope,key);
    }


    @Override
    public  void invalidate(Scope scope, String key) {
        lock.writeLock().lock();
        try {
            CacheKey cacheKey = new CacheKey(scope, key);
            cacheAdapter.remove(cacheKey);
        } finally {
         lock.writeLock().unlock();
        }
    }

    @Override
    public void invalidate(Scope scope) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void invalidate() {
        lock.writeLock().lock();
        try {
            cacheAdapter.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    @Override
    public String generateKey(Object... params) {
        String key = "";
        // generate the key by concatenating parameters
        if (params != null) {
            int max = params.length;
            for (int index = 0; index < max; index++) {
                key += (params[index] == null) ? "" : params[index].toString();
                if ((index + 1 == max)) {
                    key += ",";
                }
            }
        }
        return key;
    }

    @Override
    public void invalidateAndRemoveFromQueue(String fullpath, String site) {
        DmPathTO pathTO = new DmPathTO(fullpath);
        String path = pathTO.getRelativePath();
        GoLiveQueue queue = (GoLiveQueue) get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY,site);
        if (null != queue) {
            queue.remove(path);
        }        
    }

    public EhCacheAdapter<CacheKey, Serializable> getEhCacheAdapter() {
        return cacheAdapter;
    }

    public void setEhCacheAdapter(EhCacheAdapter<CacheKey, Serializable> ehCacheAdapter) {
        this.cacheAdapter = ehCacheAdapter;
    }

    protected class CacheKey implements Serializable {

        protected Scope scope;
        protected String key;

        protected CacheKey(Scope scope, String key) {
            this.scope = scope;
            this.key = key;
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (key != null ? !key.equals(cacheKey.key) : cacheKey.key != null) return false;
            if (scope != cacheKey.scope) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = scope != null ? scope.hashCode() : 0;
            result = 31 * result + (key != null ? key.hashCode() : 0);
            return result;
        }
    }
}
