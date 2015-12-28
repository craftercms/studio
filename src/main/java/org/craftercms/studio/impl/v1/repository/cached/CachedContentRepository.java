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
package org.craftercms.studio.impl.v1.repository.cached;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;
import org.craftercms.studio.api.v1.repository.ContentRepository;

import java.io.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Wrap a repository with this class to provide caching 
 * @author russdanner
 *
 */
public class CachedContentRepository extends AbstractContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(CachedContentRepository.class);
    ContentRepository wrappedRepo;

    public ContentRepository getWrappedRepository() {
        return wrappedRepo;
    }

    public void setWrappedRepository(ContentRepository repo) {
        wrappedRepo = repo;
    }

    /**
     * default constructor 
     */
    public CachedContentRepository() {
    }

    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        InputStream value = null;

        try {
            BytesContainer cachedBytesContainer = (BytesContainer)getCachedObject("getContent-"+path);

            if(path.startsWith("/cstudio")) {
                if(cachedBytesContainer == null) {
                    value = wrappedRepo.getContent(path);

                    if(value != null ) {
                        byte[] bytes = IOUtils.toByteArray(value);
                        value.reset();
                        BytesContainer container = new BytesContainer(bytes);
                        cacheObject("getContent-"+path, bytes);
                    }
                }
                else {
                    byte[] cachedBytes = cachedBytesContainer.getBytes();
                    value = new ByteArrayInputStream(cachedBytes);
                }
            }
            else {
                // web content
                value = wrappedRepo.getContent(path);                
            }
        }
        catch(Exception err) {
            value = wrappedRepo.getContent(path);
        }

        return value;
    }

    @Override
    public boolean contentExists(String path) {
        Boolean value = null;

        value = (Boolean)getCachedObject("contentExists-"+path);

        if(value == null) {
            value = wrappedRepo.contentExists(path);

            if(value != null) {
                cacheObject("contentExists-"+path, value);
            }
        }

        return value;
     }

    @Override
    public boolean writeContent(String path, InputStream content) {
        // nothing to cache
        return wrappedRepo.writeContent(path, content);
    }

    @Override
    public boolean createFolder(String path, String name) {
        // nothing to cache
        return wrappedRepo.createFolder(path, name);
    }

    @Override
    public boolean deleteContent(String path) {
        // nothing to cache
        return wrappedRepo.deleteContent(path);
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        // nothing to cache
        return wrappedRepo.copyContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        // nothing to cache
        return wrappedRepo.moveContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath, String newName) {
        // nothing to cache
        return wrappedRepo.moveContent(fromPath, toPath, newName);
    }

    @Override
    public RepositoryItem[] getContentChildren(String path) {
        RepositoryItem[] value = null;

        value = (RepositoryItem[])getCachedObject("getContentChildren-"+path);

        if(value == null) {
            value = wrappedRepo.getContentChildren(path);

            if(value != null) {
                cacheObject("getContentChildren-"+path, value);
            }
        }

        return value;
    }

    @Override
    public VersionTO[] getContentVersionHistory(String path) {
        VersionTO[] value = null;
        value = (VersionTO[])getCachedObject("getContentVersionHistory-"+path);

        if(value == null) {
            value = wrappedRepo.getContentVersionHistory(path);

            if(value != null) {
                cacheObject("getContentVersionHistory-"+path, value);
            }
        }

        return value;
    }

    @Override
    public String createVersion(String path, boolean majorVersion) {
        // nothing to cache
        return wrappedRepo.createVersion(path, majorVersion);
    }

    @Override
    public String createVersion(String path, String comment, boolean majorVersion) {
        // nothing to cache
        return wrappedRepo.createVersion(path, comment, majorVersion);
    }

    @Override
    public boolean revertContent(String path, String label, boolean major, String comment) {
        // nothing to cache
        return wrappedRepo.revertContent(path, label, major, comment);
    }

    @Override
    public InputStream getContentVersion(String path, String version) 
	throws ContentNotFoundException {
		// nothing to cache    
    	return wrappedRepo.getContentVersion(path, version);
    }
    
    @Override
    public void lockItem(String site, String path) {
      //nothing to cache
      wrappedRepo.lockItem(site, path);
    }

    @Override
    public void unLockItem(String site, String path) {
        // nothing to cache
       wrappedRepo.unLockItem(site, path);
    }

    protected void cacheObject(String key, Object value) {
        HashMap<String, Object> cache = this.getCache();
        cache.put(key, value);
    }
    
    protected Object getCachedObject(String key) {
        Object value = null;

        HashMap<String, Object> cache = this.getCache();
        value = cache.get(key);

        if(value != null) {
            logger.debug("HIT: "+ key);
        }

        return value;
    }

    long cacheAge = System.currentTimeMillis();
    HashMap<String, Object> globalCache = new HashMap<String, Object>();

    protected HashMap<String, Object> getCache() {
        HashMap<String, Object> cache = null;

        if(System.currentTimeMillis() - cacheAge > 10000){
            cacheAge = System.currentTimeMillis(); 
            globalCache = new HashMap<String, Object>();
        }


//        ResponseCache respoCache = ResponseCache.currentResponseCache();

//        if(respoCache != null) {
//            cache = respoCache.getCacheMap();
//        }

//        if(cache == null) {
//            // return empty cache (warn filter not run)
//            cache = new HashMap<String, Object>();
//        }

        return globalCache;        
    }

    @Override
    public Date getModifiedDate(String path) {
        Date value = null;
        value = (Date)getCachedObject("getModifiedDate-"+path);

        if(value == null) {
            value = wrappedRepo.getModifiedDate(path);

            if(value != null) {
                cacheObject("getModifiedDate-"+path, value);
            }
        }

        return value;
    }

    protected class BytesContainer {
        byte[] bytes = null;

        public BytesContainer(byte[] value) {
            bytes = value;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}