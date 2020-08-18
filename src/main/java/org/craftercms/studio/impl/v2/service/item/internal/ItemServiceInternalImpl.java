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
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW;

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

    // Create items representing all ancestors until repository root
    // In order to insert item into DB we need to insert all ancestors to establish parent-child relationship
    private List<Item> getAncestors(Item item) {
        List<Item> ancestors = new ArrayList<Item>();
        String itemPath = item.getPath();
        Path p = Paths.get(itemPath);
        List<Path> parts = new LinkedList<>();
        p.getParent().iterator().forEachRemaining(parts::add);
        Item i = Item.Builder.buildFromClone(item).withPath(StringUtils.EMPTY).build();
        if (CollectionUtils.isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                i = Item.Builder.buildFromClone(i).withPath(i.getPath() + FILE_SEPARATOR + ancestor.toString())
                        .withPreviewUrl(i.getPath()).withSystemType("folder").build();
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

    @Override
    public void setSystemProcessing(String siteId, String path, boolean isSystemProcessing) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        setSystemProcessingBulk(siteId, paths, isSystemProcessing);
    }

    @Override
    public void setSystemProcessingBulk(String siteId, List<String> paths, boolean isSystemProcessing) {
        if (isSystemProcessing) {
            setStatesBySiteAndPathBulk(siteId, paths, ItemState.SYSTEM_PROCESSING.value);
        } else {
            resetStatesBySiteAndPathBulk(siteId, paths, ItemState.SYSTEM_PROCESSING.value);
        }
    }

    private void setStatesBySiteAndPathBulk(String siteId, List<String> paths, long statesBitMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        itemDao.setStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
    }

    private void setStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        itemDao.setStatesByIdBulk(itemIds, statesBitMap);
    }

    private void resetStatesBySiteAndPathBulk(String siteId, List<String> paths, long statesBitMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        itemDao.resetStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
    }

    private void resetStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        itemDao.resetStatesByIdBulk(itemIds, statesBitMap);
    }

    @Override
    public void setStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        setStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void resetStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        resetStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void setStateBits(long itemId, long statesBitMask) {
        List<Long> ids = new ArrayList<Long>();
        ids.add(itemId);
        setStatesByIdBulk(ids, statesBitMask);
    }

    @Override
    public void resetStateBits(long itemId, long statesBitMask) {
        List<Long> ids = new ArrayList<Long>();
        ids.add(itemId);
        resetStatesByIdBulk(ids, statesBitMask);
    }

    @Override
    public void updateStateBits(String siteId, String path, long onStateBitMap, long offStateBitMap) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        updateStatesBySiteAndPathBulk(siteId, paths, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBits(long itemId, long onStateBitMap, long offStateBitMap) {
        List<Long> ids = new ArrayList<Long>();
        ids.add(itemId);
        updateStateBitsBulk(ids, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBitsBulk(String siteId, List<String> paths, long onStateBitMap, long offStateBitMap) {
        updateStatesBySiteAndPathBulk(siteId, paths, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBitsBulk(List<Long> itemIds, long onStateBitMap, long offStateBitMap) {
        itemDao.updateStatesByIdBulk(itemIds, onStateBitMap, offStateBitMap);
    }

    private void updateStatesBySiteAndPathBulk(String siteId, List<String> paths, long onStateBitMap,
                                               long offStateBitMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        itemDao.updateStatesBySiteAndPathBulk(siteFeed.getId(), paths, onStateBitMap, offStateBitMap);
    }

    @Override
    public Item.Builder instantiateItem(String siteName, String path) {
        Item item = getItem(siteName, path);
        if (Objects.isNull(item))  {
            item = new Item();
            Map<String, String> params = new HashMap<String, String>();
            params.put(SITE_ID, siteName);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            item.setSiteId(siteFeed.getId());
            item.setSiteName(siteName);
            item.setPath(path);
            item.setState(NEW.value);
        }
        return Item.Builder.buildFromClone(item);
    }

    @Override
    public Item instantiateItem(long siteId, String siteName, String path, String previewUrl, long state, long ownedBy,
                                String owner, long createdBy, String creator, ZonedDateTime createdOn,
                                long lastModifiedBy, String modifier, ZonedDateTime lastModifiedOn, String label,
                                String contentTypeId, String systemType, String mimeType, int disabledAsInt,
                                boolean disabled, String localeCode, long translationSourceId, long size, long parentId,
                                String commitId) {

        return instantiateItem(siteName, path).withPreviewUrl(previewUrl).withState(state).withOwnedBy(ownedBy)
                .withOwner(owner).withCreatedBy(createdBy).withCreator(creator).withCreatedOn(createdOn)
                .withLastModifiedBy(lastModifiedBy).withModifier(modifier).withLastModifiedOn(lastModifiedOn)
                .withLabel(label).withContentTypeId(contentTypeId).withSystemType(systemType).withMimeType(mimeType)
                .withDisabledAsInt(disabledAsInt).withDisabled(disabled).withLocaleCode(localeCode)
                .withTranslationSourceId(translationSourceId).withSize(size).withParentId(parentId)
                .withCommitId(commitId).build();

    }
}
