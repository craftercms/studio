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

package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.ItemMetadataMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.springframework.dao.DuplicateKeyException;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMetadataManagerImpl implements ObjectMetadataManager {

    private final static Logger logger = LoggerFactory.getLogger(ObjectMetadataManagerImpl.class);

    protected ItemMetadataMapper itemMetadataMapper;

    @Override
    @ValidateParams
    public void insertNewObjectMetadata(@ValidateStringParam(name = "site") String site,
                                        @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        try {
        itemMetadataMapper.insertEntry(params);
        } catch (DuplicateKeyException e) {
            logger.debug("Failed to insert metadata for site: " + site + " and path: " + path +
                    " into item_metadata table, because it is duplicate entry.");
        }
    }

    @Override
    @ValidateParams
    public void setObjectMetadata(@ValidateStringParam(name = "site") String site,
                                  @ValidateSecurePathParam(name = "path") String path,
                                  Map<String, Object> properties) {
        path = FilenameUtils.normalize(path, true);
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.putAll(properties);
        itemMetadataMapper.setProperties(params);
    }

    @Override
    @ValidateParams
    public void setObjectMetadataForCommitId(@ValidateStringParam(name = "site") String site,
                                             @ValidateStringParam(name = "commitId") String commitId,
                                             Map<String, Object> properties) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("commitId", commitId);
        params.putAll(properties);
        itemMetadataMapper.setPropertiesForCommit(params);
    }

    @Override
    public void updateObjectMetadata(ItemMetadata itemMetadata) {
        itemMetadataMapper.updateObjectMetadata(itemMetadata);
    }

    @Override
    @ValidateParams
    public ItemMetadata getProperties(@ValidateStringParam(name = "site") String site,
                                      @ValidateSecurePathParam(name = "path") String path) {
        String cleanPath = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", cleanPath);
        return itemMetadataMapper.getProperties(params);
    }

    @Override
    @ValidateParams
    public boolean metadataExist(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        int cnt = itemMetadataMapper.countEntries(params);
        if (cnt < 1) {
            return false;
        } else if (cnt > 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    @ValidateParams
    public boolean isRenamed(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        if (metadataExist(site, path)) {
            ItemMetadata metadata = getProperties(site, path);
            return metadata.getRenamed() > 0;
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public String getOldPath(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        if (metadataExist(site, path)) {
            ItemMetadata metadata = getProperties(site, path);
            return metadata.getOldUrl();
        } else {
            return "";
        }
    }

    @Override
    @ValidateParams
    public void lockContent(@ValidateStringParam(name = "site") String site,
                            @ValidateSecurePathParam(name = "path") String path,
                            @ValidateStringParam(name = "lockOwner") String lockOwner) {
        path = FilenameUtils.normalize(path, true);
        if (!metadataExist(site, path)) {
            insertNewObjectMetadata(site, path);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("lockOwner", lockOwner);
        itemMetadataMapper.setLockOwner(params);
    }

    @Override
    @ValidateParams
    public void unLockContent(@ValidateStringParam(name = "site") String site,
                              @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        if (!metadataExist(site, path)) {
            insertNewObjectMetadata(site, path);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("lockOwner", null);
        itemMetadataMapper.setLockOwner(params);
    }

    @Override
    @ValidateParams
    public void deleteObjectMetadataForSite(@ValidateStringParam(name = "site") String site) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        itemMetadataMapper.deleteObjectMetadataForSite(params);
    }

    @Override
    @ValidateParams
    public void deleteObjectMetadata(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        itemMetadataMapper.deleteEntry(params);
    }

    @Override
    @ValidateParams
    public void deleteObjectMetadataForFolder(@ValidateStringParam(name = "site") String site,
                                              @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path + "/%");
        itemMetadataMapper.deleteFolder(params);
    }

    @Override
    @ValidateParams
    public void updateObjectPath(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "oldPath") String oldPath,
                                 @ValidateSecurePathParam(name = "newPath") String newPath) {
        newPath = FilenameUtils.normalize(newPath, true);
        oldPath = FilenameUtils.normalize(oldPath, true);
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        itemMetadataMapper.updateObjectPath(params);
    }

    @Override
    @ValidateParams
    public void clearRenamed(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, Object> params = new HashMap<>();
        params.put("renamed", false);
        params.put(ItemMetadata.PROP_OLD_URL, "");
        setObjectMetadata(site, path, params);
    }

    @Override
    @ValidateParams
    public void updateCommitId(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path,
                               @ValidateStringParam(name = "commitId") String commitId) {
        path = FilenameUtils.normalize(path, true);
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("commitId", commitId);
        itemMetadataMapper.updateCommitId(params);
    }

    @Override
    @ValidateParams
    public boolean movedPathExists(@ValidateStringParam(name = "site") String site,
                                   @ValidateSecurePathParam(name = "path") String path) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("path", path);
        return itemMetadataMapper.movedPathExists(params) > 0;
    }

    @Override
    @ValidateParams
    public List<String> getSameCommitItems(@ValidateStringParam(name = "site") String site,
                                           @ValidateSecurePathParam(name = "path") String path) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("path", path);
        return itemMetadataMapper.getSameCommitItems(params);
    }

    @Override
    @ValidateParams
    public int countAllItems() {
        return itemMetadataMapper.countAllItems();
    }

    @Override
    public int getContentDashboardTotal(String siteId, String path, String modifier, String contentType, long state,
                                        ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("path", path);
        params.put("modifier", modifier);
        params.put("contentType", contentType);
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        return itemMetadataMapper.getContentDashboardTotal(params);
    }

    @Override
    public List<ItemMetadata> getContentDashboard(String siteId, String path, String modifier, String contentType,
                                                  long state, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                  String sortBy, String order, String groupBy, int offset, int limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("path", path);
        params.put("modifier", modifier);
        params.put("contentType", contentType);
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("sortBy", sortBy);
        params.put("order", order);
        params.put("groupBy", groupBy);
        params.put("offset", offset);
        params.put("limit", limit);
        return itemMetadataMapper.getContentDashboard(params);
    }

    public ItemMetadataMapper getItemMetadataMapper() {
        return itemMetadataMapper;
    }

    public void setItemMetadataMapper(ItemMetadataMapper itemMetadataMapper) {
        this.itemMetadataMapper = itemMetadataMapper;
    }
}
