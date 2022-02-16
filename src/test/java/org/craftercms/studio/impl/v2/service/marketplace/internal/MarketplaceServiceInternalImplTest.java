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
package org.craftercms.studio.impl.v2.service.marketplace.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.dom4j.DocumentException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.craftercms.studio.impl.v2.service.marketplace.internal.MarketplaceServiceInternalImpl.MODULE_CONFIG_KEY;
import static org.craftercms.studio.impl.v2.service.marketplace.internal.MarketplaceServiceInternalImpl.PATH_CONFIG_KEY;
import static org.craftercms.studio.impl.v2.service.marketplace.internal.MarketplaceServiceInternalImpl.TEMPLATE_CONFIG_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

/**
 * @author joseross
 * @since 4.0.0
 */
@SuppressWarnings("rawtypes")
public class MarketplaceServiceInternalImplTest {

    public static final String SITE_ID = "mySite";

    public static final String MODULE = "studio";

    @Mock
    private HierarchicalConfiguration widgetMapping;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private MarketplaceServiceInternalImpl marketplaceService;

    @Captor
    private ArgumentCaptor<InputStream> inputStreamCaptor;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @BeforeTest
    public void setUp() throws IOException {
        initMocks(this);
        marketplaceService.widgetMapping = widgetMapping;

        for(Object[] args : getConfigWiringParameters()) {
            HierarchicalConfiguration mapping = mock(HierarchicalConfiguration.class);
            when(mapping.getString(MODULE_CONFIG_KEY)).thenReturn(MODULE);
            when(mapping.getString(PATH_CONFIG_KEY)).thenReturn((String) args[1]);
            when(mapping.getString(TEMPLATE_CONFIG_KEY)).thenReturn((String) args[2]);
            when(widgetMapping.configurationAt((String) args[0])).thenReturn(mapping);

            when(configurationService.getConfigurationAsString(SITE_ID, MODULE, (String) args[1], null))
                    .thenReturn(IOUtils.toString(((Resource) args[4]).getInputStream(), UTF_8));
        }
    }

    /**
     * Provides the set of data to run the tests. Every array must follow this structure:
     * [0]: wiring type
     * [1]: config file name
     * [2]: path of the xslt template to use
     * [3]: resource with the content for the plugin object
     * [4]: resource with the content for the config file
     * [5]: resource with the content for the expected config file
     * [6]: boolean to indicate if the config should change or not (skip config comparison)
     */
    @DataProvider(name = "wiringParameters")
    public Object[][] getConfigWiringParameters() {
        return new Object[][] {
                new Object[] {
                        "no-parent",
                        "no-parent.xml",
                        "crafter/studio/marketplace/wiring/datasource.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-no-parent/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-no-parent/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-no-parent/expected.xml"),
                        true
                },
                new Object[] {
                        "nothing",
                        "nothing.xml",
                        "crafter/studio/marketplace/wiring/ui.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-nothing/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-nothing/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-nothing/expected.xml"),
                        true
                },
                new Object[] {
                        "empty",
                        "empty.xml",
                        "crafter/studio/marketplace/wiring/ui.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-empty/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-empty/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-empty/expected.xml"),
                        true
                },
                new Object[] {
                        "section",
                        "section.xml",
                        "crafter/studio/marketplace/wiring/ui.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-section/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-section/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-section/expected.xml"),
                        true
                },
                new Object[] {
                        "existing",
                        "existing.xml",
                        "crafter/studio/marketplace/wiring/ui.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-existing/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-existing/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-existing/expected.xml"),
                        true
                },
                new Object[] {
                        "repeated",
                        "repeated.xml",
                        "crafter/studio/marketplace/wiring/ui.xslt",
                        new ClassPathResource("crafter/studio/marketplace/wiring-repeated/plugin.json"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-repeated/config.xml"),
                        new ClassPathResource("crafter/studio/marketplace/wiring-repeated/expected.xml"),
                        false
                }
        };
    }

    // TODO: This needs to be updated to use git instead of contentService, disabled for now
    @Test(dataProvider = "wiringParameters", enabled = false)
    public void configWiringTest(String key, String configFile, String template, Resource pluginJson,
                                 Resource config, Resource expectedXml, boolean shouldUpdate)
            throws ServiceLayerException, IOException, UserNotFoundException, TransformerException, DocumentException {
        try (InputStream pluginContent = pluginJson.getInputStream();
             InputStream expectedContent = expectedXml.getInputStream()) {
            // load the plugin object from the given resource
            Plugin plugin = MAPPER.readValue(pluginContent, Plugin.class);

            // execute the wiring
            marketplaceService.performConfigurationWiring(plugin, SITE_ID, emptyList(), null);

            if (shouldUpdate) {
                // check that the right service was called
                verify(configurationService).writeConfiguration(
                        eq(SITE_ID), eq(MODULE), eq(configFile), eq(null), inputStreamCaptor.capture());

                // compare the argument sent with the expected configuration
                Diff diff = DiffBuilder
                        .compare(expectedContent)
                        .withTest(inputStreamCaptor.getValue())
                        .ignoreWhitespace()
                        .ignoreComments()
                        .build();

                // there should not be any differences
                assertEquals(IterableUtils.size(diff.getDifferences()), 0, diff.toString());
            } else {
                // check that the service was never called
                verify(configurationService, never()).writeConfiguration(
                        eq(SITE_ID), eq(MODULE), eq(configFile), eq(null), any());
            }
        }
    }

}
