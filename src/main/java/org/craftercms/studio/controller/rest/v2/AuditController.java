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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_CLUSTER_NODE_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DATE_FROM;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DATE_TO;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_INCLUDE_PARAMETERS;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OPERATIONS;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ORDER;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ORIGIN;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITE_NAME;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SORT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_TARGET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_USER;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.AUDIT;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PATH_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_AUDIT_LOG;

@RestController
public class AuditController {

    private AuditService auditService;
    private SiteService siteService;

    @GetMapping(API_2 + AUDIT)
    public ResponseBody getAuditLog(
            @RequestParam(value = REQUEST_PARAM_SITEID, required = false) String siteId,
            @RequestParam(value = REQUEST_PARAM_SITE_NAME, required = false) String siteName,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_USER, required = false, defaultValue = "") String user,
            @RequestParam(value = REQUEST_PARAM_OPERATIONS, required = false) List<String> operations,
            @RequestParam(value = REQUEST_PARAM_INCLUDE_PARAMETERS, required = false) boolean includeParameters,
            @RequestParam(value = REQUEST_PARAM_DATE_FROM, required = false) @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateFrom,
            @RequestParam(value = REQUEST_PARAM_DATE_TO, required = false) @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateTo,
            @RequestParam(value = REQUEST_PARAM_TARGET, required = false) String target,
            @RequestParam(value = REQUEST_PARAM_ORIGIN, required = false) String origin,
            @RequestParam(value = REQUEST_PARAM_CLUSTER_NODE_ID, required = false) String clusterNodeId,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false) String sort,
            @RequestParam(value = REQUEST_PARAM_ORDER, required = false) String order) throws SiteNotFoundException {

        if (StringUtils.isNotEmpty(siteId) && !siteService.exists(siteId)) {
            throw new SiteNotFoundException("Site " + siteId + " not found.");
        }

        if (StringUtils.isNotEmpty(siteName) && !siteService.existsByName(siteName)) {
            throw new SiteNotFoundException("Site " + siteName + " not found.");
        }

        int total = auditService.getAuditLogTotal(siteId, siteName, user, operations, includeParameters, dateFrom,
                dateTo, target, origin, clusterNodeId);

        List<AuditLog> auditLog = auditService.getAuditLog(siteId, siteName, offset, limit, user, operations,
                includeParameters, dateFrom, dateTo, target, origin, clusterNodeId, sort, order);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<AuditLog> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setLimit(CollectionUtils.isEmpty(auditLog) ? 0 : auditLog.size());
        result.setOffset(offset);
        result.setEntities(RESULT_KEY_AUDIT_LOG, auditLog);
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(API_2 + AUDIT + PATH_PARAM_ID)
    public ResponseBody getAuditLogEntry(@PathVariable(REQUEST_PARAM_ID) long auditLogId) {
        AuditLog auditLogEntry = auditService.getAuditLogEntry(auditLogId);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<AuditLog> result = new ResultOne<AuditLog>();
        result.setEntity(RESULT_KEY_AUDIT_LOG, auditLogEntry);
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
