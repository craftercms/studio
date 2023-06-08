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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.COMPLETED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.DalUtils.mapSortFields;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_ITEM_EDITABLE_TYPES;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceInternalImpl.class);

    private ContentRepository contentRepository;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SiteFeedMapper siteFeedMapper;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private SemanticsAvailableActionsResolver semanticsAvailableActionsResolver;

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

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
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
}
