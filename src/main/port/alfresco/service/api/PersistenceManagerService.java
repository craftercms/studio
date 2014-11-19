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
package org.craftercms.cstudio.alfresco.service.api;

/**
 * @author Sumer Jabri
 * @author Russ Danner
 * @author Dejan Brkic
 * @author Carlos Ortiz
 */

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PersistenceManagerService {


    public NodeRef getCompanyHomeNodeRef();

    public NodeRef getNodeRef(String fullPath);

    public NodeRef getNodeRef(String rootPath, String relativePath);

    public NodeRef getNodeRef(NodeRef rootNode, String relativePath);

    public DmContentItemTO getContentItem(String fullPath) throws ContentNotFoundException, ServiceException;

    public DmContentItemTO getContentItem(String fullPath, boolean populateDependencies) throws ContentNotFoundException, ServiceException;


    /**
     * Content Services
     * get the content specified by nodeRef
     *
     * @param nodeRef content nodeRef
     * @return content as string
     */
    public String getContentAsString(NodeRef nodeRef);


    /**
     * get the content input stream specified by nodeRef
     *
     * @param nodeRef content nodeRef
     * @return content as input stream
     */
    public InputStream getContentAsStream(NodeRef nodeRef);


    /**
     * load xml from a nodeRef that is associated with xml content
     *
     * @param nodeRef
     * @return XML from the file content
     */
    public Document loadXml(NodeRef nodeRef);


    /**
     * get the mime type of the given content
     *
     * @param nodeRef
     * @return mime type
     */
    public String getMimeType(NodeRef nodeRef);

    /**
     * <b>Node Service</b>
     */
    public Serializable getProperty(NodeRef siteRef, QName propName);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> listFolders(NodeRef nodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> listFolders(String fullPath);

    public Document loadXml(String fullPath);

    /**
     * create QName from the given prefixedQName (e.g. cm:content)
     *
     * @param prefixedQName
     * @return QName if the given QName is in a valid format. Otherwise it
     *         returns null
     */
    public QName createQName(String prefixedQName);

    public String getContentAsString(String configPath);

    /**
     * <b>Node Service</b>
     */
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName);

    /**
     * <b>Node Service</b>
     */
    public Path getPath(NodeRef nodeRef);

    /**
     * <b>Node Service</b>
     */
    public ChildAssociationRef createNode(NodeRef parentNodeRef, QName assocContains, QName assocQName, QName typeFolder, Map<QName, Serializable> nodeProperties);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> list(NodeRef parentNodeRef);

    /**
     * <b>Node Service</b>
     */
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef);

    /**
     * <b>Node Service</b>
     */
    public void deleteNode(NodeRef nodeRef);

    /**
     * <b>Content Service</b>
     */
    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update);

    /**
     * <b>File Folder Service</b>
     */
    public FileInfo getFileInfo(NodeRef nodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public boolean exists(String fullPath);

    /**
     * <b>File Folder Service</b>
     */
    public boolean exists(NodeRef nodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public FileInfo getFileInfo(String fullPath);

    /**
     * <b>Lock Service</b>
     */
    public LockStatus getLockStatus(NodeRef nodeRef);

    /**
     * <b>Lock Service</b>
     */
    public void lock(NodeRef nodeRef, LockType lockType);

    /**
     * <b>Lock Service</b>
     */
    public void unlock(NodeRef nodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public ContentReader getReader(NodeRef nodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> listFiles(NodeRef contextNodeRef);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException;

    /**
     * <b>File Folder Service</b>
     */
    public NodeRef searchSimple(NodeRef contextNodeRef, String name);

    /**
     * <b>Publishing Service</b>
     */
    public List<PublishingEvent> getUnpublishEventsForNode(NodeRef unpublishedNode);

    /**
     * <b>Authentication Service</b>
     */
    public String getCurrentUserName();

    /**
     * <b>Version Service</b>
     */
    public VersionHistory getVersionHistory(NodeRef nodeRef);

    /**
     * <b>Authority Service</b>
     */
    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * <b>Authority Service</b>
     */
    public void addAuthority(String roleName, String user);

    /**
     * <b>Authority Service</b>
     */
    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate);

    /**
     * <b>Authority Service</b>
     */
    public void removeAuthority(String parentName, String childName);

    /**
     * <b>Authority Service</b>
     */
    public Set<String> getAuthoritiesForUser(String user);

    /**
     * <b>File folder Service</b>
     */
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName);

    /**
     * <b>Node Service</b>
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName);

    /**
     * <b>Node Service</b>
     */
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, RegexQNamePattern qnamePattern);

    /**
     * <b>File Folder Service</b>
     */
    public ContentWriter getWriter(String fullPath);

    /**
     * <b>Node Service</b>
     */
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value);

    /**
     * <b>Node Service</b>
     */
    public Set<QName> getAspects(NodeRef nodeRef);


    /**
     * <b>Node Service</b>
     */
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties);

    /**
     * <b>Node Service</b>
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames);

    /**
     * <b>Node Service</b>
     */
    public QName getType(NodeRef nodeRef);

    /**
     * <b>Node Service</b>
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef);

    /**
     * <b>Script Service</b>
     */
    public Map<String, Object> buildDefaultModel(NodeRef person, NodeRef companyHome, NodeRef userHome, NodeRef script, NodeRef document, NodeRef space);

    /**
     * <b>Script Service</b>
     */
    public void executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model);

    /**
     * <b>Person Service</b>
     */
    public NodeRef getPerson(String userName);

    public Map<QName, Serializable> getProperties(NodeRef nodeRef);

    /**
     * <b>Copy Service</b>
     */
    public NodeRef copy(NodeRef sourceNodeRef, NodeRef targetParentNodeRef, QName assocTypeQName, QName assocQName);

    /**
     * <b>Node Service</b>
     */
    public void deleteNode(String fullPath);

    /**
     * <b>File Folder Service</b>
     */
    public List<FileInfo> list(String fullPath);

    /**
     * <b>Node Service</b>
     */
    public Serializable getProperty(String fullPath, QName propName);

    /**
     * <b>File Folder Service</b>
     */
    public void move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException;

    /**
     * <b>Node Service</b>
     */
    public void setProperty(String fullPath, QName propName, Serializable value);

    /**
     * <b>Node Service</b>
     */
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName);

    /**
     * <b>Node Service</b>
     */
    public void removeAspect(String fullPath, QName aspectTypeQName);

    /**
     * <b>Node Service</b>
     */
    public void removeProperty(NodeRef nodeRef, QName propName);

    /**
     * <b>Node Service</b>
     */
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties);

    /**
     * <b>Transaction Service</b>
     */
    public RetryingTransactionHelper getRetryingTransactionHelper();

    /**
     * <b>Transaction Service</b>
     */
    public boolean isReadOnly();

    /**
     * <b>Lock Service</b>
     */
    public void unlock(String fullPath);

    /**
     * <b>Workflow Service</b>
     */
    public void cancelWorkflow(String workflowId);

    /**
     * <b>Workflow Service</b>
     */
    public WorkflowTask getTaskById(String taskId);

    /**
     * <b>Workflow Service</b>
     */
    public void endTask(String taskId, String trnasition_launch);

    /**
     * <b>Workflow Service</b>
     */
    public void updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove);

    /**
     * <b>Copy Service</b>
     */
    public void copy(NodeRef sourceNodeRef, NodeRef destinationNodeRef);

    /**
     * <b>Node Service</b>
     */
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(NodeRef nodeRef, QName propertyQName, Status value);

    /**
     * <b>Workflow Service</b>
     */
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query);

    /**
     * <b>Node Service</b>
     */
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName);

    /**
     * <b>File Folder Service</b>
     */
    public ContentWriter getWriter(NodeRef nodeRef);

    /**
     * <b>Person Service</b>
     */
    public boolean createMissingPeople();

    /**
     * <b>Person Service</b>
     */
    public void setCreateMissingPeople(boolean createMissing);

    /**
     * <b>Person Service</b>
     */
    public NodeRef createPerson(Map<QName, Serializable> properties);

    /**
     * <b>Mutable Authentication Service</b>
     */
    public void createAuthentication(String userName, char[] password);

    /**
     * <b>Authority Service</b>
     */
    public String getName(AuthorityType type, String shortName);

    /**
     * <b>Permition Service</b>
     */
    public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow);

    /**
     * <b>Rule Service</b>
     */
    public void removeAllRules(NodeRef nodeRef);

    /**
     * <b>Rule Service</b>
     */
    public void saveRule(NodeRef nodeRef, Rule rule);

    /**
     * <b>Action Service</b>
     */
    public CompositeAction createCompositeAction();

    /**
     * <b>Action Service</b>
     */
    public Action createAction(String actionName);

    /**
     * <b>Action Service</b>
     */
    public ActionCondition createActionCondition(String name);

    /**
     * <b>Mime Type Service</b>
     */
    public String guessMimetype(String fileName);

    /**
     * <b>Dictionary Service</b>
     */
    public PropertyDefinition getProperty(QName propertyName);

    /**
     * <b>Authority Service</b>
     */
    public boolean authorityExists(String name);

    /**
     * <b>Authority Service</b>
     */
    public void createAuthority(AuthorityType type, String shortName, String authorityDisplayName, Set<String> authorityZones);

    /**
     * <b>Person Service</b>
     */
    public boolean personExists(String userName);

    /**
     * <b>Authentication Service</b>
     */
    public void authenticate(String userName, char[] password);

    /**
     * <b>Action Service Service</b>
     */
    public CompositeActionCondition createCompositeActionCondition();

    /**
     * <b>Rule Service</b>
     */
    public List<Rule> getRules(NodeRef nodeRef);

    /**
     * <b>Node Service</b>
     */
    public void removeChild(NodeRef parentRef, NodeRef childRef);

    /**
     * <b>Node Service</b>
     */
    public void moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName, QName assocQName);

    /**
     * <b>File Folder Service</b>
     */
    public ContentReader getReader(String fullPath);

    /**
     * <b>Version Service</b>
     */
    public Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties);

    /**
     * <b>Version Service</b>
     */
    public void revert(NodeRef nodeRef, Version versionTo);

    /**
     * <b>Content Service</b>
     */
    public ContentReader getReader(NodeRef nodeRef, QName propertyQName);

    /**
     * <b>Dictionary Service</b>
     */
    public TypeDefinition getType(QName qName);

    /**
     * <b>WorkFlow Service</b>
     */
    public Serializable createPackage(NodeRef container);

    /**
     * <b>Script Service</b>
     */
    public Object executeScriptString(String script, Map<String, Object> model);

    /**
     * Transaction Service
     */
    public UserTransaction getNonPropagatingUserTransaction();

    public ObjectStateService.State getObjectState(String fullPath);

    public ObjectStateService.State getObjectState(NodeRef nodeRef);

    public void setObjectState(String fullPath, ObjectStateService.State state);

    public void setObjectState(NodeRef nodeRef, ObjectStateService.State state);

    public void transition(String fullPath, ObjectStateService.TransitionEvent event);

    public void transition(NodeRef nodeRef, ObjectStateService.TransitionEvent event);

    public void setSystemProcessing(String fullPath, boolean isSystemProcessing);

    public void setSystemProcessing(NodeRef nodeRef, boolean isSystemProcessing);

    public void setSystemProcessingBulk(List<String> objectIds, final boolean isSystemProcessing);

    public String getNodePath(NodeRef nodeRef);

    public String getRelativeNodePath(NodeRef rootNodeRef, NodeRef nodeRef);

    public NodeRef createNewFolder(String fullPath);

    public NodeRef createNewFolder(String fullPath, Map<QName, Serializable> nodeProperties);

    public NodeRef createNewFolder(String parentPath, String folderName);

    public NodeRef createNewFolder(String parentPath, String folderName, Map<QName, Serializable> nodeProperties);

    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName);

    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName, Map<QName, Serializable> nodeProperties);

    public NodeRef createNewFile(String fullPath, InputStream content);

    public NodeRef createNewFile(String fullPath, InputStream content, Map<QName, Serializable> nodeProperties);

    public NodeRef createNewFile(String parentPath, String fileName, InputStream content);

    public NodeRef createNewFile(String parentPath, String fileName, InputStream content, Map<QName, Serializable> nodeProperties);

    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content);

    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content, Map<QName, Serializable> nodeProperties);

    public void insertNewObjectEntry(String fullPath);

    public void insertNewObjectEntry(NodeRef nodeRef);

    public void deleteSite(String site);

    public List<NodeRef> getSubmittedItems(String site);

    public void setManagerPermissions(NodeRef nodeRef, String authority);

    public void rename(NodeRef nodeRef, String newName) throws FileNotFoundException;

    public void updateObjectPath(String fullPath, String newPath);

    public void updateObjectPath(NodeRef nodeRef, String newPath);

    public boolean isUpdatedOrNew(String fullPath);

    public boolean isUpdatedOrNew(NodeRef nodeRef);

    public void initializeCacheScope(String site);

    public void initializeCacheScope(String site, int maxItems);

    public boolean isNew(String fullPath);

    public boolean isNew(NodeRef nodeRef);

    public List<NodeRef> getChangeSet(String site);

    public void deleteObjectState(String objectId);

    public void deleteObjectStateForPath(String site, String path);

    public void deleteObjectStateForPaths(String site, List<String> paths);

    public void disableBehaviour(QName className);

    public void disableBehaviour(NodeRef nodeRef, QName className);

    public void enableBehaviour(QName className);

    public void enableBehaviour(NodeRef nodeRef, QName className);

    public void transitionBulk(List<String> objectIds, ObjectStateService.TransitionEvent event, ObjectStateService.State defaultTargetState);

    public void createLiveRepository(String site);

    public boolean isScheduled(String fullPath);

    public boolean isScheduled(NodeRef nodeRef);

    public String getAdministratorUserName();

    public boolean isInWorkflow(String fullPath);

    public boolean isInWorkflow(NodeRef nodeRef);

    public NodeRef.Status getNodeStatus(NodeRef nodeRef);
}
