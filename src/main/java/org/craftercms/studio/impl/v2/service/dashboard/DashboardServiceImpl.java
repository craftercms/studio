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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.PublishRequest;
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
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.dashboard.Activity;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentItem;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentResult;
import org.craftercms.studio.model.rest.dashboard.PublishingStats;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_QUERY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRED_SORT_BY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DASHBOARD_CONTENT_EXPIRING_QUERY;
import static org.craftercms.studio.impl.v2.utils.DateUtils.ISO_FORMATTER;
import static org.craftercms.studio.impl.v2.utils.DateUtils.parseDateIso;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

public class DashboardServiceImpl implements DashboardService {

    private final ActivityStreamServiceInternal activityStreamServiceInternal;
    private final PublishServiceInternal publishServiceInternal;
    private final ContentServiceInternal contentServiceInternal;
    private final SecurityService securityService;
    private final WorkflowServiceInternal workflowServiceInternal;
    private final ItemServiceInternal itemServiceInternal;
    private final SearchService searchService;
    private final StudioConfiguration studioConfiguration;
    private final SiteService siteService;

    private static final String ALL_CONTENT_REGEX = ".*";
    private static final String DATE_FROM_REGEX = "\\{dateFrom\\}";
    private static final String DATE_TO_REGEX = "\\{dateTo\\}";

    @ConstructorProperties({"activityStreamServiceInternal", "publishServiceInternal", "contentServiceInternal",
            "securityService", "workflowServiceInternal", "itemServiceInternal", "searchService", "studioConfiguration", "siteService"})
    public DashboardServiceImpl(final ActivityStreamServiceInternal activityStreamServiceInternal, final PublishServiceInternal publishServiceInternal,
                                final ContentServiceInternal contentServiceInternal, final SecurityService securityService,
                                final WorkflowServiceInternal workflowServiceInternal, final ItemServiceInternal itemServiceInternal,
                                final SearchService searchService, final StudioConfiguration studioConfiguration,
                                final SiteService siteService) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
        this.publishServiceInternal = publishServiceInternal;
        this.contentServiceInternal = contentServiceInternal;
        this.securityService = securityService;
        this.workflowServiceInternal = workflowServiceInternal;
        this.itemServiceInternal = itemServiceInternal;
        this.searchService = searchService;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getActivitiesForUsersTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<String> usernames, List<String> actions,
                                          ZonedDateTime dateFrom, ZonedDateTime dateTo) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return activityStreamServiceInternal.getActivitiesForUsersTotal(siteId, usernames, actions, dateFrom, dateTo);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<Activity> getActivitiesForUsers(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<String> usernames, List<String> actions,
                                                ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return activityStreamServiceInternal
                .getActivitiesForUsers(siteId, usernames, actions, dateFrom, dateTo, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getMyActivitiesTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<String> actions,
                                    ZonedDateTime dateFrom, ZonedDateTime dateTo) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        var username = securityService.getCurrentUser();
        return activityStreamServiceInternal
                .getActivitiesForUsersTotal(siteId, List.of(username), actions, dateFrom, dateTo);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<Activity> getMyActivities(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, List<String> actions, ZonedDateTime dateFrom,
                                          ZonedDateTime dateTo, int offset, int limit) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        var username = securityService.getCurrentUser();
        return activityStreamServiceInternal
                .getActivitiesForUsers(siteId, List.of(username), actions, dateFrom, dateTo, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getContentPendingApprovalTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return itemServiceInternal.getItemStatesTotal(siteId, ALL_CONTENT_REGEX, SUBMITTED_MASK);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<DetailedItem> getContentPendingApproval(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, int offset, int limit) throws ServiceLayerException, UserNotFoundException {
        siteService.checkSiteExists(siteId);
        var items =
                itemServiceInternal.getItemStates(siteId, ALL_CONTENT_REGEX, SUBMITTED_MASK, offset, limit);
        if (items.isEmpty()) {
            return emptyList();
        }

        var paths = items.stream().map(Item::getPath).collect(toList());
        List<DetailedItem> result = new ArrayList<>();
        for (String path : paths) {
            var item = contentServiceInternal.getItemByPath(siteId, path, false);
            result.add(item);
        }

        return result;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getContentPendingApprovalDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                             String publishingPackageId)
            throws UserNotFoundException, ServiceLayerException {
        siteService.checkSiteExists(siteId);
        var workflowEntries = workflowServiceInternal.getContentPendingApprovalDetail(siteId, publishingPackageId);
        if (isEmpty(workflowEntries)) {
            throw new PublishingPackageNotFoundException(siteId, publishingPackageId);
        }
        var ids = workflowEntries.stream()
                .map(Workflow::getItemId)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsById(siteId, ids, true);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getContentUnpublishedTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return itemServiceInternal.getItemStatesTotal(siteId, ALL_CONTENT_REGEX, UNPUBLISHED_MASK);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getContentUnpublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                   int offset, int limit)
            throws UserNotFoundException, ServiceLayerException {
        siteService.checkSiteExists(siteId);
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
    public ExpiringContentResult getContentExpiring(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                    ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                    int offset, int limit)
            throws AuthenticationException, ServiceLayerException, UserNotFoundException {
        siteService.checkSiteExists(siteId);
        SearchParams searchParams = new SearchParams();
        String query = getContentExpiringQuery()
                .replaceAll(DATE_FROM_REGEX, DateUtils.formatDate(dateFrom, ISO_FORMATTER))
                .replaceAll(DATE_TO_REGEX, DateUtils.formatDate(dateTo, ISO_FORMATTER));
        prepareSearchParams(searchParams, query, Asc.jsonValue(), offset, limit);
        SearchResult result = searchService.search(siteId, searchParams);
        return processResults(siteId, result);
    }

    @Override
    public ExpiringContentResult getContentExpired(String siteId, int offset, int limit)
            throws AuthenticationException, ServiceLayerException, UserNotFoundException {
        siteService.checkSiteExists(siteId);
        SearchParams searchParams = new SearchParams();
        String query = getContentExpiredQuery();
        prepareSearchParams(searchParams, query, Desc.jsonValue(), offset, limit);
        SearchResult result = searchService.search(siteId, searchParams);
        return processResults(siteId, result);
    }

    protected ExpiringContentResult processResults(String siteId, SearchResult results) throws ServiceLayerException, UserNotFoundException {
        List<ExpiringContentItem> items = new ArrayList<>();
        for (var item : results.getItems()) {
            SandboxItem  sandboxItem =
                    contentServiceInternal.getSandboxItemsByPath(siteId, Arrays.asList(item.getPath()), false)
                    .stream()
                    .findFirst().orElse(null);
            ExpiringContentItem contentItem = new ExpiringContentItem(
                    item.getName(),
                    item.getPath(),
                    parseDateIso((String) item.getAdditionalFields().get(getExpireFieldName())),
                    sandboxItem
            );
            items.add(contentItem);
        }
        return new ExpiringContentResult(items, results.getTotal());
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getPublishingScheduledTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                           String publishingTarget, String approver,
                                           ZonedDateTime dateFrom, ZonedDateTime dateTo) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return publishServiceInternal.getPublishingItemsScheduledTotal(siteId, publishingTarget, approver, dateFrom, dateTo);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<DetailedItem> getPublishingScheduled(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String publishingTarget,
            String approver, ZonedDateTime dateFrom, ZonedDateTime dateTo,
            int offset, int limit) throws ServiceLayerException, UserNotFoundException {
        siteService.checkSiteExists(siteId);
        var items =
                publishServiceInternal.getPublishingItemsScheduled(siteId, publishingTarget, approver, dateFrom, dateTo, offset, limit);
        if (items.isEmpty()) {
            return emptyList();
        }

        var paths = items.stream().map(PublishRequest::getPath).collect(toList());
        List<DetailedItem> result = new ArrayList<>();
        for (String path : paths) {
            var item = contentServiceInternal.getItemByPath(siteId, path, false);
            result.add(item);
        }

        return result;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getPublishingScheduledDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                        String publishingPackageId)
            throws UserNotFoundException, ServiceLayerException {
        siteService.checkSiteExists(siteId);
        var publishingPackageDetails =
                publishServiceInternal.getPublishingPackageDetails(siteId, publishingPackageId);
        if (isEmpty(publishingPackageDetails.getItems())) {
            throw new PublishingPackageNotFoundException(siteId, publishingPackageId);
        }
        var paths = publishingPackageDetails.getItems().stream()
                .map(PublishingPackageDetails.PublishingPackageItem::getPath)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, true);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getPublishingHistoryTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         String publishingTarget, String approver, ZonedDateTime dateFrom,
                                         ZonedDateTime dateTo) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return publishServiceInternal.getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, dateFrom,
                dateTo);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<DashboardPublishingPackage> getPublishingHistory(
            @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String publishingTarget, String approver,
            ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return publishServiceInternal.getPublishingPackagesHistory(siteId, publishingTarget, approver, dateFrom,
                dateTo, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public int getPublishingHistoryDetailTotalItems(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                    String publishingPackageId) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
        return publishServiceInternal.getPublishingHistoryDetailTotalItems(siteId, publishingPackageId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getPublishingHistoryDetail(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                        String publishingPackageId, int offset, int limit)
            throws UserNotFoundException, ServiceLayerException {
        siteService.checkSiteExists(siteId);
        var packageDetails = publishServiceInternal.getPublishingHistoryDetail(siteId, publishingPackageId, offset, limit);
        if (isEmpty(packageDetails)) {
            throw new PublishingPackageNotFoundException(siteId, publishingPackageId);
        }

        var paths = packageDetails.stream()
                .map(PublishRequest::getPath)
                .collect(toList());
        return contentServiceInternal.getSandboxItemsByPath(siteId, paths, true);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public PublishingStats getPublishingStats(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, int days) throws SiteNotFoundException {
        siteService.checkSiteExists(siteId);
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
}
