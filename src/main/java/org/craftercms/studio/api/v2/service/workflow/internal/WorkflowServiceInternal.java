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

package org.craftercms.studio.api.v2.service.workflow.internal;

import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.util.List;

public interface WorkflowServiceInternal {

    WorkflowItem getWorkflowItem(String siteId, String path, String state);

    /**
     * Get workflow entry
     * @param siteId
     * @param path
     * @return
     */
    WorkflowItem getWorkflowEntry(String siteId, String path);

    /**
     * Get workflow entry for approval
     * @param itemId item identifier
     * @return
     */
    Workflow getWorkflowEntryForApproval(Long itemId);

    /**
     * Get workflow entry
     * @param siteId
     * @param path
     * @param publishingPackageId
     * @return
     */
    Workflow getWorkflowEntry(String siteId, String path, String publishingPackageId);

    /**
     * insert new workflow entry
     * @param workflow workflow entry
     */
    void insertWorkflow(Workflow workflow);

    /**
     * insert new workflow entries
     * @param workflowEntries list of workflow entries
     */
    void insertWorkflowEntries(List<Workflow> workflowEntries);

    /**
     * Update new workflow entry
     * @param workflow workflow entry
     */
    void updateWorkflow(Workflow workflow);

    /**
     * Get submitted items for site
     * @param site site identifier
     * @return
     */
    List<WorkflowItem> getSubmittedItems(String site);

    /**
     * Delete workflow entries for given site and paths
     * @param site site identifier
     * @param paths list of paths to delete workflow
     */
    void deleteWorkflowEntries(String site, List<String> paths);

    /**
     * Delete workflow entry for given site and path
     * @param site site identifier
     * @param path path to delete workflow
     */
    void deleteWorkflowEntry(String site, String path);

    /**
     * Delete workflow entries for given site
     * @param siteId site id
     */
    void deleteWorkflowEntriesForSite(long siteId);

    /**
     * Get total number of workflow packages pending approval
     * @param siteId site identifier
     * @return total number of workflow packages
     */
    int getContentPendingApprovalTotal(String siteId);

    /**
     * Get workflow packages pending approval
     * @param siteId site identifier
     * @param offset offset of the first record in the result
     * @param limit limit number of records in the result
     * @return list of workflow packages
     */
    List<DashboardPublishingPackage> getContentPendingApproval(String siteId, int offset, int limit);

    /**
     * Get items from workflow package pending approval
     * @param siteId site identifier
     * @param packageId workflow package identifier
     * @return list of workflow entries
     */
    List<Workflow> getContentPendingApprovalDetail(String siteId, String packageId);
}
