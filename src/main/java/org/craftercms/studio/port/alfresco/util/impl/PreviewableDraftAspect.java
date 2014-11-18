/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.alfresco.util.impl;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.preview.PreviewDeployer;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SiteService;
import org.craftercms.cstudio.alfresco.util.PreviewDeployUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

/**
 * Class containing behaviour for the previewable draft aspect
 *
 * @author Alfonso Vásquez
 * @author Dejan Brkic
 */
public class PreviewableDraftAspect implements NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnMoveNodePolicy,
        ContentServicePolicies.OnContentUpdatePolicy {

    private static final Logger logger = LoggerFactory.getLogger(PreviewableDraftAspect.class);

    protected PolicyComponent policyComponent;
    protected PreviewDeployer deployer;
    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setDeployer(PreviewDeployer deployer) {
        this.deployer = deployer;
    }

    /**
     * Initialise the previewable draft aspect policies
     */
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE_DRAFT,
                new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE_DRAFT,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE_DRAFT,
                new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (aspectTypeQName.equals(CStudioContentModel.ASPECT_PREVIEWABLE_DRAFT)) {
            deployFile(nodeRef);
        }
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
        //String oldParentPath = DmUtils.getNodePath(fileFolderService, nodeService, oldChildAssocRef.getParentRef());
        //String oldName = oldChildAssocRef.getQName().getLocalName();

        //deleteFile(oldParentPath + "/" + oldName);
        deployFile(newChildAssocRef.getChildRef());
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        deployFile(nodeRef);
    }

    protected void deployFile(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        SiteService siteService = getServicesManager().getService(SiteService.class);
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo != null) {
            String deploymentPath = DmUtils.getNodePath(persistenceManagerService, nodeRef);
            Matcher m = DmConstants.DM_DRAFT_PATH_PATTERN.matcher(deploymentPath);
            if (m.matches()) {
                m = DmConstants.DM_REPO_TYPE_PATH_PATTERN.matcher(deploymentPath);
                if (m.matches()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m.group(1));
                    sb.append(m.group(2));
                    sb.append(DmConstants.DM_WORK_AREA_REPO_FOLDER);
                    sb.append(m.group(4));
                    deploymentPath = sb.toString();
                }

                try {
                    if (!fileInfo.isFolder()) {
                        DmPathTO dmPathTO = new DmPathTO(deploymentPath);
                        String site = dmPathTO.getSiteName();
                        DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                        PreviewDeployUtils.deployFile(site, deploymentPath, fileInfo, persistenceManagerService, deployer, deploymentConfigTO);
                    }
                } catch (Exception e) {
                    logger.error("Error while deploying file to " + deploymentPath, e);
                }
            }
        } else {
            logger.error("Unable to deploy node " + nodeRef + ": The node is not a file or a folder");
        }
    }

}
