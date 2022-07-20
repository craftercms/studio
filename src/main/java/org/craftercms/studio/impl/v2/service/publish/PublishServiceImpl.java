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

package org.craftercms.studio.impl.v2.service.publish;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.security.HasAnyPermissions;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.StudioUtils;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;
import org.craftercms.studio.permissions.CompositePermission;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_PUBLISHING_PACKAGE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_PUBLISHING_PACKAGE_ON_MASK;
import static org.craftercms.studio.impl.v2.utils.DateUtils.formatDateIso;
import static org.craftercms.studio.impl.v2.utils.DateUtils.getCurrentTime;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CANCEL_PUBLISH;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_GET_PUBLISHING_QUEUE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH;

public class PublishServiceImpl implements PublishService {

    private PublishServiceInternal publishServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;
    private ItemServiceInternal itemServiceInternal;
    private StudioUtils studioUtils;
    private ServicesConfig servicesConfig;
    private ActivityStreamServiceInternal activityStreamServiceInternal;
    private UserServiceInternal userServiceInternal;

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public int getPublishingPackagesTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String environment,
                                          String path, List<String> states)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public List<PublishingPackage> getPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                         String environment, String path, List<String> states,
                                                         int offset, int limit) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public PublishingPackageDetails getPublishingPackageDetails(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                                String packageId) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackageDetails(siteId, packageId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CANCEL_PUBLISH)
    public void cancelPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         List<String> packageIds) throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        SiteFeed siteFeed = siteService.getSite(siteId);
        String username = securityService.getCurrentUser();
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        publishServiceInternal.cancelPublishingPackages(siteId, packageIds);
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
        for (String pId : packageIds) {
            PublishingPackageDetails packageDetails = publishServiceInternal.getPublishingPackageDetails(siteId, pId);
            List<String> paths = new ArrayList<>();
            for (PublishingPackageDetails.PublishingPackageItem item : packageDetails.getItems()) {
                paths.add(item.getPath());
                AuditLogParameter auditLogParameter = new AuditLogParameter();
                auditLogParameter.setTargetId(siteId + ":" + item.getPath());
                auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
                auditLogParameter.setTargetValue(item.getPath());
                auditLogParameters.add(auditLogParameter);
            }

            itemServiceInternal.updateStateBitsBulk(siteId, paths, CANCEL_PUBLISHING_PACKAGE_ON_MASK,
                    CANCEL_PUBLISHING_PACKAGE_OFF_MASK);

            createAuditLogEntry(siteFeed, username, auditLogParameters);

            activityStreamServiceInternal.insertActivity(siteFeed.getId(), user.getId(),
                    OPERATION_CANCEL_PUBLISHING_PACKAGE, DateUtils.getCurrentTime(), null, pId);
        }
    }

    private void createAuditLogEntry(SiteFeed siteFeed, String username, List<AuditLogParameter> auditLogParameters) {

        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CANCEL_PUBLISHING_PACKAGE);
        auditLog.setActorId(username);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteFeed.getSiteId());
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteFeed.getSiteId());
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state) {
        return publishServiceInternal.getPublishingHistoryTotal(siteId, environment, path, publisher, dateFrom, dateTo,
                contentType, state);
    }

    @Override
    public List<PublishingDashboardItem> getPublishingHistory(String siteId, String environment, String path,
                                                              String publisher, ZonedDateTime dateFrom,
                                                              ZonedDateTime dateTo, String contentType, long state,
                                                              String sortBy, String order, int offset, int limit) {
        List<PublishingHistoryItem> publishingHistoryItems = publishServiceInternal.getPublishingHistory(siteId,
                environment, path, publisher, dateFrom, dateTo, contentType, state, sortBy, order, offset,
                limit);
        return publishingHistoryItems
                .stream()
                .map(itemServiceInternal::convertHistoryItemToDashboardItem)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeploymentHistoryGroup> getDeploymentHistory(String siteId, int daysFromToday, int numberOfItems,
                                                             String filterType) {
        ZonedDateTime toDate = DateUtils.getCurrentTime();
        ZonedDateTime fromDate = toDate.minusDays(daysFromToday);
        List<String> environments = studioUtils.getEnvironmentNames(siteId);
        List<DeploymentHistoryItem> deploymentHistoryItems = publishServiceInternal.getDeploymentHistory(siteId,
                environments, fromDate, toDate, filterType, numberOfItems);
        List<DeploymentHistoryGroup> groups = new ArrayList<>();

        if (deploymentHistoryItems != null) {
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
                        deployedItem.setUser(entry.getUser());
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
        }
        return groups;
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_PUBLISH)
    public void publishAll(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String publishingTarget, String comment)
            throws ServiceLayerException, UserNotFoundException {
        siteService.checkSiteExists(siteId);

        publishServiceInternal.publishAll(siteId, publishingTarget, comment);

        SiteFeed siteFeed = siteService.getSite(siteId);
        String username = securityService.getCurrentUser();
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_PUBLISH_ALL);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(username);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        auditServiceInternal.insertAuditLog(auditLog);

        activityStreamServiceInternal.insertActivity(siteFeed.getId(), user.getId(), OPERATION_PUBLISH_ALL,
                getCurrentTime(), null, null);
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
    @HasAnyPermissions(type = DefaultPermission.class, actions = {PERMISSION_PUBLISH, PERMISSION_CONTENT_READ})
    public List<PublishingTarget> getAvailablePublishingTargets(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
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
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public boolean isSitePublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        return publishServiceInternal.isSitePublished(siteId);
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setStudioUtils(StudioUtils studioUtils) {
        this.studioUtils = studioUtils;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }
}
