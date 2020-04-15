/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.dal.ItemMetadataDAO;
import org.craftercms.studio.api.v2.service.content.internal.ItemMetadataServiceInternal;

public class ItemMetadataServiceInternalImpl implements ItemMetadataServiceInternal {

    private ItemMetadataDAO itemMetadataDao;

    @Override
    public void calculateParentIdForSite(String siteId) {
        itemMetadataDao.updateParentIdForSite(siteId);
    }

    public ItemMetadataDAO getItemMetadataDao() {
        return itemMetadataDao;
    }

    public void setItemMetadataDao(ItemMetadataDAO itemMetadataDao) {
        this.itemMetadataDao = itemMetadataDao;
    }
}
