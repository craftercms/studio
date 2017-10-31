/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.ObjectMetadataMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class ObjectMetadataManagerImpl implements ObjectMetadataManager {

    private final static Logger logger = LoggerFactory.getLogger(ObjectMetadataManagerImpl.class);

    @Override
    @ValidateParams
    public void insertNewObjectMetadata(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        objectMetadataMapper.insertEntry(params);
    }

    @Override
    @ValidateParams
    public void setObjectMetadata(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, Map<String, Object> properties) {
        path = path.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.putAll(properties);
        objectMetadataMapper.setProperties(params);
    }

    @Override
    public void updateObjectMetadata(ObjectMetadata objectMetadata) {
        objectMetadataMapper.updateObjectMetadata(objectMetadata);
    }

    @Override
    @ValidateParams
    public ObjectMetadata getProperties(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        return objectMetadataMapper.getProperties(params);
    }

    @Override
    @ValidateParams
    public boolean metadataExist(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        int cnt = objectMetadataMapper.countEntries(params);
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
    public boolean isRenamed(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        if (metadataExist(site, path)) {
            ObjectMetadata metadata = getProperties(site, path);
            return metadata.getRenamed() > 0;
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public String getOldPath(@ValidateStringParam(name = "path") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        if (metadataExist(site, path)) {
            ObjectMetadata metadata = getProperties(site, path);
            return metadata.getOldUrl();
        } else {
            return "";
        }
    }

    @Override
    @ValidateParams
    public void lockContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "lockOwner") String lockOwner) {
        path = path.replace("//", "/");
        if (!metadataExist(site, path)) {
            insertNewObjectMetadata(site, path);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("lockOwner", lockOwner);
        objectMetadataMapper.setLockOwner(params);
    }

    @Override
    @ValidateParams
    public void unLockContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        if (!metadataExist(site, path)) {
            insertNewObjectMetadata(site, path);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("lockOwner", null);
        objectMetadataMapper.setLockOwner(params);
    }

    @Override
    @ValidateParams
    public void deleteObjectMetadataForSite(@ValidateStringParam(name = "site") String site) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        objectMetadataMapper.deleteObjectMetadataForSite(params);
    }

    @Override
    @ValidateParams
    public void deleteObjectMetadata(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        objectMetadataMapper.deleteEntry(params);
    }

    @Override
    @ValidateParams
    public void updateObjectPath(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "oldPath") String oldPath, @ValidateSecurePathParam(name = "newPath") String newPath) {
        newPath = newPath.replace("//", "/");
        oldPath = oldPath.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        objectMetadataMapper.updateObjectPath(params);
    }

    @Override
    @ValidateParams
    public void clearRenamed(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        path = path.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("renamed", false);
        params.put(ObjectMetadata.PROP_OLD_URL, "");
        setObjectMetadata(site, path, params);
    }

    @Autowired
    protected ObjectMetadataMapper objectMetadataMapper;
}
