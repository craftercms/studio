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

import org.craftercms.cstudio.alfresco.cache.api.CacheStoreAdapter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that returns a different {@link CacheStoreAdapter} depending on the value of a flag property
 * that indicates if caching should be turned on or off.
 *
 * @author Alfonso Vásquez
 */
public class SwitchableCacheStoreAdapterFactoryBean implements org.springframework.beans.factory.FactoryBean<org.craftercms.cstudio.alfresco.cache.api.CacheStoreAdapter> {

    private CacheStoreAdapter offCacheStoreAdapter;
    private CacheStoreAdapter onCacheStoreAdapter;
    private boolean cacheOn;

    public SwitchableCacheStoreAdapterFactoryBean() {
        cacheOn = true;
    }

    @Required
    public void setOffCacheStoreAdapter(CacheStoreAdapter offCacheStoreAdapter) {
        this.offCacheStoreAdapter = offCacheStoreAdapter;
    }

    @Required
    public void setOnCacheStoreAdapter(CacheStoreAdapter onCacheStoreAdapter) {
        this.onCacheStoreAdapter = onCacheStoreAdapter;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    @Override
    public CacheStoreAdapter getObject() throws Exception {
        if (cacheOn) {
            return onCacheStoreAdapter;
        } else {
            return offCacheStoreAdapter;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return CacheStoreAdapter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
