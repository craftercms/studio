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

package org.craftercms.studio.impl.v2.service.publish.internal;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RepositoryChanges;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_ASSET;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_COMPONENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.CANCELLED;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.COMPLETED;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.READY_FOR_LIVE;
import static org.craftercms.studio.impl.v1.service.deployment.PublishingManagerImpl.LIVE_ENVIRONMENT;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class PublishServiceInternalImpl implements PublishServiceInternal, ApplicationContextAware {

    private PublishRequestDAO publishRequestDao;
    private ContentRepository contentRepository;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    protected ItemServiceInternal itemServiceInternal;

    protected ApplicationContext applicationContext;

    @Override
    public int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states) {
        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path,
                                                         List<String> states, int offset, int limit) {
        return publishRequestDao.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    public PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) {
        List<PublishRequest> publishingRequests = publishRequestDao.getPublishingPackageDetails(siteId, packageId);
        PublishingPackageDetails publishingPackageDetails = new PublishingPackageDetails();
        List<PublishingPackageDetails.PublishingPackageItem> packageItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(publishingRequests)) {
            PublishRequest pr = publishingRequests.get(0);
            publishingPackageDetails.setSiteId(pr.getSite());
            publishingPackageDetails.setPackageId(pr.getPackageId());
            publishingPackageDetails.setEnvironment(pr.getEnvironment());
            publishingPackageDetails.setState(pr.getState());
            publishingPackageDetails.setScheduledDate(pr.getScheduledDate());
            publishingPackageDetails.setUser(pr.getUser());
            publishingPackageDetails.setComment(pr.getSubmissionComment());
        }
        for (PublishRequest publishRequest : publishingRequests) {
            PublishingPackageDetails.PublishingPackageItem item = new PublishingPackageDetails.PublishingPackageItem();
            item.setPath(publishRequest.getPath());
            item.setContentTypeClass(publishRequest.getContentTypeClass());
            packageItems.add(item);
        }
        publishingPackageDetails.setItems(packageItems);
        return publishingPackageDetails;
    }

    @Override
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        retryingDatabaseOperationFacade.cancelPackages(siteId, packageIds, CANCELLED);
    }

    @Override
    public int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state) {
        return 0;
    }

    @Override
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                            String publisher, ZonedDateTime dateFrom,
                                                            ZonedDateTime dateTo, String contentType, long state,
                                                            String sortBy, String order, int offset, int limit) {
        return contentRepository.getPublishingHistory(siteId, environment, path, publisher, dateFrom, dateTo, limit);
    }

    @Override
    public List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            String filterType, int numberOfItems) {
        int offset = 0;
        int counter = 0;
        List<DeploymentHistoryItem> toRet = new ArrayList<>();

        String contentTypeClass;
        switch (filterType) {
            case CONTENT_TYPE_PAGE:
            case CONTENT_TYPE_COMPONENT:
            case CONTENT_TYPE_ASSET:
                contentTypeClass = filterType;
                break;
            default:
                contentTypeClass = null;
                break;
        }
        List<PublishRequest> deploymentHistory = publishRequestDao.getDeploymentHistory(siteId, environments, COMPLETED,
                    contentTypeClass, fromDate, toDate, offset, numberOfItems);
            if (CollectionUtils.isNotEmpty(deploymentHistory)) {
                for (PublishRequest publishRequest : deploymentHistory) {
                        DeploymentHistoryItem dhi = new DeploymentHistoryItem();
                        dhi.setSite(siteId);
                        dhi.setPath(publishRequest.getPath());
                        dhi.setDeploymentDate(publishRequest.getPublishedOn());
                        dhi.setUser(publishRequest.getUser());
                        dhi.setEnvironment(publishRequest.getEnvironment());
                        toRet.add(dhi);
                        if (++counter >= numberOfItems) {
                            break;
                        }

            }

        }
        toRet.sort((o1, o2) -> o2.getDeploymentDate().compareTo(o1.getDeploymentDate()));
        return toRet;
    }

    @Override
    public void cancelScheduledQueueItems(String siteId, List<String> paths) {
        retryingDatabaseOperationFacade.cancelScheduledQueueItems(siteId, paths, DateUtils.getCurrentTime(), CANCELLED,
                READY_FOR_LIVE);
    }

    @Override
    public boolean isSitePublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        // Site is published if PUBLISHED repo exists
        return contentRepository.publishedRepositoryExists(siteId);
    }

    @Override
    public void initialPublish(String siteId) throws SiteNotFoundException {
        contentRepository.initialPublish(siteId);
    }

    @Override
    public int getPublishingPackagesScheduledTotal(String siteId, String publishingTarget, ZonedDateTime dateFrom,
                                                   ZonedDateTime dateTo) {
        return publishRequestDao
                .getPublishingPackagesScheduledTotal(siteId, publishingTarget, READY_FOR_LIVE, dateFrom, dateTo)
                .orElse(0);
    }

    @Override
    public List<DashboardPublishingPackage> getPublishingPackagesScheduled(String siteId, String publishingTarget,
                                                                           ZonedDateTime dateFrom,
                                                                           ZonedDateTime dateTo, int offset, int limit) {
        return publishRequestDao.getPublishingPackagesScheduled(siteId, publishingTarget, READY_FOR_LIVE, dateFrom,
                dateTo, offset, limit);
    }

    @Override
    public int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver,
                                                 ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        // Need to check if null because of COUNT + GROUP BY
        return publishRequestDao
                .getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, COMPLETED, dateFrom, dateTo)
                .orElse(0);
    }

    @Override
    public List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget,
                                                                         String approver, ZonedDateTime dateFrom,
                                                                         ZonedDateTime dateTo, int offset, int limit) {
        return publishRequestDao.getPublishingPackagesHistory(siteId, publishingTarget, approver, COMPLETED, dateFrom,
                dateTo, offset, limit);
    }

    @Override
    public int getNumberOfPublishes(String siteId, int days) {
        return publishRequestDao.getNumberOfPublishes(siteId, days);
    }

    @Override
    public int getNumberOfPublishedItemsByState(String siteId, int days, String activityAction, String publishState,
                                                String publishAction) {
        return publishRequestDao.getNumberOfPublishedItemsByState(siteId, days, activityAction, publishState,
                                                                    publishAction);
    }

    @Override
    public void publishAll(String siteId, String publishingTarget, String comment) throws ServiceLayerException {
        // do the operations in the repo
        RepositoryChanges changes = contentRepository.publishAll(siteId, publishingTarget, comment);
        // update the state for the changed items
        long onMask;
        long offMask;
        if (LIVE_ENVIRONMENT.equals(publishingTarget)) {
            onMask = PUBLISH_TO_LIVE_ON_MASK;
            offMask = PUBLISH_TO_LIVE_OFF_MASK;
        } else {
            onMask = PUBLISH_TO_STAGE_ON_MASK;
            offMask = PUBLISH_TO_STAGE_OFF_MASK;
        }
        if (changes.isInitialPublish()) {
            itemServiceInternal.updateStatesForSite(siteId, onMask, offMask);
        } else {
            // Deleted items not included since those will be gone from the DB, those might need to be included
            // later if soft-delete is implemented
            itemServiceInternal.updateStateBitsBulk(siteId, changes.getUpdatedPaths(), onMask, offMask);
        }
        // trigger the event
        applicationContext.publishEvent(new PublishEvent(siteId));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setPublishRequestDao(PublishRequestDAO publishRequestDao) {
        this.publishRequestDao = publishRequestDao;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

}
