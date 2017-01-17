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
    public void insertNewObjectMetadata(String site, String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        objectMetadataMapper.insertEntry(params);
    }

    @Override
    public void setObjectMetadata(String site, String path, Map<String, Object> properties) {
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
    public ObjectMetadata getProperties(String site, String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        return objectMetadataMapper.getProperties(params);
    }

    @Override
    public boolean metadataExist(String site, String path) {
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
    public boolean isRenamed(String site, String path) {
        path = path.replace("//", "/");
        if (metadataExist(site, path)) {
            ObjectMetadata metadata = getProperties(site, path);
            return metadata.getRenamed() > 0;
        } else {
            return false;
        }
    }

    @Override
    public String getOldPath(String site, String path) {
        path = path.replace("//", "/");
        if (metadataExist(site, path)) {
            ObjectMetadata metadata = getProperties(site, path);
            return metadata.getOldUrl();
        } else {
            return "";
        }
    }

    @Override
    public void lockContent(String site, String path, String lockOwner) {
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
    public void unLockContent(String site, String path) {
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
    public void deleteObjectMetadataForSite(String site) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        objectMetadataMapper.deleteObjectMetadataForSite(params);
    }

    @Override
    public void deleteObjectMetadata(String site, String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        objectMetadataMapper.deleteEntry(params);
    }

    @Override
    public void updateObjectPath(String site, String oldPath, String newPath) {
        newPath = newPath.replace("//", "/");
        oldPath = oldPath.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        objectMetadataMapper.updateObjectPath(params);
    }

    @Override
    public void clearRenamed(String site, String path) {
        path = path.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("renamed", false);
        params.put(ObjectMetadata.PROP_OLD_URL, "");
        setObjectMetadata(site, path, params);
    }

    @Override
    public void updateCommitId(String site, String path, String commitId) {
        path = path.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        params.put("commitId", commitId);
        objectMetadataMapper.updateCommitId(params);
    }

    @Autowired
    protected ObjectMetadataMapper objectMetadataMapper;
}
