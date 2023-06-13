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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.impl.v2.service.item.internal.ItemServiceInternalImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Spy;

import org.springframework.context.ApplicationContext;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentServiceInternalImplTest {
    private static final String SITE_ID = "sample-site";
    private static final String PATH = "/sample/path";
    private static final String NON_EXIST_CONTENT_PATH = "/sample/non-exists-content-path";
    private static final String USERNAME = "sample-user";
    private static final String CREATE_FOLDER_PARENT_PATH = "/sample/parent-path";
    private static final String CREATE_FOLDER_NEW_FOLDER_NAME = "new-folder";
    private static final String CREATE_FOLDER_COMMIT_ID = "new-commit-id";

    @Mock
    protected ContentRepository contentRepository;

    @Mock
    protected ItemServiceInternalImpl itemServiceInternal;

    @Mock
    protected SecurityService securityService;

    @Mock
    protected AuditServiceInternal auditServiceInternal;

    @Mock
    protected SiteDAO siteDAO;

    @Mock
    ApplicationContext applicationContext;

    @Spy
    @InjectMocks
    protected ContentServiceInternalImpl serviceInternal;

    @Before
    public void setUp() throws UserNotFoundException, ServiceLayerException {
        Site site = new Site();
        site.setSiteId(SITE_ID);
        when(siteDAO.getSite(SITE_ID)).thenReturn(site);

        AuditLog auditLog = new AuditLog();
        when(auditServiceInternal.createAuditLogEntry()).thenReturn(auditLog);

        when(contentRepository.createFolder(SITE_ID, CREATE_FOLDER_PARENT_PATH, CREATE_FOLDER_NEW_FOLDER_NAME))
                .thenReturn(CREATE_FOLDER_COMMIT_ID);
        when(securityService.getCurrentUser()).thenReturn(USERNAME);
        when(itemServiceInternal.getItem(SITE_ID, CREATE_FOLDER_PARENT_PATH, true)).thenReturn(new Item());
        doNothing().when(itemServiceInternal)
                .persistItemAfterCreateFolder(anyString(), anyString(), anyString(), anyString(), anyString(), anyLong());
        when(auditServiceInternal.insertAuditLog(any())).thenReturn(true);
        doNothing().when(contentRepository).insertGitLog(SITE_ID, CREATE_FOLDER_COMMIT_ID, 1, 1);
        doNothing().when(siteDAO).updateLastCommitId(SITE_ID, CREATE_FOLDER_COMMIT_ID);

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

    @Test
    public void testCreateFolder() throws UserNotFoundException, ServiceLayerException {
        serviceInternal.createFolder(SITE_ID, CREATE_FOLDER_PARENT_PATH, CREATE_FOLDER_NEW_FOLDER_NAME);
        verify(contentRepository, times(1)).createFolder(SITE_ID, CREATE_FOLDER_PARENT_PATH,
                CREATE_FOLDER_NEW_FOLDER_NAME);
        verify(serviceInternal, times(0))
                .createMissingParentItem(SITE_ID, CREATE_FOLDER_PARENT_PATH, CREATE_FOLDER_COMMIT_ID);
    }

    @Test
    public void testCreateMissingParentItem() throws UserNotFoundException, ServiceLayerException {
        String parentPath = "/sample/a/b/c";
        when(itemServiceInternal.getItem(SITE_ID, "/sample/a/b", false))
                .thenReturn(null)
                .thenReturn(new Item.Builder()
                        .withId(3)
                        .withPath("/sample/a/b")
                        .withAvailableActions(0)
                        .build());
        when(itemServiceInternal.getItem(SITE_ID, "/sample/a", false))
                .thenReturn(null)
                .thenReturn(new Item.Builder()
                        .withId(2)
                        .withPath("/sample/a")
                        .withAvailableActions(0)
                        .build());
        when(itemServiceInternal.getItem(SITE_ID, "/sample", false))
                .thenReturn(null)
                .thenReturn(new Item.Builder()
                        .withId(1)
                        .withPath("/sample")
                        .withAvailableActions(0)
                        .build());
        when(itemServiceInternal.getItem(SITE_ID, "", false)).thenReturn(null);

        serviceInternal.createMissingParentItem(SITE_ID, parentPath, CREATE_FOLDER_COMMIT_ID);
        verify(itemServiceInternal, times(1)).persistItemAfterCreateFolder(
                SITE_ID, parentPath, "c", USERNAME, CREATE_FOLDER_COMMIT_ID, 3L
        );
        verify(itemServiceInternal, times(1)).persistItemAfterCreateFolder(
                SITE_ID, "/sample/a/b", "b", USERNAME, CREATE_FOLDER_COMMIT_ID, 2L
        );
        verify(itemServiceInternal, times(1)).persistItemAfterCreateFolder(
                SITE_ID, "/sample/a", "a", USERNAME, CREATE_FOLDER_COMMIT_ID, 1L
        );
        verify(itemServiceInternal, times(1)).persistItemAfterCreateFolder(
                SITE_ID, "/sample", "sample", USERNAME, CREATE_FOLDER_COMMIT_ID, null
        );
    }

}
