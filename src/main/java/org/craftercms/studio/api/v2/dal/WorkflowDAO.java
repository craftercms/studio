/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.util.List;
import java.util.Optional;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ITEM_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PACKAGE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISHING_PACKAGE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATE_OPENED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.WORKFLOW;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.WORKFLOW_ENTRIES;

public interface WorkflowDAO {

    /**
     * Get workflow entry
     * @param siteId site identifier
     * @param path path of the item
     * @param stateOpened state opened
     * @return
     */
    WorkflowItem getWorkflowEntryOpened(@Param(SITE_ID) String siteId, @Param(PATH) String path,
                                    @Param(STATE_OPENED) String stateOpened);

    /**
     * Get workflow entry for approval
     * @param itemId item identifier
     * @param stateOpened state opened
     * @return
     */
    Workflow getWorkflowEntryForApproval(@Param(ITEM_ID) Long itemId, @Param(STATE_OPENED) String stateOpened);

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
     * Insert workflow entries
     * @param workflowEntries list of workflow entries
     */
    void insertWorkflowEntries(@Param(WORKFLOW_ENTRIES) List<Workflow> workflowEntries);

    /**
     * Update workflow entry
     * @param workflow workflow entry
     */
    void updateWorkflowEntry(Workflow workflow);

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

    /**
     * Get total number of workflow packages pending approval
     * @param siteId site identifier
     * @param openedState value for OPENED state
     * @return total number of workflow packages pending approval
     */
    Optional<Integer> getContentPendingApprovalTotal(@Param(SITE_ID) String siteId, @Param(STATE) String openedState);

    /**
     * Get workflow packages pending approval
     * @param siteId site identifier
     * @param openedState value for OPENED state
     * @param offset offset of the first record in the result
     * @param limit limit number of results
     * @return list of workflow packages pending approval
     */
    List<DashboardPublishingPackage> getContentPendingApproval(@Param(SITE_ID) String siteId,
                                                               @Param(STATE) String openedState,
                                                               @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Get content pending approval for given workflow package id
     * @param siteId site identifier
     * @param packageId workflow package identifier
     * @return List of workflow entries
     */
    List<Workflow> getContentPendingApprovalDetail(@Param(SITE_ID) String siteId, @Param(PACKAGE_ID) String packageId);
}
