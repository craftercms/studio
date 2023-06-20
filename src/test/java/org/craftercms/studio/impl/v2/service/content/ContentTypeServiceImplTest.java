/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeServiceImplTest {
    private static final String SITE_ID = "sample-site";
    private static final String CONTENT_TYPE_ID = "sample-content-type-id";
    private static final String PATH = "/sample/path";

    @Mock
    ContentTypeServiceInternal contentTypeServiceInternal;

    @Mock
    SiteService siteService;
    @Spy
    @InjectMocks
    protected ContentTypeServiceImpl contentTypeService;

    @Before
    public void setUp() throws SiteNotFoundException {
        doNothing().when(siteService).checkSiteExists(SITE_ID);
    }

    @Test
    public void testGetContentType() throws ServiceLayerException {
        contentTypeService.getContentType(SITE_ID, CONTENT_TYPE_ID);
        verify(contentTypeServiceInternal, times(1)).loadContentTypeConfiguration(SITE_ID, CONTENT_TYPE_ID);
    }

    @Test
    public void testGetContentTypes() throws ServiceLayerException {
        contentTypeService.getContentTypes(SITE_ID, PATH);
        verify(contentTypeServiceInternal, times(1)).getContentTypes(SITE_ID, PATH);
    }
}