/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v2.repository.blob;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.impl.BlobStoreResolverImpl;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.craftercms.commons.file.blob.BlobStore.CONFIG_KEY_PATTERN;

/**
 * Implementation of {@link StudioBlobStoreResolver}
 *
 * @author joseross
 * @since 3.1.6
 */
@SuppressWarnings("rawtypes")
public class StudioBlobStoreResolverImpl extends BlobStoreResolverImpl implements StudioBlobStoreResolver {

    public static final String CACHE_KEY_CONFIG = "_blob-store-config";

    public static final String CACHE_KEY_STORE = "_blob-store_";

    protected ContentRepository contentRepository;

    protected Ehcache cache;

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    @Override
    public BlobStore getByPaths(String site, String... paths)
            throws ServiceLayerException, ConfigurationException {
        logger.debug("Looking blob store for paths {} for site {}", Arrays.toString(paths), site);
        HierarchicalConfiguration config;
        logger.debug("Checking cache for config");
        String cacheKey = site + CACHE_KEY_CONFIG;
        Element element = cache.get(cacheKey);
        if (element == null || element.isExpired()) {
            logger.debug("Config not found in cache");
            try {
                config = getConfiguration(new ConfigurationProviderImpl(site));
                cache.put(new Element(cacheKey, config));
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error getting blob store configuration for site " + site, e);
            }
        } else {
            config = (HierarchicalConfiguration) element.getObjectValue();
        }
        if (config != null) {
            String storeId = findStoreId(config, store -> paths[0].matches(store.getString(CONFIG_KEY_PATTERN)));
            BlobStore blobStore;
            logger.debug("Checking cache for blob store {}", storeId);
            cacheKey = site + CACHE_KEY_STORE + storeId;
            element = cache.get(cacheKey);
            if (element == null || element.isExpired()) {
                logger.debug("Blob store {} not found in cache", storeId);
                try {
                    blobStore = getById(config, storeId);
                    cache.put(new Element(blobStore, cacheKey));
                } catch (ConfigurationException e) {
                    throw new RuntimeException("Error looking for blob store " + storeId, e);
                }
            } else {
                blobStore = (BlobStore) element.getObjectValue();
            }

            // We have to compare each one to know if the exception should be thrown
            if (blobStore != null && !Stream.of(paths).allMatch(blobStore::isCompatible)) {
                throw new ServiceLayerException("Unsupported operation for paths " + Arrays.toString(paths));
            }
            return blobStore;
        }
        return null;
    }

    /**
     * Internal class to provide access to configuration files
     */
    private class ConfigurationProviderImpl implements ConfigurationProvider {

        private final String site;

        public ConfigurationProviderImpl(String site) {
            this.site = site;
        }

        @Override
        public boolean configExists(String path) {
            return StudioBlobStoreResolverImpl.this.contentRepository.contentExists(site, path);
        }

        @Override
        public InputStream getConfig(String path) throws IOException {
            try {
                return StudioBlobStoreResolverImpl.this.contentRepository.getContent(site, path);
            } catch (Exception e) {
                throw new IOException("Error reading file", e);
            }
        }

    }

}
