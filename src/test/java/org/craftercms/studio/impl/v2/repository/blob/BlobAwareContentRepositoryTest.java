/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.FilenameUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.GitContentRepository.GitPublishChangeSet;
import org.craftercms.studio.api.v2.repository.PublishItemTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore.PublishChangeSet;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.impl.v2.repository.GitContentRepositoryImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.ADD;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.*;

/**
 * @author joseross
 */
public class BlobAwareContentRepositoryTest {

    public static final String SITE = "test";
    public static final long SITE_ID = 123;
    public static final String PARENT_PATH = "/static-assets";
    public static final String ORIGINAL_PATH = PARENT_PATH + "/test.txt";
    public static final String ORIGINAL_PATH_2 = PARENT_PATH + "/test2.txt";
    public static final String LOCAL_FOLDER_PATH = PARENT_PATH + "/local/test";

    public static final String BLOB_EXT = "blob";
    public static final String POINTER_PATH = ORIGINAL_PATH + "." + BLOB_EXT;
    public static final String GIT_REPO_PATH = removeStart(ORIGINAL_PATH + "." + BLOB_EXT, "/");
    public static final String POINTER_PATH_2 = ORIGINAL_PATH_2 + "." + BLOB_EXT;
    public static final String GIT_REPO_PATH_2 = removeStart(ORIGINAL_PATH_2 + "." + BLOB_EXT, "/");
    public static final String FOLDER_PATH = PARENT_PATH + "/folder";
    public static final String NEW_FOLDER_PATH = FOLDER_PATH + "2";
    public static final String NEW_FILE_PATH = FOLDER_PATH + "/test.txt";
    public static final String NEW_POINTER_PATH = NEW_FILE_PATH + "." + BLOB_EXT;
    public static final String NO_EXT_PATH = PARENT_PATH + "/test";
    public static final ByteArrayInputStream CONTENT = new ByteArrayInputStream("test".getBytes());
    public static final ByteArrayInputStream POINTER = new ByteArrayInputStream("pointer".getBytes());
    public static final long SIZE = 42;
    public static final String USER = "John Doe";
    public static final String ENV = "live";
    public static final String STORE_ID = "BLOB_STORE";
    public static final String LOCAL_PATH = "/site/website/index.xml";
    public static final String CONFIG_PATH = "/config/studio/site-config.xml";
    public static final String COMMIT_1 = "some commit";
    public static final String COMMIT_2 = "some other commit";

    @InjectMocks
    private BlobAwareContentRepository proxy;

    @Mock
    private org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryImpl localV1;

    @Mock
    private GitContentRepositoryImpl localRepositoryV2;

    @Mock
    private StudioBlobStore store;

    @Mock
    private StudioBlobStoreResolver resolver;

    @Captor
    private ArgumentCaptor<List<PublishItemTO>> itemsCaptor;

    @Mock
    private Site site;

    @Mock
    private PublishPackage publishPackage;

    private AutoCloseable mocks;

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mocks = openMocks(this);

        when(store.getId()).thenReturn(STORE_ID);
        when(resolver.getByPaths(SITE, FOLDER_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, ORIGINAL_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, ORIGINAL_PATH_2)).thenReturn(store);
        when(store.isCompatible(ORIGINAL_PATH)).thenReturn(true);
        when(store.isCompatible(ORIGINAL_PATH_2)).thenReturn(true);
        when(store.isCompatible(FOLDER_PATH)).thenReturn(true);
        when(resolver.getByPaths(SITE, ORIGINAL_PATH, NEW_FILE_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, FOLDER_PATH, NEW_FOLDER_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, NO_EXT_PATH)).thenReturn(store);
        when(resolver.getByPaths(SITE, CONFIG_PATH)).thenReturn(null);

        when(resolver.getAll(SITE)).thenReturn(List.of(store));

        when(localRepositoryV2.isFolder(SITE, FOLDER_PATH)).thenReturn(true);
        when(localRepositoryV2.isFolder(SITE, NEW_FOLDER_PATH)).thenReturn(true);

        when(localV1.contentExists(SITE, ORIGINAL_PATH)).thenReturn(false);
        when(localV1.contentExists(SITE, POINTER_PATH)).thenReturn(true);
        when(localV1.contentExists(SITE, POINTER_PATH_2)).thenReturn(true);
        when(localV1.getContent(SITE, POINTER_PATH)).thenReturn(POINTER);
        when(localV1.getContent(SITE, POINTER_PATH_2)).thenReturn(POINTER);
        when(localRepositoryV2.isFolder(SITE, PARENT_PATH)).thenReturn(true);

        doAnswer(invocation -> {
            Consumer<String> consumer = (Consumer<String>) invocation.getArgument(2);
            consumer.accept(POINTER_PATH);
            consumer.accept(NEW_POINTER_PATH);
            return null;
        }).when(localRepositoryV2).forAllSitePaths(eq(SITE), any(), any());

        when(store.contentExists(SITE, ORIGINAL_PATH)).thenReturn(true);
        when(store.contentExists(SITE, POINTER_PATH)).thenReturn(false);
        when(store.getContent(SITE, ORIGINAL_PATH, false)).thenReturn(CONTENT);
        when(store.getContentSize(SITE, ORIGINAL_PATH)).thenReturn(SIZE);

        when(store.moveContent(any(),any(),any(),any())).thenReturn(EMPTY);

        proxy.setFileExtension(BLOB_EXT);

        when(site.getSiteId()).thenReturn(SITE);
        when(site.getId()).thenReturn(SITE_ID);

        when(localRepositoryV2.publish(any(), any(), any())).thenAnswer((Answer<GitPublishChangeSet<PublishItemTO>>) invocation -> {
            Collection<PublishItemTO> itemsParam = invocation.getArgument(2, Collection.class);

            return new GitPublishChangeSet<>(COMMIT_1, itemsParam, emptyList());
        });


        when(publishPackage.getSite()).thenReturn(site);
    }

    @Test
    public void configShouldNotBeIntercepted() {
        proxy.contentExists(SITE, CONFIG_PATH);

        verify(localRepositoryV2).contentExists(SITE, CONFIG_PATH);
        verify(store, never()).contentExists(SITE, CONFIG_PATH);
    }

    @Test
    public void contentExistsTest() {
        assertTrue(proxy.contentExists(SITE, ORIGINAL_PATH), "original path should exist");
    }

    @Test
    public void getContentTest() throws ContentNotFoundException {
        assertEquals(proxy.getContent(SITE, ORIGINAL_PATH), CONTENT, "original path should return the original content");
    }

    @Test
    public void getContentSizeTest() {
        assertEquals(proxy.getContentSize(SITE, ORIGINAL_PATH), -1, "Blob content items should return -1");
    }

    @Test
    public void writeContentTest() throws ServiceLayerException {
        proxy.writeContent(SITE, ORIGINAL_PATH, CONTENT);

        verify(store).writeContent(SITE, ORIGINAL_PATH, CONTENT);
        verify(localV1).writeContent(eq(SITE), eq(POINTER_PATH), any());
    }

    @Test
    public void writeContentFailTest() throws ServiceLayerException {
        when(store.writeContent(SITE, ORIGINAL_PATH, CONTENT)).thenThrow(new ServiceLayerException("Test"));

        try {
            proxy.writeContent(SITE, ORIGINAL_PATH, CONTENT);
        } catch (Exception e) {
            // expected
        }

        verify(store).writeContent(SITE, ORIGINAL_PATH, CONTENT);
        verify(localV1, never()).writeContent(eq(SITE), eq(POINTER_PATH), any());
    }

    @Test
    public void deleteFileTest() throws ServiceLayerException {
        proxy.deleteContent(SITE, List.of(ORIGINAL_PATH), USER);

        verify(store).deleteContent(SITE, ORIGINAL_PATH);
        verify(localRepositoryV2).deleteContent(SITE, List.of(POINTER_PATH), USER);
    }

    @Test
    public void deleteRemoteFolderTest() throws ServiceLayerException {
        proxy.deleteContent(SITE, List.of(FOLDER_PATH), USER);

        verify(store).deleteContent(SITE, FOLDER_PATH);
        verify(localRepositoryV2).deleteContent(SITE, List.of(FOLDER_PATH), USER);
    }

    @Test
    public void deleteLocalFolderTest() throws ServiceLayerException {
        proxy.deleteContent(SITE, List.of(LOCAL_FOLDER_PATH), USER);

        verify(store, never()).deleteContent(SITE, LOCAL_FOLDER_PATH);
        verify(localRepositoryV2).deleteContent(SITE, List.of(LOCAL_FOLDER_PATH), USER);
    }

    @Test
    public void deleteContentFailTest() throws ServiceLayerException {
        doThrow(ServiceLayerException.class).when(store).deleteContent(SITE, ORIGINAL_PATH);

        assertThrows(ServiceLayerException.class, () -> proxy.deleteContent(SITE, List.of(ORIGINAL_PATH), USER));

        verify(localRepositoryV2, never()).deleteContent(SITE, List.of(POINTER_PATH), USER);
    }

    @Test
    public void moveFileTest() throws ServiceLayerException {
        proxy.moveContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH);

        verify(store).moveContent(SITE, ORIGINAL_PATH, NEW_FILE_PATH, null);
        verify(localV1).moveContent(SITE, POINTER_PATH, NEW_POINTER_PATH, null);
    }

    @Test
    public void moveFolderTest() throws ServiceLayerException {
        proxy.moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH);

        verify(store).moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH, null);
        verify(localV1).moveContent(SITE, FOLDER_PATH, NEW_FOLDER_PATH, null);
    }

    @Test
    public void getContentChildrenWithoutRemoteTest() {
        RepositoryItem item = new RepositoryItem();
        item.path = ORIGINAL_PATH;
        when(localV1.getContentChildren(SITE, PARENT_PATH)).thenReturn(new RepositoryItem[]{item});

        RepositoryItem[] result = proxy.getContentChildren(SITE, PARENT_PATH);

        assertNotNull(result);
        assertEquals(result.length, 1);
        assertEquals(result[0].path, ORIGINAL_PATH);
    }

    @Test
    public void getContentChildrenWithRemoteTest() {
        RepositoryItem item = new RepositoryItem();
        item.path = PARENT_PATH;
        item.name = FilenameUtils.getName(POINTER_PATH);
        when(localV1.getContentChildren(SITE, PARENT_PATH)).thenReturn(new RepositoryItem[]{item});

        RepositoryItem[] result = proxy.getContentChildren(SITE, PARENT_PATH);

        assertNotNull(result);
        assertEquals(result.length, 1);
        assertEquals(result[0].path, PARENT_PATH);
        assertEquals(result[0].name, FilenameUtils.getName(ORIGINAL_PATH));
    }

    @Test
    public void isFolderTest() {
        assertTrue(proxy.isFolder(SITE, PARENT_PATH), "parent path should be recognized as folder");
        assertFalse(proxy.isFolder(SITE, ORIGINAL_PATH), "original path should be recognized as file");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void publishRemoteFileTest() throws ServiceLayerException, IOException {
        PublishItemTO publishItemTO = mock(PublishItemTO.class);
        when(publishItemTO.getPath()).thenReturn(ORIGINAL_PATH);
        when(publishItemTO.getAction()).thenReturn(ADD);
        when(publishItemTO.getError()).thenReturn(0);

        when(store.publish(any(), any(), any())).then((Answer<PublishChangeSet<PublishItemTO>>) invocationOnMock -> {
            Collection<PublishItemTO> itemsParam = invocationOnMock.getArgument(2, Collection.class);
            return new PublishChangeSet<>(itemsParam, emptyList());
        });

        List<PublishItemTO> publishItems = singletonList(publishItemTO);
        proxy.publish(publishPackage, ENV, publishItems);

        verify(store).publish(any(), eq(ENV), itemsCaptor.capture());

        PublishItemTO capturedPublishItem = itemsCaptor.getValue().getFirst();
        assertEquals(capturedPublishItem.getPath(), ORIGINAL_PATH);
        assertEquals(capturedPublishItem.getAction(), ADD);

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        capturedPublishItem = itemsCaptor.getValue().getFirst();
        assertEquals(capturedPublishItem.getPath(), GIT_REPO_PATH);
    }

    @Test
    public void publishLocalFileTest() throws ServiceLayerException, IOException {
        PublishItemTO publishItemTO = mock(PublishItemTO.class);
        when(publishItemTO.getPath()).thenReturn(LOCAL_PATH);
        when(publishItemTO.getAction()).thenReturn(ADD);
        List<PublishItemTO> publishItems = singletonList(publishItemTO);

        proxy.publish(publishPackage, ENV, publishItems);

        verify(store, never()).publish(any(), any(), any());

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        PublishItemTO capturedPublishItem = itemsCaptor.getValue().getFirst();
        assertEquals(capturedPublishItem.getPath(), LOCAL_PATH);
        assertEquals(capturedPublishItem.getAction(), ADD);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void publishMixFilesTest() throws ServiceLayerException, IOException {
        PublishItemTO remoteItem = mock(PublishItemTO.class);
        when(remoteItem.getPath()).thenReturn(ORIGINAL_PATH);
        when(remoteItem.getAction()).thenReturn(ADD);

        PublishItemTO localItem = mock(PublishItemTO.class);
        when(localItem.getPath()).thenReturn(LOCAL_PATH);
        when(localItem.getAction()).thenReturn(ADD);

        when(store.publish(any(), any(), any())).thenAnswer((Answer<PublishChangeSet<PublishItemTO>>) invocationOnMock -> {
            Collection<PublishItemTO> itemsParam = invocationOnMock.getArgument(2, Collection.class);
            return new PublishChangeSet<>(itemsParam, emptyList());
        });

        List<PublishItemTO> items = List.of(remoteItem, localItem);
        proxy.publish(publishPackage, ENV, items);

        verify(store).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(ORIGINAL_PATH)), "remote file should have been published");

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(GIT_REPO_PATH)), "pointer file should have been published");

        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(LOCAL_PATH)), "local file should have been published");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void failedBlobTest() throws ServiceLayerException, IOException {
        PublishItemTO remoteItem = mock(PublishItemTO.class);
        when(remoteItem.getPath()).thenReturn(ORIGINAL_PATH);
        when(remoteItem.getAction()).thenReturn(ADD);

        PublishItemTO localItem = mock(PublishItemTO.class);
        when(localItem.getPath()).thenReturn(LOCAL_PATH);
        when(localItem.getAction()).thenReturn(ADD);

        when(store.publish(any(), any(), any())).thenAnswer((Answer<PublishChangeSet<PublishItemTO>>) invocationOnMock -> {
            Collection<PublishItemTO> itemsParam = invocationOnMock.getArgument(2, Collection.class);
            itemsParam.stream().findFirst().ifPresent(i -> i.setFailed(101));
            return new PublishChangeSet<>(emptyList(), itemsParam);
        });

        List<PublishItemTO> items = List.of(remoteItem, localItem);
        GitPublishChangeSet<PublishItemTO> resultChangeSet = proxy.publish(publishPackage, ENV, items);

        assertEquals(resultChangeSet.failedItems().size(), 1, "Exactly one failed item was expected");
        assertEquals(resultChangeSet.successfulItems().size(), 1, "Exactly one successful item was expected");

        verify(store).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(ORIGINAL_PATH)), "remote file should have been published");

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().noneMatch(i -> i.getPath().equals(GIT_REPO_PATH)), "pointer file should NOT have been published");

        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(LOCAL_PATH)), "local file should have been published");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partiallyFailedBlobsTest() throws ServiceLayerException, IOException {
        PublishItemTO remoteItem = mock(PublishItemTO.class);
        when(remoteItem.getPath()).thenReturn(ORIGINAL_PATH);
        when(remoteItem.getAction()).thenReturn(ADD);
        PublishItemTO remoteItem2 = mock(PublishItemTO.class);
        when(remoteItem2.getPath()).thenReturn(ORIGINAL_PATH_2);
        when(remoteItem2.getAction()).thenReturn(ADD);

        PublishItemTO localItem = mock(PublishItemTO.class);
        when(localItem.getPath()).thenReturn(LOCAL_PATH);
        when(localItem.getAction()).thenReturn(ADD);

        when(store.publish(any(), any(), any())).thenAnswer((Answer<PublishChangeSet<PublishItemTO>>) invocationOnMock -> {
            Collection<PublishItemTO> itemsParam = invocationOnMock.getArgument(2, Collection.class);
            List<PublishItemTO> failed = itemsParam.stream().filter(i -> i.getPath().equals(ORIGINAL_PATH)).peek(i -> i.setFailed(101)).toList();
            List<PublishItemTO> successful = itemsParam.stream().filter(i -> i.getPath().equals(ORIGINAL_PATH_2)).toList();
            itemsParam.stream().findFirst().ifPresent(i -> i.setFailed(101));
            return new PublishChangeSet<>(successful, failed);
        });

        List<PublishItemTO> items = List.of(remoteItem, localItem, remoteItem2);
        GitPublishChangeSet<PublishItemTO> resultChangeSet = proxy.publish(publishPackage, ENV, items);

        assertEquals(resultChangeSet.failedItems().size(), 1, "Exactly one failed item was expected");
        assertEquals(resultChangeSet.successfulItems().size(), 2, "Exactly two successful item was expected");

        verify(store).publish(any(), eq(ENV), itemsCaptor.capture());
        assertEquals(itemsCaptor.getValue().size(), 2, "Only two blobStore items should have been published");
        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(ORIGINAL_PATH)), "remote file should have been published");

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().noneMatch(i -> i.getPath().equals(GIT_REPO_PATH)), "pointer file should NOT have been published");

        verify(localRepositoryV2).publish(any(), eq(ENV), itemsCaptor.capture());
        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(GIT_REPO_PATH_2)), "pointer file should have been published");

        assertTrue(itemsCaptor.getValue().stream().anyMatch(i -> i.getPath().equals(LOCAL_PATH)), "local file should have been published");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void allItemsAreBlobsFailed() throws ServiceLayerException, IOException {
        PublishItemTO remoteItem1 = mock(PublishItemTO.class);
        when(remoteItem1.getPath()).thenReturn(ORIGINAL_PATH);
        when(remoteItem1.getAction()).thenReturn(ADD);
        PublishItemTO remoteItem2 = mock(PublishItemTO.class);
        when(remoteItem2.getPath()).thenReturn(ORIGINAL_PATH_2);
        when(remoteItem2.getAction()).thenReturn(ADD);

        when(store.publish(any(), any(), any())).thenAnswer((Answer<PublishChangeSet<PublishItemTO>>) invocationOnMock -> {
            Collection<PublishItemTO> itemsParam = invocationOnMock.getArgument(2, Collection.class);
            itemsParam.forEach(i -> i.setFailed(101));
            return new PublishChangeSet<>(emptyList(), itemsParam);
        });
        when(localRepositoryV2.publish(any(), any(), any())).thenCallRealMethod();

        List<PublishItemTO> items = List.of(remoteItem1, remoteItem2);

        GitPublishChangeSet<PublishItemTO> resultChangeSet = proxy.publish(publishPackage, ENV, items);
        assertEquals(resultChangeSet.successfulItems().size(), 0, "No successful items were expected");
        assertEquals(resultChangeSet.failedItems().size(), 2, "Exactly two failed items were expected");
        assertNull(resultChangeSet.commitId(), "No commit id was expected");

        verify(store).publish(any(), eq(ENV), itemsCaptor.capture());
        assertEquals(itemsCaptor.getValue().size(), 2, "Exactly two blobStore items should have been published");
    }

    @Test
    public void getContentVersionHistoryTest() {
        VersionTO version1 = new VersionTO();
        VersionTO version2 = new VersionTO();

        when(localV1.getContentVersionHistory(eq(SITE), eq(POINTER_PATH)))
                .thenReturn(new VersionTO[]{version1, version2});

        VersionTO[] versions = proxy.getContentVersionHistory(SITE, ORIGINAL_PATH);

        assertNotNull(versions);
        assertEquals(versions.length, 2);
    }

    @Test
    public void fileWithoutExtensionTest() throws ServiceLayerException {
        proxy.writeContent(SITE, NO_EXT_PATH, CONTENT);

        verify(store).writeContent(SITE, NO_EXT_PATH, CONTENT);
        verify(localV1).writeContent(eq(SITE), eq(NO_EXT_PATH + "." + BLOB_EXT), any());
    }

    @Test
    public void getChangeSetPathsFromDeltaTest() throws Exception {
        proxy.forAllSitePaths(SITE, d -> {
                },
                value -> assertFalse(value.endsWith(BLOB_EXT),
                        "The changeSet should not contain pointer paths"));
    }

    @Test
    public void initialPublishTest() throws ServiceLayerException {
        proxy.initialPublish(SITE);

        verify(store).initialPublish(SITE);
        verify(localRepositoryV2).initialPublish(SITE);
    }

    @Test(expectedExceptions = SiteNotFoundException.class)
    public void blobAwareRepoBubblesUpSiteNotFoundExceptionTest() throws ServiceLayerException {
        String nonExistingSite = "nonExistingSite";
        doThrow(SiteNotFoundException.class).when(localRepositoryV2).initialPublish(nonExistingSite);
        proxy.initialPublish(nonExistingSite);
    }

}
