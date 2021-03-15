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

package org.craftercms.studio.api.v1.dal;

import org.apache.ibatis.annotations.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DEPLOYED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public interface ItemMetadataMapper {

    ItemMetadata getProperties(Map params);

    void setProperties(Map params);

    void insertEntry(Map params);

    int countEntries(Map params);

    int countAllItems();

    void deleteEntry(Map params);

    void deleteFolder(Map params);

    void setLockOwner(Map params);

    void deleteObjectMetadataForSite(Map params);

    void updateObjectPath(Map params);

    void updateObjectMetadata(ItemMetadata itemMetadata);

    void updateCommitId(Map params);

    int movedPathExists(Map params);

    List<String> getSameCommitItems(Map params);

    void setPropertiesForCommit(Map params);

    int getContentDashboardTotal(Map params);

    List<ItemMetadata> getContentDashboard(Map params);

    void updateLastDeployedDate(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                                @Param(DEPLOYED) ZonedDateTime deployed);

    void updateLastDeployedDateBulk(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths,
                                    @Param(DEPLOYED) ZonedDateTime deployed);
}
