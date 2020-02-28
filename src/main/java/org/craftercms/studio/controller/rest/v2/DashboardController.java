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

import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.dashboard.AuditDashboardItem;
import org.craftercms.studio.model.rest.dashboard.AuditDashboardRequestParameters;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.AUDIT_DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DASHBOARD;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
@RequestMapping(API_2 + DASHBOARD)
public class DashboardController {

    private AuditService auditService;

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

        int total = auditService.getAuditLogTotal(null, requestParameters.getSiteId(), user, operations, false, dateFrom,
                dateTo, target, null, null);

        List<AuditLog> auditLog = auditService.getAuditLog(null, requestParameters.getSiteId(),
                requestParameters.getOffset(), requestParameters.getLimit(), user, operations, false, dateFrom,
                dateTo, target, null, null, requestParameters.getSortBy(), requestParameters.getOrder());

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

    public AuditService getAuditService() {
        return auditService;
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }
}
