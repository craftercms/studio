/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.dependency.internal;

import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DependencyServiceInternalImplTest {
	private static final String SITE_ID = "sample-site";
	private static final String PATH = "/sample/path";
	private static final String DEPENDENT_ITEM_1 = "/sample/dependent-item-1";
	private static final String DEPENDENT_ITEM_2 = "/sample/dependent-item-2";
	@Mock
	protected DependencyDAO dependencyDAO;

	@Spy
	@InjectMocks
	protected DependencyServiceInternalImpl serviceInternal;

	@Before
	public void setUp() {
		LightItem dep1 = new LightItem();
		dep1.setPath(DEPENDENT_ITEM_1);
		LightItem dep2 = new LightItem();
		dep2.setPath(DEPENDENT_ITEM_2);
		when(dependencyDAO.getDependentItems(SITE_ID, Collections.singletonList(PATH))).thenReturn(
				List.of(dep1, dep2)
		);
	}

	@Test
	public void getDependentItemsTest() {
		List<LightItem> items = new ArrayList<>(serviceInternal.getDependentItems(SITE_ID, PATH));
		items.sort(Comparator.comparing(LightItem::getPath));
		verify(dependencyDAO, times(1)).getDependentItems(SITE_ID, Collections.singletonList(PATH));
		assertEquals(2, items.size());
		assertEquals(DEPENDENT_ITEM_1, items.get(0).getPath());
		assertEquals(DEPENDENT_ITEM_2, items.get(1).getPath());
	}
}
