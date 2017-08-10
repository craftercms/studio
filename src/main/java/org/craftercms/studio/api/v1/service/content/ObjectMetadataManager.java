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

package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.dal.ItemMetadata;

import java.util.Map;

public interface ObjectMetadataManager {

    void insertNewObjectMetadata(String site, String path);

    void setObjectMetadata(String site, String path, Map<String, Object> properties);

    ItemMetadata getProperties(String site, String path);

    boolean metadataExist(String site, String path);

    boolean isRenamed(String site, String path);

    String getOldPath(String site, String path);

    void lockContent(String site, String path, String lockOwner);

    void unLockContent(String site, String path);

    void deleteObjectMetadataForSite(String site);

    void deleteObjectMetadata(String site, String path);

    void updateObjectPath(String site, String oldUri, String relativePath);

    void clearRenamed(String site, String path);

    void updateObjectMetadata(ItemMetadata itemMetadata);

    void updateCommitId(String site, String path, String commitId);
}
