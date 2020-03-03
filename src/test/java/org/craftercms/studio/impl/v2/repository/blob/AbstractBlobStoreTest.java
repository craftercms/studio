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
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.commons.config.ConfigUtils;
import org.craftercms.commons.config.profiles.aws.S3ProfileMapper;
import org.craftercms.studio.impl.v2.repository.blob.s3.AwsS3BlobStore;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Collections.emptyMap;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

/**
 * @author joseross
 */
public class AbstractBlobStoreTest {

    public static final Resource CONFIG_FILE = new ClassPathResource("crafter/studio/config/stores.xml");

    @InjectMocks
    private AwsS3BlobStore store; // cant't test abstract class so use the impl

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private S3ProfileMapper profileMapper;

    @BeforeMethod
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void initTest() throws IOException, org.craftercms.commons.config.ConfigurationException {
        try (InputStream is = CONFIG_FILE.getInputStream()) {
            HierarchicalConfiguration<ImmutableNode> config = ConfigUtils.readXmlConfiguration(is, emptyMap());

            store.init(config.configurationsAt("blobStore").get(0));

            assertNotNull(store.profile.getProfileId(), "profile should have an id");
            assertNull(store.profile.getBucketName(), "profile should not have a bucket name");
            assertNotNull(store.profile.getRegion(), "profile should have a region");

            assertFalse(isEmpty(store.mappings), "mappings should not be empty");
            assertEquals(store.mappings.size(), 3, "there should be 3 mappings");
        }
    }

}
