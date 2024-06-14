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

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.annotation.RequireContentExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class ContentServiceImplTest {
    private static final String EXISTING_PATH = "/path/to/existing/item";
    private static final String SITE_NAME = "siteName";

    @Mock
    ContentService contentServiceV1;
    @Mock
    ContentServiceInternal contentServiceInternal;
    @InjectMocks
    ContentServiceImpl contentService;

    private AutoCloseable mocks;

    @Before
    public void setUp() throws ServiceLayerException {
        mocks = openMocks(this);
        doThrow(new ContentNotFoundException()).when(contentServiceV1).checkContentExists(anyString(), not(eq(EXISTING_PATH)));

    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void contentHistoryNonExistentItem() throws NoSuchMethodException {
        Method method = ContentServiceImpl.class.getMethod("getContentVersionHistory", String.class, String.class);
        assertTrue(method.isAnnotationPresent(RequireContentExists.class));
    }

    @Test
    public void contentHistoryNonExistentSite() throws NoSuchMethodException {
        Method method = ContentServiceImpl.class.getMethod("getContentVersionHistory", String.class, String.class);
        assertTrue(method.isAnnotationPresent(RequireSiteReady.class));
    }

    @Test
    public void contentHistoryValidParams() throws ServiceLayerException {
        contentService.getContentVersionHistory(SITE_NAME, EXISTING_PATH);
        verify(contentServiceInternal).getContentVersionHistory(SITE_NAME, EXISTING_PATH);
    }

    @Test
    public void testSiteNotFound() throws NoSuchMethodException {
        Method method = ContentServiceImpl.class.getMethod("contentExists", String.class, String.class);
        assertTrue(method.isAnnotationPresent(RequireSiteExists.class));
    }

}
