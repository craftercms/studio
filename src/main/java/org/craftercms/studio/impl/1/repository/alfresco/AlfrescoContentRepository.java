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
/*
import javolution.util.FastMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEngineConstants;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEventItem;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.filter.DmFilterWrapper;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.event.EventService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.PublishingChannelConfigTO;
import org.craftercms.cstudio.alfresco.to.PublishingChannelGroupConfigTO;
*/
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.transaction.*;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.net.*;
import javax.servlet.http.*;

import net.sf.json.*;

import org.apache.commons.io.IOUtils;

import org.craftercms.cstudio.api.log.Logger;
import org.craftercms.cstudio.api.log.LoggerFactory;
import org.craftercms.cstudio.api.service.deployment.CopyToEnvironmentItem;
import org.craftercms.cstudio.api.service.deployment.DeploymentException;
import org.craftercms.cstudio.api.service.deployment.PublishingTargetItem;
import org.craftercms.cstudio.api.service.fsm.TransitionEvent;
import org.craftercms.cstudio.impl.repository.AbstractContentRepository;

import org.craftercms.commons.http.RequestContext;
/**
 * Alfresco repository implementation.  This is the only point of contact with Alfresco's API in
 * the entire system under the org.craftercms.cstudio.impl package structure
 * @author russdanner
 *
 */

public class AlfrescoContentRepository extends AbstractContentRepository {

    protected static final String MSG_ERROR_RUN_AS_FAILED = "err_alfresco_run_as_failure";
    protected static final String MSG_NODE_REF_IS_NULL_FOR_PATH = "alfresco_noderef_null_for_path";
    protected static final String MSG_CONTENT_FOR_FOLDER_REQUESTED = "alfresco_content_for_folder_requested";

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoContentRepository.class);

    /**
     * perform operation as a specific user
     * @param userName the name of the user account performing the operation
     * @param obj the object that contains the method to executre
     * @param work the method that represents the work to perform
     * @param args any number of arguments to pass to the method
     */
    private static final String SITE_REPO_ROOT_PATTERN = "/wem-projects/{site}/{site}/work-area";
    private static final String SITE_ENVIRONMENT_ROOT_PATTERN = "/wem-projects/{site}/{site}/{environment}";
    private static final String SITE_REPLACEMENT_PATTERN = "\\{site\\}";
    private static final String ENVIRONMENT_REPLACEMENT_PATTERN = "\\{environment\\}";
    private static final String WORK_AREA_REPOSITORY = "work-area";
    private static final String LIVE_REPOSITORY = "live";

    protected String getAlfTicket() {
        String ticket = "UNSET";
        RequestContext context = RequestContext.getCurrent();
        HttpServletRequest request = context.getRequest();

        if(request != null) {
            ticket = request.getParameter("alf_ticket");

            if(ticket == null) {
                // check the cookies
                Cookie[] cookies = request.getCookies();
                for(int i=0; i<cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if(cookie.getName().equals("alf_ticket")) {
                        ticket = cookie.getValue();
                        break;
                    }
                    else if(cookie.getName().equals("ccticket")) {
                        ticket = cookie.getValue();
                        break;
                    }
                }
            }
        }
        else {
            // maybe inside a chron trigger on other not request context
        }

        return ticket;
    }

    protected InputStream alfrescoGetRequest(String uri, Map<String, String> params) throws Exception {
        InputStream retResponse = null;
        String serviceUrlBase = "http://127.0.0.1:8080/alfresco/service";
        String ticket = getAlfTicket();

        for(String key : params.keySet()) {
            uri = uri.replace("{"+key+"}", URLEncoder.encode(params.get(key), "utf-8"));
        }

        URI serviceURI = new URI(serviceUrlBase + uri + "&alf_ticket="+ticket);        

        retResponse = serviceURI.toURL().openStream();
     
        return retResponse;
    }

    protected String getNodeRefForPath(String path) { 
        String nodeRef = null;
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
        String namespacedPath = cleanPath.replaceAll("/", "/cm:");
        String query = "PATH:\"/app:company_home" + namespacedPath + "\"";

        String lookupNodeRefURI = "/slingshot/node/search?q={q}&lang={lang}&store={store}&maxResults={maxResults}";
        params.put("q", query);
        params.put("lang","fts-alfresco");
        params.put("store", "workspace://SpacesStore");
        params.put("maxResults", "100");

        try{
            InputStream responseStream = this.alfrescoGetRequest(lookupNodeRefURI, params);
            String jsonResponse = IOUtils.toString(responseStream, "utf-8");
            JsonConfig cfg = new JsonConfig();
            JSONObject root = JSONObject.fromObject(jsonResponse, cfg);
            int resultCount = root.getInt("numResults");

            if(resultCount == 1) {
                JSONObject result = root.getJSONArray("results").getJSONObject(0);
                nodeRef = result.getString("nodeRef");
            }
            else if(resultCount == 0) {
                throw new Exception("no results for query (" + query + ")");
            }
            else {
                throw new Exception("too many results (" + resultCount + ") for query (" + query + ")");
            }

        }
        catch(Exception err) {
            System.out.println("err getting noderef for path (" + path + "): "+err);   
        }

        return nodeRef;
    }

    /**
     * this method is no longer meaningful
     * take it out and clean up the code, it's just an extra later of reflections at this point
     */
    public Object runAs(final String userName, final Object obj, final Method work, final Object ... args) {
        Object retObject = null;

        try {
            try {
                retObject =  work.invoke(obj, null);
            }
            catch(Exception err) {
                logger.error(MSG_ERROR_RUN_AS_FAILED, err, userName, obj.getClass().getName());
            }
        }
        catch(Exception err) {
            logger.error(MSG_ERROR_RUN_AS_FAILED, err, userName, obj.getClass().getName());
        }

        return retObject;
    }

    /**
     * get transaction
     */
    public UserTransaction getTransaction() {
    //    return _transactionService.getUserTransaction();
        return null;
    }

    /**
     * @return true if site has content object at path
     */
    public boolean contentExists(String site, String path) {
        return false;
        //return _dmContentService.contentExists(site, path, path);
    }

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    public InputStream getContent(String path) {
        InputStream retStream = null;
        String name = path.substring(path.lastIndexOf("/")+1);

        try {
            String nodeRef = getNodeRefForPath(path);

            if(nodeRef != null) {
                // construct and execute url to download result
                String downloadURI = "/api/node/content/workspace/SpacesStore/{nodeRef}/{name}?a=true";
                Map<String, String> lookupContentParams = new HashMap<String, String>();
                lookupContentParams.put("nodeRef", nodeRef.replace("workspace://SpacesStore/", ""));
                lookupContentParams.put("name", name);

                retStream = this.alfrescoGetRequest(downloadURI, lookupContentParams);
            }
            else {
                throw new Exception("nodeRef not found for path: [" + path + "]");
            }
        }
        catch(Exception err) {
            System.out.println("err getting content: " + err);   
        }

        return retStream;
    }

    public String getContentAsString(String path) throws Exception {
        return IOUtils.toString(this.getContent(path));
    }

    public void writeContent(String path, InputStream content) {
    //     PersistenceManagerService persistenceManagerService = _servicesManager.getService(PersistenceManagerService.class);
    //     //NodeRef nodeRef = persistenceManagerService.getNodeRef(path);

    //     //if (nodeRef != null) {
    //     ContentWriter writer = persistenceManagerService.getWriter(path);
    //     writer.putContent(content);
    //     //}
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