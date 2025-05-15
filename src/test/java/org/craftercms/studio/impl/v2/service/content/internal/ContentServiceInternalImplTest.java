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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceInternalImplTest {
	private static final String SITE_ID = "sample-site";
	private static final String PATH = "/sample/path";
	private static final String NON_EXIST_CONTENT_PATH = "/sample/non-exists-content-path";

	@Mock
	protected GitContentRepository contentRepository;

	@InjectMocks
	protected ContentServiceInternalImpl serviceInternal;

	@Before
	public void setUp() {
		when(contentRepository.contentExists(SITE_ID, PATH)).thenReturn(true);
		when(contentRepository.contentExists(SITE_ID, NON_EXIST_CONTENT_PATH)).thenReturn(false);
	}

	@Test
	public void testContentExits() {
		boolean result = serviceInternal.contentExists(SITE_ID, PATH);
		verify(contentRepository, times(1)).contentExists(SITE_ID, PATH);
		assertEquals(true, result);
	}

	@Test
	public void testPathNonExist() {
		boolean result = serviceInternal.contentExists(SITE_ID, NON_EXIST_CONTENT_PATH);
		verify(contentRepository, times(1)).contentExists(SITE_ID, NON_EXIST_CONTENT_PATH);
		assertEquals(false, result);
	}

	private void testCreationPathComparator(List<String> paths, List<String> expected) {
		paths.sort(serviceInternal.creationPathComparator());

		assertEquals(expected.size(), paths.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), paths.get(i));
		}
	}

	@Test
	public void testPathCreationOrder() {
		// Parent folders should go before children
		// XXX/index.xml should go before other XXX/yyyy items
		List<String> paths = new ArrayList<>(List.of("/a/b/c", "/a/b", "/a/b/index.xml", "/a", "/a/index.xml"));
		List<String> expected = List.of("/a", "/a/index.xml", "/a/b", "/a/b/index.xml", "/a/b/c");

		testCreationPathComparator(paths, expected);
	}

	@Test
	public void testPathCreationOrder2() {
		// Parent folders should go before children
		// XXX/index.xml should go before other XXX/yyyy items
		List<String> paths = new ArrayList<>(List.of(
			"/site/website/en/articles/new-cms/features/index.xml",
			"/site/website/en/articles/new-cms/features/",
			"/site/website/en/index.xml",
			"/site/website/en/news/archive/new-release/index.xml",
			"/site/website/en/news/archive/new-release",
			"/site/website/en/news/archive/",
			"/site/website/en/news",
			"/site/website/en",
			"/site/website/en/articles/new-cms/index.xml",
			"/site/website/en/articles",
			"/site/website/en/articles/new-cms"));

		List<String> expected = List.of(
			"/site/website/en",
			"/site/website/en/index.xml",
			"/site/website/en/news",
			"/site/website/en/articles",
			"/site/website/en/news/archive/",
			"/site/website/en/articles/new-cms",
			"/site/website/en/articles/new-cms/index.xml",
			"/site/website/en/news/archive/new-release",
			"/site/website/en/news/archive/new-release/index.xml",
			"/site/website/en/articles/new-cms/features/",
			"/site/website/en/articles/new-cms/features/index.xml");

		testCreationPathComparator(paths, expected);
	}

}
