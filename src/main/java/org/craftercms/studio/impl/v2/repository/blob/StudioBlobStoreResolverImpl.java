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
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.impl.BlobStoreResolverImpl;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;

import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;
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

    protected ContentRepository contentRepository;

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public BlobStore getByPaths(String site, String... paths) throws ServiceLayerException, ConfigurationException {
        logger.debug("Looking blob store for paths {} for site {}", Arrays.toString(paths), site);
        Function<String, InputStream> configGetter = path -> {
            try {
                return contentRepository.getContent(site, path);
            } catch (Exception e) {
                logger.error("Error reading blob stores configuration for site " + site, e);
            }
            return null;
        };
        HierarchicalConfiguration config = getConfiguration(configGetter);
        if (config != null) {
            BlobStore blobStore = findStore(config, store -> paths[0].matches(store.getString(CONFIG_KEY_PATTERN)));
            // We have to compare each one to know if the exception should be thrown
            if (blobStore != null && !Stream.of(paths).allMatch(blobStore::isCompatible)) {
                throw new ServiceLayerException("Unsupported operation for paths " + Arrays.toString(paths));
            }
            return blobStore;
        }
        return null;
    }

}
