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
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.exception.CompositeException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.SecurityService;
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

import java.util.UUID;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SitesServiceInternalImplTest {

    private static final String ROOT_SITE_ID = "studio_root";
    private static final String SITE_ID = "site1";

    @Mock
    protected RetryingDatabaseOperationFacadeImpl retryingDatabaseOperationFacade;
    @Mock
    protected SiteDAO siteDAO;
    @Mock
    protected ContentRepository contentRepository;
    @Mock
    protected SiteFeedMapper siteFeedMapper;
    @Mock
    protected Deployer deployer;
    @Mock
    protected StudioConfiguration studioConfiguration;
    @Mock
    protected SecurityService securityService;
    @Mock
    protected AuditServiceInternal auditServiceInternal;
    @Mock
    protected ConfigurationService configurationService;
    @Mock
    protected ApplicationContext applicationContext;

    @Spy
    @InjectMocks
    protected SitesServiceInternalImpl serviceInternal;

    @Before
    public void setUp() {
        serviceInternal.setApplicationContext(applicationContext);
        Site site = new Site();
        site.setSiteId(SITE_ID);
        site.setName("Site 1");
        site.setSiteUuid(UUID.randomUUID().toString());
        when(siteDAO.getSite(SITE_ID)).thenReturn(site);

        Site rootSite = new Site();
        rootSite.setId(1);
        when(siteDAO.getSite(ROOT_SITE_ID)).thenReturn(rootSite);

        when(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE)).thenReturn(ROOT_SITE_ID);

        when(auditServiceInternal.createAuditLogEntry()).thenReturn(new AuditLog());

        when(securityService.getCurrentUser()).thenReturn("admin");

        when(contentRepository.deleteSite(SITE_ID)).thenReturn(true);

        doCallRealMethod().when(retryingDatabaseOperationFacade).retry(any(Runnable.class));
    }

    @Test
    public void siteDeleteTest() throws ServiceLayerException {
        doNothing().when(serviceInternal).destroySitePreviewContext(SITE_ID);
        serviceInternal.deleteSite(SITE_ID);

        verify(siteDAO, times(1)).startSiteDelete(SITE_ID);
        verify(siteDAO, times(1)).enablePublishing(SITE_ID, false);
        verify(deployer, times(1)).deleteTargets(SITE_ID);
        verify(serviceInternal, times(1)).destroySitePreviewContext(SITE_ID);
        verify(contentRepository, times(1)).deleteSite(SITE_ID);
        verify(configurationService, times(1)).invalidateConfiguration(SITE_ID);
        verify(siteDAO, times(1)).deleteSiteRelatedItems(SITE_ID);
        verify(siteDAO, times(1)).completeSiteDelete(SITE_ID);
        verify(auditServiceInternal, times(2)).insertAuditLog(any());
    }

    @Test
    public void deployerDownSiteDeleteTest() throws ServiceLayerException {
        doNothing().when(serviceInternal).destroySitePreviewContext(SITE_ID);
        doThrow(new RestClientException("Deployer is down")).when(deployer).deleteTargets(SITE_ID);

        assertThrows(ServiceLayerException.class, () -> serviceInternal.deleteSite(SITE_ID));

        verify(siteDAO, times(1)).startSiteDelete(SITE_ID);
        verify(siteDAO, times(1)).enablePublishing(SITE_ID, false);
        verify(deployer, times(1)).deleteTargets(SITE_ID);
        verify(serviceInternal, times(1)).destroySitePreviewContext(SITE_ID);
        verify(contentRepository, times(1)).deleteSite(SITE_ID);
        verify(configurationService, times(1)).invalidateConfiguration(SITE_ID);
        verify(siteDAO, times(1)).deleteSiteRelatedItems(SITE_ID);
        verify(siteDAO, never()).completeSiteDelete(SITE_ID);
        verify(auditServiceInternal, times(1)).insertAuditLog(any());
    }

    @Test
    public void multipleExceptionsSiteDeleteTest() throws ServiceLayerException {
        doNothing().when(serviceInternal).destroySitePreviewContext(SITE_ID);
        doThrow(new RestClientException("Deployer is down")).when(deployer).deleteTargets(SITE_ID);
        doThrow(new Exception("Unexpected file system error")).when(contentRepository).deleteSite(SITE_ID);

        CompositeException exception = assertThrows(CompositeException.class, () -> serviceInternal.deleteSite(SITE_ID));
        assertEquals(2, exception.getExceptions().size());

        verify(siteDAO, times(1)).startSiteDelete(SITE_ID);
        verify(siteDAO, times(1)).enablePublishing(SITE_ID, false);
        verify(deployer, times(1)).deleteTargets(SITE_ID);
        verify(serviceInternal, times(1)).destroySitePreviewContext(SITE_ID);
        verify(contentRepository, times(1)).deleteSite(SITE_ID);
        verify(configurationService, times(1)).invalidateConfiguration(SITE_ID);
        verify(siteDAO, times(1)).deleteSiteRelatedItems(SITE_ID);
        verify(siteDAO, never()).completeSiteDelete(SITE_ID);
        verify(auditServiceInternal, times(1)).insertAuditLog(any());
    }

    @Test
    public void nonCriticalErrorsSiteDeleteTest() throws ServiceLayerException {
        doThrow(new ServiceLayerException("Failed to destroy site preview context")).when(serviceInternal).destroySitePreviewContext(SITE_ID);

        doThrow(new RuntimeException("Unexpected db error")).when(siteDAO).enablePublishing(SITE_ID, false);

        serviceInternal.deleteSite(SITE_ID);

        verify(siteDAO, times(1)).startSiteDelete(SITE_ID);
        verify(siteDAO, times(1)).enablePublishing(SITE_ID, false);
        verify(deployer, times(1)).deleteTargets(SITE_ID);
        verify(serviceInternal, times(1)).destroySitePreviewContext(SITE_ID);
        verify(contentRepository, times(1)).deleteSite(SITE_ID);
        verify(configurationService, times(1)).invalidateConfiguration(SITE_ID);
        verify(siteDAO, times(1)).deleteSiteRelatedItems(SITE_ID);
        verify(siteDAO, times(1)).completeSiteDelete(SITE_ID);
        verify(auditServiceInternal, times(2)).insertAuditLog(any());
    }

}
