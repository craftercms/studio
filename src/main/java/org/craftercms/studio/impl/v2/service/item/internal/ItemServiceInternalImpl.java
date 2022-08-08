/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.DetailedItem;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY_REGEX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIG_REGEX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.COMPLETED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public class ItemServiceInternalImpl implements ItemServiceInternal {
    // TODO: SJ: Add logging to this class

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
    private GeneralLockService generalLockService;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public boolean upsertEntry(Item item) {
        retryingDatabaseOperationFacade.upsertEntry(item);
        return true;
    }

    @Override
    public void upsertEntries(String siteId, List<Item> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            retryingDatabaseOperationFacade.upsertEntries(items);
        }
    }

    @Override
    public DetailedItem getItem(String siteId, long id) {
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        return itemDao.getItemById(id, siteId, CONTENT_TYPE_FOLDER, COMPLETED, stagingEnv, liveEnv);
    }

    @Override
    public Item getItem(String siteId, String path) {
        return getItem(siteId, path, false);
    }

    @Override
    public Item getItem(String siteId, String path, boolean preferContent) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        DetailedItem item;
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        if (preferContent) {
            item = itemDao.getItemBySiteIdAndPathPreferContent(siteFeed.getId(), path, CONTENT_TYPE_FOLDER,
                    COMPLETED, stagingEnv, liveEnv);
        } else {
            item = itemDao.getItemBySiteIdAndPath(siteFeed.getId(), path, CONTENT_TYPE_FOLDER, COMPLETED,
                    stagingEnv, liveEnv);
        }
        if (Objects.nonNull(item)) {
            item.setSiteName(siteId);
        }
        return Item.getInstance(item);
    }

    @Override
    public List<Item> getItems(String siteId, List<String> paths) {
        return getItems(siteId, paths, false);
    }

    @Override
    public List<Item> getItems(String siteId, List<String> paths, boolean preferContent) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getSandboxItemsByPath(siteFeed.getId(), paths, CONTENT_TYPE_FOLDER, preferContent);
    }

    @Override
    public void deleteItem(long itemId) {
        retryingDatabaseOperationFacade.deleteById(itemId);
    }

    @Override
    public void deleteItem(String siteId, String path) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        retryingDatabaseOperationFacade.deleteBySiteAndPath(siteFeed.getId(), path);
    }

    @Override
    public void updateItem(Item item) {
        retryingDatabaseOperationFacade.updateItem(item);
    }

    @Override
    public void setSystemProcessing(String siteId, String path, boolean isSystemProcessing) {
        List<String> paths = new ArrayList<>();
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
            Map<String, String> params = new HashMap<>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            retryingDatabaseOperationFacade.setStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
        }
    }

    private void setStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            retryingDatabaseOperationFacade.setStatesByIdBulk(itemIds, statesBitMap);
        }
    }

    private void resetStatesBySiteAndPathBulk(String siteId, List<String> paths, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(paths)) {
            Map<String, String> params = new HashMap<>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            retryingDatabaseOperationFacade.resetStatesBySiteAndPathBulk(siteFeed.getId(), paths, statesBitMap);
        }
    }

    private void resetStatesByIdBulk(List<Long> itemIds, long statesBitMap) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            retryingDatabaseOperationFacade.resetStatesByIdBulk(itemIds, statesBitMap);
        }
    }

    @Override
    public void setStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<>();
        paths.add(path);
        setStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void setStateBitsBulk(String siteId, List<String> paths, long statesBitMask) {
        setStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void resetStateBits(String siteId, String path, long statesBitMask) {
        List<String> paths = new ArrayList<>();
        paths.add(path);
        resetStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void resetStateBitsBulk(String siteId, List<String> paths, long statesBitMask) {
        resetStatesBySiteAndPathBulk(siteId, paths, statesBitMask);
    }

    @Override
    public void setStateBits(long itemId, long statesBitMask) {
        List<Long> ids = new ArrayList<>();
        ids.add(itemId);
        setStatesByIdBulk(ids, statesBitMask);
    }

    @Override
    public void resetStateBits(long itemId, long statesBitMask) {
        List<Long> ids = new ArrayList<>();
        ids.add(itemId);
        resetStatesByIdBulk(ids, statesBitMask);
    }

    @Override
    public void updateStateBits(String siteId, String path, long onStateBitMap, long offStateBitMap) {
        List<String> paths = new ArrayList<>();
        paths.add(path);
        updateStatesBySiteAndPathBulk(siteId, paths, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBits(long itemId, long onStateBitMap, long offStateBitMap) {
        List<Long> ids = new ArrayList<>();
        ids.add(itemId);
        updateStateBitsBulk(ids, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBitsBulk(String siteId, Collection<String> paths, long onStateBitMap, long offStateBitMap) {
        updateStatesBySiteAndPathBulk(siteId, paths, onStateBitMap, offStateBitMap);
    }

    @Override
    public void updateStateBitsBulk(List<Long> itemIds, long onStateBitMap, long offStateBitMap) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            retryingDatabaseOperationFacade.updateStatesByIdBulk(itemIds, onStateBitMap, offStateBitMap);
        }
    }

    private void updateStatesBySiteAndPathBulk(String siteId, Collection<String> paths, long onStateBitMap,
                                               long offStateBitMap) {
        if (CollectionUtils.isNotEmpty(paths)) {
            Map<String, String> params = new HashMap<>();
            params.put(SITE_ID, siteId);
            SiteFeed siteFeed = siteFeedMapper.getSite(params);
            retryingDatabaseOperationFacade.updateStatesBySiteAndPathBulk(siteFeed.getId(), paths,
                                                                          onStateBitMap, offStateBitMap);
        }
    }

    @Override
    public Item.Builder instantiateItem(String siteName, String path) {
        Item item = getItem(siteName, path);
        if (Objects.isNull(item))  {
            item = new Item();
            Map<String, String> params = new HashMap<>();
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
                                String contentTypeId, String systemType, String mimeType, String localeCode,
                                Long translationSourceId, long size, Long parentId, String commitId) {

        return instantiateItem(siteName, path).withPreviewUrl(previewUrl).withState(state).withLockedBy(ownedBy)
                .withLockOwner(owner).withCreatedBy(createdBy).withCreator(creator).withCreatedOn(createdOn)
                .withLastModifiedBy(lastModifiedBy).withModifier(modifier).withLastModifiedOn(lastModifiedOn)
                .withLabel(label).withContentTypeId(contentTypeId).withSystemType(systemType).withMimeType(mimeType)
                .withLocaleCode(localeCode).withTranslationSourceId(translationSourceId).withSize(size)
                .withParentId(parentId).withCommitId(commitId).build();

    }

    @Override
    public void deleteItemsForSite(long siteId) {
        retryingDatabaseOperationFacade.deleteItemsForSite(siteId);
    }

    @Override
    public void deleteItemsById(List<Long> itemIds) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            retryingDatabaseOperationFacade.deleteItemsById(itemIds);
        }
    }

    @Override
    public void deleteItemsForSiteAndPaths(long siteId, List<String> paths) {
        if (CollectionUtils.isNotEmpty(paths)) {
            retryingDatabaseOperationFacade.deleteItemsForSiteAndPath(siteId, paths);
        }
    }

    @Override
    public int getContentDashboardTotal(String siteId, String path, String modifier, String contentType, long state,
                                        ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return itemDao.getContentDashboardTotal(siteId, path, modifier, contentType, state, dateFrom, dateTo);
    }

    @Override
    public List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier,
                                                          String contentType, long state, ZonedDateTime dateFrom,
                                                          ZonedDateTime dateTo, String sortBy, String order,
                                                          int offset, int limit) {
        List<Item> items = itemDao.getContentDashboard(siteId, path, modifier, contentType, state, dateFrom,
                dateTo, sortBy, order, offset, limit);
        return items.stream()
                .map(i -> convertItemToContentDashboardItem(siteId, i))
                .collect(toList());
    }

    @Override
    public String getBrowserUrl(String site, String path) {
        String replacePattern;
        boolean isPage = false;
        if (ContentUtils.matchesPatterns(path, servicesConfig.getRenderingTemplatePatterns(site))) {
            return null;
        } else if (ContentUtils.matchesPatterns(path, List.of(CONTENT_TYPE_TAXONOMY_REGEX))) {
            return null;
        } else if (ContentUtils.matchesPatterns(path, servicesConfig.getComponentPatterns(site)) ||
                StringUtils.endsWith(path,FILE_SEPARATOR + servicesConfig.getLevelDescriptorName(site))) {
            return null;
        } else if (ContentUtils.matchesPatterns(path, servicesConfig.getScriptsPatterns(site))) {
            return null;
        } else if (ContentUtils.matchesPatterns(path, List.of(CONTENT_TYPE_CONFIG_REGEX))) {
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
                                       boolean unlock, Long parentId)
            throws ServiceLayerException, UserNotFoundException {
        String lockKey = "persistItemAfterCreate:" + siteId ;
        generalLockService.lock(lockKey);
        try {
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
                    .withCreatedBy(userObj.getId())
                    .withCreatedOn(DateUtils.getCurrentTime())
                    .withLastModifiedBy(userObj.getId())
                    .withLastModifiedOn(DateUtils.getCurrentTime())
                    .withLabel(label)
                    .withSystemType(contentService.getContentTypeClass(siteId, path))
                    .withContentTypeId(descriptor.queryDescriptorValue(CONTENT_TYPE))
                    .withMimeType(StudioUtils.getMimeType(path))
                    .withLocaleCode(descriptor.queryDescriptorValue(LOCALE_CODE))
                    .withCommitId(commitId)
                    .withSize(contentServiceInternal.getContentSize(siteId, path))
                    .withParentId(parentId)
                    .build();
            if (unlock) {
                item.setState(ItemState.savedAndClosed(item.getState()));
            } else {
                item.setLockedBy(userObj.getId());
                item.setState(ItemState.savedAndNotClosed(item.getState()));
            }
            if (disabled) {
                item.setState(item.getState() | ItemState.DISABLED.value);
            }
            retryingDatabaseOperationFacade.upsertEntry(item);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public void persistItemAfterWrite(String siteId, String path, String username, String commitId, boolean unlock)
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
                .withLastModifiedOn(DateUtils.getCurrentTime())
                .withLabel(label)
                .withSystemType(contentService.getContentTypeClass(siteId, path))
                .withContentTypeId(descriptor.queryDescriptorValue(CONTENT_TYPE))
                .withMimeType(StudioUtils.getMimeType(path))
                .withLocaleCode(descriptor.queryDescriptorValue(LOCALE_CODE))
                .withCommitId(commitId)
                .withSize(contentServiceInternal.getContentSize(siteId, path))
                .build();
        if (unlock) {
            item.setState(ItemState.savedAndClosed(item.getState()));
        } else {
            item.setState(ItemState.savedAndNotClosed(item.getState()));
        }
        if (disabled) {
            item.setState(item.getState() | ItemState.DISABLED.value);
        } else {
            item.setState(item.getState() & ~ItemState.DISABLED.value);
        }
        upsertEntry(item);
    }

    @Override
    public void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, String username,
                                             String commitId, Long parentId)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        Item item = instantiateItem(siteId, folderPath)
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(DateUtils.getCurrentTime())
                .withLabel(folderName)
                .withCommitId(commitId)
                .withParentId(parentId)
                .build();
        item.setState(ItemState.savedAndClosed(item.getState()));
        item.setSystemType("folder");
        upsertEntry(item);
    }

    @Override
    public void persistItemAfterRenameContent(String siteId, String path, String name, String username,
                                             String commitId, String contentType)
            throws ServiceLayerException, UserNotFoundException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, username);
        Item item = instantiateItem(siteId, path)
                .withPreviewUrl(contentType == CONTENT_TYPE_FOLDER ? null : getBrowserUrl(siteId, path))
                .withLastModifiedBy(userObj.getId())
                .withLastModifiedOn(DateUtils.getCurrentTime())
                .withLabel(name)
                .withCommitId(commitId)
                .build();
        item.setState(ItemState.savedAndClosed(item.getState()));
        item.setSystemType(contentType);
        updateItem(item);
    }

    @Override
    public void moveItem(String siteId, String oldPath, String newPath, Long parentId, String label) {
        String oldPreviewUrl = getBrowserUrl(siteId, oldPath);
        String newPreviewUrl = getBrowserUrl(siteId, newPath);
        retryingDatabaseOperationFacade.retry(() ->
                itemDao.moveItem(siteId, oldPath, newPath, parentId, oldPreviewUrl, newPreviewUrl, label,
                        SAVE_AND_CLOSE_ON_MASK, SAVE_AND_CLOSE_OFF_MASK));
    }

    @Override
    public void moveItems(String siteId, String oldPath, String newPath, Long parentId, String label) {
        String oldPreviewUrl = getBrowserUrl(siteId, oldPath);
        String newPreviewUrl = getBrowserUrl(siteId, newPath);

        retryingDatabaseOperationFacade.retry(() ->
                itemDao.moveItems(siteId, oldPath, newPath, parentId, oldPreviewUrl, newPreviewUrl, label, SAVE_AND_CLOSE_ON_MASK,
                        SAVE_AND_CLOSE_OFF_MASK));
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
        retryingDatabaseOperationFacade.clearPreviousPath(siteId, path);
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
        retryingDatabaseOperationFacade.deleteBySiteAndPathForFolder(siteId, folderPath);
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
        retryingDatabaseOperationFacade.updateCommitId(siteId, path, commitId);
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
        retryingDatabaseOperationFacade.updateLastPublishedOn(siteId, path, lastPublishedOn);
    }

    @Override
    public void updateLastPublishedOnBulk(String siteId, List<String> paths, ZonedDateTime lastPublishedOn) {
        retryingDatabaseOperationFacade.updateLastPublishedOnBulk(siteId, paths, lastPublishedOn);
    }

    @Override
    public void lockItemByPath(String siteId, String path, String username)
            throws UserNotFoundException, ServiceLayerException {
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        retryingDatabaseOperationFacade.lockItemByPath(siteId, path, user.getId(), USER_LOCKED.value, CONTENT_TYPE_FOLDER);
    }

    @Override
    public void lockItemsByPath(String siteId, List<String> paths, String username)
            throws UserNotFoundException, ServiceLayerException {
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        retryingDatabaseOperationFacade.lockItemsByPath(siteId, paths, user.getId(), USER_LOCKED.value,
                CONTENT_TYPE_FOLDER);
    }

    @Override
    public void unlockItemByPath(String siteId, String path) {
        retryingDatabaseOperationFacade.unlockItemByPath(siteId, path, ~USER_LOCKED.value);
    }

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemDao.getItemStatesTotal(siteId, path, states);
    }

    @Override
    public List<Item> getItemStates(String siteId, String path, Long states, int offset, int limit) {
        return itemDao.getItemStates(siteId, path, states, offset, limit);
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        if (CollectionUtils.isNotEmpty(paths)) {
            long setStatesMask = getSetStatesMask(live, staged, isNew, modified);
            long resetStatesMask = getResetStatesMask(clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);

            SiteFeed siteFeed = siteFeedMapper.getSite(Collections.singletonMap(SITE_ID, siteId));
            retryingDatabaseOperationFacade.updateStatesBySiteAndPathBulk(siteFeed.getId(), paths, setStatesMask,
                    resetStatesMask);
        }
    }

    protected long getSetStatesMask(Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        long setStatesMask = 0L;
        if (BooleanUtils.isTrue(live)) {
            setStatesMask |= LIVE.value;
        }
        if (BooleanUtils.isTrue(staged)) {
            setStatesMask |= STAGED.value;
        }
        if (BooleanUtils.isTrue(isNew)) {
            setStatesMask |= NEW.value;
        }
        if (BooleanUtils.isTrue(modified)) {
            setStatesMask |= MODIFIED.value;
        }
        return setStatesMask;
    }

    protected long getResetStatesMask(boolean clearSystemProcessing, boolean clearUserLocked, Boolean live,
                                      Boolean staged, Boolean isNew, Boolean modified) {
        long resetStatesMask = 0L;

        if (clearSystemProcessing) {
            resetStatesMask |= SYSTEM_PROCESSING.value;
        }
        if (clearUserLocked) {
            resetStatesMask |= USER_LOCKED.value;
        }
        if (BooleanUtils.isFalse(live)) {
            resetStatesMask |= LIVE.value;
        }
        if (BooleanUtils.isFalse(staged)) {
            resetStatesMask |= STAGED.value;
        }
        if (BooleanUtils.isFalse(isNew)) {
            resetStatesMask |= NEW.value;
        }
        if (BooleanUtils.isFalse(modified)) {
            resetStatesMask |= MODIFIED.value;
        }
        return resetStatesMask;
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        long setStatesMask = getSetStatesMask(live, staged, isNew, modified);
        long resetStatesMask = getResetStatesMask(clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);

        retryingDatabaseOperationFacade.updateStatesByQuery(siteId, path, states, setStatesMask, resetStatesMask);
    }

    @Override
    public List<String> getSubtreeForDelete(String siteId, String path) {
        String likePath = path + (path.endsWith(FILE_SEPARATOR) ? "" : FILE_SEPARATOR) + "%";
        return itemDao.getSubtreeForDelete(siteId, likePath);
    }

    @Override
    public void updateStatesForSite(String siteId, long onStateBitMap, long offStateBitMap) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        retryingDatabaseOperationFacade.updateStatesForSite(siteFeed.getId(), onStateBitMap, offStateBitMap);
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
