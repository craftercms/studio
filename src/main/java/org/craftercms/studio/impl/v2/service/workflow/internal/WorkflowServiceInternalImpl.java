/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections4.ListUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.DalUtils;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;

public class WorkflowServiceInternalImpl implements WorkflowServiceInternal {

    private final WorkflowDAO workflowDao;
    private final DependencyServiceInternal dependencyService;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @ConstructorProperties({"dependencyService", "retryingDatabaseOperationFacade", "workflowDao"})
    public WorkflowServiceInternalImpl(final DependencyServiceInternal dependencyService, final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade,
                                       final WorkflowDAO workflowDao) {
        this.dependencyService = dependencyService;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.workflowDao = workflowDao;
    }

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
        retryingDatabaseOperationFacade.retry(() -> workflowDao.insertWorkflowEntry(workflow));
    }

    @Override
    public void insertWorkflowEntries(List<Workflow> workflowEntries) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.insertWorkflowEntries(workflowEntries));
    }

    @Override
    public void updateWorkflow(Workflow workflow) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.updateWorkflowEntry(workflow));
    }

    @Override
    public List<WorkflowItem> getSubmittedItems(String site) {
        return workflowDao.getSubmittedItems(site, STATE_OPENED);
    }

    @Override
    public void deleteWorkflowEntries(String siteId, Collection<String> paths) {
        deleteWorkflowEntries(siteId, paths, null);
    }

    @Override
    public void deleteWorkflowEntries(final String siteId, final Collection<String> paths, final String workflowState) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.deleteWorkflowEntries(siteId, paths, workflowState));
    }

    @Override
    public void deleteWorkflowEntry(String siteId, String path) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.deleteWorkflowEntry(siteId, path));
    }

    @Override
    public void deleteWorkflowEntriesForSite(long siteId) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.deleteWorkflowEntriesForSite(siteId));
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

    @Override
    public Collection<String> getWorkflowAffectedPaths(final String siteId, final String path) throws ServiceLayerException {
        Set<String> affectedPaths = new HashSet<>();
        affectedPaths.add(path);
        affectedPaths.addAll(workflowDao.getSamePackagePaths(siteId, path));
        affectedPaths.addAll(getWorkflowHardDeps(siteId, path));

        return affectedPaths;
    }

    @Override
    public Collection<String> getWorkflowHardDeps(final String siteId, final String path) throws ServiceLayerException {
        List<String> hardDependencies = dependencyService.getHardDependencies(siteId, List.of(path));
        return ListUtils.partition(hardDependencies, DalUtils.MY_BATIS_QUERY_BATCH_SIZE).stream()
                .map(batch -> workflowDao.getPathsInWorkflow(siteId, batch))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public void deleteWorkflowPackages(String siteId, Collection<String> affectedPackages) {
        retryingDatabaseOperationFacade.retry(() -> workflowDao.deleteWorkflowPackages(siteId, affectedPackages));
    }
}
