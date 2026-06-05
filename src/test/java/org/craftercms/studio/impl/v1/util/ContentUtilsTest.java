/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static java.lang.String.format;
import static org.craftercms.studio.impl.v1.util.ContentUtils.getParentUrl;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentUtilsTest {

	private static final String PAGE_URL = "/site/website/articles/testing/my-article/index.xml";
	private static final String PAGE_URL_PARENT = "/site/website/articles/testing";
	private static final String COMPONENT_URL = "/site/components/articles/testing/my-article.xml";
	private static final String COMPONENT_URL_PARENT = "/site/components/articles/testing";
	private static final String FOLDER_URL = "/site/components/articles/testing";
	private static final String FOLDER_URL_PARENT = "/site/components/articles";

	@Test
	public void calculatePageParentUrlTest() {
		String parentUrl = getParentUrl(PAGE_URL);
		assertEquals(PAGE_URL_PARENT, parentUrl, format("Parent of '%s' does not match expected value", PAGE_URL));
	}

	@Test
	public void calculateComponentPageParentUrlTest() {
		String parentUrl = getParentUrl(COMPONENT_URL);
		assertEquals(COMPONENT_URL_PARENT, parentUrl, format("Parent of '%s' does not match expected value", COMPONENT_URL));
	}

	@Test
	public void calculateFolderParentUrlTest() {
		String parentUrl = getParentUrl(FOLDER_URL);
		assertEquals(FOLDER_URL_PARENT, parentUrl, format("Parent of '%s' does not match expected value", FOLDER_URL));
	}

	@Test
	public void areSiblingsTest() {
		String url1 = "/site/website/articles/testing/my-article/index.xml";
		String url2 = "/site/website/articles/testing/another-article/index.xml";
		String url3 = "/site/website/articles/my-article/index.xml";
		String url4 = "/site/website/articles/subfolder/index.xml";

		assertTrue(ContentUtils.areSiblings(url1, url2), format("'%s' and '%s' should be siblings", url1, url2));
		assertFalse(ContentUtils.areSiblings(url1, url3), format("'%s' and '%s' should not be siblings", url1, url3));
		assertFalse(ContentUtils.areSiblings(url1, url4), format("'%s' and '%s' should not be siblings", url1, url4));
		assertTrue(ContentUtils.areSiblings(url3, url4), format("'%s' and '%s' should be siblings", url2, url4));
	}
}
