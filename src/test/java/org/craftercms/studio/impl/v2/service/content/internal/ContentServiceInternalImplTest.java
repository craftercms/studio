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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceInternalImplTest {
    private static final String SITE_ID = "sample-site";
    private static final String PATH = "/sample/path";
    private static final String NON_EXIST_CONTENT_PATH = "/sample/non-exists-content-path";

    @Mock
    protected ContentRepository contentRepository;

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

}
