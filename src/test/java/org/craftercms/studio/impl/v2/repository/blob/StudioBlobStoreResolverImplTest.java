/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import com.google.common.cache.CacheBuilder;
import org.craftercms.commons.config.ConfigurationResolver;
import org.craftercms.commons.config.ConfigurationResolverImpl;
import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.commons.crypto.impl.NoOpTextEncryptor;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author joseross
 */
public class StudioBlobStoreResolverImplTest {

    public static final String SITE_ID = "mySite";

    public static final String STORE_ID = "myBlobStore";

    public static final String BLOB_STORE_TYPE = "s3BlobStore";

    public static final String ANOTHER_BLOB_STORE = "anotherBlobStore";

    public static final String LOCAL_PATH = "/static-assets/test.png";

    public static final String REMOTE_PATH = "/static-assets/another/test.png";

    public static final String CONFIG_PATH = "/config/studio/stores.xml";

    public static final String CACHE_KEY_CONFIG = "config-file";

    public static final String CONFIG_MODULE = "studio";

    public static final String CONFIG_FILENAME = "stores.xml";

    public static final String ENVIRONMENT = "default";

    public static final Resource CONFIG_FILE = new ClassPathResource("crafter/studio/config/stores.xml");

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private StudioBlobStore myBlobStore;

    @Mock
    private StudioBlobStore anotherBlobStore;

    @Mock
    private StudioConfiguration studioConfiguration;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private StudioBlobStoreResolverImpl resolver;

    @BeforeMethod
    public void setUp() throws ContentNotFoundException, IOException {
        initMocks(this);

        when(contentRepository.contentExists(SITE_ID, CONFIG_PATH)).thenReturn(true);
        when(contentRepository.getContent(SITE_ID, CONFIG_PATH)).thenReturn(CONFIG_FILE.getInputStream());

        when(myBlobStore.getId()).thenReturn(STORE_ID);

        when(anotherBlobStore.isCompatible(REMOTE_PATH)).thenReturn(true);

        when(applicationContext.getBean(BLOB_STORE_TYPE, BlobStore.class)).thenReturn(myBlobStore);
        when(applicationContext.getBean(ANOTHER_BLOB_STORE, BlobStore.class)).thenReturn(anotherBlobStore);

        when(studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE)).thenReturn(ENVIRONMENT);

        when(configurationService.getCacheKey(SITE_ID, CONFIG_MODULE, CONFIG_FILENAME, ENVIRONMENT))
                .thenReturn(CACHE_KEY_CONFIG);

        ConfigurationResolver configResolver = new ConfigurationResolverImpl(ENVIRONMENT, "/config/{module}",
                null, new EncryptionAwareConfigurationReader(new NoOpTextEncryptor()));

        resolver.setConfigModule(CONFIG_MODULE);
        resolver.setConfigPath(CONFIG_FILENAME);
        resolver.setConfigurationResolver(configResolver);
        resolver.setInterceptedPaths(new String[] { "/static-assets/.*" });
        resolver.setCache(CacheBuilder.newBuilder().build());
    }

    @Test
    public void getByRemotePathTest() throws ServiceLayerException {
        BlobStore store = resolver.getByPaths(SITE_ID, REMOTE_PATH, REMOTE_PATH);

        assertNotNull(store, "store should not be null");
        assertNotEquals(store.getId(), STORE_ID);
    }

    @Test
    public void getByLocalPathTest() throws ServiceLayerException {
        BlobStore store = resolver.getByPaths(SITE_ID, LOCAL_PATH, LOCAL_PATH);

        assertNull(store, "store should be null");
    }

    @Test(expectedExceptions = { ServiceLayerException.class })
    public void getByMixedPathsTest() throws ServiceLayerException {
        resolver.getByPaths(SITE_ID, REMOTE_PATH, LOCAL_PATH);
    }

    @Test
    public void isBlobTest() throws ServiceLayerException {
        assertFalse(resolver.isBlob(SITE_ID, CONFIG_PATH));
        assertFalse(resolver.isBlob(SITE_ID,LOCAL_PATH));
        assertTrue(resolver.isBlob(SITE_ID, REMOTE_PATH));
    }

    @Test
    public void getAllTest() throws ServiceLayerException {
        List<StudioBlobStore> stores = resolver.getAll(SITE_ID);
        assertNotNull(stores);
        assertFalse(stores.isEmpty());
        assertEquals(stores.size(), 2);
    }

}
