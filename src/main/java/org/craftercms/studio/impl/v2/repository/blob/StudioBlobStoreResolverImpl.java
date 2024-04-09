/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.file.blob.exception.BlobStoreConfigurationMissingException;
import org.craftercms.commons.file.blob.impl.BlobStoreResolverImpl;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.core.service.Context;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.rometools.utils.Strings.isNotEmpty;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.craftercms.commons.file.blob.BlobStore.*;

/**
 * Implementation of {@link StudioBlobStoreResolver}
 *
 * @author joseross
 * @since 3.1.6
 */
@SuppressWarnings("rawtypes, unchecked")
public class StudioBlobStoreResolverImpl extends BlobStoreResolverImpl implements StudioBlobStoreResolver {

    public static final String CACHE_KEY_STORE = "blob-store";

    protected ContentRepository contentRepository;

    protected Cache<String, Object> cache;

    protected ConfigurationService configurationService;

    protected StudioConfiguration studioConfiguration;

    /**
     * The patterns of urls that should be handled by blob stores
     */
    protected String[] interceptedPaths;
    private ContextManager contextManager;

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

    public void setInterceptedPaths(String[] interceptedPaths) {
        this.interceptedPaths = interceptedPaths;
    }

    @Override
    protected HierarchicalConfiguration getConfiguration(ConfigurationProvider provider) throws ConfigurationException {
        var config = super.getConfiguration(provider);
        if (config == null) {
            config = new XMLConfiguration();
        }
        return config;
    }

    protected HierarchicalConfiguration getConfiguration(String siteId) throws ExecutionException {
        String cacheKey1 = configurationService.getCacheKey(siteId, configModule, configPath, getEnvironment());
        return (HierarchicalConfiguration) cache.get(cacheKey1, () -> {
            logger.debug("Cache miss in site '{}' key '{}'", siteId, cacheKey1);
            return getConfiguration(new ConfigurationProviderImpl(siteId));
        });
    }

    protected StudioBlobStore getBlobStore(String siteId, String storeId, HierarchicalConfiguration config)
            throws ExecutionException {
        String cacheKey2 = join(":", siteId, CACHE_KEY_STORE, storeId);
        return (StudioBlobStore) cache.get(cacheKey2, () -> {
            logger.debug("Cache miss in site '{}' store '{}' key '{}'", siteId, storeId, cacheKey2);
            return getById(config, storeId);
        });
    }

    @Override
    public List<StudioBlobStore> getAll(String siteId) throws ServiceLayerException {
        logger.debug("Look up all the blob stores for site '{}'", siteId);
        List<StudioBlobStore> result = new LinkedList<>();
        try {
            HierarchicalConfiguration config = getConfiguration(siteId);
            if (config == null) {
                return result;
            }
            List<HierarchicalConfiguration> stores = config.configurationsAt(CONFIG_KEY_STORE);
            for (HierarchicalConfiguration store : stores) {
                String storeId = store.getString(CONFIG_KEY_ID);
                result.add(getBlobStore(siteId, storeId, config));
            }
            return result;
        } catch (ExecutionException e) {
            logger.error("Failed to lookup the blob stores for site '{}'", siteId, e);
            throw new ServiceLayerException("Error looking for blob store", e);
        }
    }

    @Override
    public StudioBlobStore getByPaths(String site, String... paths)
            throws ServiceLayerException {

        if (Stream.of(paths).noneMatch(p -> RegexUtils.matchesAny(p, interceptedPaths))) {
            logger.debug("One of the paths '{}' in site '{}' should not be intercepted, skip",
                    paths, site);
            return null;
        }

        logger.debug("Look up the blob store in site '{}' for paths '{}'", site, Arrays.toString(paths));
        try {
            HierarchicalConfiguration config = getConfiguration(site);
            if (config == null || config.isEmpty()) {
                logger.debug("No blob store found in site '{}' for paths '{}'", site, paths);
                return null;
            }
            String storeId = findStoreId(config, store -> paths[0].matches(store.getString(CONFIG_KEY_PATTERN)));
            if (isNotEmpty(storeId)) {
                StudioBlobStore blobStore = getBlobStore(site, storeId, config);
                // We have to compare each one to know if the exception should be thrown
                if (!Stream.of(paths).allMatch(blobStore::isCompatible)) {
                    logger.error("Unsupported operation in site '{}' paths '{}'", site, Arrays.toString(paths));
                    throw new ServiceLayerException(format("Unsupported operation in site '%s' paths '%s'",
                            site, Arrays.toString(paths)));
                }
                return blobStore;
            }
            logger.debug("No blob store found in site '{}' for paths '{}'", site, paths);
            return null;
        } catch (ExecutionException e) {
            logger.error("Failed to look up the blob store for site '{}'", site, e);
            throw new ServiceLayerException(format("Failed to look up the blob store for site '%s'", site), e);
        }
    }

    @Override
    public boolean isBlob(String site, String path) throws ServiceLayerException {
        try {
            return getByPaths(site, path) != null;
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("The blob store configuration is missing or invalid in site '{}'", site, e);
            return false;
        }
    }

    protected String getEnvironment() {
        return studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE);
    }

    public void setContextManager(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    /**
     * Internal class to provide access to configuration files
     */
    private class ConfigurationProviderImpl implements ConfigurationProvider {

        private final String site;
        private final Context context;

        public ConfigurationProviderImpl(String site) {
            this.site = site;
            this.context = contextManager.getContext(site);
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
                throw new IOException(format("Failed to read the file '%s'", path), e);
            }
        }

        @Override
        public Map<String, String> getLookupVariables() {
            return context.getConfigLookupVariables();
        }
    }

}
