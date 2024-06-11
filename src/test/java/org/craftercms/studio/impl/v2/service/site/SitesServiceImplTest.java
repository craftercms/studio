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

package org.craftercms.studio.impl.v2.service.site;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.impl.v2.service.site.internal.SitesServiceInternalImpl;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SitesServiceImplTest {

    private static final String EXISTING_SITE_ID = "existing-site";
    private static final String NON_EXISTING_SITE_ID = "non-existing-site";
    private static final String SOURCE_SITE_ID = "original";
    private static final String NEW_SITE_ID = "the-copy";

    @Mock
    protected SiteService siteServiceV1;
    @Mock
    protected SitesServiceInternalImpl sitesServiceInternal;
    @InjectMocks
    protected SitesServiceImpl sitesService;
    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() throws SiteNotFoundException {
        MockitoAnnotations.openMocks(this);
        doThrow(new SiteNotFoundException(NON_EXISTING_SITE_ID)).when(siteServiceV1).checkSiteExists(NON_EXISTING_SITE_ID);
        when(siteServiceV1.exists(EXISTING_SITE_ID)).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void duplicateNonExistentSiteTest() throws NoSuchMethodException {
        Method method = SitesServiceImpl.class.getMethod("duplicate", String.class, String.class, String.class,
                String.class, String.class, boolean.class);
        assertTrue(method.isAnnotationPresent(RequireSiteReady.class));
    }

    @Test
    public void duplicateIntoAlreadyExistentSiteTest() {
        assertThrows(SiteAlreadyExistsException.class, () ->
                sitesService.duplicate(SOURCE_SITE_ID, EXISTING_SITE_ID, "site_name", "The new site", "main_branch", false));
    }


    @Test
    public void duplicateSiteTest() throws ServiceLayerException {
        sitesService.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", "main_branch", false);

        verify(sitesServiceInternal).duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", "main_branch", false);
    }
}
