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

package org.craftercms.studio.impl.v2.service.dashboard;

import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.dashboard.DashboardService;
import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class DashboardServiceImpl implements DashboardService {

    private ObjectMetadataManager objectMetadataManager;
    private AuditServiceInternal auditServiceInternal;

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
    public int getContentDashboardTotal(String siteId, String path, String modifier, String contentType, long state,
                                        ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        return objectMetadataManager.getContentDashboardTotal(siteId, path, modifier, contentType, state, dateFrom,
                dateTo);
    }

    @Override
    public List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier,
                                                          String contentType, long state, ZonedDateTime dateFrom,
                                                          ZonedDateTime dateTo, String sortBy, String order,
                                                          String groupBy, int offset, int limit) {
        return prepareContentDashboardResult(objectMetadataManager.getContentDashboard(siteId, path, modifier,
                contentType, state, dateFrom, dateTo, sortBy, order, groupBy, offset, limit));
    }

    private List<ContentDashboardItem> prepareContentDashboardResult(List<ItemMetadata> itemMetadataList) {
        List<ContentDashboardItem> contentDashboardItemList = new ArrayList<ContentDashboardItem>();
        if (itemMetadataList != null && itemMetadataList.size() > 0) {
            for (ItemMetadata item : itemMetadataList) {
                ContentDashboardItem contentDashboardItem = new ContentDashboardItem();
                contentDashboardItem.setSiteId(item.getSite());
                contentDashboardItem.setPath(item.getPath());
                contentDashboardItem.setLabel(item.getName());
                contentDashboardItem.setModifier(item.getModifier());
                contentDashboardItem.setModifiedDate(item.getModified());
                contentDashboardItemList.add(contentDashboardItem);
            }
        }
        return contentDashboardItemList;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }
}
