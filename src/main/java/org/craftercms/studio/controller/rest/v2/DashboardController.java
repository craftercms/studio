/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.dashboard.AuditDashboardItem;
import org.craftercms.studio.model.rest.dashboard.AuditDashboardRequestParameters;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardRequestParameters;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardRequestParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.AUDIT_DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CONTENT_DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PUBLISHING_DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
@RequestMapping(API_2 + DASHBOARD)
public class DashboardController {

    private PublishService publishService;
    private DashboardService dashboardService;

    @GetMapping(AUDIT_DASHBOARD)
    public ResponseBody getAuditDashboard(AuditDashboardRequestParameters requestParameters) {

        String user = null;
        List<String> operations = null;
        ZonedDateTime dateFrom = null;
        ZonedDateTime dateTo = null;
        String target = null;

        if (requestParameters.getFilters() != null) {
            user = requestParameters.getFilters().getActor();
            operations = requestParameters.getFilters().getOperations();
            dateFrom = requestParameters.getFilters().getOperationTimestampFrom();
            dateTo = requestParameters.getFilters().getOperationTimestampTo();
            target = requestParameters.getFilters().getTarget();
        }

        int total = dashboardService.getAuditDashboardTotal(requestParameters.getSiteId(), user, operations, dateFrom,
                dateTo, target);

        List<AuditLog> auditLog = dashboardService.getAuditDashboard(requestParameters.getSiteId(),
                requestParameters.getOffset(), requestParameters.getLimit(), user, operations, dateFrom, dateTo, target,
                requestParameters.getSortBy(), requestParameters.getOrder());

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<AuditDashboardItem> result = new PaginatedResultList<AuditDashboardItem>();
        result.setTotal(total);
        result.setLimit(CollectionUtils.isEmpty(auditLog) ? 0 : auditLog.size());
        result.setOffset(requestParameters.getOffset());
        result.setEntities(RESULT_KEY_ITEMS, prepareAuditResult(auditLog));
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    private List<AuditDashboardItem> prepareAuditResult(List<AuditLog> auditLogs) {
        List<AuditDashboardItem> resultItems = new ArrayList<AuditDashboardItem>();
        for (AuditLog auditLog : auditLogs) {
            AuditDashboardItem item = new AuditDashboardItem();
            item.setSiteId(auditLog.getSiteName());
            item.setActor(auditLog.getActorId());
            item.setOperation(auditLog.getOperation());
            item.setOperationTimestamp(auditLog.getOperationTimestamp());
            item.setTarget(auditLog.getPrimaryTargetValue());
            resultItems.add(item);
        }
        return resultItems;
    }

    @GetMapping(PUBLISHING_DASHBOARD)
    public ResponseBody getPublishingDashboard(PublishingDashboardRequestParameters requestParameters) {

        String user = null;
        String environment = null;
        String path = null;
        ZonedDateTime dateFrom = null;
        ZonedDateTime dateTo = null;
        String contentType = null;
        long state = 0;

        if (requestParameters.getFilters() != null) {
            user = requestParameters.getFilters().getPublisher();
            path = requestParameters.getFilters().getPath();
            environment = requestParameters.getFilters().getEnvironment();
            dateFrom = requestParameters.getFilters().getPublishedDateFrom();
            dateTo = requestParameters.getFilters().getPublishedDateTo();
            contentType = requestParameters.getFilters().getContentType();
        }

        int total = 0;
        List<PublishingDashboardItem> publishingHistory =
                publishService.getPublishingHistory(requestParameters.getSiteId(), environment, path, user,
                        dateFrom, dateTo, contentType, state, requestParameters.getSortBy(),
                        requestParameters.getOrder(), requestParameters.getGroupBy(),
                        requestParameters.getOffset(), requestParameters.getLimit());

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<PublishingDashboardItem> result = new PaginatedResultList<PublishingDashboardItem>();
        result.setTotal(total);
        result.setLimit(CollectionUtils.isEmpty(publishingHistory) ? 0 : publishingHistory.size());
        result.setOffset(requestParameters.getOffset());
        result.setEntities(RESULT_KEY_ITEMS, publishingHistory);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(CONTENT_DASHBOARD)
    public ResponseBody getContentDashboard(ContentDashboardRequestParameters requestParameters) {
        String modifier = null;
        String path = null;
        ZonedDateTime dateFrom = null;
        ZonedDateTime dateTo = null;
        String contentType = null;
        long state = 0;

        if (requestParameters.getFilters() != null) {
            modifier = requestParameters.getFilters().getModifier();
            path = requestParameters.getFilters().getPath();
            dateFrom = requestParameters.getFilters().getModifiedDateFrom();
            dateTo = requestParameters.getFilters().getModifiedDateTo();
            contentType = requestParameters.getFilters().getContentType();
            state = requestParameters.getFilters().getState();
        }

        int total = dashboardService.getContentDashboardTotal(requestParameters.getSiteId(), path, modifier, contentType,
                state, dateFrom, dateTo);
        List<ContentDashboardItem> contentDashboardItems =
                dashboardService.getContentDashboard(requestParameters.getSiteId(), path, modifier, contentType,
                        state, dateFrom, dateTo, requestParameters.getSortBy(), requestParameters.getOrder(),
                        requestParameters.getGroupBy(), requestParameters.getOffset(), requestParameters.getLimit());

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<ContentDashboardItem> result = new PaginatedResultList<ContentDashboardItem>();
        result.setTotal(total);
        result.setLimit(CollectionUtils.isEmpty(contentDashboardItems) ? 0 : contentDashboardItems.size());
        result.setOffset(requestParameters.getOffset());
        result.setEntities(RESULT_KEY_ITEMS, contentDashboardItems);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public PublishService getPublishService() {
        return publishService;
    }

    public void setPublishService(PublishService publishService) {
        this.publishService = publishService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
}
