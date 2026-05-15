/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.item.internal;

import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceInternalImplTest {

	private static final String SITE_ID = "sample-site";
	private static final String PATH = "/sample/path";
	private static final Long PARENT_ID = 123L;

	@Mock
	private GitContentRepository contentRepository;
	@Mock
	private GeneralLockService generalLockService;
	@Mock
	private SiteDAO siteDAO;
	@Mock
	private ItemDAO itemDAO;
	@Mock
	private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
	@Spy
	@InjectMocks
	private ItemServiceInternalImpl service;

	@Before
	public void setUp() throws ServiceLayerException {
		doNothing().when(retryingDatabaseOperationFacade).retry(any(Runnable.class));

		Site mockSite = mock(Site.class);
		when(mockSite.getId()).thenReturn(123L);
		when(siteDAO.getSite(SITE_ID)).thenReturn(mockSite);
		when(itemDAO.getItemByPath(123L, PATH, false)).thenReturn(mock(org.craftercms.studio.api.v2.dal.Item.class));

		doReturn("browser/url").when(service).getBrowserUrl(SITE_ID, PATH);
	}

	@Test
	public void testSavedAsDraftTrue() throws AuthenticationException, ServiceLayerException {
		testSavedAsDraft("true", true);
	}

	@Test
	public void testSavedAsDraftFalse() throws AuthenticationException, ServiceLayerException {
		testSavedAsDraft("false", false);
	}

	@Test
	public void testSavedAsDraftInvalidValue() throws AuthenticationException, ServiceLayerException {
		testSavedAsDraft("invalid", false);
	}

	private void testSavedAsDraft(String valueInXml, boolean expected) throws AuthenticationException, ServiceLayerException {
		Item mockItem = mock(Item.class);
		when(mockItem.queryDescriptorValue(ItemServiceInternalImpl.SAVED_AS_DRAFT)).thenReturn(valueInXml);
		when(contentRepository.getItem(SITE_ID, PATH, false)).thenReturn(mockItem);

		try (MockedStatic<SecurityUtils> secUtilsMock = mockStatic(SecurityUtils.class);
			 MockedStatic<ContentUtils> contentUtilsMock = mockStatic(ContentUtils.class)) {
			AuthenticatedUser user = mock(AuthenticatedUser.class);
			secUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);

			contentUtilsMock.when(() -> ContentUtils.getContentTypeClass(any(), any(), any(), any())).thenReturn("the type");
			service.persistItemAfterCreate(SITE_ID, PATH, PARENT_ID);
			verify(service).upsertEntry(argThat(item -> item.getSavedAsDraft() == expected));
		}
	}
}
