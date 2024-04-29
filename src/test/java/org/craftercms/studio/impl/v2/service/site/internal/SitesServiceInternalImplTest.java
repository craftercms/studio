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

package org.craftercms.studio.impl.v2.service.site.internal;

import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobAwareContentRepository;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.dal.RetryingDatabaseOperationFacadeImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_ENABLED;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SitesServiceInternalImplTest {

    private static final String USED_SITE_NAME = "already-taken";
    private static final String SOURCE_SITE_ID = "original";
    private static final String NEW_SITE_ID = "the-copy";
    private static final String SOURCE_SANDBOX_BRANCH = "develop";
    private static final String DUPLICATE_SANDBOX_BRANCH = "feature1";

    @Mock
    SiteFeedMapper siteFeedMapper;
    @Mock
    SiteService siteServiceV1;
    @Mock
    Deployer deployer;
    @Spy
    RetryingDatabaseOperationFacadeImpl retryingDatabaseOperationFacade;
    @Mock
    StudioBlobAwareContentRepository contentRepository;
    @Mock
    ConfigurationService configurationService;
    @Mock
    StudioConfiguration studioConfiguration;
    @Mock
    SecurityService securityService;
    @Mock
    SiteDAO siteDAO;
    @Mock
    ApplicationContext applicationContext;
    @Spy
    @InjectMocks
    SitesServiceInternalImpl sitesServiceInternal;

    @Before
    public void setUp() throws IOException {
        when(siteFeedMapper.isNameUsed(NEW_SITE_ID, USED_SITE_NAME)).thenReturn(true);

        doNothing().when(sitesServiceInternal).addSiteUuidFile(anyString(), anyString());
        doCallRealMethod().when(retryingDatabaseOperationFacade).retry(any(Runnable.class));

        doNothing().when(sitesServiceInternal).auditSiteDuplicate(anyString(), anyString(), anyString());
        sitesServiceInternal.setApplicationContext(applicationContext);

        when(studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false)).thenReturn(false);

        Site sourceSite = new Site();
        sourceSite.setPublishingEnabled(true);
        sourceSite.setSandboxBranch(SOURCE_SANDBOX_BRANCH);
        when(siteDAO.getSite(SOURCE_SITE_ID)).thenReturn(sourceSite);
    }

    @Test
    public void duplicateAlreadyTakenNameTest() {
        assertThrows(SiteAlreadyExistsException.class, () ->
                sitesServiceInternal.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, USED_SITE_NAME, "The new site", "main_branch", false));
    }

    @Test
    public void readOnlyOnBlobStoresTest() throws ServiceLayerException {
        sitesServiceInternal.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", "main_branch", true);

        verify(configurationService).makeBlobStoresReadOnly(NEW_SITE_ID);
    }

    @Test
    public void readOnlyOffBlobStoresTest() throws ServiceLayerException {
        sitesServiceInternal.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", "main_branch", false);

        verify(configurationService, never()).makeBlobStoresReadOnly(NEW_SITE_ID);
    }

    @Test
    public void duplicateSiteTest() throws ServiceLayerException, IOException {
        sitesServiceInternal.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", DUPLICATE_SANDBOX_BRANCH, false);

        verify(contentRepository).duplicateSite(SOURCE_SITE_ID, NEW_SITE_ID, SOURCE_SANDBOX_BRANCH, DUPLICATE_SANDBOX_BRANCH);
        verify(sitesServiceInternal).addSiteUuidFile(eq(NEW_SITE_ID), any());
        verify(siteFeedMapper).duplicate(eq(SOURCE_SITE_ID), eq(NEW_SITE_ID), eq("site_name"), eq("The new site"), eq(DUPLICATE_SANDBOX_BRANCH), any());

        verify(deployer).duplicateTargets(SOURCE_SITE_ID, NEW_SITE_ID);
        verify(siteServiceV1).enablePublishing(NEW_SITE_ID, true);

        verify(siteServiceV1).enablePublishing(SOURCE_SITE_ID, false);
        verify(siteServiceV1).enablePublishing(SOURCE_SITE_ID, true);
    }

    @Test
    public void duplicateSiteErrorTest() {
        doThrow(new RestClientException("test")).when(deployer).duplicateTargets(SOURCE_SITE_ID, NEW_SITE_ID);

        assertThrows(ServiceLayerException.class, () ->
                sitesServiceInternal.duplicate(SOURCE_SITE_ID, NEW_SITE_ID, "site_name", "The new site", "main_branch", false));
        verify(siteServiceV1).deleteSite(NEW_SITE_ID);
    }
}
