/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.commons.validation.annotations.param.*;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.dashboard.Activity;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentItem;
import org.craftercms.studio.model.rest.dashboard.PublishingStats;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.USERNAME;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.*;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(API_2 + DASHBOARD)
public class DashboardController {

    private final DashboardService dashboardService;

    @ConstructorProperties({"dashboardService"})
    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Valid
    @GetMapping(value = ACTIVITY, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<Activity> getActivitiesForUsers(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_USERNAMES, required = false) List<@NotBlank @EsapiValidatedParam(type = USERNAME) String> usernames,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(required = false) List<@NotBlank @ValidateNoTagsParam String> actions,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) throws SiteNotFoundException {
        var total = dashboardService.getActivitiesForUsersTotal(siteId, usernames, actions, dateFrom, dateTo);
        var activities =
                dashboardService.getActivitiesForUsers(siteId, usernames, actions, dateFrom, dateTo, offset, limit);

        var result = new PaginatedResultList<Activity>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(activities) ? activities.size() : 0);
        result.setEntities(RESULT_KEY_ACTIVITIES, activities);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = ACTIVITY + ME, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<Activity> getMyActivities(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(required = false) List<@NotBlank @ValidateNoTagsParam String> actions,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) throws SiteNotFoundException {

        var total = dashboardService.getMyActivitiesTotal(siteId, actions, dateFrom, dateTo);
        var activities =
                dashboardService.getMyActivities(siteId, actions, dateFrom, dateTo, offset, limit);

        var result = new PaginatedResultList<Activity>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(activities) ? activities.size() : 0);
        result.setEntities(RESULT_KEY_ACTIVITIES, activities);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = CONTENT + PENDING_APPROVAL, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<DetailedItem> getContentPendingApproval(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = "dateScheduled asc")
            List<@SqlSort(columns = SCHEDULED_ITEM_SORT_FIELDS) SortField> sortFields,
            @RequestParam(value = REQUEST_PARAM_ITEM_TYPE, required = false, defaultValue = "")
            List<@ValidateStringParam(whitelistedPatterns = ITEM_TYPE_VALUES) String> systemTypes) throws ServiceLayerException, UserNotFoundException {
        var total = dashboardService.getContentPendingApprovalTotal(siteId, systemTypes);
        var publishingContent = dashboardService.getContentPendingApproval(siteId, systemTypes, sortFields, offset, limit);

        var result = new PaginatedResultList<DetailedItem>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(publishingContent) ? publishingContent.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_ITEMS, publishingContent);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = CONTENT + PENDING_APPROVAL + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public ResultList<SandboxItem> getContentPendingApprovalDetail(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) UUID packageId,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = "dateModified desc")
            List<@SqlSort(columns = ITEM_SORT_FIELDS) SortField> sortFields) throws UserNotFoundException, ServiceLayerException {
        var items = dashboardService.getContentPendingApprovalDetail(siteId, packageId.toString(), sortFields);
        var result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = CONTENT + UNPUBLISHED, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<SandboxItem> getContentUnpublished(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = "dateModified desc")
            List<@SqlSort(columns = ITEM_SORT_FIELDS) SortField> sortFields,
            @RequestParam(value = REQUEST_PARAM_ITEM_TYPE, required = false, defaultValue = "")
            List<@ValidateStringParam(whitelistedPatterns = ITEM_TYPE_VALUES) String> systemTypes) throws UserNotFoundException, ServiceLayerException {
        var total = dashboardService.getContentUnpublishedTotal(siteId, systemTypes);
        var unpublishedContent = dashboardService.getContentUnpublished(siteId, systemTypes, sortFields, offset, limit);

        var result = new PaginatedResultList<SandboxItem>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(unpublishedContent) ? unpublishedContent.size() : 0);
        result.setEntities(RESULT_KEY_UNPUBLISHED_ITEMS, unpublishedContent);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = CONTENT + EXPIRING, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<ExpiringContentItem> getContentExpiring(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException, UserNotFoundException {

        var contentExpiring = dashboardService.getContentExpiring(siteId, dateFrom, dateTo, offset,
                limit);
        var result = new PaginatedResultList<ExpiringContentItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, contentExpiring.getItems());
        result.setTotal(contentExpiring.getTotal());
        result.setLimit(limit);
        result.setOffset(offset);
        return result;
    }

    @Valid
    @GetMapping(value = CONTENT + EXPIRED, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<ExpiringContentItem> getContentExpired(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException, UserNotFoundException {

        var contentExpired = dashboardService.getContentExpired(siteId, offset, limit);
        var result = new PaginatedResultList<ExpiringContentItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, contentExpired.getItems());
        result.setTotal(contentExpired.getTotal());
        result.setLimit(limit);
        result.setOffset(offset);
        return result;
    }

    @Valid
    @GetMapping(value = PUBLISHING + SCHEDULED, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<DetailedItem> getPublishingScheduled(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @EsapiValidatedParam(type = ALPHANUMERIC)
            @RequestParam(value = REQUEST_PARAM_PUBLISHING_TARGET, required = false) String publishingTarget,
            @EsapiValidatedParam(type = USERNAME)
            @RequestParam(value = REQUEST_PARAM_APPROVER, required = false) String approver,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = "dateScheduled asc")
            List<@SqlSort(columns = PUBLISH_REQUEST_SORT_FIELDS) SortField> sortFields,
            @RequestParam(value = REQUEST_PARAM_ITEM_TYPE, required = false, defaultValue = "")
            List<@ValidateStringParam(whitelistedPatterns = ITEM_TYPE_VALUES) String> systemTypes)
            throws ServiceLayerException, UserNotFoundException {
        var total = dashboardService.getPublishingScheduledTotal(siteId, publishingTarget, approver, dateFrom, dateTo, systemTypes);
        var scheduledItems = dashboardService.getPublishingScheduled(siteId, publishingTarget, approver, dateFrom, dateTo, systemTypes, sortFields, offset, limit);

        var result = new PaginatedResultList<DetailedItem>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(scheduledItems) ? scheduledItems.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_ITEMS, scheduledItems);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = PUBLISHING + SCHEDULED + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public ResultList<SandboxItem> getPublishingScheduledDetail(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) UUID packageId)
            throws UserNotFoundException, ServiceLayerException {
        var items = dashboardService.getPublishingScheduledDetail(siteId, packageId.toString());
        var result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = PUBLISHING + HISTORY, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<DashboardPublishingPackage> getPublishingHistory(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @EsapiValidatedParam(type = ALPHANUMERIC)
            @RequestParam(value = REQUEST_PARAM_PUBLISHING_TARGET, required = false) String publishingTarget,
            @EsapiValidatedParam(type = USERNAME)
            @RequestParam(value = REQUEST_PARAM_APPROVER, required = false) String approver,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) throws SiteNotFoundException {
        int total = dashboardService.getPublishingHistoryTotal(siteId, publishingTarget, approver, dateFrom,
                dateTo);
        var packages = dashboardService.getPublishingHistory(siteId, publishingTarget, approver, dateFrom, dateTo,
                offset, limit);

        var result = new PaginatedResultList<DashboardPublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(packages) ? packages.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGES, packages);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = PUBLISHING + HISTORY + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<SandboxItem> getPublishingHistoryDetail(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) UUID packageId,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
        throws UserNotFoundException, ServiceLayerException {
        int total = dashboardService.getPublishingHistoryDetailTotalItems(siteId, packageId.toString());
        var items = dashboardService.getPublishingHistoryDetail(siteId, packageId.toString(), offset, limit);

        var result = new PaginatedResultList<SandboxItem>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(items) ? items.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        return result;
    }

    @Valid
    @GetMapping(value = PUBLISHING + STATS, produces = APPLICATION_JSON_VALUE)
    public ResultOne<PublishingStats> getPublishingStats(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DAYS) int days) throws SiteNotFoundException {
        var publishingStats = dashboardService.getPublishingStats(siteId, days);
        var result = new ResultOne<PublishingStats>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PUBLISHING_STATS, publishingStats);
        return result;
    }

}
