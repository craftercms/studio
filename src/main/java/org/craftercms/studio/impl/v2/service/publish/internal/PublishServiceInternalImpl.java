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

import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.StudioUtils;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.craftercms.studio.impl.v2.utils.DateUtils.formatDateIso;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class PublishServiceInternalImpl implements PublishService, ApplicationContextAware {

    private ContentRepository contentRepository;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private StudioUtils studioUtils;

    protected ItemServiceInternal itemServiceInternal;

    protected ApplicationContext applicationContext;
    private ServicesConfig servicesConfig;
    private UserServiceInternal userServiceInternal;

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

    public List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            String filterType, int numberOfItems) {
        // TODO: implement for new publishing system
        return Collections.emptyList();
    }

    @Override
    public List<DeploymentHistoryGroup> getDeploymentHistory(String siteId, int daysFromToday, int numberOfItems,
                                                             String filterType) throws ServiceLayerException, UserNotFoundException {
        ZonedDateTime toDate = DateUtils.getCurrentTime();
        ZonedDateTime fromDate = toDate.minusDays(daysFromToday);
        List<String> environments = studioUtils.getEnvironmentNames(siteId);
        List<DeploymentHistoryItem> deploymentHistoryItems = getDeploymentHistory(siteId,
                environments, fromDate, toDate, filterType, numberOfItems);
        List<DeploymentHistoryGroup> groups = new ArrayList<>();

        if (deploymentHistoryItems == null) {
            return groups;
        }
        int count = 0;
        Map<String, Set<String>> processedItems = new HashMap<>();
        for (int index = 0; index < deploymentHistoryItems.size() && count < numberOfItems; index++) {
            DeploymentHistoryItem entry = deploymentHistoryItems.get(index);
            String env = entry.getEnvironment();
            if (!processedItems.containsKey(env)) {
                processedItems.put(env, new HashSet<>());
            }
            if (!processedItems.get(env).contains(entry.getPath())) {
                ContentItemTO deployedItem = studioUtils.getContentItemForDashboard(entry.getSite(), entry.getPath());
                if (deployedItem != null) {
                    deployedItem.eventDate = entry.getDeploymentDate();
                    deployedItem.endpoint = entry.getTarget();
                    User user = userServiceInternal.getUserByIdOrUsername(-1, entry.getUser());
                    deployedItem.setUser(user.getUsername());
                    deployedItem.setUserFirstName(user.getFirstName());
                    deployedItem.setUserLastName(user.getLastName());
                    deployedItem.setEndpoint(entry.getEnvironment());
                    String deployedLabel = formatDateIso(entry.getDeploymentDate().truncatedTo(DAYS));
                    if (groups.size() > 0) {
                        DeploymentHistoryGroup group = groups.get(groups.size() - 1);
                        String lastDeployedLabel = group.getInternalName();
                        if (lastDeployedLabel.equals(deployedLabel)) {
                            // add to the last task if it is deployed on the same day
                            group.setNumOfChildren(group.getNumOfChildren() + 1);
                            group.getChildren().add(deployedItem);
                        } else {
                            groups.add(createDeploymentHistoryGroup(deployedLabel, deployedItem));
                        }
                    } else {
                        groups.add(createDeploymentHistoryGroup(deployedLabel, deployedItem));
                    }
                    processedItems.get(env).add(entry.getPath());
                }
            }
        }
        return groups;
    }

    private DeploymentHistoryGroup createDeploymentHistoryGroup(String deployedLabel, ContentItemTO item) {
        // otherwise just add as the last task
        DeploymentHistoryGroup group = new DeploymentHistoryGroup();
        group.setInternalName(deployedLabel);
        List<ContentItemTO> taskItems = group.getChildren();
        if (taskItems == null) {
            taskItems = new ArrayList<>();
            group.setChildren(taskItems);
        }
        taskItems.add(item);
        group.setNumOfChildren(taskItems.size());
        return group;
    }

    @Override
    public List<PublishingTarget> getAvailablePublishingTargets(@SiteId String siteId) {
        var availablePublishingTargets = new ArrayList<PublishingTarget>();
        var liveTarget = new PublishingTarget();
        liveTarget.setName(servicesConfig.getLiveEnvironment(siteId));
        availablePublishingTargets.add(liveTarget);
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            var stagingTarget = new PublishingTarget();
            stagingTarget.setName(servicesConfig.getStagingEnvironment(siteId));
            availablePublishingTargets.add(stagingTarget);
        }
        return availablePublishingTargets;
    }

    @Override
    public boolean isSitePublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        // Site is published if PUBLISHED repo exists
        return contentRepository.publishedRepositoryExists(siteId);
    }

//    @Override
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
    public long publishAll(String siteId, String publishingTarget, String comment) throws ServiceLayerException {
        // TODO: implement new publishing system
        return 0;
    }

    @Override
    public long publish(String siteId, String publishingTarget, List<String> paths, List<String> commitIds, Instant schedule, String comment) {
        // TODO: implement new publishing system
        return 0;
    }

    @Override
    public long requestPublish(String siteId, String publishingTarget, List<String> paths, List<String> commitIds, Instant schedule, String comment) {
        // TODO: implement new publishing system
        return 0;
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

    public void setStudioUtils(StudioUtils studioUtils) {
        this.studioUtils = studioUtils;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }
}
