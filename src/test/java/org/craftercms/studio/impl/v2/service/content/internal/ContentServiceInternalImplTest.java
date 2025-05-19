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

import org.craftercms.commons.security.exception.ActionDeniedException;
import org.craftercms.commons.security.permissions.PermissionEvaluator;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.content.LifeCycleContent;
import org.craftercms.studio.api.v2.content.LifeCycleContent.ContentLifeCycleItem;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.rest.content.WriteContentResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.craftercms.studio.api.v2.content.LifeCycleContent.LifeCycleOperation.NEW;
import static org.craftercms.studio.api.v2.content.LifeCycleContent.LifeCycleOperation.UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceInternalImplTest {
	private static final String SITE_ID = "sample-site";
	private static final String PATH = "/sample/path";
	private static final String NON_EXIST_CONTENT_PATH = "/sample/non-exists-content-path";

	private static final long SITE_NUMERIC_ID = 1001;
	private static final int PUBLISH_PACKAGE_ID = 101;

	@Mock
	protected GitContentRepository contentRepository;

	@Mock
	protected PermissionEvaluator<String, Object> permissionEvaluator;

	@Mock
	protected PublishService publishService;

	@Mock
	protected AuditService auditService;

	@Mock
	protected ApplicationEventPublisher applicationEventPublisher;

	@Mock
	protected ItemService itemService;

	@Mock
	protected DependencyService dependencyService;

	@Mock
	protected SitesService siteService;

	@InjectMocks
	@Spy
	protected ContentServiceInternalImpl serviceInternal;

	private InputStream contentStream;

	@Before
	public void setUp() throws SiteNotFoundException {
		when(contentRepository.contentExists(SITE_ID, PATH)).thenReturn(true);
		when(contentRepository.contentExists(SITE_ID, NON_EXIST_CONTENT_PATH)).thenReturn(false);

		Site site = mock(Site.class);
		when(site.getSiteId()).thenReturn(SITE_ID);
		when(site.getId()).thenReturn(SITE_NUMERIC_ID);
		when(siteService.getSite(SITE_ID)).thenReturn(site);

		contentStream = new ByteArrayInputStream("test content".getBytes());
	}

	@Test
	public void testContentExits() {
		boolean result = serviceInternal.contentExists(SITE_ID, PATH);
		verify(contentRepository, times(1)).contentExists(SITE_ID, PATH);
		assertTrue(result);
	}

	@Test
	public void testPathNonExist() {
		boolean result = serviceInternal.contentExists(SITE_ID, NON_EXIST_CONTENT_PATH);
		verify(contentRepository, times(1)).contentExists(SITE_ID, NON_EXIST_CONTENT_PATH);
		assertFalse(result);
	}

	private void assertCreationPathComparatorMatches(List<String> paths, List<String> expected) {
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

		assertCreationPathComparatorMatches(paths, expected);
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

		assertCreationPathComparatorMatches(paths, expected);
	}


	@Test
	public void testWriteSuccess() throws Exception {
		// Mock lifecycle content
		LifeCycleContent lifeCycleContent = mock(LifeCycleContent.class);
		ContentLifeCycleItem item = mock(ContentLifeCycleItem.class);
		when(item.repoPath()).thenReturn(PATH);
		when(lifeCycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(lifeCycleContent.getRepoPath()).thenReturn(PATH);
		when(lifeCycleContent.getOperation()).thenReturn(UPDATE);
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(true);

		// Mock repository behavior
		when(contentRepository.writeContent(eq(SITE_ID), anyCollection(), anySet())).thenReturn("commit-id");

		// Mock lifecycle execution
		doReturn(lifeCycleContent).when(serviceInternal).runLifeCycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		WriteContentResult result = serviceInternal.write(SITE_ID, PATH, contentStream);

		// Verify behavior
		assertNotNull(result);
		assertEquals(1, result.getItems().size());
		verify(contentRepository, times(1)).writeContent(eq(SITE_ID), anyCollection(), anySet());
	}

	@Test(expected = ServiceLayerException.class)
	public void testWriteEmptyLifecycleResults() throws Exception {
		// Mock lifecycle content with empty results
		LifeCycleContent lifeCycleContent = mock(LifeCycleContent.class);
		when(lifeCycleContent.getItems()).thenReturn(Collections.emptyMap());
		doReturn(lifeCycleContent).when(serviceInternal).runLifeCycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test(expected = ContentInPublishQueueException.class)
	public void testWriteItemInPublishQueue() throws Exception {
		// Mock lifecycle content
		LifeCycleContent lifeCycleContent = mock(LifeCycleContent.class);
		ContentLifeCycleItem item = mock(ContentLifeCycleItem.class);
		when(lifeCycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(true);

		when(publishService.getActivePackagesForItems(any(), anyCollection(), anyBoolean()))
			.thenReturn(List.of(new PublishPackage() {{
				id = PUBLISH_PACKAGE_ID;
			}}));

		// Mock lifecycle execution
		doReturn(lifeCycleContent).when(serviceInternal).runLifeCycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test(expected = ActionDeniedException.class)
	public void testWritePermissionDenied() throws Exception {
		// Mock lifecycle content
		LifeCycleContent lifeCycleContent = mock(LifeCycleContent.class);
		ContentLifeCycleItem item = mock(ContentLifeCycleItem.class);
		when(lifeCycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(false);

		// Mock lifecycle execution
		doReturn(lifeCycleContent).when(serviceInternal).runLifeCycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test
	public void testGetMissingFolders() {
		Map<String, LifeCycleContent.LifeCycleOperation> operationsByPath = Map.of(
			"/a/b/c/d", NEW
		);

		// Mock content existence
		when(contentRepository.contentExists(SITE_ID, "/a")).thenReturn(true);
		when(contentRepository.contentExists(SITE_ID, "/a/b")).thenReturn(false);
		when(contentRepository.contentExists(SITE_ID, "/a/b/c")).thenReturn(false);

		// Call the method
		Set<String> missingFolders = serviceInternal.getMissingFolders(SITE_ID, operationsByPath);

		// Verify the result
		assertEquals(Set.of("/a/b", "/a/b/c"), missingFolders);
	}
}
