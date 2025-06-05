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
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v2.content.ContentLifecycle;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifecycleContent;
import org.craftercms.studio.api.v2.content.LifecycleContent.ContentLifecycleItem;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.content.ContentExistException;
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.impl.v2.utils.db.DBUtils;
import org.craftercms.studio.model.rest.content.WriteContentResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.function.ThrowingSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.content.LifecycleContent.LifecycleOperation.NEW;
import static org.craftercms.studio.api.v2.content.LifecycleContent.LifecycleOperation.UPDATE;
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
	protected PlatformTransactionManager transactionManager;

	@Mock
	protected ItemService itemService;

	@Mock
	protected ItemDAO itemDAO;

	@Mock
	protected GeneralLockService generalLockService;

	@Mock
	protected ContentLifecycle contentLifecycle;

	@Mock
	protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

	@Mock
	protected DependencyService dependencyService;

	@Mock
	protected DmPageNavigationOrderService pageNavOrderService;

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
		serviceInternal.setApplicationEventPublisher(applicationEventPublisher);

		doNothing().when(retryingDatabaseOperationFacade).retry(any(Runnable.class));

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
		LifecycleContent lifecycleContent = mock(LifecycleContent.class);
		ContentLifecycleItem item = mock(ContentLifecycleItem.class);
		when(item.repoPath()).thenReturn(PATH);
		when(lifecycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(lifecycleContent.getRepoPath()).thenReturn(PATH);
		when(lifecycleContent.getOperation()).thenReturn(UPDATE);
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(true);

		// Mock repository behavior
		when(contentRepository.writeContent(eq(SITE_ID), anyCollection(), anySet())).thenReturn("commit-id");

		// Mock lifecycle execution
		doReturn(lifecycleContent).when(serviceInternal).runLifecycle(eq(SITE_ID), eq(PATH), any());

		WriteContentResult result;
		try (MockedStatic<DBUtils> dbUtilsMock = mockStatic(DBUtils.class)) {
			dbUtilsMock.when(() -> DBUtils.runInTransaction(
				any(PlatformTransactionManager.class),
				anyString(),
				any(ThrowingSupplier.class)
			)).thenAnswer(invocation -> {
				// Simulate transaction behavior
				ThrowingSupplier supplier = invocation.getArgument(2);
				return supplier.getWithException();
			});
			result = serviceInternal.write(SITE_ID, PATH, contentStream);
		}

		// Verify behavior
		assertNotNull(result);
		assertEquals(1, result.getItems().size());
		verify(contentRepository, times(1)).writeContent(eq(SITE_ID), anyCollection(), anySet());
	}

	@Test(expected = ServiceLayerException.class)
	public void testWriteEmptyLifecycleResults() throws Exception {
		// Mock lifecycle content with empty results
		LifecycleContent lifecycleContent = mock(LifecycleContent.class);
		when(lifecycleContent.getItems()).thenReturn(Collections.emptyMap());
		doReturn(lifecycleContent).when(serviceInternal).runLifecycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test(expected = ContentInPublishQueueException.class)
	public void testWriteItemInPublishQueue() throws Exception {
		// Mock lifecycle content
		LifecycleContent lifecycleContent = mock(LifecycleContent.class);
		ContentLifecycleItem item = mock(ContentLifecycleItem.class);
		when(lifecycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(true);

		when(publishService.getActivePackagesForItems(any(), anyCollection(), anyBoolean()))
			.thenReturn(List.of(new PublishPackage() {{
				id = PUBLISH_PACKAGE_ID;
			}}));

		// Mock lifecycle execution
		doReturn(lifecycleContent).when(serviceInternal).runLifecycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test(expected = ActionDeniedException.class)
	public void testWritePermissionDenied() throws Exception {
		// Mock lifecycle content
		LifecycleContent lifecycleContent = mock(LifecycleContent.class);
		ContentLifecycleItem item = mock(ContentLifecycleItem.class);
		when(lifecycleContent.getItems()).thenReturn(Map.of(PATH, item));
		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(false);

		// Mock lifecycle execution
		doReturn(lifecycleContent).when(serviceInternal).runLifecycle(eq(SITE_ID), eq(PATH), any());

		// Call the method
		serviceInternal.write(SITE_ID, PATH, contentStream);
	}

	@Test
	public void testGetMissingFolders() {
		Map<String, LifecycleContent.LifecycleOperation> operationsByPath = Map.of(
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

	@Test
	public void testMovePageSuccess() throws Exception {
		// Mock content existence
		String sourcePath = "/site/website/test1/index.xml";
		String targetPath = "/site/website/test2/index.xml";
		String sourceFolder = "/site/website/test1";
		String targetFolder = "/site/website/test2";
		String commitId = "COMMIT 123";
		List<String> children = List.of(
			"/site/website/test1/index.xml",
			"/site/website/test1/child1/index.xml",
			"/site/website/test1/child2/index.xml");

		when(contentRepository.contentExists(SITE_ID, sourcePath)).thenReturn(true);
		when(contentRepository.contentExists(SITE_ID, targetPath)).thenReturn(false);
		when(contentRepository.contentExists(SITE_ID, "/site/website")).thenReturn(true);

		when(itemDAO.getChildrenPaths(SITE_NUMERIC_ID, sourceFolder)).thenReturn(children);

		Item sourceItem = mock(Item.class);
		when(sourceItem.getSystemType()).thenReturn(CONTENT_TYPE_FOLDER);
		when(itemService.getItem(SITE_ID, sourceFolder, true)).thenReturn(sourceItem);
		when(itemService.getItem(SITE_ID, sourcePath)).thenReturn(sourceItem);

		Item sourceChild1 = mock(Item.class);
		Item sourceChild2 = mock(Item.class);

		when(itemService.getItem(SITE_ID, "/site/website/test1/child1/index.xml")).thenReturn(sourceChild1);
		when(itemService.getItem(SITE_ID, "/site/website/test1/child2/index.xml")).thenReturn(sourceChild2);

		Item targetParentItem = mock(Item.class);
		when(itemService.getItem(SITE_ID, "/site/website", true)).thenReturn(targetParentItem);

		when(permissionEvaluator.isAllowed(any(), any(), any())).thenReturn(true);

		when(contentRepository.moveContent(any(), any(), any(), any(), any())).thenReturn(commitId);

		WriteContentResult moveResult;
		try (MockedStatic<DBUtils> dbUtilsMock = mockStatic(DBUtils.class)) {
			dbUtilsMock.when(() -> DBUtils.runInTransaction(
				any(PlatformTransactionManager.class),
				anyString(),
				any(ThrowingSupplier.class)
			)).thenAnswer(invocation -> {
				// Simulate transaction behavior
				ThrowingSupplier supplier = invocation.getArgument(2);
				return supplier.getWithException();
			});
			moveResult = serviceInternal.move(SITE_ID, sourcePath, targetPath);
		}

		assertEquals("Commit ID should match", commitId, moveResult.getCommitId());
		assertEquals("Number of items should match", 3, moveResult.getItems().size());

		verify(contentRepository, times(1)).moveContent(eq(SITE_ID), eq(sourceFolder), eq(targetFolder), anyCollection(), anySet());

		verify(pageNavOrderService).move(SITE_ID, sourceFolder, targetFolder);

		verify(contentLifecycle, times(3)).execute(
			anyString(), any(LifecycleContent.class), any(ContentLoader.class)
		);
	}

	@Test(expected = ContentNotFoundException.class)
	public void testMoveSourcePathNotFound() throws Exception {
		// Mock content existence
		when(contentRepository.contentExists(SITE_ID, PATH)).thenReturn(false);

		// Call the method
		serviceInternal.move(SITE_ID, PATH, "/new/path");
	}

	@Test(expected = ContentExistException.class)
	public void testMoveTargetPathExists() throws Exception {
		// Mock content existence
		when(contentRepository.contentExists(SITE_ID, PATH)).thenReturn(true);
		when(contentRepository.contentExists(SITE_ID, "/new/path")).thenReturn(true);

		// Call the method
		serviceInternal.move(SITE_ID, PATH, "/new/path");
	}

	@Test(expected = InvalidParametersException.class)
	public void testMoveNonMatchingExtensions() throws Exception {
		// Mock content existence
		when(contentRepository.contentExists(SITE_ID, "/existing/file.jpg")).thenReturn(true);

		// Call the method
		serviceInternal.move(SITE_ID, "/existing/file.jpg", "/new/path.txt");
	}

}
