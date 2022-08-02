/*
 *
 *  * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License version 3 as published by
 *  * the Free Software Foundation.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.impl.v1.service.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.dom4j.DocumentException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ContentServiceImplTest {

    private static final String SITE = "my_test_site";

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentServiceImpl contentService;

    @BeforeTest
    public void setUp() throws IOException, DocumentException, ServiceLayerException {
        initMocks(this);
    }

    private RepositoryItem getRepoItem(String path, String name, boolean isFolder) {
        RepositoryItem item = new RepositoryItem();
        item.name = name;
        item.path = path;
        item.isFolder = isFolder;

        return item;
    }

    private RepositoryItem[] getRepoItems(String path, List<String> names, boolean folders) {
        return names.stream()
                .map(name -> getRepoItem(path, name, folders))
                .toArray(RepositoryItem[]::new);
    }

    @Test(testName = "Cut/Copy folder into folder on collision")
    public void testFolderCutPasteOnCollision() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems("/site/website/articles", List.of("folder"), true);
        List<String> existentPaths = List.of("/site/website/articles/folder");
        when(contentRepository.getContentChildren(SITE, "/site/website/articles")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/folder",
                "/site/website/articles", true);

        assertEquals("/site/website/articles/folder-copy-1", pathMap.filePath, "File path is not the expected");
        assertEquals("folder-copy-1", pathMap.fileName, "File name is not the expected");
        assertEquals("articles", pathMap.fileFolder, "File folder is not the expected");
    }

    @Test(testName = "Cut/Copy folder into folder on multiple collision")
    public void testFolderCutPasteOnCollisionMultiple() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems(
                "/site/website/articles",
                List.of("folder", "folder-copy-1", "folder-copy-2"),
                true);
        List<String> existentPaths = List.of("/site/website/articles/folder",
                "/site/website/articles/folder-copy-3",
                "/site/website/articles/folder-copy-2");
        when(contentRepository.getContentChildren(SITE, "/site/website/articles")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/folder",
                "/site/website/articles", true);

        assertEquals("/site/website/articles/folder-copy-3", pathMap.filePath, "File path is not the expected");
        assertEquals("folder-copy-3", pathMap.fileName, "File name is not the expected");
        assertEquals("articles", pathMap.fileFolder, "File folder is not the expected");
    }

    @Test(testName = "Cut/Copy page into folder on collision")
    public void testPageCutPasteIntoFolderOnCollision() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems(
                "/site/website/articles",
                List.of("style"),
                true);
        List<String> existentPaths = List.of("/site/website/articles/style/index.xml");
        when(contentRepository.getContentChildren(SITE, "/site/website/articles")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/style/index.xml",
                "/site/website/articles", true);

        assertEquals("/site/website/articles/style-copy-1/index.xml", pathMap.filePath, "File path is not the expected");
        assertEquals("index.xml", pathMap.fileName, "File name is not the expected");
        assertEquals("style-copy-1", pathMap.fileFolder, "File folder is not the expected");
    }

    @Test(testName = "Cut/Copy page into folder on multiple collision")
    public void testPageCutPasteIntoFolderOnCollisionMultiple() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems(
                "/site/website/articles",
                List.of("style", "style-copy-1", "style-copy-2"),
                true);
        List<String> existentPaths = List.of("/site/website/articles/style/index.xml");
        when(contentRepository.getContentChildren(SITE, "/site/website/articles")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/style/index.xml",
                "/site/website/articles", true);

        assertEquals("/site/website/articles/style-copy-3/index.xml", pathMap.filePath, "File path is not the expected");
        assertEquals("index.xml", pathMap.fileName, "File name is not the expected");
        assertEquals("style-copy-3", pathMap.fileFolder, "File folder is not the expected");
    }

    @Test(testName = "Cut/Copy folder into page on collision")
    public void testFolderCutPasteIntoPageOnCollision() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems(
                "/site/website/style",
                List.of("style", "articles-copy-1", "articles-copy-2"),
                true);
        List<String> existentPaths = List.of(
                "/site/website/style/articles-copy-1",
                "/site/website/style/articles-copy-2");
        when(contentRepository.getContentChildren(SITE, "/site/website/style")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/articles-copy-1",
                "/site/website/style/index.xml", true);

        assertEquals("/site/website/style/articles-copy-3", pathMap.filePath, "File path is not the expected");
        assertEquals("articles-copy-3", pathMap.fileName, "File name is not the expected");
        assertEquals("style", pathMap.fileFolder, "File folder is not the expected");
    }


    @Test(testName = "Cut/Copy page into page on collision")
    public void testPageCutPasteIntoPageOnCollision() throws ServiceLayerException {
        RepositoryItem[] repoItems = getRepoItems(
                "/site/website/health",
                List.of("style", "style-copy-1", "style-copy-2"),
                false);
        List<String> existentPaths = List.of(
                "/site/website/health/style-copy-1",
                "/site/website/health/style-copy-2");
        when(contentRepository.getContentChildren(SITE, "/site/website/health")).thenReturn(repoItems);
        existentPaths.forEach(path -> when(contentService.contentExists(SITE, path)).thenReturn(true));

        ContentServiceImpl.PastedPathMap pathMap = contentService.constructNewPathForCutCopy(SITE,
                "/site/website/style-copy-1",
                "/site/website/health/index.xml", true);

        assertEquals("/site/website/health/style-copy-3", pathMap.filePath, "File path is not the expected");
        assertEquals("style-copy-3", pathMap.fileName, "File name is not the expected");
        assertEquals("health", pathMap.fileFolder, "File folder is not the expected");
    }
}
