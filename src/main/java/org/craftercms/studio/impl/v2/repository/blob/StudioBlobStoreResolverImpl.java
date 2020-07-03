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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.impl.BlobStoreResolverImpl;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.config.profiles.ConfigurationProviderImpl;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.tuckey.web.filters.urlrewrite.Run;

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

    public static String CACHE_KEY_CONFIG = "blob-store-config";

    public static String CACHE_KEY_STORE = "blob-store";

    protected ContentRepository contentRepository;

    protected CacheTemplate cacheTemplate;

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    public BlobStore getByPaths(String site, String... paths)
            throws ServiceLayerException, ConfigurationException {
        SiteContext context = SiteContext.getCurrent();
        logger.debug("Looking blob store for paths {} for site {}", Arrays.toString(paths), site);
        HierarchicalConfiguration config;
        if (context != null) {
            logger.debug("Checking cache for config");
            config = cacheTemplate.getObject(context.getContext(),
                    () -> {
                        logger.debug("Config not found in cache");
                        try {
                            return getConfiguration(new ConfigurationProviderImpl(site));
                        } catch (ConfigurationException e) {
                            throw new RuntimeException("Error getting blob store configuration for site " + site, e);
                        }
                    }, CACHE_KEY_CONFIG, site);
        } else {
            // this happens in background jobs
            logger.debug("No cache available");
            config = getConfiguration(new ConfigurationProviderImpl(site));
        }
        if (config != null) {
            String storeId = findStoreId(config, store -> paths[0].matches(store.getString(CONFIG_KEY_PATTERN)));
            BlobStore blobStore;
            if (context != null) {
                logger.debug("Checking cache for blob store {}", storeId);
                blobStore = cacheTemplate.getObject(context.getContext(), () -> {
                    logger.debug("Blob store {} not found in cache", storeId);
                    try {
                        return getById(config, storeId);
                    } catch (ConfigurationException e) {
                        throw new RuntimeException("Error looking for blob store " + storeId, e);
                    }
                }, CACHE_KEY_STORE, site, storeId);
            } else {
                logger.debug("No cache available");
                blobStore = getById(config, storeId);
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

        private String site;

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
