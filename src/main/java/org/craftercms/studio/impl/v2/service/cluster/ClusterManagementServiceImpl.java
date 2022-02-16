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

package org.craftercms.studio.impl.v2.service.cluster;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.api.v2.service.cluster.internal.ClusterManagementServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REMOVE_CLUSTER_NODE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CLUSTER_NODE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class ClusterManagementServiceImpl implements ClusterManagementService {

    private ClusterManagementServiceInternal clusterManagementServiceInternal;
    private ContentRepository contentRepository;
    private SiteService siteService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_cluster")
    public List<ClusterMember> getAllMembers() {
        return clusterManagementServiceInternal.getAllMembers();
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_cluster")
    public boolean removeMembers(List<Long> memberIds) throws SiteNotFoundException {
        List<ClusterMember> members = getAllMembers();
        boolean toRet = clusterManagementServiceInternal.removeMembers(memberIds);
        if (toRet) {
            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setOperation(OPERATION_REMOVE_CLUSTER_NODE);
            auditLog.setActorId(securityService.getCurrentUser());
            auditLog.setPrimaryTargetId(siteFeed.getSiteId());
            auditLog.setPrimaryTargetType(TARGET_TYPE_CLUSTER_NODE);
            auditLog.setPrimaryTargetValue(siteFeed.getName());
            List<AuditLogParameter> paramters = new ArrayList<AuditLogParameter>();
            for (ClusterMember m : members) {
                AuditLogParameter paramter = new AuditLogParameter();
                paramter.setTargetId(Long.toString(m.getId()));
                paramter.setTargetType(TARGET_TYPE_CLUSTER_NODE);
                paramter.setTargetValue(m.getLocalAddress());
                paramters.add(paramter);
            }
            auditLog.setParameters(paramters);
            auditServiceInternal.insertAuditLog(auditLog);
        }
        return toRet;
    }

    public ClusterManagementServiceInternal getClusterManagementServiceInternal() {
        return clusterManagementServiceInternal;
    }

    public void setClusterManagementServiceInternal(ClusterManagementServiceInternal clusterManagementServiceInternal) {
        this.clusterManagementServiceInternal = clusterManagementServiceInternal;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
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
}
