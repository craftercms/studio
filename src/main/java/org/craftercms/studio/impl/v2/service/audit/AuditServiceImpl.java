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

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateCollectionParam;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.rometools.utils.Strings.isNotEmpty;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.*;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_AUDIT_LOG;

public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;
    private ContentService contentService;
    private SecurityService securityService;

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_AUDIT_LOG)
    public List<AuditLog> getAuditLog(@EsapiValidatedParam(type = SITE_ID, notEmpty = false, notBlank = false) @ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      int offset, int limit,
                                      @EsapiValidatedParam(type = USERNAME, notEmpty = false, notBlank = false) String user,
                                      @ValidateCollectionParam(notNull = false, notEmpty = false) @ValidateNoTagsParam List<String> operations,
                                      boolean includeParameters, ZonedDateTime dateFrom,
                                      ZonedDateTime dateTo,
                                      @ValidateNoTagsParam String target,
                                      @EsapiValidatedParam(type = ALPHANUMERIC, notNull = false, notEmpty = false, notBlank = false) String origin,
                                      @ValidateNoTagsParam String clusterNodeId,
                                      String sort, String order) throws SiteNotFoundException {
        if (isNotEmpty(siteId)) {
            siteService.checkSiteExists(siteId);
        }

        return auditServiceInternal.getAuditLog(siteId, offset, limit, user, operations, includeParameters,
                dateFrom, dateTo, target, origin, clusterNodeId, sort, order);
    }

    @Override
    @ValidateParams
    public int getAuditLogTotal(@EsapiValidatedParam(type = SITE_ID, notEmpty = false, notBlank = false) String siteId,
                                @EsapiValidatedParam(type = USERNAME, notEmpty = false, notBlank = false) String user,
                                @ValidateCollectionParam(notNull = false, notEmpty = false) @ValidateNoTagsParam List<String> operations,
                                boolean includeParameters, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                @ValidateNoTagsParam String target,
                                @EsapiValidatedParam(type = ALPHANUMERIC, notNull = false, notEmpty = false, notBlank = false) String origin,
                                @ValidateNoTagsParam String clusterNodeId) throws SiteNotFoundException {
        if (isNotEmpty(siteId)) {
            siteService.checkSiteExists(siteId);
        }

        return auditServiceInternal.getAuditLogTotal(siteId, user, operations, includeParameters, dateFrom,
                dateTo, target, origin, clusterNodeId);
    }

    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_AUDIT_LOG)
    public AuditLog getAuditLogEntry(@EsapiValidatedParam(type = SITE_ID, notEmpty = false, notBlank = false) @ProtectedResourceId(SITE_ID_RESOURCE_ID) final String siteId,
                                     final long auditLogId) throws SiteNotFoundException {
        if(isNotEmpty(siteId)){
            siteService.checkSiteExists(siteId);
        }
        return auditServiceInternal.getAuditLogEntry(siteId, auditLogId);
    }

    @Override
    public List<ContentItemTO> getUserActivities(String site, int limit, String sort, boolean ascending,
                                                 boolean excludeLive, String filterType) {
        int startPos = 0;
        List<ContentItemTO> contentItems = new ArrayList<>();
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

    protected boolean getActivityFeeds(String user, String site, int startPos, int size, String filterType,
                                       boolean hideLiveItems, List<ContentItemTO> contentItems, int remainingItem) {

        List<AuditLog> activityFeeds = auditServiceInternal.selectUserFeedEntries(user, site, startPos, size, filterType,
                hideLiveItems);

        boolean hasMoreItems = activityFeeds.size() >= size;

        // If the number of items returned is less than the size, then it means that the table has no more records

        // TODO: SJ: Simplify the code below
        if (CollectionUtils.isNotEmpty(activityFeeds)) {
            for (int index = 0; index < activityFeeds.size() && remainingItem!=0; index++) {
                AuditLog auditLog = activityFeeds.get(index);
                String id = auditLog.getPrimaryTargetValue();
                ContentItemTO item = createActivityItem(site, auditLog, id);
                contentItems.add(item);
                remainingItem--;
            }
        }

        logger.debug("The total items retrieved from the activity feed in site '{}' is '{}' and hasMore is '{}'",
                site, contentItems.size(), hasMoreItems);

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
                item.eventDate = null;
            }

            return item;
        } catch (Exception e) {
            logger.error("Failed to fetch content item from site '{}' with ID '{}'", site, id, e);
            return null;
        }
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
