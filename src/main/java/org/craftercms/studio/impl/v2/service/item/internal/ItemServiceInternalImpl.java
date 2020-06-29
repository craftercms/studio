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

package org.craftercms.studio.impl.v2.service.item.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public class ItemServiceInternalImpl implements ItemServiceInternal {

    SiteFeedMapper siteFeedMapper;
    ItemDAO itemDao;

    public ItemServiceInternalImpl(SiteFeedMapper siteFeedMapper, ItemDAO itemDao) {
        this.siteFeedMapper = siteFeedMapper;
        this.itemDao = itemDao;
    }

    @Override
    public void upsertEntry(String siteId, Item item) {
        List<Item> items = new ArrayList<Item>();
        items.addAll(getAncestors(item));
        items.add(item);
        upsertEntries(siteId, items);
    }

    private List<Item> getAncestors(Item item) {
        List<Item> ancestors = new ArrayList<Item>();
        String itemPath = item.getPath();
        Path p = Paths.get(itemPath);
        List<Path> parts = new LinkedList<>();
        p.getParent().iterator().forEachRemaining(parts::add);
        Item i = Item.cloneItem(item);
        i.setPath("");
        if (CollectionUtils.isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                i = Item.cloneItem(i);
                i.setPath(i.getPath() + FILE_SEPARATOR + ancestor.toString());
                i.setPreviewUrl(i.getPath());
                i.setSystemType("folder");
                ancestors.add(i);
            }
        } else {
            i.setPreviewUrl(i.getPath());
            i.setSystemType("folder");
            ancestors.add(i);
        }
        return ancestors;
    }

    @Override
    public void upsertEntries(String siteId, List<Item> items) {
        itemDao.upsertEntries(items);
    }

    @Override
    public void updateParentIds(String siteId, String rootPath) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        itemDao.updateParentIdForSite(siteFeed.getId(), rootPath);
    }

    @Override
    public Item getItem(long id) {
        return itemDao.getItemById(id);
    }

    @Override
    public Item getItem(String siteId, String path) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getItemBySiteIdAndPath(siteFeed.getId(), path);
    }

    @Override
    public void deleteItem(long itemId) {
        itemDao.deleteById(itemId);
    }

    @Override
    public void deleteItem(String siteId, String path) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        itemDao.deleteBySiteAndPath(siteFeed.getId(), path);
    }

    @Override
    public void updateItem(Item item) {
        itemDao.updateItem(item);
    }
}
