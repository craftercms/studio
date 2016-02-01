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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;

/**
 * This is virtually a copy of the Springframework version, with the exception
 * that it uses the newer constructors for the <code>EHCacheManager</code>
 * instances.
 *
 * @author Derek Hulley
 */
public class EhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean
{
    static
    {
        // https://jira.terracotta.org/jira/browse/EHC-652
        // Force old-style LruMemoryStore
        // System.setProperty("net.sf.ehcache.use.classic.lru", "true");
    }

    protected final Log logger = LogFactory.getLog(EhCacheManagerFactoryBean.class);

    private Resource configLocation;
    private CacheManager cacheManager;

    /**
     *
     * @param configLocation a resource location using the <b>file:</b> or <b>classpath:</b> prefix
     */
    public void setConfigLocation(Resource configLocation)
    {
        this.configLocation = configLocation;
    }

    public void afterPropertiesSet() throws IOException, CacheException {

        // Double-check the config location or EHCache will throw an NPE
        try
        {
            URL configUrl = this.configLocation.getURL();
            logger.info("Initializing EHCache CacheManager using URL: " + configLocation);
            this.cacheManager = new CacheManager(configUrl);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unabled to read EHCache configuration file at " + configLocation, e);
        }
    }

    public Object getObject()
    {
        return this.cacheManager;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType()
    {
        return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void destroy()
    {
        logger.info("Shutting down EHCache CacheManager");
        if(logger.isDebugEnabled()) {
            String[] caches = this.cacheManager.getCacheNames();
            for(String cache : caches) {
                logger.debug("Shutting down EHCache instance " + cache);
            }
        }
        this.cacheManager.shutdown();
    }
}
