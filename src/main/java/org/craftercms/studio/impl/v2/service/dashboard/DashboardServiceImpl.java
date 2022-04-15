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

package org.craftercms.studio.impl.v2.service.dashboard;


import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.exception.PublishingPackageNotFoundException;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.api.v2.service.search.SearchService;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.dashboard.Activity;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentItem;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentResult;
import org.craftercms.studio.model.rest.dashboard.PublishingStats;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;

import java.time.ZonedDateTime;
import java.util.List;

import static co.elastic.clients.elasticsearch._types.SortOrder.Asc;
import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v1.dal.PublishRequest.Action.NEW;
import static org.craftercms.studio.api.v1.dal.PublishRequest.Action.UPDATE;
import static org.craftercms.studio.api.v1.dal.PublishRequest.State.COMPLETED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.ItemState.UNPUBLISHED_MASK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_QUERY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_SORT_BY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRING_QUERY;
import static org.craftercms.studio.impl.v2.utils.DateUtils.ISO_FORMATTER;
import static org.craftercms.studio.impl.v2.utils.DateUtils.parseDateIso;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DashboardServiceImpl implements DashboardService {

    private ActivityStreamServiceInternal activityStreamServiceInternal;
    private PublishServiceInternal publishServiceInternal;
    private ContentServiceInternal contentServiceInternal;
    private SecurityService securityService;
    private WorkflowServiceInternal workflowServiceInternal;
    private ItemServiceInternal itemServiceInternal;
    private SearchService searchService;
    private StudioConfiguration studioConfiguration;

    private static final String ALL_CONTENT_REGEX = ".*";
    private static final String DATE_FROM_REGEX = "\\{dateFrom\\}";
    private static final String DATE_TO_REGEX = "\\{dateTo\\}";

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getActivitiesForUsersTotal(String siteId, List<String> usernames, List<String> actions,
                                          ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return activityStreamServiceInternal.getActivitiesForUsersTotal(siteId, usernames, actions, dateFrom, dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<Activity> getActivitiesForUsers(String siteId, List<String> usernames, List<String> actions,
                                                ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit) {
        return activityStreamServiceInternal
                .getActivitiesForUsers(siteId, usernames, actions, dateFrom, dateTo, offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getMyActivitiesTotal(String siteId, List<String> actions, ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        var username = securityService.getCurrentUser();
        return activityStreamServiceInternal
                .getActivitiesForUsersTotal(siteId, List.of(username), actions, dateFrom, dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<Activity> getMyActivities(String siteId, List<String> actions, ZonedDateTime dateFrom,
                                          ZonedDateTime dateTo, int offset, int limit) {
        var username = securityService.getCurrentUser();
        return activityStreamServiceInternal
                .getActivitiesForUsers(siteId, List.of(username), actions, dateFrom, dateTo, offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getContentPendingApprovalTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        return workflowServiceInternal.getContentPendingApprovalTotal(siteId);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<DashboardPublishingPackage> getContentPendingApproval(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, int offset, int limit) {
        return workflowServiceInternal.getContentPendingApproval(siteId, offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getContentPendingApprovalDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                             String publishingPackageId)
            throws UserNotFoundException, ServiceLayerException {
        var workflowEntries = workflowServiceInternal.getContentPendingApprovalDetail(siteId, publishingPackageId);
        var ids = workflowEntries.stream()
                .map(Workflow::getItemId)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsById(siteId, ids, true);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getContentUnpublishedTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        return itemServiceInternal.getItemStatesTotal(siteId, ALL_CONTENT_REGEX, UNPUBLISHED_MASK);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getContentUnpublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                   int offset, int limit)
            throws UserNotFoundException, ServiceLayerException {
        var items =
                itemServiceInternal.getItemStates(siteId, ALL_CONTENT_REGEX, UNPUBLISHED_MASK, offset, limit);
        if (items.isEmpty()) {
            return emptyList();
        }
        var ids = items.stream().map(Item::getId)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsById(siteId, ids, false);
    }

    protected void prepareSearchParams(SearchParams searchParams, String query, String order, int offset, int limit) {
        searchParams.setQuery(query);
        searchParams.setAdditionalFields(List.of(getExpireFieldName()));
        searchParams.setSortBy(getExpireFieldName());
        searchParams.setSortOrder(order);
        searchParams.setOffset(offset);
        searchParams.setLimit(limit);
    }

    @Override
    public ExpiringContentResult getContentExpiring(String siteId, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                    int offset, int limit)
            throws AuthenticationException, ServiceLayerException {
        SearchParams searchParams = new SearchParams();
        String query = getContentExpiringQuery()
                .replaceAll(DATE_FROM_REGEX, DateUtils.formatDate(dateFrom, ISO_FORMATTER))
                .replaceAll(DATE_TO_REGEX, DateUtils.formatDate(dateTo, ISO_FORMATTER));
        prepareSearchParams(searchParams, query, Asc.jsonValue(), offset, limit);
        SearchResult result = searchService.search(siteId, searchParams);
        return processResults(result);
    }

    @Override
    public ExpiringContentResult getContentExpired(String siteId, int offset, int limit)
            throws AuthenticationException, ServiceLayerException {
        SearchParams searchParams = new SearchParams();
        String query = getContentExpiredQuery();
        prepareSearchParams(searchParams, query, Desc.jsonValue(), offset, limit);
        SearchResult result = searchService.search(siteId, searchParams);
        return processResults(result);
    }

    protected ExpiringContentResult processResults(SearchResult results) {
        List<ExpiringContentItem> items = results.getItems().stream()
                .map(result -> new ExpiringContentItem(result.getName(), result.getPath(),
                                    parseDateIso((String) result.getAdditionalFields().get(getExpireFieldName()))))
                .collect(toList());
        return new ExpiringContentResult(items, results.getTotal());
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getPublishingScheduledTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                           String publishingTarget, ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return publishServiceInternal.getPublishingPackagesScheduledTotal(siteId, publishingTarget, dateFrom, dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<DashboardPublishingPackage> getPublishingScheduled(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String publishingTarget, ZonedDateTime dateFrom,
            ZonedDateTime dateTo, int offset, int limit) {
        return publishServiceInternal.getPublishingPackagesScheduled(siteId, publishingTarget, dateFrom, dateTo,
                offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getPublishingScheduledDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                        String publishingPackageId)
            throws UserNotFoundException, ServiceLayerException {
        var publishingPackageDetails =
                publishServiceInternal.getPublishingPackageDetails(siteId, publishingPackageId);
        var paths = publishingPackageDetails.getItems().stream()
                .map(PublishingPackageDetails.PublishingPackageItem::getPath)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, true);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public int getPublishingHistoryTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         String publishingTarget, String approver, ZonedDateTime dateFrom,
                                         ZonedDateTime dateTo) {
        return publishServiceInternal.getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, dateFrom,
                dateTo);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<DashboardPublishingPackage> getPublishingHistory(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String publishingTarget, String approver,
            ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit) {
        return publishServiceInternal.getPublishingPackagesHistory(siteId, publishingTarget, approver, dateFrom,
                dateTo, offset, limit);
    }

    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    @Override
    public List<SandboxItem> getPublishingHistoryDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                        String publishingPackageId)
            throws UserNotFoundException, ServiceLayerException {
        // TODO: Try to do a single query
        var publishingPackageDetails =
                publishServiceInternal.getPublishingPackageDetails(siteId, publishingPackageId);
        var paths = publishingPackageDetails.getItems().stream()
                .map(PublishingPackageDetails.PublishingPackageItem::getPath)
                .collect(toList());
        if (isEmpty(paths)) {
            throw new PublishingPackageNotFoundException(siteId, publishingPackageId);
        }
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, true);
    }

    @Override
    public PublishingStats getPublishingStats(String siteId, int days) {
        var publishingStats = new PublishingStats();
        publishingStats.setNumberOfPublishes(publishServiceInternal.getNumberOfPublishes(siteId, days));
        publishingStats.setNumberOfNewAndPublishedItems(
                publishServiceInternal.getNumberOfPublishedItemsByState(siteId, days, OPERATION_CREATE, COMPLETED, NEW));
        publishingStats.setNumberOfEditedAndPublishedItems(
                publishServiceInternal.getNumberOfPublishedItemsByState(siteId, days, OPERATION_UPDATE, COMPLETED, UPDATE));
        return publishingStats;
    }

    private String getContentExpiringQuery() {
        return studioConfiguration.getProperty(CONFIGURATION_DASHBOARD_CONTENT_EXPIRING_QUERY);
    }

    private String getContentExpiredQuery() {
        return studioConfiguration.getProperty(CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_QUERY);
    }

    private String getExpireFieldName() {
        return studioConfiguration.getProperty(CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_SORT_BY);
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
