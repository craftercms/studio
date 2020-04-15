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
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.ItemMetadataDAO;
import org.craftercms.studio.api.v2.dal.SandboxItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.model.rest.content.GetChildrenResult;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private ContentRepository contentRepository;
    private ItemMetadataDAO itemMetadataDao;
    private ServicesConfig servicesConfig;

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
        List<SandboxItem> resultSet = itemMetadataDao.getChildrenByPath(siteId, path, ldPath, ldName, parentFolderPath,
                locale, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setTotal(getChildrenByPathTotal(siteId, path, locale));
        return toRet;
    }

    private GetChildrenResult processResultSet(String siteId, List<SandboxItem> resultSet) {
        GetChildrenResult toRet = new GetChildrenResult();
        if (resultSet != null && resultSet.size() > 0) {
            toRet.setParent(resultSet.get(0));
            if (resultSet.size() > 1) {
                int idx = 1;
                SandboxItem item = resultSet.get(idx);
                if (StringUtils.endsWith(item.getPath(), FILE_SEPARATOR +
                        servicesConfig.getLevelDescriptorName(siteId))) {
                    toRet.setLevelDescriptor(item);
                    idx++;
                }
                List<SandboxItem> children = new ArrayList<SandboxItem>();
                while (idx < resultSet.size()) {
                    children.add(resultSet.get(idx));
                    idx++;
                }
                toRet.setChildren(children);
                toRet.setLimit(children.size());
            }
        }
        return toRet;
    }

    @Override
    public int getChildrenByPathTotal(String siteId, String path, String locale) {
        return itemMetadataDao.getChildrenByPathTotal(siteId, path, servicesConfig.getLevelDescriptorName(siteId),
                locale);
    }

    @Override
    public GetChildrenResult getChildrenById(String siteId, String parentId, String locale, String sortStrategy,
                                             String order,
                                             int offset, int limit) {
        List<SandboxItem> resultSet = itemMetadataDao.getChildrenById(siteId, parentId,
                servicesConfig.getLevelDescriptorName(siteId), locale, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setOffset(offset);
        toRet.setTotal(getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId), locale));
        return toRet;
    }

    @Override
    public int getChildrenByIdTotal(String siteId, String parentId, String ldName, String locale) {
        return itemMetadataDao.getChildrenByIdTotal(siteId, parentId, servicesConfig.getLevelDescriptorName(siteId),
                locale);
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ItemMetadataDAO getItemMetadataDao() {
        return itemMetadataDao;
    }

    public void setItemMetadataDao(ItemMetadataDAO itemMetadataDao) {
        this.itemMetadataDao = itemMetadataDao;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
