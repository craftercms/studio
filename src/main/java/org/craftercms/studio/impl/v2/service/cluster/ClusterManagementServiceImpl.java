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
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.api.v2.service.cluster.internal.ClusterManagementServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class ClusterManagementServiceImpl implements ClusterManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterManagementServiceImpl.class);

    private ClusterManagementServiceInternal clusterManagementServiceInternal;
    private ContentRepository contentRepository;
    private SiteService siteService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;
    private StudioClusterUtils studioClusterUtils;
    private RetryingOperationFacade retryingOperationFacade;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_cluster")
    public List<ClusterMember> getAllMemebers() {
        return clusterManagementServiceInternal.getAllMembers();
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_cluster")
    public ClusterMember getMemberByLocalAddress(String localAddress) {
        return clusterManagementServiceInternal.getMemberByLocalAddress(localAddress);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_cluster")
    public boolean removeMembers(List<Long> memberIds) throws SiteNotFoundException {
        List<ClusterMember> members = getAllMemebers();
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

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_cluster")
    public boolean setClusterPrimary() {
        logger.debug("Enabling global publishing flag.");
        studioClusterUtils.enableGlobalPublishing();
        logger.debug("Enabled global publishing flag with result: " + studioClusterUtils.isGlobalPublishingEnabled());

        if (studioClusterUtils.memberPrimaryPublisher()) {
            logger.debug("Member has already been the primary publisher. No action occurs.");
            return true;
        }

        // set member as primary publisher
        String localAddress = studioClusterUtils.getClusterNodeLocalAddress();
        ClusterMember localNode = clusterManagementServiceInternal.getMemberByLocalAddress(localAddress);
        logger.debug("Set this node as primary publisher.");
        retryingOperationFacade.setClusterPrimary(localNode.getId());

        // add audit log
        try {
            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setOperation(OPERATION_SET_CLUSTER_PRIMARY);
            auditLog.setActorId(securityService.getCurrentUser());
            auditLog.setPrimaryTargetId(siteFeed.getSiteId());
            auditLog.setPrimaryTargetType(TARGET_TYPE_CLUSTER_NODE);
            auditLog.setPrimaryTargetValue(siteFeed.getName());
            List<AuditLogParameter> parameters = new ArrayList<>();
            AuditLogParameter parameter = new AuditLogParameter();
            parameter.setTargetId(Long.toString(localNode.getId()));
            parameter.setTargetType(TARGET_TYPE_CLUSTER_NODE);
            parameter.setTargetValue(localNode.getLocalAddress());
            parameters.add(parameter);

            auditLog.setParameters(parameters);
            auditServiceInternal.insertAuditLog(auditLog);
        } catch (SiteNotFoundException e) {
            logger.error("Error while adding audit log.", e);
        }

        return true;
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

    public StudioClusterUtils getStudioClusterUtils() {
        return studioClusterUtils;
    }

    public void setStudioClusterUtils(StudioClusterUtils studioClusterUtils) {
        this.studioClusterUtils = studioClusterUtils;
    }

    public RetryingOperationFacade getRetryingOperationFacade() {
        return retryingOperationFacade;
    }

    public void setRetryingOperationFacade(RetryingOperationFacade retryingOperationFacade) {
        this.retryingOperationFacade = retryingOperationFacade;
    }
}
