/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.apache.commons.io.FileUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.craftercms.studio.api.v2.utils.StudioUtils.createTempFile;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeControllerUpgradeOperationTest {
	@Mock
	private StudioConfiguration studioConfiguration;
	@InjectMocks
	private ContentTypeControllerUpgradeOperation operation;

	@Before
	public void setUp() throws Exception {
		operation.afterPropertiesSet();
	}

	@Test
	public void testDefaultScript() throws IOException, UpgradeException {
		ClassPathResource defaultScriptResource = new ClassPathResource("crafter/studio/upgrade/content-type/default/controller.groovy");
		Path scriptFile = createTempFile("controller.groovy", defaultScriptResource.getInputStream());
		try {
			operation.updateFile(mock(StudioUpgradeContext.class), scriptFile);
			Assert.assertFalse("Default script file was not deleted", Files.exists(scriptFile));
		} finally {
			FileUtils.deleteQuietly(scriptFile.toFile());
		}
	}

	@Test
	public void testCustomScript() throws UpgradeException, IOException, URISyntaxException {
		ClassPathResource inputResource = new ClassPathResource("crafter/studio/upgrade/content-type/5.0/5.0.0/custom/input.groovy");
		Path inputScriptFile = createTempFile("controller.groovy", inputResource.getInputStream());
		operation.updateFile(mock(StudioUpgradeContext.class), inputScriptFile);
		ClassPathResource expectedResource = new ClassPathResource("crafter/studio/upgrade/content-type/5.0/5.0.0/custom/expected.groovy");
		Assert.assertEquals("Output file does not match ", -1, Files.mismatch(inputScriptFile, Paths.get(expectedResource.getURL().toURI())));
	}

}
