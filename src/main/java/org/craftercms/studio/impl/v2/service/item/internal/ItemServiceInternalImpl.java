/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.ItemState.IN_PROGRESS_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SAVE_AND_CLOSE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMITTED_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW;

public class ItemServiceInternalImpl implements ItemServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceInternalImpl.class);

    public final static String INTERNAL_NAME = "//internal-name";
    public final static String CONTENT_TYPE = "//content-type";
    public final static String DISABLED = "//disabled";
    public final static String LOCALE_CODE = "//locale-code";

    private UserServiceInternal userServiceInternal;
    private SiteFeedMapper siteFeedMapper;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private ContentServiceInternal contentServiceInternal;
    private ContentService contentService;

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
        if (Objects.nonNull(p.getParent())) {
            p.getParent().iterator().forEachRemaining(parts::add);
        }
        Item i = Item.Builder.buildFromClone(item).withPath(StringUtils.EMPTY).build();
        if (CollectionUtils.isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                if (StringUtils.isNotEmpty(ancestor.toString())) {
                    i = instantiateItem(item.getSiteName(), i.getPath() + FILE_SEPARATOR + ancestor.toString())
                            .withSystemType("folder").withLabel(ancestor.toString()).build();
                    if (i.getId() < 1) {
                        ancestors.add(i);
                    }
                }
            }
        }
        return ancestors;
    }

    @Override
    public void upsertEntries(String siteId, List<Item> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            itemDao.upsertEntries(items);
        }
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
        Item item = itemDao.getItemBySiteIdAndPath(siteFeed.getId(), path);
        if (Objects.nonNull(item)) {
            item.setSiteName(siteId);
        }
        return item;
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
        if (CollectionUtils.isNotEmpty(paths)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            itemDao.setStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
        }
    }

    private void setStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            itemDao.setStatesByIdBulk(itemIds, statesBitMap);
        }
    }

    private void resetStatesBySiteAndPathBulk(String siteId, List<String> paths, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(paths)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            itemDao.resetStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
        }
    }

    private void resetStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            itemDao.resetStatesByIdBulk(itemIds, statesBitMap);
        }
    }

    @Override
    public void setStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        setStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void setStateBitsBulk(String siteId, List<String> paths, long statesBitMask) {
        setStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void resetStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        resetStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void resetStateBitsBulk(String siteId, List<String> paths, long statesBitMask) {
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
        if (CollectionUtils.isNotEmpty(itemIds)) {
            itemDao.updateStatesByIdBulk(itemIds, onStateBitMap, offStateBitMap);
        }
    }

    private void updateStatesBySiteAndPathBulk(String siteId, List<String> paths, long onStateBitMap,
                                               long offStateBitMap) {
        if (CollectionUtils.isNotEmpty(paths)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            itemDao.updateStatesBySiteAndPathBulk(siteFeed.getId(), paths, onStateBitMap, offStateBitMap);
        }
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
        return Item.Builder.buildFromClone(item).withId(item.getId());
    }

    @Override
    public Item instantiateItem(long siteId, String siteName, String path, String previewUrl, long state, Long ownedBy,
                                String owner, Long createdBy, String creator, ZonedDateTime createdOn,
                                Long lastModifiedBy, String modifier, ZonedDateTime lastModifiedOn, String label,
                                String contentTypeId, String systemType, String mimeType, int disabledAsInt,
                                boolean disabled, String localeCode, Long translationSourceId, long size, Long parentId,
                                String commitId) {

        return instantiateItem(siteName, path).withPreviewUrl(previewUrl).withState(state).withOwnedBy(ownedBy)
                .withOwner(owner).withCreatedBy(createdBy).withCreator(creator).withCreatedOn(createdOn)
                .withLastModifiedBy(lastModifiedBy).withModifier(modifier).withLastModifiedOn(lastModifiedOn)
                .withLabel(label).withContentTypeId(contentTypeId).withSystemType(systemType).withMimeType(mimeType)
                .withDisabledAsInt(disabledAsInt).withDisabled(disabled).withLocaleCode(localeCode)
                .withTranslationSourceId(translationSourceId).withSize(size).withParentId(parentId)
                .withCommitId(commitId).build();

    }

    @Override
    public Item instantiateItemAfterWrite(String siteId, String path, String username, ZonedDateTime lastModifiedOn,
                                          String label, String contentTypeId, String locale, String commitId,
                                          long size, Optional<Boolean> unlock)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        Item item = instantiateItem(siteId, path)
                .withPreviewUrl(path)
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(lastModifiedOn)
                .withLabel(label)
                .withContentTypeId(contentTypeId)
                .withMimeType(StudioUtils.getMimeType(path))
                .withLocaleCode(locale)
                .withCommitId(commitId)
                .withSize(size)
                .build();
        if (unlock.isPresent() && !unlock.get()) {
            item.setState(ItemState.savedAndNotClosed(item.getState()));
        } else {
            item.setState(ItemState.savedAndClosed(item.getState()));
        }
        return item;
    }

    @Override
    public void deleteItemsForSite(long siteId) {
        itemDao.deleteItemsForSite(siteId);
    }

    @Override
    public void deleteItemsById(List<Long> itemIds) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            itemDao.deleteItemsById(itemIds);
        }
    }

    @Override
    public void deleteItemsForSiteAndPaths(long siteId, List<String> paths) {
        if (CollectionUtils.isNotEmpty(paths)) {
            itemDao.deleteItemsForSiteAndPath(siteId, paths);
        }
    }

    @Override
    public int getContentDashboardTotal(String siteId, String path, String modifier, String contentType, long state,
                                        ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return itemDao.getContentDashboardTotal(siteId, path, modifier, contentType, state, dateFrom, dateTo);
    }

    @Override
    public List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier, String contentType, long state,
                                                          ZonedDateTime dateFrom, ZonedDateTime dateTo, String sortBy, String order,
                                                          int offset, int limit) {
        List<Item> items = itemDao.getContentDashboard(siteId, path, modifier, contentType, state, dateFrom,
                dateTo, sortBy, order, offset, limit);
        return items.stream()
                .map(i -> convertItemToContentDashboardItem(siteId, i))
                .collect(Collectors.toList());
    }

    @Override
    public String getBrowserUrl(String site, String path) {
        String replacePattern;
        boolean isPage = false;
        if (ContentUtils.matchesPatterns(path, servicesConfig.getComponentPatterns(site)) ||
                StringUtils.endsWith(path,FILE_SEPARATOR + servicesConfig.getLevelDescriptorName(site))) {
            return null;
        } else if (ContentUtils.matchesPatterns(path, servicesConfig.getAssetPatterns(site))) {
            replacePattern = StringUtils.EMPTY;
        } else if (ContentUtils.matchesPatterns(path, servicesConfig.getDocumentPatterns(site))) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
            isPage = true;
        }

        return getBrowserUri(path, replacePattern, isPage);
    }

    protected String getBrowserUri(String uri, String replacePattern, boolean isPage) {
        String browserUri = uri.replaceFirst(replacePattern, "");
        browserUri = browserUri.replaceFirst(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = FILE_SEPARATOR;
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
    }

    @Override
    public void persistItemAfterCreate(String siteId, String path, String username, String commitId,
                                       Optional<Boolean> unlock)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        var descriptor = contentServiceInternal.getItem(siteId, path, false);
        String disabledStr = descriptor.queryDescriptorValue(DISABLED);
        boolean disabled = StringUtils.isNotEmpty(disabledStr) && "true".equalsIgnoreCase(disabledStr);
        String label = descriptor.queryDescriptorValue(INTERNAL_NAME);
        if (StringUtils.isEmpty(label)) {
            logger.error("Label = " + label);
        }
        Item item = instantiateItem(siteId, path)
                .withPreviewUrl(getBrowserUrl(siteId, path))
                .withOwnedBy(userObj.getId())
                .withCreatedBy(userObj.getId())
                .withCreatedOn(ZonedDateTime.now())
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(ZonedDateTime.now())
                .withLabel(label)
                .withSystemType(contentService.getContentTypeClass(siteId, path))
                .withContentTypeId(descriptor.queryDescriptorValue(CONTENT_TYPE))
                .withMimeType(StudioUtils.getMimeType(path))
                .withLocaleCode(descriptor.queryDescriptorValue(LOCALE_CODE))
                .withCommitId(commitId)
                .withDisabled(disabled)
                .withSize(contentServiceInternal.getContentSize(siteId, path))
                .build();
        if (unlock.isPresent() && !unlock.get()) {
            item.setState(ItemState.savedAndNotClosed(item.getState()));
        } else {
            item.setState(ItemState.savedAndClosed(item.getState()));
        }
        upsertEntry(siteId, item);
    }

    @Override
    public void persistItemAfterWrite(String siteId, String path, String username, String commitId,
                                      Optional<Boolean> unlock)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        var descriptor = contentServiceInternal.getItem(siteId, path, false);
        String disabledStr = descriptor.queryDescriptorValue(DISABLED);
        boolean disabled = StringUtils.isNotEmpty(disabledStr) && "true".equalsIgnoreCase(disabledStr);
        String label = descriptor.queryDescriptorValue(INTERNAL_NAME);
        if (StringUtils.isEmpty(label)) {
            label = FilenameUtils.getName(path);
        }
        Item item = instantiateItem(siteId, path)
                .withPreviewUrl(getBrowserUrl(siteId, path))
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(ZonedDateTime.now())
                .withLabel(label)
                .withSystemType(contentService.getContentTypeClass(siteId, path))
                .withContentTypeId(descriptor.queryDescriptorValue(CONTENT_TYPE))
                .withMimeType(StudioUtils.getMimeType(path))
                .withLocaleCode(descriptor.queryDescriptorValue(LOCALE_CODE))
                .withCommitId(commitId)
                .withDisabled(disabled)
                .withSize(contentServiceInternal.getContentSize(siteId, path))
                .build();
        if (unlock.isPresent() && !unlock.get()) {
            item.setState(ItemState.savedAndNotClosed(item.getState()));
        } else {
            item.setState(ItemState.savedAndClosed(item.getState()));
        }
        upsertEntry(siteId, item);
    }

    @Override
    public void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, String username,
                                             String commitId)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        Item item = instantiateItem(siteId, folderPath)
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(ZonedDateTime.now())
                .withLabel(folderName)
                .withCommitId(commitId)
                .build();
        item.setState(ItemState.savedAndClosed(item.getState()));
        item.setSystemType("folder");
        upsertEntry(siteId, item);
    }

    @Override
    public void persistItemAfterRenameFolder(String siteId, String folderPath, String folderName, String username,
                                             String commitId)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        Item item = instantiateItem(siteId, folderPath)
                .withPreviewUrl(getBrowserUrl(siteId, folderPath))
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(ZonedDateTime.now())
                .withLabel(folderName)
                .withCommitId(commitId)
                .build();
        item.setState(ItemState.savedAndClosed(item.getState()));
        item.setSystemType("folder");
        updateItem(item);
    }

    @Override
    public void moveItem(String siteId, String oldPath, String newPath) {
        itemDao.moveItem(siteId, oldPath, newPath);
    }

    @Override
    public void moveItems(String siteId, String oldPath, String newPath) {
        String oldPreviewUrl = getBrowserUrl(siteId, oldPath);
        String newPreviewUrl = getBrowserUrl(siteId, newPath);
        itemDao.moveItems(siteId, oldPath, newPath, oldPreviewUrl, newPreviewUrl, SAVE_AND_CLOSE_ON_MASK,
                SAVE_AND_CLOSE_OFF_MASK);
    }

    @Override
    public boolean isNew(String siteId, String path) {
        Item item = getItem(siteId, path);
        return ItemState.isNew(item.getState());
    }

    @Override
    public int countAllContentItems() {
        return itemDao.countAllContentItems(List.of(CONTENT_TYPE_FOLDER, CONTENT_TYPE_UNKNOWN));
    }

    @Override
    public void clearPreviousPath(String siteId, String path) {
        itemDao.clearPreviousPath(siteId, path);
    }

    @Override
    public PublishingDashboardItem convertHistoryItemToDashboardItem(PublishingHistoryItem historyItem) {
        PublishingDashboardItem dashboardItem = new PublishingDashboardItem();
        Item item = getItem(historyItem.getSiteId(), historyItem.getPath());
        dashboardItem.setSiteId(historyItem.getSiteId());
        dashboardItem.setPath(historyItem.getPath());
        dashboardItem.setLabel(item.getLabel());
        dashboardItem.setEnvironment(historyItem.getEnvironment());
        dashboardItem.setDatePublished(historyItem.getPublishedDate());
        dashboardItem.setPublisher(historyItem.getPublisher());
        return dashboardItem;
    }

    @Override
    public ContentDashboardItem convertItemToContentDashboardItem(String siteId, Item item) {
        ContentDashboardItem contentDashboardItem = new ContentDashboardItem();
        contentDashboardItem.setSiteId(siteId);
        contentDashboardItem.setPath(item.getPath());
        contentDashboardItem.setLabel(item.getLabel());
        contentDashboardItem.setModifier(item.getModifier());
        contentDashboardItem.setModifiedDate(item.getLastModifiedOn());
        contentDashboardItem.setContentType(item.getContentTypeId());
        contentDashboardItem.setState(item.getState());
        return contentDashboardItem;
    }

    @Override
    public List<Item> getInProgressItems(String siteId) {
        return itemDao.getInProgressItems(siteId, IN_PROGRESS_MASK);
    }

    @Override
    public List<Item> getSubmittedItems(String siteId) {
        return itemDao.getSubmittedItems(siteId, SUBMITTED_MASK);
    }

    @Override
    public boolean isUpdatedOrNew(String siteId, String path) {
        Item item = getItem(siteId, path);
        return ItemState.isNew(item.getState()) || ItemState.isModified(item.getState());
    }

    @Override
    public void deleteItemForFolder(long siteId, String folderPath) {
        itemDao.deleteBySiteAndPathForFolder(siteId, folderPath);
    }

    @Override
    public boolean isSystemProcessing(String siteId, String path) {
        Item item = getItem(siteId, path);
        if (Objects.nonNull(item)) {
            return ItemState.isSystemProcessing(item.getState());
        } else {
            return false;
        }
    }

    @Override
    public boolean previousPathExists(String siteId, String path) {
        return itemDao.countPreviousPaths(siteId, path) > 0;
    }

    @Override
    public void updateCommitId(String siteId, String path, String commitId) {
        itemDao.updateCommitId(siteId, path, commitId);
    }

    @Override
    public List<String> getMandatoryParentsForPublishing(String siteId, List<String> paths) {
        return itemDao.getMandatoryParentsForPublishing(siteId, paths, NEW_MASK, MODIFIED_MASK);
    }

    @Override
    public List<String> getExistingRenamedChildrenOfMandatoryParentsForPublishing(String siteId, List<String> parents) {
        return itemDao
                .getExistingRenamedChildrenOfMandatoryParentsForPublishing(siteId, parents, NEW_MASK, MODIFIED_MASK);
    }

    @Override
    public List<String> getChangeSetForSubtree(String siteId, String path) {
        String likePath = path + (path.endsWith(FILE_SEPARATOR) ? "" : FILE_SEPARATOR) + "%";
        return itemDao.getChangeSetForSubtree(siteId, path, likePath,
                List.of(CONTENT_TYPE_FOLDER, CONTENT_TYPE_UNKNOWN), IN_PROGRESS_MASK);
    }

    @Override
    public List<String> getSameCommitItems(String siteId, String path) {
        return itemDao.getSameCommitItems(siteId, path);
    }

    @Override
    public void updateLastPublishedOn(String siteId, String path, ZonedDateTime lastPublishedOn) {
        itemDao.updateLastPublishedOn(siteId, path, lastPublishedOn);
    }

    @Override
    public void updateLastPublishedOnBulk(String siteId, List<String> paths, ZonedDateTime lastPublishedOn) {
        itemDao.updateLastPublishedOnBulk(siteId, paths, lastPublishedOn);
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public ItemDAO getItemDao() {
        return itemDao;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
