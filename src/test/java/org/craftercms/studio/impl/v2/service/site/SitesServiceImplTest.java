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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SitesServiceImplTest {

    private static final String SITE_ID = "site1";
    private static final String NON_EXISTING_SITE_ID = "non-existing-site-id";

    @Mock
    protected SitesService sitesServiceInternal;
    @InjectMocks
    protected SitesServiceImpl sitesService;

    @Before
    public void setUp() {
        when(sitesServiceInternal.exists(NON_EXISTING_SITE_ID)).thenReturn(false);
        when(sitesServiceInternal.exists(SITE_ID)).thenReturn(true);
    }

    @Test
    public void siteDeleteTest() throws ServiceLayerException {
        sitesService.deleteSite(SITE_ID);
        verify(sitesServiceInternal).deleteSite(SITE_ID);
    }

    @Test
    public void nonExistingSiteDeleteTest() throws ServiceLayerException {
        assertThrows(SiteNotFoundException.class, () -> sitesService.deleteSite(NON_EXISTING_SITE_ID));
    }
}
