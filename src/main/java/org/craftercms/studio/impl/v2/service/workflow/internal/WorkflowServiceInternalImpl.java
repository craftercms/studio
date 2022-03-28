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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.dal.WorkflowPackage;
import org.craftercms.studio.api.v2.dal.WorkflowPackageDAO;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_WORKFLOW_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_WORKFLOW_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.DESTINATION;
import static org.craftercms.studio.api.v2.dal.ItemState.IN_WORKFLOW;
import static org.craftercms.studio.api.v2.dal.ItemState.SCHEDULED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;
import static org.craftercms.studio.api.v2.dal.WorkflowPackage.Status.APPROVED;
import static org.craftercms.studio.api.v2.dal.WorkflowPackage.Status.REJECTED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_LIVE;

public class WorkflowServiceInternalImpl implements WorkflowServiceInternal {

    private WorkflowDAO workflowDao;
    private SiteFeedMapper siteFeedMapper;
    private WorkflowPackageDAO workflowPackageDao;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private ContentServiceInternal contentServiceInternal;
    private ItemServiceInternal itemServiceInternal;
    private ServicesConfig servicesConfig;
    private StudioConfiguration studioConfiguration;

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
        workflowPackage.setItems(items);
        return workflowPackage;
    }

    @Override
    public void approveWorkflowPackage(String siteId, String packageId, long reviewerId,
                                       String reviewerComment,
                                       ZonedDateTime schedule) {
        var workflowPackage = workflowPackageDao.getWorkflowPackage(packageId);
        var workflowPackageItemIds = workflowPackageDao.getWorkflowPackageItemIds(packageId);

        workflowPackage.setReviewerId(reviewerId);
        workflowPackage.setReviewerComment(reviewerComment);
        workflowPackage.setSchedule(schedule);
        workflowPackage.setStatus(APPROVED.name());
        updateWorkflowPackage(workflowPackage);

        long onMask = 0l;
        long offMask = IN_WORKFLOW.value;

        if (schedule.isAfter(DateUtils.getCurrentTime())) {
            onMask += SCHEDULED.value;
        }
        String liveTarget = StringUtils.EMPTY;
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            liveTarget = servicesConfig.getLiveEnvironment(siteId);
        }
        boolean isLive = false;
        if (StringUtils.isEmpty(liveTarget)) {
            liveTarget = studioConfiguration.getProperty(REPO_PUBLISHED_LIVE);
        }
        if (liveTarget.equals(workflowPackage.getPublishingTarget())) {
            isLive = true;
        }
        if (isLive) {
            onMask += DESTINATION.value;
        } else {
            offMask += DESTINATION.value;
        }
        itemServiceInternal.updateStateBitsBulk(workflowPackageItemIds, onMask, offMask);
    }

    @Override
    public void rejectWorkflowPackage(long siteId, String packageId, long reviewerId, String reviewerComment) {
        var workflowPackage = workflowPackageDao.getWorkflowPackage(packageId);
        var workflowPackageItemIds = workflowPackageDao.getWorkflowPackageItemIds(packageId);

        workflowPackage.setReviewerId(reviewerId);
        workflowPackage.setReviewerComment(reviewerComment);
        workflowPackage.setStatus(REJECTED.name());
        updateWorkflowPackage(workflowPackage);

        itemServiceInternal.updateStateBitsBulk(workflowPackageItemIds, CANCEL_WORKFLOW_ON_MASK, CANCEL_WORKFLOW_OFF_MASK);
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

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
