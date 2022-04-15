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

package org.craftercms.studio.impl.v2.service.workflow.internal;

import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;

public class WorkflowServiceInternalImpl implements WorkflowServiceInternal {

    private WorkflowDAO workflowDao;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public WorkflowItem getWorkflowItem(String siteId, String path, String state) {
        return workflowDao.getWorkflowEntryOpened(siteId, path, state);
    }

    @Override
    public WorkflowItem getWorkflowEntry(String siteId, String path) {
        return getWorkflowItem(siteId, path, STATE_OPENED);
    }

    @Override
    public Workflow getWorkflowEntryForApproval(Long itemId) {
        return workflowDao.getWorkflowEntryForApproval(itemId, STATE_OPENED);
    }

    @Override
    public Workflow getWorkflowEntry(String siteId, String path, String publishingPackageId) {
        return workflowDao.getWorkflowEntry(siteId, path, publishingPackageId);
    }

    @Override
    public void insertWorkflow(Workflow workflow) {
        retryingDatabaseOperationFacade.insertWorkflowEntry(workflow);
    }

    @Override
    public void insertWorkflowEntries(List<Workflow> workflowEntries) {
        retryingDatabaseOperationFacade.insertWorkflowEntries(workflowEntries);
    }

    @Override
    public void updateWorkflow(Workflow workflow) {
        retryingDatabaseOperationFacade.updateWorkflowEntry(workflow);
    }

    @Override
    public List<WorkflowItem> getSubmittedItems(String site) {
        return workflowDao.getSubmittedItems(site, STATE_OPENED);
    }

    @Override
    public void deleteWorkflowEntries(String siteId, List<String> paths) {
        retryingDatabaseOperationFacade.deleteWorkflowEntries(siteId, paths);
    }

    @Override
    public void deleteWorkflowEntry(String siteId, String path) {
        retryingDatabaseOperationFacade.deleteWorkflowEntry(siteId, path);
    }

    @Override
    public void deleteWorkflowEntriesForSite(long siteId) {
        retryingDatabaseOperationFacade.deleteWorkflowEntriesForSite(siteId);
    }

    @Override
    public int getContentPendingApprovalTotal(String siteId) {
        return workflowDao.getContentPendingApprovalTotal(siteId, STATE_OPENED).orElse(0);
    }

    @Override
    public List<DashboardPublishingPackage> getContentPendingApproval(String siteId, int offset, int limit) {
        return workflowDao.getContentPendingApproval(siteId, STATE_OPENED, offset, limit);
    }

    @Override
    public List<Workflow> getContentPendingApprovalDetail(String siteId, String packageId) {
        return workflowDao.getContentPendingApprovalDetail(siteId, packageId);
    }

    public void setWorkflowDao(WorkflowDAO workflowDao) {
        this.workflowDao = workflowDao;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
