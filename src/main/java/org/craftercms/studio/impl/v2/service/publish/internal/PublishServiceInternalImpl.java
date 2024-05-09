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
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RepositoryChanges;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class PublishServiceInternalImpl implements PublishServiceInternal, ApplicationContextAware {

    private ContentRepository contentRepository;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    protected ItemServiceInternal itemServiceInternal;

    protected ApplicationContext applicationContext;
    private ServicesConfig servicesConfig;

    @Override
    public int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path,
                                                         List<String> states, int offset, int limit) {
        // TODO: implement for new publishing system
        return Collections.emptyList();
//        return publishRequestDao.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    public PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) {
        // TODO: implement for new publishing system
        return null;
//        List<PublishRequest> publishingRequests = publishRequestDao.getPublishingPackageDetails(siteId, packageId);
//        PublishingPackageDetails publishingPackageDetails = new PublishingPackageDetails();
//        List<PublishingPackageDetails.PublishingPackageItem> packageItems = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(publishingRequests)) {
//            PublishRequest pr = publishingRequests.get(0);
//            publishingPackageDetails.setSiteId(pr.getSite());
//            publishingPackageDetails.setPackageId(pr.getPackageId());
//            publishingPackageDetails.setEnvironment(pr.getEnvironment());
//            publishingPackageDetails.setState(pr.getState());
//            publishingPackageDetails.setScheduledDate(pr.getScheduledDate());
//            publishingPackageDetails.setUser(pr.getUser());
//            publishingPackageDetails.setComment(pr.getSubmissionComment());
//        }
//        for (PublishRequest publishRequest : publishingRequests) {
//            PublishingPackageDetails.PublishingPackageItem item = new PublishingPackageDetails.PublishingPackageItem();
//            item.setPath(publishRequest.getPath());
//            item.setContentTypeClass(publishRequest.getContentTypeClass());
//            packageItems.add(item);
//        }
//        publishingPackageDetails.setItems(packageItems);
//        return publishingPackageDetails;
    }

    @Override
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        // TODO: implement for new publishing system
//        retryingDatabaseOperationFacade.retry(() -> publishRequestDao.cancelPackages(siteId, packageIds, CANCELLED));
    }

    @Override
    public int getPublishingHistoryDetailTotalItems(String siteId, String packageId) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getPublishingHistoryDetailTotalItems(siteId, packageId);
    }

    // TODO: implement for new publishing system
//    public List<PublishRequest> getPublishingHistoryDetail(String siteId, String packageId, int offset, int limit) {
//        return publishRequestDao.getPublishingHistoryDetail(siteId, packageId, offset, limit);
//    }

    @Override
    public List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            String filterType, int numberOfItems) {
        // TODO: implement for new publishing system
        return Collections.emptyList();
//
//        int offset = 0;
//        int counter = 0;
//        List<DeploymentHistoryItem> toRet = new ArrayList<>();
//
//        String contentTypeClass;
//        switch (filterType) {
//            case CONTENT_TYPE_PAGE:
//            case CONTENT_TYPE_COMPONENT:
//            case CONTENT_TYPE_ASSET:
//                contentTypeClass = filterType;
//                break;
//            default:
//                contentTypeClass = null;
//                break;
//        }
//        List<PublishRequest> deploymentHistory = publishRequestDao.getDeploymentHistory(siteId, environments, COMPLETED,
//                    contentTypeClass, fromDate, toDate, offset, numberOfItems);
//            if (CollectionUtils.isNotEmpty(deploymentHistory)) {
//                for (PublishRequest publishRequest : deploymentHistory) {
//                        DeploymentHistoryItem dhi = new DeploymentHistoryItem();
//                        dhi.setSite(siteId);
//                        dhi.setPath(publishRequest.getPath());
//                        dhi.setDeploymentDate(publishRequest.getPublishedOn());
//                        dhi.setUser(publishRequest.getUser());
//                        dhi.setEnvironment(publishRequest.getEnvironment());
//                        toRet.add(dhi);
//                        if (++counter >= numberOfItems) {
//                            break;
//                        }
//
//            }
//
//        }
//        toRet.sort((o1, o2) -> o2.getDeploymentDate().compareTo(o1.getDeploymentDate()));
//        return toRet;
    }

    @Override
    public void cancelScheduledQueueItems(String siteId, List<String> paths) {
        // TODO: implement for new publishing system
//        retryingDatabaseOperationFacade.retry(() -> publishRequestDao.cancelScheduledQueueItems(siteId, paths, DateUtils.getCurrentTime(), CANCELLED,
//                READY_FOR_LIVE));
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
    public int getPublishingItemsScheduledTotal(String siteId, String publishingTarget, String approver,
                                                ZonedDateTime dateFrom, ZonedDateTime dateTo, List<String> systemTypes) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao
//                .getPublishingItemsScheduledTotal(siteId, publishingTarget, approver, READY_FOR_LIVE, dateFrom, dateTo, systemTypes)
//                .orElse(0);
    }

    // TODO: implement for new publishing system
//    @Override
//    public List<PublishRequest> getPublishingItemsScheduled(String siteId, String publishingTarget, String approver,
//                                                            ZonedDateTime dateFrom, ZonedDateTime dateTo,
//                                                            List<String> systemTypes, List<SortField> sortFields, int offset, int limit) {
//        return publishRequestDao.getPublishingItemsScheduled(siteId, publishingTarget, approver, READY_FOR_LIVE,
//                dateFrom, dateTo, systemTypes, DalUtils.mapSortFields(sortFields,PublishRequestDAO.SORT_FIELD_MAP), offset, limit);
//    }

    @Override
    public int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver,
                                                 ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        // TODO: implement for new publishing system
        return 0;
        // Need to check if null because of COUNT + GROUP BY
//        return publishRequestDao
//                .getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, COMPLETED, dateFrom, dateTo)
//                .orElse(0);
    }

    @Override
    public List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget,
                                                                         String approver, ZonedDateTime dateFrom,
                                                                         ZonedDateTime dateTo, int offset, int limit) {
        // TODO: implement for new publishing system
        return Collections.emptyList();
//        return publishRequestDao.getPublishingPackagesHistory(siteId, publishingTarget, approver, COMPLETED, dateFrom,
//                dateTo, offset, limit);
    }

    @Override
    public int getNumberOfPublishes(String siteId, int days) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getNumberOfPublishes(siteId, days);
    }

    @Override
    public int getNumberOfPublishedItemsByState(String siteId, int days, String activityAction, String publishState,
                                                String publishAction) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getNumberOfPublishedItemsByState(siteId, days, activityAction, publishState,
//                                                                    publishAction);
    }

    @Override
    public RepositoryChanges publishAll(String siteId, String publishingTarget, String comment) throws ServiceLayerException {
        String liveEnvironment = servicesConfig.getLiveEnvironment(siteId);
        // do the operations in the repo
        RepositoryChanges changes = contentRepository.publishAll(siteId, publishingTarget, comment);
        // update the state for the changed items
        long onMask;
        long offMask;
        if (liveEnvironment.equals(publishingTarget)) {
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
            Collection<String> publishedItems = CollectionUtils.subtract(changes.getUpdatedPaths(), changes.getFailedPaths());
            itemServiceInternal.updateStateBitsBulk(siteId, publishedItems, onMask, offMask);
        }
        // trigger the event
        applicationContext.publishEvent(new PublishEvent(siteId));

        return changes;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
