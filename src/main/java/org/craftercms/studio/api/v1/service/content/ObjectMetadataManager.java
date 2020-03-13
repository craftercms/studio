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

package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.dal.ItemMetadata;

import java.time.ZonedDateTime;
import java.util.List;
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

    void deleteObjectMetadataForFolder(String site, String path);

    void updateObjectPath(String site, String oldUri, String relativePath);

    void clearRenamed(String site, String path);

    void updateObjectMetadata(ItemMetadata itemMetadata);

    void updateCommitId(String site, String path, String commitId);

    boolean movedPathExists(String site, String path);

    List<String> getSameCommitItems(String site, String path);

    void setObjectMetadataForCommitId(String site, String commitId, Map<String, Object> props);

    int countAllItems();

    /**
     * Get total number of records for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter for result set
     * @param modifier filter results by user
     * @param contentType filter results by content type
     * @param state filter results by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @return total number of records in result set
     */
    int getContentDashboardTotal(String siteId, String path, String modifier, String contentType,
                                 long state, ZonedDateTime dateFrom, ZonedDateTime dateTo);

    /**
     * Get result set for content dashboard
     *
     * @param siteId site identifier
     * @param path path regular expression to apply as filter for result set
     * @param modifier filter results by user
     * @param contentType filter results by content type
     * @param state filter results by state
     * @param dateFrom lower boundary for modified date
     * @param dateTo upper boundary for modified date
     * @param sortBy sort results by column
     * @param order order of results
     * @param offset offset of the first record in result set
     * @param limit number of records to return
     * @return list of item metadata records
     */
    List<ItemMetadata> getContentDashboard(String siteId, String path, String modifier, String contentType,
                                           long state, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                           String sortBy, String order, int offset, int limit);
}
