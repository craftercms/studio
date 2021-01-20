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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.annotation.IsActionAllowed;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.security.AvailableActions.READ_CONST_LONG;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private ContentRepository contentRepository;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SiteFeedMapper siteFeedMapper;
    private SecurityService securityService;
    private org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2;
    private StudioConfiguration studioConfiguration;

    public ContentServiceInternalImpl(ContentRepository contentRepository,
                                      ItemDAO itemDao,
                                      ServicesConfig servicesConfig,
                                      SiteFeedMapper siteFeedMapper,
                                      SecurityService securityService,
                                      org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2,
                                      StudioConfiguration studioConfiguration) {
        this.contentRepository = contentRepository;
        this.itemDao = itemDao;
        this.servicesConfig = servicesConfig;
        this.siteFeedMapper = siteFeedMapper;
        this.securityService = securityService;
        this.securityServiceV2 = securityServiceV2;
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public List<String> getSubtreeItems(String siteId, String path) {
        return contentRepository.getSubtreeItems(siteId, path);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public List<String> getSubtreeItems(String siteId, List<String> paths) {
        List<String> subtreeItems = new ArrayList<String>();
        for (String path : paths) {
            subtreeItems.addAll(contentRepository.getSubtreeItems(siteId, path));
        }
        return subtreeItems;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String sortStrategy,
                                               String order, int offset, int limit)
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
        List<Item> resultSet = itemDao.getChildrenByPath(siteFeed.getId(), path, ldPath, ldName, parentFolderPath,
                locale, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setLimit(limit);
        toRet.setTotal(itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath, ldName, locale));
        return toRet;
    }

    private GetChildrenResult processResultSet(String siteId, List<Item> resultSet)
            throws ServiceLayerException, UserNotFoundException {
        GetChildrenResult toRet = new GetChildrenResult();
        String user = securityService.getCurrentUser();
        if (resultSet != null && resultSet.size() > 0) {
            Item parent = resultSet.get(0);
            parent.setAvailableActions(securityServiceV2.getAvailableActions(user, siteId, parent.getPath()));
            toRet.setParent(SandboxItem.getInstance(parent));
            if (resultSet.size() > 1) {
                int idx = 1;
                Item item = resultSet.get(idx);
                item.setAvailableActions(securityServiceV2.getAvailableActions(user, siteId, item.getPath()));
                if (StringUtils.endsWith(item.getPath(), FILE_SEPARATOR +
                        servicesConfig.getLevelDescriptorName(siteId))) {
                    toRet.setLevelDescriptor(SandboxItem.getInstance(item));
                    idx++;
                }
                List<SandboxItem> children = new ArrayList<SandboxItem>();
                while (idx < resultSet.size()) {
                    Item child = resultSet.get(idx);
                    child.setAvailableActions(securityServiceV2.getAvailableActions(user, siteId, child.getPath()));
                    children.add(SandboxItem.getInstance(child));
                    idx++;
                }
                toRet.setChildren(children);
            }
        }
        return toRet;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public int getChildrenByPathTotal(String siteId, String path, String locale) {
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath,
                servicesConfig.getLevelDescriptorName(siteId), locale);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public GetChildrenResult getChildrenById(String siteId, String parentId, String locale, String sortStrategy,
                                             String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        List<Item> resultSet = itemDao.getChildrenById(siteId, parentId,
                servicesConfig.getLevelDescriptorName(siteId), locale, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setTotal(getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId), locale));
        return toRet;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public int getChildrenByIdTotal(String siteId, String parentId, String ldName, String locale) {
        return itemDao.getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId),
                locale);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public org.craftercms.core.service.Item getItem(String siteId, String path, boolean flatten) {
        return contentRepository.getItem(siteId, path, flatten);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public long getContentSize(String siteId, String path) {
        return contentRepository.getContentSize(siteId, path);
    }

    @Override
    public DetailedItem getItemByPath(String siteId, String path)
            throws ContentNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, "Content not found at path " + path + " site " + siteId);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        Item item = itemDao.getItemByPath(siteFeed.getId(), path);
        DetailedItem detailedItem = Objects.nonNull(item) ? DetailedItem.getInstance(item) : null;
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }

    private void populateDetailedItemPropertiesFromRepository(String siteId, DetailedItem detailedItem) {

        detailedItem.setStaging(contentRepository.getItemEnvironmentProperties(siteId, GitRepositories.PUBLISHED,
                studioConfiguration.getProperty(StudioConfiguration.REPO_PUBLISHED_STAGING), detailedItem.getPath()));
        detailedItem.setLive(contentRepository.getItemEnvironmentProperties(siteId, GitRepositories.PUBLISHED,
                studioConfiguration.getProperty(StudioConfiguration.REPO_PUBLISHED_LIVE), detailedItem.getPath()));
    }

    @Override
    public DetailedItem getItemById(String siteId, long id)
            throws ContentNotFoundException {
        Item item = itemDao.getItemById(id);
        if (!contentRepository.contentExists(siteId, item.getPath())) {
            throw new ContentNotFoundException(item.getPath(), siteId,
                    "Content not found at path " + item.getPath() + " site " + siteId);
        }
        DetailedItem detailedItem = Objects.nonNull(item) ? DetailedItem.getInstance(item) : null;
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }
}
