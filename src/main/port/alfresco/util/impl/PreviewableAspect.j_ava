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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.preview.PreviewDeployer;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SiteService;
import org.craftercms.cstudio.alfresco.to.DeploymentConfigTO;
import org.craftercms.cstudio.alfresco.util.PreviewDeployUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * Class containing behaviour for the previewable aspect
 *
 * @author Alfonso Vásquez
 * @author Dejan Brkic
 */
public class PreviewableAspect implements NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy,
        NodeServicePolicies.OnDeleteNodePolicy,
        NodeServicePolicies.OnMoveNodePolicy,
        ContentServicePolicies.OnContentUpdatePolicy {

    private static final Logger logger = LoggerFactory.getLogger(PreviewableAspect.class);

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
     * Initialise the previewable aspect policies
     */
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE,
                new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE,
                new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE,
                new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                CStudioContentModel.ASPECT_PREVIEWABLE,
                new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (aspectTypeQName.equals(CStudioContentModel.ASPECT_PREVIEWABLE)) {
            deployFile(nodeRef);
        }
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (aspectTypeQName.equals(CStudioContentModel.ASPECT_PREVIEWABLE)) {
            deleteFile(nodeRef);
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
        try {
            NodeRef parentRef = childAssocRef.getParentRef();
            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
            // If the parent node doesn't exist or is archived, it means the parent has also been deleted, so skip.

            if (persistenceManagerService.exists(parentRef) && !persistenceManagerService.hasAspect(parentRef, ContentModel.ASPECT_ARCHIVED) && !parentRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_ARCHIVE)) {
                String parentPath = DmUtils.getNodePath(persistenceManagerService, parentRef);
                String name = childAssocRef.getQName().getLocalName();
                deleteFile(parentPath + "/" + name);
            }
        }
        catch(Exception err) {
            logger.error("Error while onDeleteNode " + childAssocRef, err);
        }
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, final ChildAssociationRef newChildAssocRef) {
        final PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        if (oldChildAssocRef.getParentRef() != null && persistenceManagerService.exists(oldChildAssocRef.getParentRef())) {
            String oldParentPath = DmUtils.getNodePath(persistenceManagerService, oldChildAssocRef.getParentRef());
            String oldName = oldChildAssocRef.getQName().getLocalName();
            deleteFile(oldParentPath + "/" + oldName);
        }
        deployFile(newChildAssocRef.getChildRef());
        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        final Runnable worker = new Runnable() {
            @Override
            public void run() {
                AuthenticationUtil.setFullyAuthenticatedUser(user);
                TransactionService transactionService = servicesManager.getService(TransactionService.class);
                UserTransaction tx = transactionService.getNonPropagatingUserTransaction();
                try {
                    tx.begin();
                    deployChildren(newChildAssocRef.getChildRef());
                    tx.commit();
                } catch (Exception e) {
                    logger.error("Error while synchronizing preview for children on parent move", e);
                    try {
                        tx.rollback();
                    } catch (SystemException e1) {
                        logger.error("Error rolling back transaction", e);
                    }
                } finally {
                    tx = null;
                }
            }
        };

        TransactionListener listener = new TransactionListener() {
            @Override
            public void flush() { }

            @Override
            public void beforeCommit(boolean b) { }

            @Override
            public void beforeCompletion() { }

            @Override
            public void afterCommit() {
                ExecutorService es = Executors.newSingleThreadExecutor();
                es.execute(worker);
            }

            @Override
            public void afterRollback() { }
        };

        AlfrescoTransactionSupport.bindListener(listener);
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        deployFile(nodeRef);
    }

    protected void deployChildren(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo != null && fileInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(nodeRef);
            for (FileInfo child : children) {
                if (child.isFolder()) {
                    deployChildren(child.getNodeRef());
                } else {
                    deployFile(child.getNodeRef());
                }
            }
        }
    }

    protected void deployFile(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        SiteService siteService = getServicesManager().getService(SiteService.class);
        if (persistenceManagerService.exists(nodeRef)) {
            FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
            if (fileInfo != null) {
                String deploymentPath = persistenceManagerService.getNodePath(nodeRef);
                Matcher m = DmConstants.DM_WORK_AREA_PATH_PATTERN.matcher(deploymentPath);
                if (m.matches()) {
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
        } else {
            logger.warn(String.format("Unable to deploy node %s because nodeRef does not exist or is invalid", nodeRef.toString()));
        }
    }

    protected void deleteFile(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        deleteFile(persistenceManagerService.getNodePath(nodeRef));
    }

    protected void deleteFile(String deploymentPath) {
        try {
            Matcher m = DmConstants.DM_WORK_AREA_PATH_PATTERN.matcher(deploymentPath);
            if (m.matches()) {
                DmPathTO dmPathTO = new DmPathTO(deploymentPath);
                String site = dmPathTO.getSiteName();
                SiteService siteService = getServicesManager().getService(SiteService.class);
                DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                PreviewDeployUtils.deleteFileOrDir(site, deploymentPath, deployer, deploymentConfigTO);
            }
        } catch (Exception e) {
            logger.error("Error while deleting deployed file at " + deploymentPath, e);
        }
    }

}
