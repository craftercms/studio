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

import com.google.common.cache.Cache;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.impl.BlobStoreResolverImpl;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.rometools.utils.Strings.isNotEmpty;
import static java.lang.String.join;
import static org.craftercms.commons.file.blob.BlobStore.CONFIG_KEY_PATTERN;

/**
 * Implementation of {@link StudioBlobStoreResolver}
 *
 * @author joseross
 * @since 3.1.6
 */
@SuppressWarnings("rawtypes")
public class StudioBlobStoreResolverImpl extends BlobStoreResolverImpl implements StudioBlobStoreResolver {

    public static String CACHE_KEY_STORE = "blob-store";

    protected ContentRepository contentRepository;

    protected Cache<String, Object> cache;

    protected ConfigurationService configurationService;

    protected StudioConfiguration studioConfiguration;

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setCache(Cache<String, Object> cache) {
        this.cache = cache;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    protected HierarchicalConfiguration getConfiguration(ConfigurationProvider provider) throws ConfigurationException {
        var config = super.getConfiguration(provider);
        if (config == null) {
            config = new XMLConfiguration();
        }
        return config;
    }

    @Override
    public BlobStore getByPaths(String site, String... paths)
            throws ServiceLayerException {
        logger.debug("Looking blob store for paths {} for site {}", Arrays.toString(paths), site);
        HierarchicalConfiguration config;
        try {
            var cacheKey1 = configurationService.getCacheKey(site, configModule, configPath, getEnvironment());
            config = (HierarchicalConfiguration) cache.get(cacheKey1, () -> {
                logger.debug("Cache miss: {}", cacheKey1);
                return getConfiguration(new ConfigurationProviderImpl(site));
            });
            if (!config.isEmpty()) {
                String storeId = findStoreId(config, store -> paths[0].matches(store.getString(CONFIG_KEY_PATTERN)));
                if (isNotEmpty(storeId)) {
                    var cacheKey2 = join(":", site, CACHE_KEY_STORE, storeId);
                    BlobStore blobStore = (BlobStore) cache.get(cacheKey2, () -> {
                        logger.debug("Cache miss: {}", cacheKey2);
                        return getById(config, storeId);
                    });
                    // We have to compare each one to know if the exception should be thrown
                    if (!Stream.of(paths).allMatch(blobStore::isCompatible)) {
                        throw new ServiceLayerException("Unsupported operation for paths " + Arrays.toString(paths));
                    }
                    return blobStore;
                } else {
                    logger.debug("No blob store found in site {} for paths {}", site, paths);
                }
            }
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException("Error looking for blob store", e);
        }
    }

    protected String getEnvironment() {
        return studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE);
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
