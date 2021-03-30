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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_ITEM_EDITABLE_TYPES;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceInternalImpl.class);

    private ContentRepository contentRepository;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SiteFeedMapper siteFeedMapper;
    private SecurityService securityService;
    private org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2;
    private StudioConfiguration studioConfiguration;
    private SemanticsAvailableActionsResolver semanticsAvailableActionsResolver;

    @Override
    public List<String> getSubtreeItems(String siteId, String path) {
        return contentRepository.getSubtreeItems(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, List<String> paths) {
        List<String> subtreeItems = new ArrayList<String>();
        for (String path : paths) {
            subtreeItems.addAll(contentRepository.getSubtreeItems(siteId, path));
        }
        return subtreeItems;
    }

    @Override
    public GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String keyword,
                                               List<String> excludes, String sortStrategy, String order, int offset,
                                               int limit)
            throws ServiceLayerException, UserNotFoundException, ContentNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, "Content not found at path " + path + " site " + siteId);
        }
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        String ldName = servicesConfig.getLevelDescriptorName(siteId);
        String ldPath = parentFolderPath + FILE_SEPARATOR + ldName;
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        List<Item> resultSet = itemDao.getChildrenByPath(siteFeed.getId(), ldPath, ldName, parentFolderPath,
                locale, keyword, excludes, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setLimit(limit);
        toRet.setTotal(itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath, ldName, locale, keyword,
                excludes));
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
            List<SandboxItem> children = new ArrayList<SandboxItem>();
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
    public int getChildrenByPathTotal(String siteId, String path, String locale, String keyword,
                                      List<String> excludes) {
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath,
                servicesConfig.getLevelDescriptorName(siteId), locale, keyword, excludes);
    }

    @Override
    public GetChildrenResult getChildrenById(String siteId, String parentId, String locale, String keyword,
                                             List<String> excludes, String sortStrategy, String order, int offset,
                                             int limit)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        List<Item> resultSet = itemDao.getChildrenById(siteFeed.getId(), parentId,
                servicesConfig.getLevelDescriptorName(siteId), locale, keyword, excludes, sortStrategy,
                order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setTotal(getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId), locale,
                keyword, excludes));
        return toRet;
    }

    @Override
    public int getChildrenByIdTotal(String siteId, String parentId, String ldName, String locale, String keyword,
                                    List<String> excludes) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getChildrenByIdTotal(siteFeed.getId(), parentId, servicesConfig.getLevelDescriptorName(siteId),
                locale, keyword, excludes);
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
            throw new ContentNotFoundException(path, siteId, "Content not found at path " + path + " site " + siteId);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        Item item = null;
        if (preferContent) {
            item = itemDao.getItemByPathPreferContent(siteFeed.getId(), path);
        } else {
            item = itemDao.getItemByPath(siteFeed.getId(), path);
        }
        DetailedItem detailedItem = Objects.nonNull(item) ? DetailedItem.getInstance(item) : null;
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }

    private void populateDetailedItemPropertiesFromRepository(String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        if (Objects.nonNull(detailedItem)) {
            String user = securityService.getCurrentUser();
            detailedItem.setAvailableActions(
                    semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, detailedItem));
            detailedItem.setStaging(contentRepository.getItemEnvironmentProperties(siteId, GitRepositories.PUBLISHED,
                    studioConfiguration.getProperty(StudioConfiguration.REPO_PUBLISHED_STAGING), detailedItem.getPath()));
            detailedItem.setLive(contentRepository.getItemEnvironmentProperties(siteId, GitRepositories.PUBLISHED,
                    studioConfiguration.getProperty(StudioConfiguration.REPO_PUBLISHED_LIVE), detailedItem.getPath()));
        }
    }

    @Override
    public DetailedItem getItemById(String siteId, long id, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        Item item = null;
        if (preferContent) {
            item = itemDao.getItemByIdPreferContent(id);
        } else {
            item = itemDao.getItemById(id);
        }
        if (!contentRepository.contentExists(siteId, item.getPath())) {
            throw new ContentNotFoundException(item.getPath(), siteId,
                    "Content not found at path " + item.getPath() + " site " + siteId);
        }
        DetailedItem detailedItem = Objects.nonNull(item) ? DetailedItem.getInstance(item) : null;
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }

    @Override
    public List<SandboxItem> getSandboxItemsByPath(String siteId, List<String> paths, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        List<Item> items = null;
        if (preferContent) {
            items = itemDao.getSandboxItemsByPathPreferContent(siteFeed.getId(), paths);
        } else {
            items = itemDao.getSandboxItemsByPath(siteFeed.getId(), paths);
        }
        return calculatePossibleActions(siteId, items);
    }

    @Override
    public List<SandboxItem> getSandboxItemsById(String siteId, List<Long> ids, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        List<Item> items = null;
        if (preferContent) {
            items = itemDao.getSandboxItemsByIdPreferContent(ids);
        } else {
            items = itemDao.getSandboxItemsById(ids);
        }
        return calculatePossibleActions(siteId, items);
    }

    private List<SandboxItem> calculatePossibleActions(String siteId, List<Item> items)
            throws ServiceLayerException, UserNotFoundException {
        List<SandboxItem> toRet = new ArrayList<SandboxItem>();
        if (CollectionUtils.isNotEmpty(items)) {
            String user = securityService.getCurrentUser();
            for (Item item : items) {
                if (!contentRepository.contentExists(siteId, item.getPath())) {
                    logger.warn("Content not found at path " + item.getPath() + " site " + siteId);
                } else {
                    item.setAvailableActions(
                            semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
                    toRet.add(SandboxItem.getInstance(item));
                }
            }
        }
        return toRet;
    }

    @Override
    public boolean isEditable(Item item) {
        List<String> editableMimeTypes =
                Arrays.asList(studioConfiguration.getArray(CONTENT_ITEM_EDITABLE_TYPES, String.class));
        String mimeType = item.getMimeType();
        if (StringUtils.isEmpty(mimeType)) {
            mimeType = StudioUtils.getMimeType(item.getPath());
        }
        return editableMimeTypes.contains(mimeType);
    }

    @Override
    public boolean isEditable(DetailedItem detailedItem) {
        List<String> editableMimeTypes =
                Arrays.asList(studioConfiguration.getArray(CONTENT_ITEM_EDITABLE_TYPES, String.class));
        String mimeType = detailedItem.getMimeType();
        if (StringUtils.isEmpty(mimeType)) {
            mimeType = StudioUtils.getMimeType(detailedItem.getPath());
        }
        return editableMimeTypes.contains(mimeType);
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
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

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public org.craftercms.studio.api.v2.service.security.SecurityService getSecurityServiceV2() {
        return securityServiceV2;
    }

    public void setSecurityServiceV2(org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2) {
        this.securityServiceV2 = securityServiceV2;
    }

    public SemanticsAvailableActionsResolver getSemanticsAvailableActionsResolver() {
        return semanticsAvailableActionsResolver;
    }

    public void setSemanticsAvailableActionsResolver(SemanticsAvailableActionsResolver semanticsAvailableActionsResolver) {
        this.semanticsAvailableActionsResolver = semanticsAvailableActionsResolver;
    }
}
