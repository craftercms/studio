/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.service.impl;

import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.pagenavigationordersequence.PageNavigationOrderSequenceDaoService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.GeneralLockService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.to.PageNavigationOrderSequenceTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CStudioNodeServiceImpl extends AbstractRegistrableService implements CStudioNodeService {

    private static Logger logger = LoggerFactory.getLogger(CStudioNodeServiceImpl.class);


    protected CompanyHomeNodeLocator _companyHomeNodeLocator;

    public CompanyHomeNodeLocator getCompanyHomeNodeLocator() {
        return _companyHomeNodeLocator;
    }

    public void setCompanyHomeNodeLocator(CompanyHomeNodeLocator companyHomeNodeLocator) {
        this._companyHomeNodeLocator = companyHomeNodeLocator;
    }

    protected PageNavigationOrderSequenceDaoService _pageNavigationOrderSequenceDaoService;

    public PageNavigationOrderSequenceDaoService getPageNavigationOrderSequenceDaoService() {
        return _pageNavigationOrderSequenceDaoService;
    }

    public void setPageNavigationOrderSequenceDaoService(PageNavigationOrderSequenceDaoService pageNavigationOrderSequenceDaoService) {
        this._pageNavigationOrderSequenceDaoService = pageNavigationOrderSequenceDaoService;
    }

    @Override
    public NodeRef getCompanyHomeNodeRef() {
        return getCompanyHomeNodeLocator().getNode(null, null);
    }

    @Override
    public void register() {
        this._servicesManager.registerService(CStudioNodeService.class, this);
    }

    /**
     * @deprecated Use {@link PersistenceManagerService#getNodeRef(String)}
     */
    @Override
    @Deprecated
    public NodeRef getNodeRef(String fullPath) {
        return getNodeRef(getCompanyHomeNodeRef(), fullPath);
    }

    /**
     * @deprecated Use {@link PersistenceManagerService#getNodeRef(NodeRef, String)}
     */
    @Override
    @Deprecated
    public NodeRef getNodeRef(NodeRef rootNode, String relativePath) {
        return getService(PersistenceManagerService.class).getNodeRef(rootNode, relativePath);
    }

    /**
     * @deprecated Use {@link PersistenceManagerService#getNodeRef(String, String)}
     */
    @Override
    @Deprecated
    public NodeRef getNodeRef(String rootPath, String relativePath) {
        return getNodeRef(getNodeRef(rootPath), relativePath);
    }

    @Override
    public String getNodePath(NodeRef nodeRef) {
        return getRelativeNodePath(getCompanyHomeNodeRef(), nodeRef);
    }

    @Override
    public String getRelativeNodePath(NodeRef rootNodeRef, NodeRef nodeRef) {
        StringBuilder sb = new StringBuilder();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        Path nodePath = persistenceManagerService.getPath(nodeRef);
        boolean foundRootNodeRef = false;
        for (Path.Element element : nodePath) {
            Path.ChildAssocElement assocElement = (Path.ChildAssocElement) element;
            ChildAssociationRef childAssociationRef = assocElement.getRef();
            NodeRef elementNode = childAssociationRef.getChildRef();
            if (foundRootNodeRef) {
                sb.append("/").append(childAssociationRef.getQName().getLocalName());
            }
            foundRootNodeRef = foundRootNodeRef || elementNode.equals(rootNodeRef);
        }
        return sb.toString();
    }

    @Override
    public NodeRef createNewFolder(String fullPath) {
        return createNewFolder(fullPath, new FastMap<QName, Serializable>());
    }

    @Override
    public NodeRef createNewFolder(String fullPath, Map<QName, Serializable> nodeProperties) {
        int idx = StringUtils.lastIndexOf(fullPath, '/');
        String parentPath = StringUtils.substring(fullPath, 0, idx - 1);
        String folderName = StringUtils.substring(fullPath, idx + 1);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        return createNewFolder(parentPath, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(String parentPath, String folderName) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, folderName);
        return createNewFolder(parentPath, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(String parentPath, String folderName, Map<QName, Serializable> nodeProperties) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        return createNewFolder(parentNodeRef, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, folderName);
        return createNewFolder(parentNodeRef, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName, Map<QName, Serializable> nodeProperties) {
        QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(folderName));
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        //updateLastNavigationOrder(parentNodeRef);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        NodeRef result = persistenceManagerService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, nodeProperties).getChildRef();
        return result;
    }

    @Override
    public NodeRef createNewFile(String fullPath, InputStream content) {
        return createNewFile(fullPath, content, new FastMap<QName, Serializable>());
    }

    @Override
    public NodeRef createNewFile(String fullPath, InputStream content, Map<QName, Serializable> nodeProperties) {
        int idx = StringUtils.lastIndexOf(fullPath, '/');
        String parentPath = StringUtils.substring(fullPath, 0, idx - 1);
        String fileName = StringUtils.substring(fullPath, idx + 1);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        return createNewFile(parentPath, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(String parentPath, String fileName, InputStream content) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, fileName);
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(String parentPath, String fileName, InputStream content, Map<QName, Serializable> nodeProperties) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, fileName);
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content, Map<QName, Serializable> nodeProperties) {
        QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(fileName));
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef newFileNodeRef = persistenceManagerService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_CONTENT, nodeProperties).getChildRef();
        ContentWriter writer = persistenceManagerService.getWriter(newFileNodeRef, ContentModel.PROP_CONTENT, true);
        OutputStream output = writer.getContentOutputStream();
        try {
            IOUtils.copy(content, output);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error while creating new file", e);
            }
        } finally {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(output);
        }
        return newFileNodeRef;
    }

    protected void updateLastNavigationOrder(final NodeRef nodeRef) {
        GeneralLockService sequenceLockService = getService(GeneralLockService.class);
        sequenceLockService.lock(nodeRef.getId());
        try {
            PageNavigationOrderSequenceTO sequenceTO = _pageNavigationOrderSequenceDaoService.getSequence(nodeRef.getId());
            if (sequenceTO == null) {
                String fullPath = getNodePath(nodeRef);
                DmPathTO dmPathTO = new DmPathTO(fullPath);
                List<FileInfo> children = getService(PersistenceManagerService.class).list(nodeRef);
                sequenceTO = new PageNavigationOrderSequenceTO(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath(), 100F * children.size());
                _pageNavigationOrderSequenceDaoService.setSequence(sequenceTO);
            } else {
                sequenceTO = _pageNavigationOrderSequenceDaoService.increaseSequence(nodeRef.getId());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error while updating the last navigation order on " + nodeRef, e);
            }
        } finally {
            sequenceLockService.unlock(nodeRef.getId());
        }

    }

    @Override
    public void deleteNode(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef parentNodeRef = persistenceManagerService.getPrimaryParent(nodeRef).getParentRef();
        persistenceManagerService.deleteNode(nodeRef);
        persistenceManagerService.deleteObjectState(nodeRef.getId());
        while (persistenceManagerService.list(parentNodeRef).size() == 0) {
            NodeRef helperNode = parentNodeRef;
            parentNodeRef = persistenceManagerService.getPrimaryParent(helperNode).getParentRef();
            persistenceManagerService.deleteNode(helperNode);
            persistenceManagerService.deleteObjectState(helperNode.getId());
        }
    }
}
