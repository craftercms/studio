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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.dashboard.Activity;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.model.rest.dashboard.ExpiringContentItem;
import org.craftercms.studio.model.rest.dashboard.PublishingStats;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_APPROVER;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DATE_FROM;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DATE_TO;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DAYS;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PUBLISHING_TARGET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_USERNAMES;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ACTIVITY;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.EXPIRED;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.EXPIRING;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.HISTORY;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ME;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PATH_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PENDING_APPROVAL;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PUBLISHING;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.SCHEDULED;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.STATS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.UNPUBLISHED;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ACTIVITIES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PUBLISHING_PACKAGES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PUBLISHING_PACKAGE_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PUBLISHING_STATS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_UNPUBLISHED_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_2 + DASHBOARD)
public class DashboardController {

    private DashboardService dashboardService;

    @GetMapping(value = ACTIVITY, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getActivitiesForUsers(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_USERNAMES, required = false) List<String> usernames,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(required = false) List<String> actions,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) {

        var total = dashboardService.getActivitiesForUsersTotal(siteId, usernames, actions, dateFrom, dateTo);
        var activities =
                dashboardService.getActivitiesForUsers(siteId, usernames, actions, dateFrom, dateTo, offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<Activity>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(activities) ? activities.size() : 0);
        result.setEntities(RESULT_KEY_ACTIVITIES, activities);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = ACTIVITY + ME, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getMyActivities(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(required = false) List<String> actions,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) {

        var total = dashboardService.getMyActivitiesTotal(siteId, actions, dateFrom, dateTo);
        var activities =
                dashboardService.getMyActivities(siteId, actions, dateFrom, dateTo, offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<Activity>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(activities) ? activities.size() : 0);
        result.setEntities(RESULT_KEY_ACTIVITIES, activities);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = CONTENT + PENDING_APPROVAL, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getContentPendingApproval(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) {

        var total = dashboardService.getContentPendingApprovalTotal(siteId);
        var packages = dashboardService.getContentPendingApproval(siteId, offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<DashboardPublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(packages) ? packages.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGES, packages);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = CONTENT + PENDING_APPROVAL + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getContentPendingApprovalDetail(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) String packageId) throws UserNotFoundException, ServiceLayerException {

        var items = dashboardService.getContentPendingApprovalDetail(siteId, packageId);
        var response = new ResponseBody();
        var result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value =  CONTENT + UNPUBLISHED, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getContentUnpublished(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) throws UserNotFoundException, ServiceLayerException {

        var total = dashboardService.getContentUnpublishedTotal(siteId);
        var unpublishedContent = dashboardService.getContentUnpublished(siteId, offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<SandboxItem>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(unpublishedContent) ? unpublishedContent.size() : 0);
        result.setEntities(RESULT_KEY_UNPUBLISHED_ITEMS, unpublishedContent);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }


    @GetMapping(value = CONTENT + EXPIRING, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getContentExpiring(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException {

        var contentExpiring = dashboardService.getContentExpiring(siteId, dateFrom, dateTo, offset,
                limit);
        var responseBody = new ResponseBody();
        var result = new PaginatedResultList<ExpiringContentItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, contentExpiring.getItems());
        result.setTotal(contentExpiring.getTotal());
        result.setLimit(limit);
        result.setOffset(offset);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = CONTENT + EXPIRED, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getContentExpired(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException {

        var contentExpired = dashboardService.getContentExpired(siteId, offset, limit);
        var responseBody = new ResponseBody();
        var result = new PaginatedResultList<ExpiringContentItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, contentExpired.getItems());
        result.setTotal(contentExpired.getTotal());
        result.setLimit(limit);
        result.setOffset(offset);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = PUBLISHING + SCHEDULED, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingScheduled(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_PUBLISHING_TARGET, required = false) String publishingTarget,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) {

        var total = dashboardService.getPublishingScheduledTotal(siteId, publishingTarget, dateFrom, dateTo);
        var packages = dashboardService.getPublishingScheduled(siteId, publishingTarget,
                dateFrom, dateTo, offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<DashboardPublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(packages) ? packages.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGES, packages);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = PUBLISHING + SCHEDULED + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingScheduledDetail(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) String packageId)
            throws UserNotFoundException, ServiceLayerException {

        var items = dashboardService.getPublishingScheduledDetail(siteId, packageId);
        var response = new ResponseBody();
        var result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = PUBLISHING + HISTORY, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingHistory(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_PUBLISHING_TARGET, required = false) String publishingTarget,
            @RequestParam(value = REQUEST_PARAM_APPROVER, required = false) String approver,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit) {

        var total = dashboardService.getPublishingHistoryTotal(siteId, publishingTarget, approver, dateFrom,
                dateTo);
        var packages = dashboardService.getPublishingHistory(siteId, publishingTarget, approver, dateFrom, dateTo,
                offset, limit);

        var response = new ResponseBody();
        var result = new PaginatedResultList<DashboardPublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isNotEmpty(packages) ? packages.size() : 0);
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGES, packages);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = PUBLISHING + HISTORY + PATH_PARAM_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingHistoryDetail(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @PathVariable(REQUEST_PARAM_ID) String packageId) throws UserNotFoundException, ServiceLayerException {

        var items = dashboardService.getPublishingHistoryDetail(siteId, packageId);
        var response = new ResponseBody();
        var result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_PUBLISHING_PACKAGE_ITEMS, items);
        result.setResponse(OK);
        response.setResult(result);
        return response;
    }

    @GetMapping(value = PUBLISHING + STATS, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingStats(
            @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
            @RequestParam(value = REQUEST_PARAM_DAYS) int days) {

        var publishingStats = dashboardService.getPublishingStats(siteId, days);
        var responseBody = new ResponseBody();
        var result = new ResultOne<PublishingStats>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PUBLISHING_STATS, publishingStats);
        responseBody.setResult(result);
        return responseBody;
    }

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
}
