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

package org.craftercms.studio.impl.v2.service.content.internal;

import com.google.common.cache.Cache;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeServiceInternalImplTest {
    private static final String SITE_ID = "mySite";
    private static final String CONTENT_TYPE = "myContentType";
    private static final String CONTENT_TYPE_DEFINITION_FILENAME = "form-definition.xml";
    private static final String FORM_DEFINITION_ROOT = "form";
    private static final String FORM_DEFINITION_IMAGE_THUMBNAIL = "imageThumbnail";
    private static final String FORM_DEFINITION_PREVIEW_IMAGE = "testImage.png";
    private static final String CONTENT_TYPE_BASE_PATH = "/config/studio/content-types";
    private static final String CONTENT_TYPE_BASE_PATH_PATTERN = CONTENT_TYPE_BASE_PATH + "/{content-type}";
    private static final String CONTENT_TYPE_DEFINITION_PATH = CONTENT_TYPE_BASE_PATH + "/" + CONTENT_TYPE + "/" + CONTENT_TYPE_DEFINITION_FILENAME;
    private static final String CONTENT_TYPE_PREVIEW_IMAGE_PATH = CONTENT_TYPE_BASE_PATH + "/" + CONTENT_TYPE + "/" + FORM_DEFINITION_PREVIEW_IMAGE;

    private static final String CONTENT_TYPE_WITHOUT_IMAGE = "noImageContentType";
    private static final String CONTENT_TYPE_DEFINITION_PATH_WITHOUT_IMAGE = CONTENT_TYPE_BASE_PATH + "/" + CONTENT_TYPE_WITHOUT_IMAGE +
            "/" + CONTENT_TYPE_DEFINITION_FILENAME;
    private static final String CONTENT_TYPE_DEFAULT_PREVIEW_IMAGE_PATH = "crafter/studio/content-type/default-contentType.jpg";

    @Mock
    private ConfigurationService configurationService;
    @Mock
    private SiteService siteService;
    @Mock
    private ContentService contentService;
    @Mock
    Resource resource;

    @Mock
    StudioConfiguration studioConfiguration;

    @Mock
    Cache<String, ContentTypeConfigTO> cache;

    @InjectMocks
    private ContentTypeServiceInternalImpl service;

    @Before
    public void setUp() throws ServiceLayerException {
        ReflectionTestUtils.setField(service, "contentTypeDefinitionFilename", CONTENT_TYPE_DEFINITION_FILENAME);
        ReflectionTestUtils.setField(service, "contentTypeBasePathPattern", CONTENT_TYPE_BASE_PATH_PATTERN);
        ReflectionTestUtils.setField(service, "previewImageXPath", "/form/imageThumbnail/text()");
        ReflectionTestUtils.setField(service, "contentService", contentService);
        ReflectionTestUtils.setField(service, "defaultPreviewImagePath", CONTENT_TYPE_DEFAULT_PREVIEW_IMAGE_PATH);

        when(configurationService.getConfigurationAsDocument(SITE_ID, null, CONTENT_TYPE_DEFINITION_PATH, null))
                .thenReturn(getDocumentWithPreviewImage());

        when(contentService.getContentAsResource(SITE_ID, CONTENT_TYPE_PREVIEW_IMAGE_PATH))
                .thenReturn(resource);

        when(configurationService.getConfigurationAsDocument(SITE_ID, null, CONTENT_TYPE_DEFINITION_PATH_WITHOUT_IMAGE, null))
                .thenReturn(getDocumentWithoutPreviewImage());

        // for get content types test cases
        when(studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_PATH))
                .thenReturn("/config/studio/content-types/{content-type}");
        when(studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME))
                .thenReturn("config.xml");
        when(contentService.contentExists(SITE_ID, "/config/studio/content-types/page/home/config.xml"))
                .thenReturn(true);
        when(configurationService.getConfigurationAsDocument(SITE_ID, null,
                "/config/studio/content-types/page/home/config.xml", null))
                .thenReturn(getHomeContentTypeConfig());
    }

    private Document getDocumentWithPreviewImage() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement( FORM_DEFINITION_ROOT );
        root.addElement(FORM_DEFINITION_IMAGE_THUMBNAIL)
                .addText(FORM_DEFINITION_PREVIEW_IMAGE);

        return document;
    }

    private Document getDocumentWithoutPreviewImage() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(FORM_DEFINITION_ROOT);
        root.addElement(FORM_DEFINITION_IMAGE_THUMBNAIL)
                .addText("undefined");
        return document;
    }

    private Document getHomeContentTypeConfig() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("content-type");
        root.addAttribute("name", "/page/home");
        root.addAttribute("is-wcm-type", "true");
        root.addElement("label").addText("Home");
        root.addElement("form").addText("/page/home");
        root.addElement("form-path").addText("simple");
        root.addElement("model-instance-path").addText("NOT-USED-BY-SIMPLE-FORM-ENGINE");
        root.addElement("file-extension").addText("xml");
        root.addElement("content-as-folder").addText("true");
        root.addElement("previewable").addText("true");
        root.addElement("quickCreate").addText("false");
        root.addElement("quickCreatePath").addText("");
        root.addElement("noThumbnail").addText("false");
        root.addElement("image-thumbnail").addText("page-home.png");
        Element paths = root.addElement("paths");
        Element excludes = paths.addElement("excludes");
        excludes.addElement("pattern").addText("^/site/.*");

        return document;
    }

    @Test
    public void getPreviewImageReturnResource() throws ServiceLayerException {
        ImmutablePair<String, Resource> pair = service.getContentTypePreviewImage(SITE_ID, CONTENT_TYPE);
        assertEquals(pair.getKey(), CONTENT_TYPE_PREVIEW_IMAGE_PATH);
        assertEquals(pair.getValue(), resource);
    }

    @Test
    public void getDefaultPreviewImage() throws ServiceLayerException {
        ImmutablePair<String, Resource> pair = service.getContentTypePreviewImage(SITE_ID, CONTENT_TYPE_WITHOUT_IMAGE);
        assertEquals(pair.getKey(), CONTENT_TYPE_DEFAULT_PREVIEW_IMAGE_PATH);
    }

    @Test
    public void testLoadContentTypeConfiguration() throws ServiceLayerException {
        ContentTypeConfigTO result = service.loadContentTypeConfiguration(SITE_ID, "/page/home");
        assertEquals("/page/home", result.getForm());
        assertEquals(true, result.isContentAsFolder());
        assertEquals("page-home.png", result.getImageThumbnail());
        assertEquals(1, result.getPathExcludes().size());
        assertEquals("^/site/.*", result.getPathExcludes().get(0));
    }
}