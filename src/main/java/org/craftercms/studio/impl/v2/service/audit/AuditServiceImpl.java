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

package org.craftercms.studio.impl.v2.service.audit;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_AUDIT_LOG;

public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;
    private ContentService contentService;
    private DeploymentService deploymentService;
    private SecurityService securityService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_AUDIT_LOG)
    public List<AuditLog> getAuditLogForSite(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site, int offset, int limit,
                                             String user, List<String> actions) throws SiteNotFoundException {
        if (StringUtils.isNotEmpty(site) && !siteService.exists(site)) {
            throw new SiteNotFoundException("Site " + site + " not found.");
        }
        return auditServiceInternal.getAuditLogForSite(site, offset, limit, user, actions);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_AUDIT_LOG)
    public int getAuditLogForSiteTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site, String user, List<String> actions)
            throws SiteNotFoundException {
        if (StringUtils.isNotEmpty(site) && !siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        return auditServiceInternal.getAuditLogForSiteTotal(site, user, actions);
    }

    @Override
    public List<AuditLog> getAuditLog(String siteId, String siteName, int offset, int limit, String user,
                                      List<String> operations, boolean includeParameters, ZonedDateTime dateFrom,
                                      ZonedDateTime dateTo, String target, String origin, String clusterNodeId,
                                      String sort, String order) {
        return auditServiceInternal.getAuditLog(siteId, siteName, offset, limit, user, operations, includeParameters,
                dateFrom, dateTo, target, origin, clusterNodeId, sort, order);
    }

    @Override
    public int getAuditLogTotal(String siteId, String siteName, String user, List<String> operations,
                                           boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                           String target, String origin, String clusterNodeId) {
        return auditServiceInternal.getAuditLogTotal(siteId, siteName, user, operations, includeParameters, dateFrom,
                dateTo, target, origin, clusterNodeId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_AUDIT_LOG)
    public AuditLog getAuditLogEntry(long auditLogId) {
        return auditServiceInternal.getAuditLogEntry(auditLogId);
    }

    @Override
    public List<ContentItemTO> getUserActivities(String site, int limit, String sort, boolean ascending,
                                                 boolean excludeLive, String filterType) throws ServiceLayerException {
        int startPos = 0;
        List<ContentItemTO> contentItems = new ArrayList<ContentItemTO>();
        boolean hasMoreItems = true;
        String user = securityService.getCurrentUser();
        while(contentItems.size() < limit && hasMoreItems){
            int remainingItems = limit - contentItems.size();
            hasMoreItems = getActivityFeeds(user, site, startPos, limit , filterType, excludeLive,contentItems,
                    remainingItems);
            startPos = startPos + limit;
        }
        if(contentItems.size() > limit){
            return contentItems.subList(0, limit);
        }
        return contentItems;
    }

    protected boolean getActivityFeeds(String user, String site,int startPos, int size, String filterType,
                                       boolean hideLiveItems, List<ContentItemTO> contentItems, int remainingItem) {

        List<AuditLog> activityFeeds = auditServiceInternal.selectUserFeedEntries(user, site, startPos, size, filterType,
                hideLiveItems);

        boolean hasMoreItems = true;

        //if number of items returned is less than size it means that table has no more records
        if(activityFeeds.size()<size){
            hasMoreItems=false;
        }

        if (activityFeeds != null && activityFeeds.size() > 0) {
            for (int index = 0; index < activityFeeds.size() && remainingItem!=0; index++) {
                AuditLog auditLog = activityFeeds.get(index);
                String id = auditLog.getPrimaryTargetValue();
                ContentItemTO item = createActivityItem(site, auditLog, id);
                contentItems.add(item);
                remainingItem--;
            }
        }
        logger.debug("Total Item post live filter : " + contentItems.size() + " hasMoreItems : "+hasMoreItems);

        return hasMoreItems;
    }

    protected ContentItemTO createActivityItem(String site, AuditLog auditLog, String id) {
        try {
            ContentItemTO item = contentService.getContentItem(site, id, 0);
            if(item == null || item.isDeleted()) {
                item = contentService.createDummyDmContentItemForDeletedNode(site, id);
                String modifier = auditLog.getActorId();
                if(modifier != null && !modifier.isEmpty()) {
                    item.user = modifier;
                }
                item.contentType = auditLog.getPrimaryTargetSubtype();
                item.setLockOwner("");
            }
            ZonedDateTime editedDate = auditLog.getOperationTimestamp();
            if (editedDate != null) {
                item.eventDate = editedDate.withZoneSameInstant(ZoneOffset.UTC);
            } else {
                item.eventDate = editedDate;
            }

            return item;
        } catch (Exception e) {
            logger.error("Error fetching content item for [" + id + "]", e.getMessage());
            return null;
        }
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
