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
package org.craftercms.cstudio.alfresco.cache.impl;

import org.craftercms.cstudio.alfresco.cache.api.CacheItem;
import org.craftercms.cstudio.alfresco.cache.api.CacheLoader;

import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of {@link org.craftercms.cstudio.alfresco.cache.api.CacheItem}.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public class CacheItemImpl implements CacheItem {
    protected final String scope;
    protected final long ticksAtCreation;

    protected final Object key;
    protected final Object value;

    protected final long ticksToExpire;
    protected final long ticksToRefresh;

    protected final long timestamp;
    protected final List<Object> dependencyKeys;

    protected final CacheLoader loader;
    protected final Object[] loaderParams;

    /**
     * Value constructor.
     */
    public CacheItemImpl(final String scope, final long ticksAtCreation,
                         final Object key, final Object value,
                         final long ticksToExpire, final long ticksToRefresh,
                         final long timestamp, final List<Object> dependencyKeys,
                         final CacheLoader loader, final Object[] loaderParams) {
        this.scope = scope;
        this.ticksAtCreation = ticksAtCreation;

        this.key = key;
        this.value = value;

        this.ticksToExpire = ticksToExpire;
        this.ticksToRefresh = ticksToRefresh;

        this.timestamp = timestamp;
        this.dependencyKeys = dependencyKeys;

        this.loader = loader;
        this.loaderParams = loaderParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getKey() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTicksAtCreation() {
        return ticksAtCreation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTicksToExpire() {
        return ticksToExpire;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTicksToRefresh() {
        return ticksToRefresh;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheLoader getLoader() {
        return loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getLoaderParams() {
        return loaderParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getDependencyKeys() {
        return dependencyKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExpired(long currentTicks) {
        return ticksToExpire != NEVER_EXPIRE && currentTicks >= (ticksAtCreation + ticksToExpire);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsRefresh(long currentTicks) {
        return ticksToRefresh != NEVER_REFRESH && currentTicks >= (ticksAtCreation + ticksToRefresh);
    }

    /**
     * Returns true if the specified {@code CacheItemImpl}'s and this instance's key and scope are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheItemImpl item = (CacheItemImpl) o;

        if (!key.equals(item.key)) {
            return false;
        }
        if (!scope.equals(item.scope)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = scope.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CacheItemImpl[" +
                "scope='" + scope + '\'' +
                ", ticksAtCreation=" + ticksAtCreation +
                ", key=" + key +
                ", value=" + value +
                ", ticksToExpire=" + ticksToExpire +
                ", ticksToRefresh=" + ticksToRefresh +
                ", timestamp=" + timestamp +
                ", dependencyKeys=" + dependencyKeys +
                ", loader=" + loader +
                ", loaderParams=" + (loaderParams == null ? null : Arrays.asList(loaderParams)) +
                ']';
    }
}
