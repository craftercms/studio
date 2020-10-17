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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private ContentRepository contentRepository;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SiteFeedMapper siteFeedMapper;
    private SecurityService securityService;

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
    public GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String sortStrategy,
                                               String order, int offset, int limit) {
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

    private GetChildrenResult processResultSet(String siteId, List<Item> resultSet) {
        GetChildrenResult toRet = new GetChildrenResult();
        String user = securityService.getCurrentUser();
        if (resultSet != null && resultSet.size() > 0) {
            Item parent = resultSet.get(0);
            parent.setAvailableActions(securityService.getAvailableActions(siteId, parent.getPath(), user));
            toRet.setParent(SandboxItem.getInstance(parent));
            if (resultSet.size() > 1) {
                int idx = 1;
                Item item = resultSet.get(idx);
                item.setAvailableActions(securityService.getAvailableActions(siteId, item.getPath(), user));
                if (StringUtils.endsWith(item.getPath(), FILE_SEPARATOR +
                        servicesConfig.getLevelDescriptorName(siteId))) {
                    toRet.setLevelDescriptor(SandboxItem.getInstance(item));
                    idx++;
                }
                List<SandboxItem> children = new ArrayList<SandboxItem>();
                while (idx < resultSet.size()) {
                    Item child = resultSet.get(idx);
                    child.setAvailableActions(securityService.getAvailableActions(siteId, child.getPath(), user));
                    children.add(SandboxItem.getInstance(child));
                    idx++;
                }
                toRet.setChildren(children);
            }
        }
        return toRet;
    }

    @Override
    public int getChildrenByPathTotal(String siteId, String path, String locale) {
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, siteId);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return itemDao.getChildrenByPathTotal(siteFeed.getId(), parentFolderPath,
                servicesConfig.getLevelDescriptorName(siteId), locale);
    }

    @Override
    public GetChildrenResult getChildrenById(String siteId, String parentId, String locale, String sortStrategy,
                                             String order,
                                             int offset, int limit) {
        List<Item> resultSet = itemDao.getChildrenById(siteId, parentId,
                servicesConfig.getLevelDescriptorName(siteId), locale, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setTotal(getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId), locale));
        return toRet;
    }

    @Override
    public int getChildrenByIdTotal(String siteId, String parentId, String ldName, String locale) {
        return itemDao.getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId),
                locale);
    }

    @Override
    public org.craftercms.core.service.Item getItem(String siteId, String path, boolean flatten) {
        return contentRepository.getItem(siteId, path, flatten);
    }

    @Override
    public long getContentSize(String siteId, String path) {
        return contentRepository.getContentSize(siteId, path);
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
}
