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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISHING_PACKAGE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.WORKFLOW;

public interface WorkflowDAO {

    /**
     * Get workflow entry
     * @param siteId site identifier
     * @param path path of the item
     * @param stateOpened state opened
     * @return
     */
    WorkflowItem getWorkflowEntryOpened(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                                    @Param(STATE) String stateOpened);

    /**
     * Get workflow entry
     * @param siteId site identifier
     * @param path
     * @param publishingPackageId
     * @return
     */
    Workflow getWorkflowEntry(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                              @Param(PUBLISHING_PACKAGE_ID) String publishingPackageId);

    /**
     * Delete workflow entry
     * @param id entry id
     */
    void deleteWorkflowEntryById(@Param(ID) long id);

    /**
     * Insert workflow entry
     * @param workflow workflow entry
     */
    void insertWorkflowEntry(@Param(WORKFLOW) Workflow workflow);

    /**
     * Get submitted items
     * @param site site identifier
     * @param stateOpened state opened
     * @return
     */
    List<WorkflowItem> getSubmittedItems(@Param(SITE_ID) String site, @Param(STATE) String stateOpened);

    /**
     * Delete workflow entries
     * @param siteId site identifier
     * @param paths list of paths
     */
    void deleteWorkflowEntries(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths);

    /**
     * Delete workflow entry
     * @param siteId site identifier
     * @param path path
     */
    void deleteWorkflowEntry(@Param(SITE_ID) String siteId, @Param(PATH) String path);

    /**
     * Delete workflow entries for site
     * @param siteId site id
     */
    void deleteWorkflowEntriesForSite(long siteId);
}
