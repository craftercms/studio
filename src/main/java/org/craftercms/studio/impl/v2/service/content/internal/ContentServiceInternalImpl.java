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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.impl.v2.utils.ContentUtils;
import org.craftercms.studio.model.history.ItemVersion;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.content.ContentEvent;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.COMPLETED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.DalUtils.mapSortFields;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_ITEM_EDITABLE_TYPES;

public class ContentServiceInternalImpl implements ContentServiceInternal, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceInternalImpl.class);

    private ApplicationContext applicationContext;
    private ContentRepository contentRepository;
    private SiteDAO siteDao;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SiteFeedMapper siteFeedMapper;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private SemanticsAvailableActionsResolver semanticsAvailableActionsResolver;
    private ItemServiceInternal itemServiceInternal;
    private AuditServiceInternal auditServiceInternal;

    @Override
    public boolean contentExists(String siteId, String path) {
        return contentRepository.contentExists(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, String path) {
        return contentRepository.getSubtreeItems(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, List<String> paths) {
        List<String> subtreeItems = new ArrayList<>();
        for (String path : paths) {
            subtreeItems.addAll(contentRepository.getSubtreeItems(siteId, path));
        }
        return subtreeItems;
    }

    @Override
    public GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String keyword,
                                               List<String> systemTypes, List<String> excludes, String sortStrategy,
                                               String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, "Content not found at path " + path + " site " + siteId);
        }
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        int total = itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath, locale, keyword, systemTypes,
                excludes);
        List<Item> resultSet = itemDao.getChildrenByPath(siteFeed.getId(), parentFolderPath,
                CONTENT_TYPE_FOLDER, locale, keyword, systemTypes, excludes, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setLimit(limit);
        toRet.setTotal(total);
        return toRet;
    }

    private GetChildrenResult processResultSet(String siteId, List<Item> resultSet)
            throws ServiceLayerException, UserNotFoundException {
        GetChildrenResult toRet = new GetChildrenResult();
        String user = securityService.getCurrentUser();
        if (resultSet != null && resultSet.size() > 0) {
            int idx = 0;
            Item item = resultSet.get(idx);
            item.setAvailableActions(
                    semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
            if (StringUtils.endsWith(item.getPath(), FILE_SEPARATOR +
                    servicesConfig.getLevelDescriptorName(siteId))) {
                toRet.setLevelDescriptor(SandboxItem.getInstance(item));
                idx++;
            }
            List<SandboxItem> children = new ArrayList<>();
            while (idx < resultSet.size()) {
                Item child = resultSet.get(idx);
                child.setAvailableActions(
                        semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, child));
                children.add(SandboxItem.getInstance(child));
                idx++;
            }
            toRet.setChildren(children);
        }
        return toRet;
    }

    @Override
    public org.craftercms.core.service.Item getItem(String siteId, String path, boolean flatten) {
        return contentRepository.getItem(siteId, path, flatten);
    }

    @Override
    public long getContentSize(String siteId, String path) {
        return contentRepository.getContentSize(siteId, path);
    }

    @Override
    public List<DetailedItem> getItemsByStates(String siteId, long statesBitMap, List<String> systemTypes, List<SortField> sortFields, int offset, int limit) throws UserNotFoundException, ServiceLayerException {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        List<org.craftercms.studio.api.v2.dal.DetailedItem> items = itemDao.getDetailedItemsByStates(siteFeed.getId(), statesBitMap,
                CONTENT_TYPE_FOLDER, COMPLETED,
                systemTypes, mapSortFields(sortFields, ItemDAO.DETAILED_ITEM_SORT_FIELD_MAP), stagingEnv, liveEnv, offset, limit);
        List<DetailedItem> result = new ArrayList<>();
        for (org.craftercms.studio.api.v2.dal.DetailedItem item : items) {
            DetailedItem detailedItem = DetailedItem.getInstance(item);
            populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
            result.add(detailedItem);
        }
        return result;
    }

    @Override
    public DetailedItem getItemByPath(String siteId, String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
        }
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        org.craftercms.studio.api.v2.dal.DetailedItem item;
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        if (preferContent) {
            item = itemDao.getItemByPathPreferContent(siteFeed.getId(), path, CONTENT_TYPE_FOLDER, COMPLETED,
                    stagingEnv, liveEnv);
        } else {
            item = itemDao.getItemByPath(siteFeed.getId(), path, CONTENT_TYPE_FOLDER, COMPLETED, stagingEnv,
                    liveEnv);
        }
        if (item == null) {
            throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
        }
        DetailedItem detailedItem = DetailedItem.getInstance(item);
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }

    private void populateDetailedItemPropertiesFromRepository(String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        if (Objects.nonNull(detailedItem)) {
            String user = securityService.getCurrentUser();
            detailedItem.setAvailableActions(
                    semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, detailedItem));
        }
    }

    @Override
    public List<SandboxItem> getSandboxItemsByPath(String siteId, List<String> paths, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        List<Item> items = itemDao.getSandboxItemsByPath(siteFeed.getId(), paths, CONTENT_TYPE_FOLDER, preferContent);
        return calculatePossibleActions(siteId, items);
    }

    @Override
    public List<SandboxItem> getSandboxItemsById(String siteId, List<Long> ids, List<SortField> sortFields, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        List<Item> items;
        if (preferContent) {
            items = itemDao.getSandboxItemsByIdPreferContent(ids, CONTENT_TYPE_FOLDER, mapSortFields(sortFields, ItemDAO.SORT_FIELD_MAP));
        } else {
            items = itemDao.getSandboxItemsById(ids, CONTENT_TYPE_FOLDER, mapSortFields(sortFields, ItemDAO.SORT_FIELD_MAP));
        }
        return calculatePossibleActions(siteId, items);
    }

    private List<SandboxItem> calculatePossibleActions(String siteId, List<Item> items)
            throws ServiceLayerException, UserNotFoundException {
        if (!CollectionUtils.isNotEmpty(items)) {
            return emptyList();
        }
        List<SandboxItem> toRet = new ArrayList<>();
        String user = securityService.getCurrentUser();
        for (Item item : items) {
            if (!contentRepository.contentExists(siteId, item.getPath())) {
                logger.warn("Content not found in site '{}' path '{}'", siteId, item.getPath());
            } else {
                item.setAvailableActions(
                        semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
                toRet.add(SandboxItem.getInstance(item));
            }
        }
        return toRet;
    }

    @Override
    public boolean isEditable(String itemPath, String mimeType) {
        List<String> editableMimeTypes =
                Arrays.asList(studioConfiguration.getArray(CONTENT_ITEM_EDITABLE_TYPES, String.class));

        MimeType itemMimeType;
        if (StringUtils.isEmpty(mimeType)) {
            itemMimeType = MimeType.valueOf(StudioUtils.getMimeType(itemPath));
        } else {
            itemMimeType = MimeType.valueOf(mimeType);
        }

        return editableMimeTypes.stream()
                .anyMatch(type -> (MimeType.valueOf(type)).isCompatibleWith(itemMimeType));
    }

    @Override
    public void itemLockByPath(String siteId, String path) {
        contentRepository.lockItem(siteId, path);
    }

    @Override
    public void itemUnlockByPath(String siteId, String path) {
        contentRepository.itemUnlock(siteId, path);
    }

    @Override
    public Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
            throws ContentNotFoundException {
        return contentRepository.getContentByCommitId(siteId, path, commitId);
    }

    @Override
    public List<ItemVersion> getContentVersionHistory(String siteId, String path) throws ServiceLayerException {
        try {
            List<ItemVersion> history = contentRepository.getContentItemHistory(siteId, path);
            for (ItemVersion itemVersion : history) {
                if (itemVersion.getVersionNumber() != null) {
                    itemVersion.setAuthor(auditServiceInternal.getAuthor(itemVersion.getVersionNumber()));
                }
            }
            return history;
        } catch (IOException | GitAPIException e) {
            throw new ServiceLayerException(format("Error getting content version history for site '%s' path '%s'", siteId, path), e);
        }
    }

    @Override
    public void createFolder(String siteId, String path, String name) throws ServiceLayerException, UserNotFoundException {
        String folderPath = Paths.get(path, name).toString();
        String commitId = contentRepository.createFolder(siteId, path, name);
        Item parentItem = itemServiceInternal.getItem(siteId, path, true);
        if (Objects.isNull(parentItem)) {
            parentItem = createMissingParentItem(siteId, path, commitId);
        }
        itemServiceInternal.persistItemAfterCreateFolder(siteId, folderPath, name, securityService.getCurrentUser(),
                commitId, parentItem.getId());

        String username = securityService.getCurrentUser();
        Site site = siteDao.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(site.getId());
        auditLog.setActorId(username);

        auditLog.setPrimaryTargetId(siteId + ":" + folderPath);
        auditLog.setPrimaryTargetType(TARGET_TYPE_FOLDER);
        auditLog.setPrimaryTargetValue(folderPath);
        auditServiceInternal.insertAuditLog(auditLog);

        contentRepository.insertGitLog(siteId, commitId, 1, 1);
        siteDao.updateLastCommitId(siteId, commitId);
        applicationContext.publishEvent(new ContentEvent(securityService.getAuthentication(), siteId, folderPath));
    }

    /**
     * Recursive method to create missing parent item while creating a new folder
     * @param site site identifier
     * @param parentPath parent path
     * @param commitId commit id
     * @return parent path {@link Item}
     *
     * @throws UserNotFoundException
     * @throws ServiceLayerException
     */
    protected Item createMissingParentItem(String site, String parentPath, String commitId)
            throws UserNotFoundException, ServiceLayerException {
        String ancestorPath = ContentUtils.getParentUrl(parentPath);
        String name = ContentUtils.getPageName(parentPath);

        Item ancestor = itemServiceInternal.getItem(site, ancestorPath, false);
        if (Objects.isNull(ancestor) && StringUtils.isNotEmpty(ancestorPath)) {
            createMissingParentItem(site, ancestorPath, commitId);
            ancestor = itemServiceInternal.getItem(site, ancestorPath, false);
        }

        Long ancestorId = ancestor != null ? ancestor.getId() : null;
        itemServiceInternal.persistItemAfterCreateFolder(site, parentPath, name, securityService.getCurrentUser(),
                commitId, ancestorId);

        return itemServiceInternal.getItem(site, parentPath, true);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setSiteDao(SiteDAO siteDao) {
        this.siteDao = siteDao;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setSemanticsAvailableActionsResolver(SemanticsAvailableActionsResolver semanticsAvailableActionsResolver) {
        this.semanticsAvailableActionsResolver = semanticsAvailableActionsResolver;
    }

    public void setAuditServiceInternal(final AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
