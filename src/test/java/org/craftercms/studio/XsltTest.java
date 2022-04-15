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
package org.craftercms.studio;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.craftercms.studio.impl.v2.utils.XsltUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.testng.Assert.assertEquals;

/**
 * @author joseross
 */
public class XsltTest {

    /**
     * Each array must follow this structure:
     * 0: resource for the XSLT template
     * 1: resource for the input XML
     * 2: resource for the expected XML
     * 3: a map of strings to add as parameters for the XSLT template
     */
    @DataProvider(name = "xsltData")
    public Object[][] getTestData() {
        return new Object[][] {
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/site-config/site-config-v9.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/site-config-v9/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/site-config-v9/expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/site-config/site-config-v10.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/site-config-v10/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/site-config-v10/expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/4.0.0.2/site/rte-refactor.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/form-definition.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/form-definition-expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/config-list/config-list-v14.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/config-list.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/config-list-expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/site-config-tools/site-config-tools-v13.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/site-config-tools.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/rte-refactor/site-config-tools-expected.xml"),
                emptyMap()
            },
            new Object[]{
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/resolver-config/resolver-config-v3.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/resolver-config-v3/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/resolver-config-v3/expected.xml"),
                emptyMap()
            },
            new Object[]{
                new ClassPathResource("crafter/studio/upgrade/copy.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/cdata/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/cdata/expected.xml"),
                emptyMap()
            },
            new Object[]{
                new ClassPathResource("crafter/studio/upgrade/4.0.x/4.0.0.3/site/additional-fields.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/additional-fields/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/additional-fields/expected.xml"),
                emptyMap()
            },
            new Object[]{
                new ClassPathResource("crafter/studio/upgrade/4.0.x/4.0.0.36/system/" +
                                        "global-permission-mappings-config.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/search-plugins/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/search-plugins/expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/permission-mappings-config/" +
                                        "permission-mappings-config-v4.0.3.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/permission-mappings-config-v4.0.3/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/permission-mappings-config-v4.0.3/expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/permission-mappings-config/" +
                                        "permission-mappings-config-v4.0.4.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/permission-mappings-config-v4.0.4/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/permission-mappings-config-v4.0.4/expected.xml"),
                emptyMap()
            },
            new Object[] {
                new ClassPathResource("crafter/studio/upgrade/4.0.x/config/ui/ui-v4.0.2.xslt"),
                new ClassPathResource("crafter/studio/upgrade/xslt/ui-v4.0.2/input.xml"),
                new ClassPathResource("crafter/studio/upgrade/xslt/ui-v4.0.2/expected.xml"),
                emptyMap()
            }
        };
    }

    @Test(dataProvider = "xsltData")
    public void testXsltTemplate(Resource template, Resource content, Resource expected, Map<String, Object> params)
            throws IOException, TransformerException {
        try (InputStream templateIs = template.getInputStream();
             InputStream contentIs = content.getInputStream();
             InputStream expectedIs = expected.getInputStream()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XsltUtils.executeTemplate(templateIs, params, null, contentIs, output);
            String result = new String(output.toByteArray());

            // Compare the result
            Diff diff = DiffBuilder
                    .compare(expectedIs)
                    .withTest(result)
                    .ignoreWhitespace()
                    .ignoreComments()
                    .checkForSimilar()
                    .build();

            // there should not be any differences
            assertEquals(IterableUtils.size(diff.getDifferences()), 0,
                    "The result XML should be equal to the expected XML");

            // test that the XSLT is idempotent
            output = new ByteArrayOutputStream();
            try (InputStream template2 = template.getInputStream()) {
                XsltUtils.executeTemplate(template2, params, null, toInputStream(result, UTF_8), output);

                // Compare the result
                diff = DiffBuilder
                        .compare(result)
                        .withTest(output.toByteArray())
                        .ignoreWhitespace()
                        .ignoreComments()
                        .checkForSimilar()
                        .build();

                // there should not be any differences
                assertEquals(IterableUtils.size(diff.getDifferences()), 0,
                        "The result XML should not change the second time");
            }
        }
    }

}
