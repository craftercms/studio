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

package org.craftercms.studio.impl.v2.service.dependency.internal;

import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.model.rest.content.DependencyItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
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
    protected ItemServiceInternal itemServiceInternal;
    @Mock
    protected DependencyDAO dependencyDAO;

    @Spy
    @InjectMocks
    protected DependencyServiceInternalImpl serviceInternal;

    @Before
    public void setUp() {
        when(dependencyDAO.getDependentItems(SITE_ID, Collections.singletonList(PATH))).thenReturn(
                Arrays.asList(DEPENDENT_ITEM_1, DEPENDENT_ITEM_2)
        );

        when(itemServiceInternal.getItem(SITE_ID, DEPENDENT_ITEM_1)).thenReturn(
                new Item.Builder().withPath(DEPENDENT_ITEM_1).withAvailableActions(0).build()
        );

        when(itemServiceInternal.getItem(SITE_ID, DEPENDENT_ITEM_2)).thenReturn(
                new Item.Builder().withPath(DEPENDENT_ITEM_2).withAvailableActions(0).build()
        );
    }

    @Test
    public void getDependentItemsTest() {
        List<DependencyItem> items = serviceInternal.getDependentItems(SITE_ID, PATH);

        verify(dependencyDAO, times(1)).getDependentItems(SITE_ID, Collections.singletonList(PATH));
        verify(itemServiceInternal, times(1)).getItem(SITE_ID, DEPENDENT_ITEM_1);
        verify(itemServiceInternal, times(1)).getItem(SITE_ID, DEPENDENT_ITEM_2);
        assertEquals(2, items.size());
        assertEquals(DEPENDENT_ITEM_1, items.get(0).getPath());
        assertEquals(DEPENDENT_ITEM_2, items.get(1).getPath());
    }
}
