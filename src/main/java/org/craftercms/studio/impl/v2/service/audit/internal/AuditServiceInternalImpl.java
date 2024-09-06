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

package org.craftercms.studio.impl.v2.service.audit.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.dal.AuditDAO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.model.rest.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

public class AuditServiceInternalImpl implements AuditServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceInternalImpl.class);

    private AuditDAO auditDao;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public List<AuditLog> getAuditLog(String siteId, int offset, int limit, String user,
                                      List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                                      ZonedDateTime dateTo, String target, String origin, String clusterNodeId,
                                      String sort, String order) {
        Map<String, Object> params = new HashMap<>();
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        if (StringUtils.isNotEmpty(siteId)) {
            params.put(SITE_ID, siteId);
        }
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(operations)) {
            params.put(OPERATIONS, operations);
        }
        if (dateFrom != null) {
            params.put(DATE_FROM, dateFrom);
        }
        if (dateTo != null) {
            params.put(DATE_TO, dateTo);
        }
        if (StringUtils.isNotEmpty(target)) {
            params.put(TARGET, target);
        }
        if (StringUtils.isNotEmpty(origin)) {
            if (StringUtils.equalsIgnoreCase(origin, ORIGIN_API)) {
                params.put(ORIGIN, ORIGIN_API);
            } else if (StringUtils.equalsIgnoreCase(origin, ORIGIN_GIT)) {
                params.put(ORIGIN, ORIGIN_GIT);
            }
        }
        if (StringUtils.isNotEmpty(clusterNodeId)) {
            params.put(CLUSTER_NODE_ID, clusterNodeId);
        }
        if (StringUtils.isNotEmpty(sort) && StringUtils.equalsIgnoreCase(sort, "date")) {
            params.put(SORT, "operation_timestamp");
        }
        if (StringUtils.isNotEmpty(order)) {
            if (StringUtils.equalsIgnoreCase("DESC", order)) {
                params.put(ORDER, "DESC");
            } else {
                params.put(ORDER, "ASC");
            }
        }
        params.put(INCLUDE_PARAMETERS, includeParameters);
        return auditDao.getAuditLog(params);
    }

    @Override
    public int getAuditLogTotal(String siteId, String user, List<String> operations,
                                boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo, String target,
                                String origin, String clusterNodeId) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(siteId)) {
            params.put(SITE_ID, siteId);
        }
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(operations)) {
            params.put(OPERATIONS, operations);
        }
        if (dateFrom != null) {
            params.put(DATE_FROM, dateFrom);
        }
        if (dateTo != null) {
            params.put(DATE_TO, dateTo);
        }
        if (StringUtils.isNotEmpty(target)) {
            params.put(TARGET, target);
        }
        if (StringUtils.isNotEmpty(origin)) {
            if (StringUtils.equalsIgnoreCase(origin, ORIGIN_API)) {
                params.put(ORIGIN, ORIGIN_API);
            } else if (StringUtils.equalsIgnoreCase(origin, ORIGIN_GIT)) {
                params.put(ORIGIN, ORIGIN_GIT);
            }
        }
        if (StringUtils.isNotEmpty(clusterNodeId)) {
            params.put(CLUSTER_NODE_ID, clusterNodeId);
        }
        params.put(INCLUDE_PARAMETERS, includeParameters);
        return auditDao.getAuditLogTotal(params);
    }

    @Override
    public AuditLog getAuditLogEntry(final String siteId, final long auditLogId) {
        Map<String, Object> params = new HashMap<>();
        params.put(ID, auditLogId);
        if (!isEmpty(siteId)) {
            params.put(SITE_ID, siteId);
        }
        return auditDao.getAuditLogEntry(params);
    }

    @Override
    // TODO: after login insert LOGIN audit
    public boolean insertAuditLog(AuditLog auditLog) {
        int result = retryingDatabaseOperationFacade.retry(() -> auditDao.insertAuditLog(auditLog));
        if (CollectionUtils.isNotEmpty(auditLog.getParameters())) {
            Map<String, Object> params = new HashMap<>();
            params.put("auditId", auditLog.getId());
            params.put("parameters", auditLog.getParameters());
            retryingDatabaseOperationFacade.retry(() -> auditDao.insertAuditLogParams(params));
        }
        return result > 0;
    }

    @Override
    public AuditLog createAuditLogEntry() {
        AuditLog auditLog = new AuditLog();
        String clusterNodeId = StringUtils.EMPTY;
        auditLog.setOrganizationId(1);
        auditLog.setOrigin(ORIGIN_API);
        auditLog.setClusterNodeId(clusterNodeId);
        return auditLog;
    }

    public List<AuditLog> selectUserFeedEntries(String user, String siteId, int offset, int limit, String contentType,
                                                boolean hideLiveItems) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", user);
        params.put("siteId", siteId);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("operations", Arrays.asList(OPERATION_CREATE, OPERATION_DELETE, OPERATION_UPDATE, OPERATION_MOVE));
        params.put("targetType", TARGET_TYPE_CONTENT_ITEM);
        if (StringUtils.isNotEmpty(contentType) && !contentType.equalsIgnoreCase("all")) {
            params.put("contentType", contentType.toLowerCase());
        }
        if (hideLiveItems) {
            params.put("liveStateBitMap", ItemState.LIVE.value);
            return auditDao.selectUserFeedEntriesHideLive(params);
        } else {
            return auditDao.selectUserFeedEntries(params);
        }
    }

    @Override
    public Person getAuthor(String commitId) {
        return auditDao.getCommitAuthor(commitId);
    }

    @Override
    public boolean isAudited(long siteId, String commitId) {
        return auditDao.isAudited(siteId, commitId);
    }

    public void setAuditDao(AuditDAO auditDao) {
        this.auditDao = auditDao;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
