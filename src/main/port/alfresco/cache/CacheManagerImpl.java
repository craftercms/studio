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
package org.craftercms.cstudio.alfresco.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HashMap based cache manager
 */
public class CacheManagerImpl implements cstudioCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerImpl.class);

    protected Map<Scope, Map<String, Serializable>> map = new HashMap<Scope, Map<String, Serializable>>();
    // should have an ehcache obj

    protected static int instances = 0;

    public CacheManagerImpl() {
        instances++;
        if (instances > 1) {
            throw new AssertionError("The cache should be singleton");
        }
    }


    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#generateKey(java.lang.Object[])
      */

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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#get(java.lang.String, java.lang.String)
      */

    public Serializable get(Scope scope, String key) {
        Map<String, Serializable> serializableMap = map.get(scope);
        if (serializableMap == null) {
            return null;
        }
        return serializableMap.get(key);

    }

    public Serializable get(Scope scope,String key,String site) {
        if(site != null) {
            key = site + ":" + key;
        }
        return get(scope,key);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#invalidate(java.lang.String, java.lang.String)
      */

    public void invalidate(Scope scope, String key) {
        Map<String, Serializable> stringSerializableMap = map.get(scope);
        if (stringSerializableMap != null) {
            stringSerializableMap.remove(key);
        }
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#invalidate(java.lang.String)
      */

    public void invalidate(Scope scope) {
        Map<String, Serializable> stringSerializableMap = map.get(scope);
        if (null != stringSerializableMap) {
            map.remove(scope);
        }
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#invalidate()
      */

    public void invalidate() {
        map.clear();
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.cache.CacheManager#put(java.lang.String, java.lang.String, java.io.Serializable)
      */

    public void put(Scope scope, String key, Serializable item) {
        Map<String, Serializable> serializableMap = map.get(scope);
        if (serializableMap == null) {
            serializableMap = new HashMap<String, Serializable>();
            map.put(scope, serializableMap);
        }
        serializableMap.put(key, item);
    }

    public void put(Scope scope,String key,String site,Serializable item) {
        if(site != null) {
            key = site + ":" + key;
        }
        put(scope,key,item);
    }

}
