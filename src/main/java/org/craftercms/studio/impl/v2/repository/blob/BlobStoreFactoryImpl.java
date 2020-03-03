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
import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.BlobStore;
import org.craftercms.studio.api.v2.repository.blob.BlobStoreFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default implementation of {@link BlobStoreFactory} that reads the configuration from the site repository
 *
 * @author joseross
 * @since 3.1.6
 */
@SuppressWarnings("unchecked, rawtypes")
public class BlobStoreFactoryImpl implements BlobStoreFactory, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BlobStoreFactoryImpl.class);

    /**
     * The path of the configuration file
     */
    protected String configurationPath;

    protected ContentRepository contentRepository;

    protected EncryptionAwareConfigurationReader configurationReader;

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setConfigurationReader(EncryptionAwareConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
    }

    @Override
    public BlobStore getByPaths(String site, String[] paths) throws ServiceLayerException, ConfigurationException {
        logger.debug("Looking blob store for paths {0} in site {1}", Arrays.toString(paths), site);
        HierarchicalConfiguration config = getConfiguration(site);
        if (config != null) {
            BlobStore blobStore = findStore(config, store -> paths[0].matches(store.getString("pattern")));
            // We have to compare each one to know if the exception should be thrown
            if (blobStore != null && !Stream.of(paths).allMatch(blobStore::isCompatible)) {
                throw new ServiceLayerException("Unsupported operation for paths " + Arrays.toString(paths));
            }
            return blobStore;
        }
        return null;
    }

    protected HierarchicalConfiguration getConfiguration(String site) throws ServiceLayerException {
        logger.debug("Reading blob store configuration for site {0}", site);
        if (!contentRepository.contentExists(site, configurationPath)) {
            logger.debug("No blob store configuration found for site {0}", site);
            return null;
        }
        try (InputStream is = contentRepository.getContent(site, configurationPath)) {
            return configurationReader.readXmlConfiguration(is, "utf-8");
        } catch (ContentNotFoundException e) {
            logger.debug("No blob store configuration found for site {0}", site);
            return null;
        } catch (Exception e) {
            logger.error("Error reading blob store configuration for site {0}", e, site);
            throw new ServiceLayerException("");
        }
    }

    protected BlobStore findStore(HierarchicalConfiguration config, Predicate<HierarchicalConfiguration> predicate)
            throws ConfigurationException {
        Optional<HierarchicalConfiguration> storeConfig =
                config.configurationsAt(CONFIG_KEY_STORE).stream().filter(predicate).findFirst();
        if (storeConfig.isPresent()) {
            HierarchicalConfiguration store = storeConfig.get();
            String type = store.getString(CONFIG_KEY_TYPE);
            BlobStore instance = applicationContext.getBean(type, BlobStore.class);
            instance.init(store);
            return instance;
        }
        return null;
    }

}
