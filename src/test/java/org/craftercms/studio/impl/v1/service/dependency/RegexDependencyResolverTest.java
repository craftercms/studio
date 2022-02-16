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
package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author joseross
 * @since
 */
public class RegexDependencyResolverTest {

    public static final String SITE_ID = "mySite";

    public static final String FORM_DEFINITION_PATH = "/config/studio/content-types/page/test/form-definition.xml";

    public static final String PAGE_A_PATH = "/config/studio/content-types/page/a/form-definition.xml";

    public static final String COMPONENT_A_PATH = "/config/studio/content-types/component/a/form-definition.xml";

    public static final String COMPONENT_B_PATH = "/config/studio/content-types/component/b/form-definition.xml";

    public static final Resource CONFIG_CONTENT = new ClassPathResource("crafter/studio/config/dependency/config.xml");

    public static final Resource FORM_CONTENT = new ClassPathResource("crafter/studio/config/dependency/form-definition.xml");

    public static final String SITE_CONFIG_FILE_NAME = "dependency/resolver-config.xml";

    public static final String CONFIG_BASE_PATH = "/configuration/dependency";

    public static final String DEFAULT_CONFIG_FILE_NAME = "resolver-config.xml";

    @Mock
    private StudioConfiguration studioConfiguration;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ContentService contentService;

    @InjectMocks
    private RegexDependencyResolver dependencyResolver;

    @BeforeTest
    public void setUp() throws IOException, DocumentException, ServiceLayerException {
        initMocks(this);

        when(studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME))
                .thenReturn(SITE_CONFIG_FILE_NAME);
        when(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH))
                .thenReturn(CONFIG_BASE_PATH);
        when(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME))
                .thenReturn(DEFAULT_CONFIG_FILE_NAME);

        try (InputStream is = CONFIG_CONTENT.getInputStream()) {
            Document doc = DocumentHelper.parseText(IOUtils.toString(is, UTF_8));

            when(configurationService.getConfigurationAsDocument(SITE_ID, MODULE_STUDIO, SITE_CONFIG_FILE_NAME, null))
                    .thenReturn(doc);
        }

        try (InputStream is = FORM_CONTENT.getInputStream()) {
            String form = IOUtils.toString(is, UTF_8);

            when(contentService.getContentAsString(SITE_ID, FORM_DEFINITION_PATH)).thenReturn(form);
        }

        when(contentService.contentExists(SITE_ID, PAGE_A_PATH)).thenReturn(true);
        when(contentService.contentExists(SITE_ID, COMPONENT_A_PATH)).thenReturn(true);
        when(contentService.contentExists(SITE_ID, COMPONENT_B_PATH)).thenReturn(true);

    }

    @Test
    public void testDependencyExtraction() {
        Map<String, Set<String>> deps = dependencyResolver.resolve(SITE_ID, FORM_DEFINITION_PATH);

        assertNotNull(deps);
        assertFalse(deps.isEmpty());

        // check that dependencies without transforms continue to work as usual
        assertTrue(deps.containsKey("direct"));
        assertEquals(deps.get("direct"), Set.of(PAGE_A_PATH));

        // check that single dependencies continue to work as usual
        assertTrue(deps.containsKey("single"));
        assertEquals(deps.get("single"), Set.of(PAGE_A_PATH));

        // check that new multi-value dependencies work as expected
        assertTrue(deps.containsKey("multiple"));
        assertEquals(deps.get("multiple"), Set.of(COMPONENT_A_PATH, COMPONENT_B_PATH));
    }

}
