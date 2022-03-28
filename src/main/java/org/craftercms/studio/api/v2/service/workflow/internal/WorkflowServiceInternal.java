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
import org.craftercms.studio.api.v2.dal.WorkflowPackage;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.List;

public interface WorkflowServiceInternal {

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

    /**
     * Get total number of workflow packages for site
     * @param siteId site identifier
     * @param status package status to filter by
     * @param dateFrom lower boundary to filter by date-time range
     * @param dateTo upper boundary to filter by date-time range
     * @return number of workflow packages
     */
    int getWorkflowPackagesTotal(String siteId, String status, ZonedDateTime dateFrom, ZonedDateTime dateTo);

    /**
     * Get  workflow packages for site ordered by schedule
     * @param siteId site identifier
     * @param status package status to filter by
     * @param dateFrom lower boundary to filter by date-time range
     * @param dateTo upper boundary to filter by date-time range
     * @param offset offset of the first result item
     * @param limit number of results to return
     * @param order ascending or descending
     * @return paginated list of workflow packages
     */
    List<WorkflowPackage> getWorkflowPackages(String siteId, String status, ZonedDateTime dateFrom,
                                              ZonedDateTime dateTo, int offset, int limit, String order);

    /**
     * Create workflow package
     * @param siteId site identifier
     * @param paths list of paths contained within package
     * @param status status of workflow package
     * @param publishingTarget publishing target
     * @param schedule schedule for publishing
     * @param authorComment author's comment
     * @param label label for package
     */
    void createWorkflowPackage(long siteId, List<String> paths, String status, String publishingTarget,
                               ZonedDateTime schedule, long authorId, String authorComment, String label);

    /**
     * Update workflow package
     * @param workflowPackage workflow package to update
     * @return updated workflow package
     */
    WorkflowPackage updateWorkflowPackage(WorkflowPackage workflowPackage);

    /**
     * Get workflow package by id
     * @param workflowPackageId workflow package id
     * @return workflow package
     */
    WorkflowPackage getWorkflowPackage(String workflowPackageId);

    /**
     * Approve workflow packages
     * @param siteId site id
     * @param packageId workflow package id
     * @param reviewerId reviewer id
     * @param reviewerComment reviewer comment
     * @param schedule scheduled
     */
    void approveWorkflowPackage(long siteId, String packageId, long reviewerId, String reviewerComment,
                               ZonedDateTime schedule);

    /**
     * Reject workflow packages
     * @param siteId site id
     * @param packageId workflow package id
     * @param reviewerId reviewer id
     * @param reviewerComment reviewer comment
     */
    void rejectWorkflowPackage(long siteId, String packageId, long reviewerId, String reviewerComment);
}
