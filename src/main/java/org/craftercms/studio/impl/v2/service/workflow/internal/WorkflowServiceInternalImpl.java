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

import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.dal.WorkflowPackage;
import org.craftercms.studio.api.v2.dal.WorkflowPackageDAO;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.impl.v2.service.content.internal.ContentServiceInternalImpl;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;

public class WorkflowServiceInternalImpl implements WorkflowServiceInternal {

    private WorkflowDAO workflowDao;
    private SiteFeedMapper siteFeedMapper;
    private WorkflowPackageDAO workflowPackageDao;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private ContentServiceInternalImpl contentServiceInternal;

    @Override
    public WorkflowItem getWorkflowEntry(String siteId, String path) {
        return workflowDao.getWorkflowEntryOpened(siteId, path, STATE_OPENED);
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
        return workflowDao.getContentPendingApprovalTotal(siteId, STATE_OPENED);
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
    public int getWorkflowPackagesTotal(String siteId, String status, ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return workflowPackageDao.getWorkflowPackagesTotal(siteId, status, dateFrom, dateTo);
    }

    @Override
    public List<WorkflowPackage> getWorkflowPackages(String siteId, String status, ZonedDateTime dateFrom,
                                                     ZonedDateTime dateTo, int offset, int limit, String order) {
        return workflowPackageDao.getWorkflowPackages(siteId, status, dateFrom, dateTo, offset, limit, order);
    }

    @Override
    public void createWorkflowPackage(long siteId, List<String> paths, String status, String publishingTarget,
                                      ZonedDateTime schedule, long authorId, String authorComment, String label) {
        WorkflowPackage workflowPackage = new WorkflowPackage();
        workflowPackage.setId(UUID.randomUUID().toString());
        workflowPackage.setSiteId(siteId);
        workflowPackage.setStatus(status);
        workflowPackage.setPublishingTarget(publishingTarget);
        workflowPackage.setSchedule(schedule);
        workflowPackage.setAuthorId(authorId);
        workflowPackage.setAuthorComment(authorComment);
        workflowPackage.setLabel(label);
        workflowPackageDao.createWorkflowPackage(workflowPackage);
        workflowPackageDao.addWorkflowPackageItems(workflowPackage.getId(), siteId, paths);
    }

    @Override
    public WorkflowPackage updateWorkflowPackage(WorkflowPackage workflowPackage) {
        return workflowPackageDao.updateWorkflowPackage(workflowPackage);
    }

    @Override
    public WorkflowPackage getWorkflowPackage(String workflowPackageId)
            throws UserNotFoundException, ServiceLayerException {
        var workflowPackage = workflowPackageDao.getWorkflowPackage(workflowPackageId);
        var packageItemIds = workflowPackageDao.getWorkflowPackageItemIds(workflowPackageId);
        var params = new HashMap<String, Object>();
        params.put(SITE_ID, workflowPackage.getSiteId());
        var site = siteFeedMapper.getSite(params);
        var items = contentServiceInternal.getSandboxItemsById(site.getSiteId(), packageItemIds, true);
        return workflowPackage;
    }

    @Override
    public void approveWorkflowPackage(long siteId, String packageId, long reviewerId, String reviewerComment, ZonedDateTime schedule) {

    }

    @Override
    public void rejectWorkflowPackage(long siteId, String packageId, long reviewerId, String reviewerComment) {

    }

    public WorkflowDAO getWorkflowDao() {
        return workflowDao;
    }

    public void setWorkflowDao(WorkflowDAO workflowDao) {
        this.workflowDao = workflowDao;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public WorkflowPackageDAO getWorkflowPackageDao() {
        return workflowPackageDao;
    }

    public void setWorkflowPackageDao(WorkflowPackageDAO workflowPackageDao) {
        this.workflowPackageDao = workflowPackageDao;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public ContentServiceInternalImpl getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternalImpl contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }
}
