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

import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class DashboardServiceImpl implements DashboardService {

    private AuditServiceInternal auditServiceInternal;
    private PublishServiceInternal publishServiceInternal;
    private ItemServiceInternal itemServiceInternal;

    @Override
    public int getAuditDashboardTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String user,
                                      List<String> operations, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                      String target) {
        return auditServiceInternal.getAuditDashboardTotal(siteId, user, operations, dateFrom, dateTo, target);
    }

    @Override
    public List<AuditLog> getAuditDashboard(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, int offset,
                                            int limit, String user, List<String> operations, ZonedDateTime dateFrom,
                                            ZonedDateTime dateTo, String target, String sort, String order) {
        return auditServiceInternal.getAuditDashboard(siteId, offset, limit, user, operations, dateFrom, dateTo, target,
                sort, order);
    }

    @Override
    public int getContentDashboardTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String path,
                                        String modifier, String contentType, long state, ZonedDateTime dateFrom,
                                        ZonedDateTime dateTo) {
        return itemServiceInternal.getContentDashboardTotal(siteId, path, modifier, contentType, state, dateFrom,
                dateTo);
    }

    @Override
    public List<ContentDashboardItem> getContentDashboard(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                          String path, String modifier, String contentType, long state,
                                                          ZonedDateTime dateFrom, ZonedDateTime dateTo, String sortBy,
                                                          String order, int offset, int limit) {
        return itemServiceInternal.getContentDashboard(siteId, path, modifier, contentType, state, dateFrom, dateTo,
                sortBy, order, offset, limit);
    }

    @Override
    public int getPublishingHistoryTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String environment,
                                         String path, String publisher, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                         String contentType, long state) {
        return publishServiceInternal.getPublishingHistoryTotal(siteId, environment, path, publisher, dateFrom, dateTo,
                contentType, state);
    }

    @Override
    public List<PublishingDashboardItem> getPublishingHistory(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                              String environment, String path, String publisher,
                                                              ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                              String contentType, long state, String sortBy,
                                                              String order, int offset, int limit) {
        List<PublishingHistoryItem> publishingHistoryItems = publishServiceInternal.getPublishingHistory(siteId,
                environment, path, publisher, dateFrom, dateTo, contentType, state, sortBy, order, offset, limit);
        return publishingHistoryItems
                .stream()
                .map(itemServiceInternal::convertHistoryItemToDashboardItem)
                .collect(Collectors.toList());
    }


    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public PublishServiceInternal getPublishServiceInternal() {
        return publishServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
