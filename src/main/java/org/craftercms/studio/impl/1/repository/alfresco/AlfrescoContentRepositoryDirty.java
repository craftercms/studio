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
package org.craftercms.cstudio.impl.repository.alfresco;


import javax.transaction.*;
import java.io.InputStream;
import java.util.*;

import org.craftercms.cstudio.impl.repository.AbstractContentRepository;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingTargetItem;
import org.craftercms.studio.api.v1.service.fsm.TransitionEvent;

/**
 * Alfresco repository implementation.  This is the only point of contact with Alfresco's API in
 * the entire system under the org.craftercms.cstudio.impl package structure
 * @author russdanner
 *
 */

public class AlfrescoContentRepositoryDirty extends AlfrescoContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoContentRepositoryDirty.class);



// Everything below this line must go
// And this class must be destroyed
// All of these calls MUST come out of the content repository interface


    protected static final String MSG_ERROR_RUN_AS_FAILED = "err_alfresco_run_as_failure";
    protected static final String MSG_NODE_REF_IS_NULL_FOR_PATH = "alfresco_noderef_null_for_path";
    protected static final String MSG_CONTENT_FOR_FOLDER_REQUESTED = "alfresco_content_for_folder_requested";

    private static final String SITE_REPO_ROOT_PATTERN = "/wem-projects/{site}/{site}/work-area";
    private static final String SITE_ENVIRONMENT_ROOT_PATTERN = "/wem-projects/{site}/{site}/{environment}";
    private static final String SITE_REPLACEMENT_PATTERN = "\\{site\\}";
    private static final String ENVIRONMENT_REPLACEMENT_PATTERN = "\\{environment\\}";
    private static final String WORK_AREA_REPOSITORY = "work-area";
    private static final String LIVE_REPOSITORY = "live";


/* ====== */
//everthing below here must go
    /**
     * write content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     * @param content is the bits to write
     */
    public void writeContent(String site, String variant, String store, String path, InputStream content) {
        writeContent("/wem-projects/"+site+"/"+site+"/"+store + path, content);
    }

    // get rid of this
    /**
     * get content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     */
    public InputStream getContent(String site, String variant, String store, String path) {
        return getContent("/wem-projects/"+site+"/"+site+"/"+store + path);
    }
    
    /**
     * get transaction
     */
    public UserTransaction getTransaction() {
    //    return _transactionService.getUserTransaction();
        return null;
    }



    @Override
    public void stateTransition(String site, List<String> paths, TransitionEvent event) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     List<String> objectIds = new ArrayList<String>();
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     for (String relativePath : paths) {
    //         NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, relativePath);
    //         if (nodeRef != null) {
    //             objectIds.add(nodeRef.getId());
    //         }
    //     }
    //     ObjectStateService.TransitionEvent convertedEvent = eventConversionMap.get(event);
    //     ObjectStateService.State defaultState = defaultStateForEvent.get(event);
    //     persistenceManagerService.transitionBulk(objectIds, convertedEvent, defaultState);
    }

    @Override
    public void stateTransition(String site, String path, TransitionEvent event) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     List<String> objectIds = new ArrayList<String>();
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);
    //     if (nodeRef != null) {
    //         ObjectStateService.TransitionEvent convertedEvent = eventConversionMap.get(event);
    //         persistenceManagerService.transition(nodeRef, convertedEvent);
    //    }
    }

    // private final Map<TransitionEvent, ObjectStateService.TransitionEvent> eventConversionMap = new HashMap<TransitionEvent, ObjectStateService.TransitionEvent>() {{
    //     put(TransitionEvent.SCHEDULED_DEPLOYMENT, ObjectStateService.TransitionEvent.SUBMIT_WITHOUT_WORKFLOW_SCHEDULED);
    //     put(TransitionEvent.DEPLOYMENT, ObjectStateService.TransitionEvent.DEPLOYMENT);
    //     put(TransitionEvent.DELETE, ObjectStateService.TransitionEvent.DELETE);
    // }};

    // private final Map<TransitionEvent, ObjectStateService.State> defaultStateForEvent = new HashMap<TransitionEvent, ObjectStateService.State>() {{
    //     put(TransitionEvent.SCHEDULED_DEPLOYMENT, ObjectStateService.State.NEW_SUBMITTED_NO_WF_SCHEDULED);
    //     put(TransitionEvent.DEPLOYMENT, ObjectStateService.State.EXISTING_UNEDITED_UNLOCKED);
    //     put(TransitionEvent.DELETE, ObjectStateService.State.EXISTING_DELETED);
    // }};

    @Override
    public void setSystemProcessing(String site, List<String> paths, boolean isSystemProcessing) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     List<String> objectIds = new ArrayList<String>();
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     for (String relativePath : paths) {
    //         NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, relativePath);
    //         if (nodeRef != null) {
    //             objectIds.add(nodeRef.getId());
    //         }
    //     }
    //     persistenceManagerService.setSystemProcessingBulk(objectIds, isSystemProcessing);
    }

    @Override
    public void setSystemProcessing(String site, String path, boolean isSystemProcessing) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);
    //     if (nodeRef != null) {
    //         persistenceManagerService.setSystemProcessing(nodeRef, isSystemProcessing);
    //     }
    }

    @Override
    public void createNewVersion(String site, String path, String submissionComment, boolean isMajorVersion) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);
    //     if (nodeRef != null) {
    //         DmVersionService dmVersionService = _servicesManager.getService(DmVersionService.class);
    //         if (isMajorVersion) {
    //             dmVersionService.createNextMajorVersion(site, path, submissionComment);
    //         } else {
    //             dmVersionService.createNextMinorVersion(site, path);
    //         }
    //     }
    }

    @Override
    public void setLockBehaviourEnabled(boolean enabled) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     if (enabled) {
    //         persistenceManagerService.enableBehaviour(ContentModel.ASPECT_LOCKABLE);
    //     } else {
    //         persistenceManagerService.disableBehaviour(ContentModel.ASPECT_LOCKABLE);
    //     }
    }

    @Override
    public void copyToEnvironment(String site, String environment, String path) throws DeploymentException {
    //     ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
    //     PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
    //     String siteRepoRootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);

    //     String envRepoPath = SITE_ENVIRONMENT_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     envRepoPath = envRepoPath.replaceAll(ENVIRONMENT_REPLACEMENT_PATTERN, environment);

    //     NodeRef envRepoRoot = persistenceManagerService.getNodeRef(envRepoPath);

    //     NodeRef envNode = persistenceManagerService.getNodeRef(envRepoRoot, path);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(siteRepoRootPath, path);
    //     if (nodeRef != null) {
    //         if (envNode == null) {
    //             envNode = createLiveRepositoryCopy(envRepoRoot, path, nodeRef);
    //         } else {
    //             FileInfo envNodeInfo = persistenceManagerService.getFileInfo(envNode);
    //             if (envNodeInfo.isFolder()) {
    //                 Map<QName, Serializable> copyNodeProps = persistenceManagerService.getProperties(nodeRef);
    //                 copyNodeProps.put(ContentModel.PROP_NAME, envNodeInfo.getName());
    //                 persistenceManagerService.setProperties(envNode, copyNodeProps);
    //             } else {
    //                 persistenceManagerService.copy(nodeRef, envNode);
    //             }
    //         }
    //         Serializable sendEmailValue = persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_WEB_WF_SEND_EMAIL);
    //         boolean sendEmail = (sendEmailValue != null) && (Boolean) sendEmailValue;

    //         if (sendEmail) {
    //             Serializable submittedByValue = persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
    //             String submittedBy = "";
    //             if (submittedByValue != null) {
    //                 submittedBy = (String) submittedByValue;
    //             } else {
    //                 logger.error("did not send approval notification as submitted by property is null");
    //                 return;
    //             }
    //             //DmPathTO path = new DmPathTO(nodePath);
    //             String approver = (String) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_WEB_APPROVED_BY);
    //             NotificationService notificationService = getServicesManager().getService(NotificationService.class);
    //             notificationService.sendApprovalNotification(site, submittedBy, path, approver);
    //                 /*
    //                 * Remove this sendmail property as we are done sending email
    //                 */
    //             persistenceManagerService.removeProperty(nodeRef, CStudioContentModel.PROP_WEB_WF_SEND_EMAIL);
    //         }

    //         Map<QName, Serializable> nodeProps = persistenceManagerService.getProperties(envNode);
    //         for (QName propName : DmConstants.SUBMITTED_PROPERTIES) {
    //             nodeProps.remove(propName);
    //         }
    //         persistenceManagerService.setProperties(envNode, nodeProps);

    //         nodeProps = persistenceManagerService.getProperties(nodeRef);
    //         for (QName propName : DmConstants.SUBMITTED_PROPERTIES) {
    //             nodeProps.remove(propName);
    //         }
    //         persistenceManagerService.setProperties(nodeRef, nodeProps);
    //     }
    }

    // protected NodeRef createEnvRepository(String site, String envRepoName) {
    //     PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
    //     String siteRoot = SITE_ENVIRONMENT_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     siteRoot = siteRoot.replaceAll(ENVIRONMENT_REPLACEMENT_PATTERN, "");
    //     NodeRef siteRootNode = persistenceManagerService.getNodeRef(siteRoot);
    //     NodeRef result = persistenceManagerService.createNewFolder(siteRootNode, envRepoName);
    //     return result;
    // }

    // protected NodeRef createLiveRepositoryCopy(NodeRef liveRepoRoot, String relativePath, NodeRef nodeRef) {
    //     logger.debug("[PUBLISHING POST PROCESSOR] creating live repository copy of " + relativePath);
    //     PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
    //     NodeRef result = null;

    //     String[] pathSegments = relativePath.split("/");
    //     NodeRef helperNode = liveRepoRoot;
    //     NodeRef parent = null;
    //     for (int i = 0; i < pathSegments.length - 1; i++) {
    //         if (!"".equals(pathSegments[i])) {
    //             parent = helperNode;
    //             helperNode = persistenceManagerService.getChildByName(helperNode, ContentModel.ASSOC_CONTAINS, pathSegments[i]);
    //             if (helperNode == null) {
    //                 logger.debug("[WORKFLOW] creating a node with name: " + pathSegments[i]);
    //                 Map<QName, Serializable> properties = new FastMap<QName, Serializable>();
    //                 properties.put(ContentModel.PROP_NAME, pathSegments[i]);
    //                 helperNode = persistenceManagerService.createNewFolder(parent, pathSegments[i], properties);
    //             }
    //         }
    //     }
    //     String nodeName = (String) persistenceManagerService.getProperty(nodeRef, ContentModel.PROP_NAME);
    //     QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(nodeName));
    //     result = persistenceManagerService.copy(nodeRef, helperNode, ContentModel.ASSOC_CONTAINS, assocQName);
    //     persistenceManagerService.setProperty(result, ContentModel.PROP_NAME, nodeName);
    //     return result;
    // }

    // @Override
    public Set<String> getAllAvailableSites() {
    //     SiteService siteService = _servicesManager.getService(SiteService.class);
    //     return siteService.getAllAvailableSites();
        return null;
    }

    // @Override
    public Set<PublishingTargetItem> getAllTargetsForSite(String site) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     SiteService siteService = _servicesManager.getService(SiteService.class);
    //     Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
    //     Set<PublishingTargetItem> targets = new HashSet<PublishingTargetItem>();
    //     Map<String, PublishingTargetItem> targetMap = new HashMap<String, PublishingTargetItem>();
    //     if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
    //         for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
    //             List<PublishingChannelConfigTO> channelConfigTOs = groupConfigTO.getChannels();
    //             if (channelConfigTOs != null && channelConfigTOs.size() > 0) {
    //                 for (PublishingChannelConfigTO channelConfigTO : channelConfigTOs) {
    //                     DeploymentEndpointConfigTO endpoint = siteService.getDeploymentEndpoint(site, channelConfigTO.getName());
    //                     if (endpoint != null) {
    //                         PublishingTargetItem targetItem = targetMap.get(endpoint.getName());
    //                         if (targetItem == null) {
    //                             targetItem = new PublishingTargetItem();
    //                             targetItem.setId(endpoint.getName());
    //                             targetItem.setName(endpoint.getName());
    //                             targetItem.setTarget(endpoint.getTarget());
    //                             targetItem.setType(endpoint.getType());
    //                             targetItem.setServerUrl(endpoint.getServerUrl());
    //                             targetItem.setStatusUrl(endpoint.getStatusUrl());
    //                             targetItem.setVersionUrl(endpoint.getVersionUrl());
    //                             targetItem.setPassword(endpoint.getPassword());
    //                             targetItem.setExcludePattern(endpoint.getExcludePattern());
    //                             targetItem.setIncludePattern(endpoint.getIncludePattern());
    //                             targetItem.setBucketSize(endpoint.getBucketSize());
    //                             targetItem.setSiteId(endpoint.getSiteId());
    //                             targetItem.setSendMetadata(endpoint.isSendMetadata());
    //                             targets.add(targetItem);
                                
    //                             targetMap.put(endpoint.getName(), targetItem);
    //                         }
    //                         targetItem.addEnvironment(groupConfigTO.getName());

    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return targets;
        return null;
    }

    // @Override
    public String getCurrentUser() {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     return persistenceManagerService.getCurrentUserName();
            return "";
    }

    // @Override
    public String getAdministratorUser() {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     return persistenceManagerService.getAdministratorUserName();
        return "";
    }

    // @Override
    public boolean isNew(String site, String path) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String fullPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site) + path;
    //     return persistenceManagerService.isNew(fullPath);
        return false;
    }

    // @Override
    public String getFilename(String site, String path) {
    //     if (path != null && !path.isEmpty()) {
    //         PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //         NodeRef nodeRef = persistenceManagerService.getNodeRef(SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site), path);
    //         if (nodeRef != null) {
    //             return DefaultTypeConverter.INSTANCE.convert(String.class, persistenceManagerService.getProperty(nodeRef, ContentModel.PROP_NAME));
    //         } else {
    //             int idx = path.lastIndexOf("/");
    //             if (idx > 0) {
    //                 return path.substring(idx + 1);
    //             } else {
    //                 return path;
    //             }
    //         }
    //     } else {
    //         return "";
    //     }
        return "";
    }

    // @Override
    public boolean isRenamed(String site, String path) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site), path);
    //     if (nodeRef != null) {
    //         return persistenceManagerService.hasAspect(nodeRef, CStudioContentModel.ASPECT_RENAMED);
    //     } else {
    //         return false;
    //     }
        return false;
    }

    // @Override
    public String getOldPath(String site, String path) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site), path);
    //     if (nodeRef != null) {
    //         if (persistenceManagerService.hasAspect(nodeRef, CStudioContentModel.ASPECT_RENAMED)) {
    //             String oldPath = DefaultTypeConverter.INSTANCE.convert(String.class, persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_RENAMED_OLD_URL));
    //             return oldPath;
    //         }
    //     }
    //     return null;
        return null;
    }

    // @Override
    public InputStream getMetadataStream(String site, String path) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site), path);
    //     Map<QName, Serializable> contentProperties = persistenceManagerService.getProperties(nodeRef);
    //     Document metadataDoc = DocumentHelper.createDocument();
    //     Element root = metadataDoc.addElement("metadata");
    //     for (Map.Entry<QName, Serializable> property : contentProperties.entrySet()) {
    //         Element elem = root.addElement(property.getKey().getLocalName());
    //         elem.addText(String.valueOf(property.getValue()));
    //     }

    //     return IOUtils.toInputStream(metadataDoc.asXML());
        return null;
    }

 //   public void publishDeployEvent(String endpoint, List<DeploymentEventItem> items) {
    //     EventService eventService = _servicesManager.getService(EventService.class);
    //     JSONObject jsonObject = new JSONObject();
    //     jsonObject.put("endpoint", endpoint);
    //     jsonObject.put("items", items);
    //     eventService.publish(DeploymentEngineConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY, jsonObject);
  //  }

    // @Override
    public void deleteContent(String site, String environment, String path) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String fullPath = SITE_ENVIRONMENT_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     fullPath = fullPath.replaceAll(ENVIRONMENT_REPLACEMENT_PATTERN, environment);
    //     fullPath = fullPath + path;
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
    //     if (nodeRef != null) {
    //         NodeRef parentNode = persistenceManagerService.getPrimaryParent(nodeRef).getParentRef();
    //         persistenceManagerService.deleteNode(nodeRef);
    //         List<FileInfo> children = persistenceManagerService.list(parentNode);
    //         while ( children == null || children.size() < 1) {
    //             NodeRef helpNode = parentNode;
    //             parentNode = persistenceManagerService.getPrimaryParent(helpNode).getParentRef();
    //             persistenceManagerService.deleteNode(helpNode);
    //             children = persistenceManagerService.list(parentNode);
    //         }
    //     }
    }

    // @Override
    public void deleteContent(CopyToEnvironmentItem item) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String fullPath = SITE_ENVIRONMENT_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, item.getSite());
    //     fullPath = fullPath.replaceAll(ENVIRONMENT_REPLACEMENT_PATTERN, WORK_AREA_REPOSITORY);
    //     fullPath = fullPath + item.getPath();
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
    //     if (nodeRef != null) {
    //         //_dmContentService.deleteContent(site, path, true, true, null);
    //         //return;
    //         List<String> paths = new ArrayList<String>();
    //         paths.add(item.getPath());
    //         _dmContentService.generateDeleteActivity(item.getSite(), paths, item.getUser());
    //         NodeRef parentNode = persistenceManagerService.getPrimaryParent(nodeRef).getParentRef();
    //         persistenceManagerService.deleteNode(nodeRef);
    //         persistenceManagerService.deleteObjectState(nodeRef.getId());
    //         List<FileInfo> children = persistenceManagerService.list(parentNode);
    //         while ( children == null || children.size() < 1) {
    //             NodeRef helpNode = parentNode;
    //             parentNode = persistenceManagerService.getPrimaryParent(helpNode).getParentRef();
    //             persistenceManagerService.deleteNode(helpNode);
    //             children = persistenceManagerService.list(parentNode);
    //         }
    //     }
    }

    // @Override
    public void clearRenamed(String site, String path) {
    //     String fullPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     fullPath = fullPath + path;
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
    //     if (nodeRef != null) {
    //         persistenceManagerService.removeAspect(fullPath, CStudioContentModel.ASPECT_RENAMED);
    //         Map<QName, Serializable> props = persistenceManagerService.getProperties(nodeRef);
    //         props.remove(CStudioContentModel.PROP_RENAMED_OLD_URL);
    //         persistenceManagerService.setProperties(nodeRef, props);
    //     }
    }

    // @Override
    public String getContentTypeClass(String site,  String path) {
        return "";
    //     if (_dmFilterWrapper.accept(site, path, DmConstants.CONTENT_TYPE_COMPONENT)) {
    //         return DmConstants.CONTENT_TYPE_COMPONENT;
    //     }else if  (_dmFilterWrapper.accept(site, path, DmConstants.CONTENT_TYPE_ASSET)){
    //         return DmConstants.CONTENT_TYPE_ASSET;
    //     }else if  (_dmFilterWrapper.accept(site, path, DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE)){
    //         return DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
    //     }else if  (_dmFilterWrapper.accept(site, path, DmConstants.CONTENT_TYPE_PAGE)){
    //         return DmConstants.CONTENT_TYPE_PAGE;
    //     }else if  (_dmFilterWrapper.accept(site, path, DmConstants.CONTENT_TYPE_DOCUMENT)){
    //         return DmConstants.CONTENT_TYPE_DOCUMENT;
    //     } else {
    //         return DmConstants.CONTENT_TYPE_OTHER;
    //     }
    }

    // @Override
    public String getFullPath(String site, String path) {
    //     String fullPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     fullPath = fullPath + path;
    //     return fullPath;
        return "";
    }

    // @Override
    public List<String> getDependentPaths(String site, String path) {
    //     DmDependencyService dmDependencyService = _servicesManager.getService(DmDependencyService.class);
    //     return dmDependencyService.getDependencyPaths(site, path);
        return null;
    }

    // @Override
    public boolean isFolder(String site, String path) {
        boolean toRet = false;
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);

    //     if (nodeRef != null) {
    //         FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
    //         toRet = fileInfo.isFolder();
    //     }
         return toRet;
    }

    // @Override
    public boolean environmentRepoExists(String site, String environment) {
    //     PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);

    //     String envRepoPath = SITE_ENVIRONMENT_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     envRepoPath = envRepoPath.replaceAll(ENVIRONMENT_REPLACEMENT_PATTERN, environment);

    //     return persistenceManagerService.exists(envRepoPath);
        return false;
    }

    // @Override
    public void createEnvironmentRepo(String site, String environment) {
    //     createEnvRepository(site, environment);
    }

    // @Override
    public String getLiveEnvironmentName(String site) {
        // SiteService siteService = getServicesManager().getService(SiteService.class);
        // return siteService.getLiveEnvironmentName(site);
        return null;
    }

    // @Override
    public Set<String> getAllPublishingEnvironments(String site) {
        // SiteService siteService = _servicesManager.getService(SiteService.class);
        // Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        // Set<String> environments = new HashSet<String>();
        // if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
        //     for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
        //         if (StringUtils.isNotEmpty(groupConfigTO.getName())) {
        //             environments.add(groupConfigTO.getName());
        //         }
        //     }
        // }
        // return environments;
        return null;
    }

    // @Override
    public void lockRepository() {
    //     GeneralLockService generalLockService = getServicesManager().getService(GeneralLockService.class);
    //     generalLockService.lock(GeneralLockService.MASTER_LOCK);
    }

    // @Override
    public void unlockRepository() {
    //     GeneralLockService generalLockService = getServicesManager().getService(GeneralLockService.class);
    //     generalLockService.unlock(GeneralLockService.MASTER_LOCK);
    }

    // @Override
    public void lockItem(final String site, final String path) {
    // MOVE TO REPO INTERFACE
    //     GeneralLockService generalLockService = getServicesManager().getService(GeneralLockService.class);
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);
    //     if (nodeRef != null) {
    //         generalLockService.lock(nodeRef.getId());
    //     }
    }

    // @Override
    public void unLockItem(final String site, final String path) {
// MOVE TO REPO INTERFACE
    //     GeneralLockService generalLockService = getServicesManager().getService(GeneralLockService.class);
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     String rootPath = SITE_REPO_ROOT_PATTERN.replaceAll(SITE_REPLACEMENT_PATTERN, site);
    //     NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, path);
    //     if (nodeRef != null) {
    //         generalLockService.unlock(nodeRef.getId());
    //     }
    }

    // /** dmContentService getter */
    // public DmContentService getDmContentService() { return _dmContentService; }
    // /** dmContentService setter */
    // public void setDmContentService(DmContentService service) { _dmContentService = service; }

    // public ServicesManager getServicesManager() { return _servicesManager; }
    // public void setServicesManager(ServicesManager servicesManager) {  this._servicesManager = servicesManager; }

    //public org.alfresco.service.transaction.TransactionService getTransactionService() { return _transactionService; }
    //public void setTransactionService(org.alfresco.service.transaction.TransactionService transactionService) { _transactionService = transactionService; }

    // public DmFilterWrapper getDmFilterWrapper() { return _dmFilterWrapper; }
    // public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) { this._dmFilterWrapper = dmFilterWrapper; }

    // protected ServicesManager _servicesManager;
    // protected DmContentService _dmContentService;
    // protected org.alfresco.service.transaction.TransactionService _transactionService;
    // protected DmFilterWrapper _dmFilterWrapper;
}