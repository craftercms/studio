package org.craftercms.studio.impl.v2.service.clipboard.internal;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClipboardServiceInternalImplTest {

    private static final String SITE_ID = "mySite";

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ClipboardServiceInternalImpl service;

    @Before
    public void setUp() {
        for (String pagePath : getPagePaths()) {
            when(contentService.getContentItem(SITE_ID, pagePath))
                    .thenReturn(createTestContentItem(false, true));
        }
        for (String folderPath : getFolderPaths()) {
            when(contentService.getContentItem(SITE_ID, folderPath))
                    .thenReturn(createTestContentItem(true, false));
        }
        for (String nonFolderPath : getNonFolderPaths()) {
            when(contentService.getContentItem(SITE_ID, nonFolderPath))
                    .thenReturn(createTestContentItem(false, false));
        }

        for (String existingPath : getExistingPaths()) {
            when(contentService.contentExists(SITE_ID, existingPath)).thenReturn(true);
        }
    }

    private String[] getPagePaths() {
        return new String[]{
                "/site/website/health/index.xml"
        };
    }

    private String[] getExistingPaths() {
        return new String[]{
                "/site/website/style/index.xml",
                "/site/website/news",
                "/static-assets/images/screenshot.png",
                "/site/components/header/default.xml",
                "/templates/web/layout/main.ftl",
                "/scripts/rest/search/documents.get.groovy",
                "/sources/folder/script.groovy",
                "/site/taxonomy/categories.xml",
                "/custom/folder/item.xls"
        };
    }

    private String[] getFolderPaths() {
        return new String[]{
                "/site/website/articles",
                "/site/components/headers",
                "/static-assets/screenshots",
                "/templates/web/blog",
                "/scripts/rest/documents/search",
                "/sources/classes/folder",
                "/site/taxonomy/categories",
                "/custom/old",
                "/reports/folder"
        };
    }

    private String[] getNonFolderPaths() {
        return new String[]{
                "/templates/web/layout.ftl",
                "/static-assets/screenshot.png"};
    }

    private ContentItemTO createTestContentItem(boolean isFolder, boolean isPage) {
        ContentItemTO item = new ContentItemTO();
        item.setFolder(isFolder);
        item.setPage(isPage);
        return item;
    }

    @Test
    public void allowPastingFolderIntoPageTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/website/news", "/site/website/health/index.xml"));
    }

    @Test
    public void allowPastingPageIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/website/style/index.xml", "/site/website/articles"));
    }

    @Test
    public void allowPastingFolderIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/website/news", "/site/website/articles"));
    }

    @Test
    public void allowPastingPageIntoPageTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/website/style/index.xml", "/site/website/health/index.xml"));
    }

    @Test
    public void allowPastingAssetIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/static-assets/images/screenshot.png", "/static-assets/screenshots"));
    }

    @Test
    public void allowPastingComponentIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/components/header/default.xml", "/site/components/headers"));
    }

    @Test
    public void allowPastingTemplateIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/templates/web/layout/main.ftl", "/templates/web/blog"));
    }

    @Test
    public void allowPastingScriptIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID,
                                "/scripts/rest/search/documents.get.groovy",
                                "/scripts/rest/documents/search"));
    }

    @Test
    public void allowPastingSourcesFileIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/sources/folder/script.groovy", "/sources/classes/folder"));
    }

    @Test
    public void allowPastingTaxonomyIntoFolderTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/site/taxonomy/categories.xml", "/site/taxonomy/categories"));
    }

    @Test
    public void allowTest() {
        assertDoesNotThrow(
                () ->
                        service.validatePasteItemsAction(
                                SITE_ID, "/custom/folder/item.xls", "/reports/folder"));
    }

    @Test
    public void allowPastingCustomPathItemIntoFolderTest()
            throws ContentNotFoundException, InvalidParametersException {
        assertDoesNotThrow(
                () -> service.validatePasteItemsAction(SITE_ID, "/custom/folder/item.xls", "/custom/old"));
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingPageIntoStaticAssetsTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/site/website/style/index.xml", "/static-assets/screenshots");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingComponentIntoStaticAssetsTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/site/components/header/default.xml", "/static-assets/screenshots");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingAssetIntoAssetTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/static-assets/images/screenshot.png", "/static-assets/screenshot.png");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingTemplateIntoStaticAssetsTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/templates/web/layout/main.ftl", "/static-assets/screenshots");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingTemplateIntoTemplateTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/templates/web/layout/main.ftl", "/templates/web/layout.ftl");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingScriptIntoTaxonomiesTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/scripts/rest/search/documents.get.groovy", "/site/taxonomy/categories");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingCustomPathItemIntoStaticAssetsTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(
                SITE_ID, "/custom/folder/item.xls", "/static-assets/screenshots");
    }

    @Test(expected = InvalidParametersException.class)
    public void preventPastingTemplateIntoCustomPathTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(SITE_ID, "/templates/web/layout/main.ftl", "/custom/old");
    }

    @Test(expected = ContentNotFoundException.class)
    public void preventPastingNonExistingContentTest()
            throws ContentNotFoundException, InvalidParametersException {
        service.validatePasteItemsAction(SITE_ID, "/templates/web/layout/unexistent.ftl", "/templates/web/blog");
    }
}
