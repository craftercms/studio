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
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.AuditDAO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.QueryParameterNames;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_NAME;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_MOVE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.ORIGIN_API;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.ORIGIN_GIT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTIONS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_NODE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_FROM;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_TO;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.INCLUDE_PARAMETERS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OPERATIONS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORDER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORIGIN;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TARGET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;

public class AuditServiceInternalImpl implements AuditServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceInternalImpl.class);

    private AuditDAO auditDao;
    private StudioConfiguration studioConfiguration;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public List<AuditLog> getAuditLogForSite(String site, int offset, int limit, String user, List<String> actions)
            throws SiteNotFoundException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_ID, site);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(actions)) {
            params.put(ACTIONS, actions);
        }
        return auditDao.getAuditLogForSite(params);
    }

    @Override
    public int getAuditLogForSiteTotal(String site, String user, List<String> actions) throws SiteNotFoundException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_ID, site);
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(actions)) {
            params.put(ACTIONS, actions);
        }
        return auditDao.getAuditLogForSiteTotal(params);
    }


    @Override
    public List<AuditLog> getAuditLog(String siteId, String siteName, int offset, int limit, String user,
                                      List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                                      ZonedDateTime dateTo, String target, String origin, String clusterNodeId,
                                      String sort, String order) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        if (StringUtils.isNotEmpty(siteId)) {
            params.put(SITE_ID, siteId);
        }
        if (StringUtils.isNotEmpty(siteName)) {
            params.put(SITE_NAME, siteName);
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
    public int getAuditLogTotal(String siteId, String siteName, String user, List<String> operations,
                                boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo, String target,
                                String origin, String clusterNodeId) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(siteId)) {
            params.put(SITE_ID, siteId);
        }
        if (StringUtils.isNotEmpty(siteName)) {
            params.put(SITE_NAME, siteName);
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
    public int getAuditDashboardTotal(String siteId, String user, List<String> operations, ZonedDateTime dateFrom,
                                      ZonedDateTime dateTo, String target) {
        Map<String, Object> params = new HashMap<String, Object>();
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
        return auditDao.getAuditDashboardTotal(params);
    }

    @Override
    public List<AuditLog> getAuditDashboard(String siteId, int offset, int limit, String user, List<String> operations,
                                            ZonedDateTime dateFrom, ZonedDateTime dateTo, String target, String sort,
                                            String order) {
        Map<String, Object> params = new HashMap<String, Object>();
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
        if (StringUtils.isNotEmpty(sort)) {
            String sortParam = "";
            switch (sort) {
                case "site":
                    sortParam = "site_name";
                    break;
                case "actor":
                    sortParam = "actor_id";
                    break;
                case "operation":
                    sortParam = "operation";
                    break;
                case "operationTimestamp":
                    sortParam = "operation_timestamp";
                    break;
                case "target":
                    sortParam = "primary_target_value";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotEmpty(sortParam)) {
                params.put(SORT, sortParam);
            }
        }
        if (StringUtils.isNotEmpty(order)) {
            if (StringUtils.equalsIgnoreCase("DESC", order)) {
                params.put(ORDER, "DESC");
            } else {
                params.put(ORDER, "ASC");
            }
        }
        return auditDao.getAuditDashboard(params);
    }

    @Override
    public AuditLog getAuditLogEntry(long auditLogId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(QueryParameterNames.ID, auditLogId);
        return auditDao.getAuditLogEntry(params);
    }

    @Override
    // TODO: after login insert LOGIN audit
    public boolean insertAuditLog(AuditLog auditLog) {
        int result = retryingDatabaseOperationFacade.insertAuditLog(auditLog);
        if (CollectionUtils.isNotEmpty(auditLog.getParameters())) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("auditId", auditLog.getId());
            params.put("parameters", auditLog.getParameters());
            retryingDatabaseOperationFacade.insertAuditLogParams(params);
        }
        return result > 0;
    }

    @Override
    public AuditLog createAuditLogEntry() {
        AuditLog auditLog = new AuditLog();
        String clusterNodeId = StringUtils.EMPTY;
        HierarchicalConfiguration<ImmutableNode> clusterNodeData =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        if (clusterNodeData != null && !clusterNodeData.isEmpty()) {
            clusterNodeId = clusterNodeData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
        }
        auditLog.setOrganizationId(1);
        auditLog.setOrigin(ORIGIN_API);
        auditLog.setClusterNodeId(clusterNodeId);
        return auditLog;
    }

    public List<AuditLog> selectUserFeedEntries(String user, String siteId, int offset, int limit, String contentType,
                                                boolean hideLiveItems) {
        HashMap<String, Object> params = new HashMap<String, Object>();
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
    public void deleteAuditLogForSite(long siteId) {
        retryingDatabaseOperationFacade.deleteAuditLogForSite(siteId);
    }

    public AuditDAO getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(AuditDAO auditDao) {
        this.auditDao = auditDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
