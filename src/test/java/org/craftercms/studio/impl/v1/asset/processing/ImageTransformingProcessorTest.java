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

package org.craftercms.studio.impl.v1.asset.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.ProcessorConfiguration;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

public class ImageTransformingProcessorTest {

    private static final String INPUT_REPO_PATH = "/static-assets/images/upload/test-image.jpg";
    private static final String OUTPUT_REPO_PATH = "/static-assets/images/transformed/test-image.jpg";
    private static final String INPUT_PATH_PATTERN = "/static-assets/images/upload/([^/]+)\\.jpg";
    private static final String OUTPUT_PATH_FORMAT = "/static-assets/images/transformed/$1.jpg";

    private ImageTransformingProcessor processor;
    private ImageTransformer transformer;

    @BeforeMethod
    public void setUp() throws Exception {
        transformer = createImageTransformer();
        processor = new ImageTransformingProcessor(transformer);
    }

    @Test
    public void testProcessWithNoOutputPathPattern() throws Exception {
        ProcessorConfiguration config = createProcessorConfigWithNoOutputPattern();

        Path inputFile = createInputFile();
        Asset input = new Asset(INPUT_REPO_PATH, inputFile);

        Asset output = processor.processAsset(config, createInputPathMatcher(), input);

        assertNotNull(output);
        assertEquals(output.getRepoPath(), input.getRepoPath());
        assertEquals(output.getFilePath(), inputFile);
        verify(transformer).transform(any(Path.class), eq(inputFile), eq(Collections.emptyMap()));
    }

    @Test
    public void testProcessWithOutputPathPattern() throws Exception {
        ProcessorConfiguration config = createProcessorConfigWithOutputPattern();

        Path inputFile = createInputFile();
        Asset input = new Asset(INPUT_REPO_PATH, inputFile);

        Asset output = processor.processAsset(config, createInputPathMatcher(), input);

        assertNotNull(output);
        assertNotEquals(output.getRepoPath(), input.getRepoPath());
        assertEquals(output.getRepoPath(), OUTPUT_REPO_PATH);
        verify(transformer).transform(eq(inputFile), eq(output.getFilePath()), eq(Collections.emptyMap()));
    }

    private ImageTransformer createImageTransformer() {
        return mock(ImageTransformer.class);
    }

    private ProcessorConfiguration createProcessorConfigWithNoOutputPattern() {
        ProcessorConfiguration configuration = new ProcessorConfiguration();
        configuration.setType("test");
        configuration.setParams(Collections.emptyMap());

        return configuration;
    }

    private ProcessorConfiguration createProcessorConfigWithOutputPattern() {
        ProcessorConfiguration configuration = new ProcessorConfiguration();
        configuration.setType("test");
        configuration.setOutputPathFormat(OUTPUT_PATH_FORMAT);
        configuration.setParams(Collections.emptyMap());

        return configuration;
    }

    private Matcher createInputPathMatcher() {
        Pattern pattern = Pattern.compile(INPUT_PATH_PATTERN);
        Matcher matcher = pattern.matcher(INPUT_REPO_PATH);

        matcher.matches();

        return matcher;
    }

    private Path createInputFile() throws IOException {
        return Files.createTempFile(FilenameUtils.getBaseName(INPUT_REPO_PATH), "." + FilenameUtils.getExtension(INPUT_REPO_PATH));
    }


}
