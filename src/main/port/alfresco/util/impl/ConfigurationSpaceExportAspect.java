package org.craftercms.cstudio.alfresco.util.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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
import org.apache.commons.io.FileUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dejan Brkic
 */
public class ConfigurationSpaceExportAspect implements NodeServicePolicies.OnAddAspectPolicy,
    NodeServicePolicies.OnRemoveAspectPolicy, NodeServicePolicies.OnDeleteNodePolicy,
    NodeServicePolicies.OnMoveNodePolicy, ContentServicePolicies.OnContentUpdatePolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpaceExportAspect.class);

    private PersistenceManagerService persistenceManagerService;
    private TransactionService transactionService;
    private String exportPath;
    private PolicyComponent policyComponent;

    public void init() {
        policyComponent.bindClassBehaviour(
            NodeServicePolicies.OnAddAspectPolicy.QNAME, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT,
            new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
            NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
            CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT,
            new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
            NodeServicePolicies.OnDeleteNodePolicy.QNAME,
            CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT,
            new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
            NodeServicePolicies.OnMoveNodePolicy.QNAME,
            CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT,
            new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(
            ContentServicePolicies.OnContentUpdatePolicy.QNAME,
            CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT,
            new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onDeleteNode(final ChildAssociationRef childAssociationRef, final boolean b) {
        if (childAssociationRef.getTypeQName().equals(ContentModel.ASSOC_CONTAINS)) {
            NodeRef parentRef = childAssociationRef.getParentRef();
            // If the parent node doesn't exist or is archived, it means the parent has also been deleted, so skip.

            if (persistenceManagerService.exists(parentRef) && !persistenceManagerService.hasAspect(parentRef, ContentModel.ASPECT_ARCHIVED) && !parentRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_ARCHIVE)) {

                String parentPath = DmUtils.getNodePath(persistenceManagerService, parentRef);
                String name = childAssociationRef.getQName().getLocalName();
                try {
                    fileSystemDelete(parentPath + "/" + name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onAddAspect(final NodeRef nodeRef, final QName qName) {
        if (qName.equals(CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT)) {
            FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
            if (fileInfo != null) {
                String deploymentPath = persistenceManagerService.getNodePath(nodeRef);
                if (deploymentPath.startsWith("/cstudio")) {
                    try {
                        if (!fileInfo.isFolder()) {
                            deployFile(deploymentPath, nodeRef);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while deploying file to " + deploymentPath, e);
                    }
                } else {
                    persistenceManagerService.removeAspect(nodeRef, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT);
                }
            }
        }
    }

    @Override
    public void onContentUpdate(final NodeRef nodeRef, final boolean b) {
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo != null) {
            String deploymentPath = persistenceManagerService.getNodePath(nodeRef);
            if (deploymentPath.startsWith("/cstudio")) {
                try {
                    if (!fileInfo.isFolder()) {
                        deployFile(deploymentPath, nodeRef);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while deploying file to " + deploymentPath, e);
                }
            } else {
                persistenceManagerService.removeAspect(nodeRef, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT);
            }
        }
    }

    @Override
    public void onMoveNode(final ChildAssociationRef oldChildAssocRef, final ChildAssociationRef newChildAssocRef) {
        if (oldChildAssocRef.getParentRef() != null && persistenceManagerService.exists(oldChildAssocRef.getParentRef())) {
            String oldParentPath = DmUtils.getNodePath(persistenceManagerService, oldChildAssocRef.getParentRef());
            String oldName = oldChildAssocRef.getQName().getLocalName();
            String deletePath = oldParentPath + "/" + oldName;
            try {
                fileSystemDelete(deletePath);
            } catch (IOException e) {
                LOGGER.error("Error while deleting file " + deletePath, e);
            }
        }
        NodeRef nodeRef = newChildAssocRef.getChildRef();
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo != null) {
            String deploymentPath = persistenceManagerService.getNodePath(nodeRef);
            if (deploymentPath.startsWith("/cstudio")) {
                try {
                    if (!fileInfo.isFolder()) {
                        deployFile(deploymentPath, nodeRef);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while deploying file to " + deploymentPath, e);
                }
            } else {
                persistenceManagerService.removeAspect(nodeRef, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT);
            }
        }
        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        final Runnable worker = new Runnable() {
            @Override
            public void run() {
                AuthenticationUtil.setFullyAuthenticatedUser(user);
                UserTransaction tx = transactionService.getNonPropagatingUserTransaction();
                try {
                    tx.begin();
                    deployChildren(newChildAssocRef.getChildRef());
                    tx.commit();
                } catch (Exception e) {
                    LOGGER.error("Error while synchronizing preview for children on parent move", e);
                    try {
                        tx.rollback();
                    } catch (SystemException e1) {
                        LOGGER.error("Error rolling back transaction", e);
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
    public void onRemoveAspect(final NodeRef nodeRef, final QName qName) {
        if (qName.equals(CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT)) {
            String deletePath = persistenceManagerService.getNodePath(nodeRef);
            try {
                fileSystemDelete(deletePath);
            } catch (IOException e) {
                LOGGER.error("Error while deleting file " + deletePath, e);
            }
        }
    }

    protected void deployFile(String path, NodeRef nodeRef) throws IOException {
        InputStream content = persistenceManagerService.getReader(nodeRef).getContentInputStream();
        try {
            fileSystemDeploy(path, content);
        } finally {
            try {
                content.close();
            } catch (IOException e) {
            }
        }
    }


    protected void createDirectory(String path) throws IOException {
        File dir = new File(exportPath, path);
        FileUtils.forceMkdir(dir);
    }

    protected void deployChildren(NodeRef nodeRef) {
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if (fileInfo != null && fileInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(nodeRef);
            for (FileInfo child : children) {
                String deploymentPath = persistenceManagerService.getNodePath(nodeRef);
                if (deploymentPath.startsWith("/cstudio")) {
                    if (child.isFolder()) {
                        deployChildren(child.getNodeRef());
                    } else {
                        try {
                            deployFile(deploymentPath, child.getNodeRef());
                        } catch (IOException e) {
                            LOGGER.error("Error while deploying file to " + deploymentPath, e);
                        }

                    }
                } else {
                    persistenceManagerService.removeAspect(nodeRef, CStudioContentModel.ASPECT_CONFIGURATION_SPACE_EXPORT);
                }
            }
        }
    }

    protected void fileSystemDeploy(String path, InputStream content) throws IOException {
        File file = new File(exportPath, path);
        FileUtils.copyInputStreamToFile(content, file);
    }


    protected void fileSystemDelete(String path) throws IOException {
        File file = new File(exportPath, path);
        FileUtils.forceDelete(file);
    }

    public void setPersistenceManagerService(final PersistenceManagerService persistenceManagerService) {
        this.persistenceManagerService = persistenceManagerService;
    }

    public void setExportPath(final String exportPath) {
        this.exportPath = exportPath;
    }

    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }


}
