/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.publish;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CANCEL_PUBLISHING_PACKAGE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_PUBLISHING_PACKAGE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_PUBLISHING_PACKAGE_ON_MASK;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CANCEL_PUBLISH;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_GET_PUBLISHING_QUEUE;

public class PublishServiceImpl implements PublishService {

    private PublishServiceInternal publishServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;
    private ItemServiceInternal itemServiceInternal;

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public int getPublishingPackagesTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String environment,
                                          String path, List<String> states)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public List<PublishingPackage> getPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                         String environment, String path, List<String> states,
                                                         int offset, int limit) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_GET_PUBLISHING_QUEUE)
    public PublishingPackageDetails getPublishingPackageDetails(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                                String packageId) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackageDetails(siteId, packageId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CANCEL_PUBLISH)
    public void cancelPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         List<String> packageIds) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        publishServiceInternal.cancelPublishingPackages(siteId, packageIds);
        List<AuditLogParameter> auditLogParameters = new ArrayList<AuditLogParameter>();
        for (String pId : packageIds) {
            PublishingPackageDetails packageDetails = publishServiceInternal.getPublishingPackageDetails(siteId, pId);
            List<String> paths = new ArrayList<String>();
            for (PublishingPackageDetails.PublishingPackageItem item : packageDetails.getItems()) {
                paths.add(item.getPath());
                AuditLogParameter auditLogParameter = new AuditLogParameter();
                auditLogParameter.setTargetId(siteId + ":" + item.getPath());
                auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
                auditLogParameter.setTargetValue(item.getPath());
                auditLogParameters.add(auditLogParameter);
            }

            itemServiceInternal.updateStateBitsBulk(siteId, paths, CANCEL_PUBLISHING_PACKAGE_ON_MASK,
                    CANCEL_PUBLISHING_PACKAGE_OFF_MASK);

            createAuditLogEntry(siteId, auditLogParameters);
        }
    }

    private void createAuditLogEntry(String siteId, List<AuditLogParameter> auditLogParameters)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CANCEL_PUBLISHING_PACKAGE);
        auditLog.setActorId(securityService.getCurrentUser());
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state) {
        return publishServiceInternal.getPublishingHistoryTotal(siteId, environment, path, publisher, dateFrom, dateTo,
                contentType, state);
    }

    @Override
    public List<PublishingDashboardItem> getPublishingHistory(String siteId, String environment, String path,
                                                              String publisher, ZonedDateTime dateFrom,
                                                              ZonedDateTime dateTo, String contentType, long state,
                                                              String sortBy, String order, int offset, int limit) {
        List<PublishingHistoryItem> publishingHistoryItems = publishServiceInternal.getPublishingHistory(siteId,
                environment, path, publisher, dateFrom, dateTo, contentType, state, sortBy, order, offset,
                limit);
        return publishingHistoryItems
                .stream()
                .map(itemServiceInternal::convertHistoryItemToDashboardItem)
                .collect(Collectors.toList());
    }



    public PublishServiceInternal getPublishServiceInternal() {
        return publishServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
