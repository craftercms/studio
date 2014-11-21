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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.cache.cstudioCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.dependency.DependencyEntity;
import org.craftercms.cstudio.alfresco.dm.filter.DmFilterWrapper;
import org.craftercms.cstudio.alfresco.dm.listener.DmWorkflowListener;
import org.craftercms.cstudio.alfresco.dm.service.api.*;
import org.craftercms.cstudio.alfresco.dm.to.*;
import org.craftercms.cstudio.alfresco.dm.util.DmContentItemComparator;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.workflow.GoLiveContext;
import org.craftercms.cstudio.alfresco.dm.workflow.MultiChannelPublishingContext;
import org.craftercms.cstudio.alfresco.dm.workflow.RequestContext;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.api.service.deployment.DeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class DmWorkflowServiceImpl extends AbstractRegistrableService implements DmWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowServiceImpl.class);

    protected static final String LIST_CHANGES_QUERY = "PATH:\"/app:company_home{site_root}//*\" AND NOT @cm\\:versionLabel:*\\.0";
    protected static final String SUBMITTED_ITEMS_QUERY = "PATH:\"/app:company_home{site_root}//*\" AND {http://cstudio.com/model/core-web/1.0}status:\"Submitted\"";
    protected static final String SCHEDULED_PUBLISHING_EVENTS_QUERY = "TYPE:\"pub:PublishingEvent\" AND @pub\\:publishingEventStatus:\"SCHEDULED\"";
    protected static final String PUBLISHING_QUEUE_QUERY = "TYPE:\"pub:PublishingQueue\"";

    /**
     * dm workflow property qnames *
     */
    protected static final QName WF_PROP_REVIEW_TYPE = null;//port QName.createQName(
            //org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "reviewType");
    protected static final QName WF_PROP_REVIEWER_CNT = null;//port QName.createQName(
            //org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "reviewerCnt");
    protected static final QName WF_PROP_APPROVE_CNT = null;//port QName.createQName(
            //org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "approveCnt");
    protected static final QName WF_ASSIGNEES = null;//port QName.createQName(
            //org.alfresco.service.namespace.NamespaceService.BPM_MODEL_1_0_URI, "assignees");

    /**
     * dm workflow proerty values *
     */
    protected static final String REVIEW_TYPE_SERIAL = "Serial";

    /**
     * the default order name *
     */
    protected static final String ORDER_DEFAULT = "default";

    /**
     * workflow constants - will move to configuration *
     */
    protected String REVIEW_TASK_NAME = "wcmwf:submitReviewTask";
    protected String TRNASITION_REJECT = "reject";
    protected String TRNASITION_APPROVE = "approve";
    protected String TRNASITION_LAUNCH = "launch";

    /**
     * default review worfklow name to submit to
     */
    protected String _reviewWorkflowName;
    public String getReviewWorkflowName() {
        return _reviewWorkflowName;
    }
    public void setReviewWorkflowName(String reviewWorkflowName) {
        this._reviewWorkflowName = reviewWorkflowName;
    }

    protected DmWorkflowListener _listener;
    public DmWorkflowListener getListener() {
        return _listener;
    }
    public void setListener(DmWorkflowListener listener) {
        this._listener = listener;
    }

    /**
     * default submit direct workflow name to submit to
     */
    protected String _submitDirectWorkflowName;
    public String getSubmitDirectWorkflowName() {
        return _submitDirectWorkflowName;
    }
    public void setSubmitDirectWorkflowName(String submitDirectWorkflowName) {
        this._submitDirectWorkflowName = submitDirectWorkflowName;
    }

    protected DmFilterWrapper _dmFilterWrapper;
    public DmFilterWrapper getDmFilterWrapper() {
        return _dmFilterWrapper;
    }
    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
        this._dmFilterWrapper = dmFilterWrapper;
    }

    protected cstudioCacheManager _cacheManager;
    public cstudioCacheManager getCacheManager() {
        return _cacheManager;
    }
    public void setCacheManager(cstudioCacheManager cacheManager) {
        this._cacheManager = cacheManager;
    }

    protected org.craftercms.cstudio.api.service.deployment.DeploymentService _deploymentService;
    public org.craftercms.cstudio.api.service.deployment.DeploymentService getDeploymentService() {
        return _deploymentService;
    }
    public void setDeploymentService(org.craftercms.cstudio.api.service.deployment.DeploymentService deploymentService) {
        this._deploymentService = deploymentService;
    }

    enum Operation {
        GO_LIVE,
        SUBMIT_TO_GO_LIVE,
        REJECT,

    }

    @Override
    public void register() {
        getServicesManager().registerService(DmWorkflowService.class, this);
    }

    @Override
    public boolean isInFlight(String assetPath) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DmError> submitToGoLive(List<DmDependencyTO> submittedItems, Date scheduledDate, boolean sendEmail, boolean submittedForDeletion, RequestContext requestContext, String submissionContent) throws ServiceException {
        return null;
    }

    protected String getFullPath(String site, DmDependencyTO submittedItem) {
        String uri = submittedItem.getUri();
        if (submittedItem.isDeleted()) {
            // if deleted, replace with the folder path
            uri = uri.replace("/" + DmConstants.INDEX_FILE, "");
        }
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, uri);
        return fullPath;
    }

    /**
     * cancel workflow associated with the given task
     *
     * @param task
     */
    protected void cancelWorkflow(WorkflowTask task) {
        String workflowId =  null;//PORT  task.path.instance.id;
        
        getService(PersistenceManagerService.class).cancelWorkflow(workflowId);
        if (logger.isDebugEnabled()) {
            logger.debug(workflowId + " canceled.");
        }
    }

    protected List<String> prepareWorkflowSubmission(String site, String user, DmDependencyTO submittedItem,
                                                     Date launchDate, boolean sendEmail) throws ServiceException {
        return prepareWorkflowSubmission(site, user, submittedItem, launchDate, sendEmail, false);
    }

    /**
     * prepare the content for workflow submission by attaching submitted aspect
     *
     * @param site
     * @param user
     * @param submittedItem
     * @param launchDate
     * @param sendEmail
     * @return
     * @throws ServiceException
     */
    protected List<String> prepareWorkflowSubmission(String site, String user, DmDependencyTO submittedItem,
                                                     Date launchDate, boolean sendEmail, boolean submittedForDeletion) throws ServiceException {
        List<String> paths = new FastList<String>();
        String fullPath = getFullPath(site, submittedItem);
        DmPathTO path = new DmPathTO(fullPath);
        PersistenceManagerService  persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node =  persistenceManagerService.getNodeRef(fullPath);
        
        if (node != null) {
            if (!submittedItem.isDeleted()) {
            	persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY, user);
            	persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SEND_EMAIL, sendEmail);
            	persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTEDFORDELETION, submittedForDeletion);
                List<String> includedItems = new FastList<String>();
                addSubmittedAspect(site, user, null, submittedItem, launchDate, includedItems);
                for (String includedItem : includedItems) {
                    paths.add(includedItem);
                }
            } else {
                // do nothing if deleted
                String topLevelItem = null;//PORT WCMUtil.getStoreRelativePath(fullPath);
                paths.add(topLevelItem);
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(submittedItem.getUri() + " does not exist.");
            }
        }

        return paths;
    }

    /**
     * add the submitted aspect to each item submitted and set properties
     *
     * @param site
     * @param user
     * @param parentUri
     * @param submittedItem
     * @param scheduledDate
     * @param includedItems
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected void addSubmittedAspect(String site, String user, String parentUri, DmDependencyTO submittedItem,
                                      Date scheduledDate, List<String> includedItems) throws ServiceException {
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, submittedItem.getUri());
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        
        try {
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                // add submitted aspect
                if (!persistenceManagerService.hasAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED)) {
                    // add submitted aspect
                    persistenceManagerService.addAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED, null);

                }

                // set direct child items
                List<String> childDependencies = getDependencies(site, user, submittedItem.getUri(), submittedItem,
                        scheduledDate, includedItems);
                persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_CHILDREN, (Serializable) childDependencies);
                // set a scheduled date

                persistenceManagerService.setProperty(node, null /*PORT WCMWorkflowModel.PROP_LAUNCH_DATE */, scheduledDate);
                Map<QName, Serializable> properties = persistenceManagerService.getProperties(node);
                // add parent URI if not null
                if (!StringUtils.isEmpty(parentUri)) {
                    Serializable parentValue = properties.get(CStudioContentModel.PROP_WEB_WF_PARENT_URI);
                    List<String> parents = (parentValue == null) ? new ArrayList<String>(1) : (List<String>) parentValue;
                    if (!parents.contains(parentUri)) {
                        parents.add(parentUri);
                    }
                    persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_PARENT_URI, (Serializable) parents);
                }
                includedItems.add(submittedItem.getUri());
            } else {
                includedItems.add(submittedItem.getUri());
            }
        } catch (Exception e) { //PORTAVMBadArgumentException e) {
            throw new ServiceException(submittedItem.getUri() + " does not exist");
        }
    }

    /**
     * get a list of dependent items and add submitted aspect to each item
     *
     * @param site
     * @param user
     * @param parentUri
     * @param submittedItem
     * @param scheduledDate
     * @param includedItems
     * @return a list of dependent items
     * @throws ServiceException
     */
    protected List<String> getDependencies(String site, String user, String parentUri, DmDependencyTO submittedItem,
                                           Date scheduledDate, List<String> includedItems) throws ServiceException {
        // get documents
        DmContentService dmContentService = getService(DmContentService.class);
        if (submittedItem.getDocuments() != null) {
            for (DmDependencyTO document : submittedItem.getDocuments()) {
                if (!document.isSubmittedForDeletion()&& dmContentService.isUpdatedOrNew(site, document.getUri())) {
                    addSubmittedAspect(site, user, parentUri, document, scheduledDate, includedItems);
                    includedItems.add(document.getUri());
                }
            }
        }
        // get components
        if (submittedItem.getComponents() != null) {
            for (DmDependencyTO component : submittedItem.getComponents()) {
                if (dmContentService.isUpdatedOrNew(site, component.getUri())) {
                    addSubmittedAspect(site, user, parentUri, component, scheduledDate, includedItems);
                    includedItems.add(component.getUri());
                }
            }
        }

        // get pages
        if (submittedItem.getPages() != null) {
            for (DmDependencyTO page : submittedItem.getPages()) {
                if (dmContentService.isNew(site, page.getUri())) {
                    addSubmittedAspect(site, user, parentUri, page, scheduledDate, includedItems);
                    includedItems.add(page.getUri());
                }
            }
        }

        // get assets
        if (submittedItem.getAssets() != null) {
            for (DmDependencyTO asset : submittedItem.getAssets()) {
                if (dmContentService.isUpdatedOrNew(site, asset.getUri())) {
                    addSubmittedAspect(site, user, parentUri, asset, scheduledDate, includedItems);
                    includedItems.add(asset.getUri());
                }
            }
        }

        // get templates
        if (submittedItem.getRenderingTemplates() != null) {
            for (DmDependencyTO template : submittedItem.getRenderingTemplates()) {
                if (dmContentService.isUpdatedOrNew(site, template.getUri())) {
                    addSubmittedAspect(site, user, parentUri, template, scheduledDate, includedItems);
                    includedItems.add(template.getUri());
                }
            }
        }

        // get level descriptors
        if (submittedItem.getLevelDescriptors() != null) {
            for (DmDependencyTO ld : submittedItem.getLevelDescriptors()) {
                if (dmContentService.isNew(site, ld.getUri())) {
                    addSubmittedAspect(site, user, parentUri, ld, scheduledDate, includedItems);
                    includedItems.add(ld.getUri());
                }
            }
        }
        //get deleted items and put in included items. we can't add aspect to deleted item
        if (submittedItem.getDeletedItems() != null) {
            for (DmDependencyTO deletedItem : submittedItem.getDeletedItems()) {
                String uri = deletedItem.getUri();
                uri = uri.replace("/" + DmConstants.INDEX_FILE, "");
                //addSubmittedAspect(site, user, parentUri, deletedItem, scheduledDate, includedItems);
                includedItems.add(uri);
            }
        }


        List<String> childItems = new FastList<String>();
        // get child items
        if (submittedItem.getChildren() != null) {
            for (DmDependencyTO child : submittedItem.getChildren()) {
                addSubmittedAspect(site, user, parentUri, child, scheduledDate, includedItems);
                childItems.add(child.getUri());
            }
        }
        return childItems;
    }

    protected void addToSubmitWorkFlow(String site, String sub, String user, Date launchDate, DmDependencyTO submittedItem, List<String> paths) {
        String label = submittedItem.getUri();
        try {
            submitToWorkflow(site, sub, _reviewWorkflowName, null, 3, launchDate, label, label,
                    true, paths);
        } catch (ServiceException e) {
            logger.error("Error while submitting to workflow", e);
        }
    }

    /**
     * submit the given list of paths to workflow
     *
     * @param site
     * @param sub
     * @param workflowName
     * @param assignee
     * @param priority
     * @param launchDate
     * @param label
     * @param description
     * @param autoDeploy
     * @param paths
     */
    @SuppressWarnings("deprecation")
    public void submitToWorkflow(final String site, final String sub, final String workflowName,
                                 final String assignee, final int priority, final Date launchDate, final String label, final String description,
                                 final boolean autoDeploy, final List<String> paths) throws ServiceException {
        submitToWorkflow(site, sub, workflowName, assignee, priority, launchDate, label, description, autoDeploy, paths, null);
    }

    /**
     * submit the given list of paths to workflow
     *
     * @param site
     * @param sub
     * @param workflowName
     * @param assignee
     * @param priority
     * @param launchDate
     * @param label
     * @param description
     * @param autoDeploy
     * @param paths
     */
    @SuppressWarnings("deprecation")
    public void submitToWorkflow(final String site, final String sub, final String workflowName,
                                 final String assignee, final int priority, final Date launchDate, final String label, final String description,
                                 final boolean autoDeploy, final List<String> paths, final MultiChannelPublishingContext mcpContext) throws ServiceException {
        _submit(site, sub, workflowName, assignee, priority, launchDate, label, description, autoDeploy,paths, mcpContext);
    }

    protected void _submit(String site, String sub, String workflowName, String assignee,
                           int priority, Date launchDate, String label, String description, boolean autoDeploy,
                           List<String> paths, MultiChannelPublishingContext mcpContext) {
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }
        if (description.length() > 255) {
            description = description.substring(0, 252) + "..";
        }
        String storeId = DmUtils.createStoreName(site);
        Map<String, Date> expirationDates = null;
        boolean validateLinks = false;
        assignee = StringUtils.isEmpty(assignee) ? getAssignee(site, sub) : assignee;
        // create parameter maps
        Map<QName, Serializable> workflowParameters = new HashMap<QName, Serializable>();
        List<NodeRef> assignees = new ArrayList<NodeRef>(1);
        workflowParameters.put(/* PORT WorkflowModel.PROP_WORKFLOW_PRIORITY)*/WF_PROP_REVIEW_TYPE, priority);
        ProfileService profileService = getService(ProfileService.class);
        NodeRef personRef = profileService.getUserRef(assignee);
//PORT        workflowParameters.put(/* PORT WorkflowModel.ASSOC_ASSIGNEE*/WF_PROP_REVIEW_TYPE, personRef);
        assignees.add(personRef);
        workflowParameters.put(WF_ASSIGNEES, (Serializable) assignees);
//PORT        workflowParameters.put(/*PORT WCMWorkflowModel.PROP_AUTO_DEPLOY*/WF_PROP_REVIEW_TYPE, autoDeploy);
        workflowParameters.put(WF_PROP_REVIEW_TYPE, REVIEW_TYPE_SERIAL);
        workflowParameters.put(WF_PROP_REVIEWER_CNT, 1);
        workflowParameters.put(WF_PROP_APPROVE_CNT, 1);
        // submit to workflow
    	if (logger.isDebugEnabled()) {
        	logger.debug("[WORKFLOW] w1,publish for " + label + ",start," + System.currentTimeMillis());
    	}
        DmPublishService dmPublishService = getService(DmPublishService.class);
        dmPublishService.publish(site, paths, launchDate, mcpContext);

    }

    /**
     * get assignee from configuration based on the given site and the sub
     *
     * @param site
     * @param sub
     * @return assignee
     */
    protected String getAssignee(String site, String sub) {
        // TODO: find assignee from configuration
        return null; //PORT AuthenticationUtil.getAdminUserName();
    }

    protected void invokeListeners(List<DmDependencyTO> submittedItems, String site, Operation operation) {
        for (DmDependencyTO submittedItem : submittedItems) {
            List<DmDependencyTO> children = submittedItem.getChildren();
            switch (operation) {
                case GO_LIVE:
                    _listener.postGolive(site, submittedItem);
                    break;
                case SUBMIT_TO_GO_LIVE:
                    _listener.postSubmitToGolive(site, submittedItem);
                    break;
                case REJECT:
                    _listener.postReject(site, submittedItem);
                    break;
            }
            if (null != children && !children.isEmpty()) {
                invokeListeners(children, site, operation);
            }

        }
    }

    /*
    * (non-Javadoc)
    *
    * @seeorg.craftercms.cstudio.alfresco.wcm.service.api.WcmWorkflowService#
    * getInProgressItems(java.lang.String, java.lang.String,
    * org.craftercms.cstudio.alfresco.wcm.util.WcmContentItemComparator, boolean)
    */

    @SuppressWarnings("unchecked")
    @Override
    public List<DmContentItemTO> getInProgressItems(final String site, final String sub,
                                                     final DmContentItemComparator comparator, final boolean inProgressOnly) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        final List<DmContentItemTO> categoryItems = new FastList<DmContentItemTO>();

        final StringBuilder storeNameBuf = new StringBuilder();

        String storeName = DmUtils.createStoreName(site);
        storeNameBuf.append(storeName);
        List<DmContentItemTO>categoryItems1 = getCategoryItems(site, sub, storeName);
        categoryItems.addAll(categoryItems1);


        long st = System.currentTimeMillis();
        List<NodeRef> changeSet = persistenceManagerService.getChangeSet(site);

        if (logger.isDebugEnabled()) {
            logger.debug("Time taken listChangedAll()  " + (System.currentTimeMillis() - st));
        }
        // the category item to add all other items that do not belong to
        // regular categories specified in the configuration
        st = System.currentTimeMillis();
        final ServicesConfig servicesConfig = getService(ServicesConfig.class);

        if (changeSet != null) {
            List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
            //List<String> inProgressItems = new FastList<String>();
            for (NodeRef node : changeSet) {
                if (node != null && persistenceManagerService.exists(node)) {
                    String nodePath = persistenceManagerService.getNodePath(node);
                    DmPathTO path = new DmPathTO(nodePath);
                    if (DmUtils.matchesPattern(path.getRelativePath(), displayPatterns)) {
                        addInProgressItems(site, node, categoryItems, comparator, inProgressOnly);
                    }
                    //addToGoLiveItems(site, node, false, false, null, categoryItems, inProgressItems, comparator, inProgressOnly, true, displayPatterns);
                }
            }
        }


        if (logger.isDebugEnabled()) {
            logger.debug("Time taken after listChangedAll() : " + (System.currentTimeMillis() - st));
        }
        return categoryItems;
    }

    protected void addInProgressItems(String site, NodeRef node, List<DmContentItemTO> categoryItems, DmContentItemComparator comparator, boolean inProgressOnly) {
        if (addToQueue(false, inProgressOnly, true)) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            String nodePath = persistenceManagerService.getNodePath(node);
            DmContentItemTO itemToAdd = null;
            try {
                itemToAdd = persistenceManagerService.getContentItem(nodePath, false);
            } catch (ServiceException e) {
                logger.error("Error getting contentItem ["+nodePath+"]",e.getMessage());
                return;
            }
            if (!(itemToAdd.isSubmitted() || itemToAdd.isInProgress())) {
                return;
            }

            itemToAdd.setDeleted(false);
            DmContentItemTO found = null;
            String uri = itemToAdd.getUri();
            for (DmContentItemTO categoryItem : categoryItems) {
                String categoryPath = categoryItem.getPath() + "/";
                if (uri.startsWith(categoryPath)) {
                    found = categoryItem;
                    break;
                }
            }
            if (found != null && !found.getUri().equals(itemToAdd.getUri())) {
                found.addChild(itemToAdd, comparator, true);
            }
        }
    }

    /**
     * get the top category items that to be displayed in UI
     *
     * @param site
     * @param sub
     * @param storeName
     */
    protected List<DmContentItemTO> getCategoryItems(final String site, final String sub, final String storeName) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String siteRootPrefix = servicesConfig.getRootPrefix(site);
        if (!StringUtils.isEmpty(sub)) {
            siteRootPrefix = siteRootPrefix + "_" + sub;
        }
        List<DmContentItemTO> categories = new FastTable<DmContentItemTO>();
        List<DmFolderConfigTO> folders = servicesConfig.getFolders(site);
        DmContentService dmContentService = getService(DmContentService.class);
        
        for (DmFolderConfigTO folder : folders) {
            String uri = (folder.isAttachRootPrefix()) ? siteRootPrefix + folder.getPath() : folder.getPath();
            // if the flag to read direct children is set to true, get direct
            // child folders and add them as categories
            if (folder.isReadDirectChildren()) {
                String fullPath = dmContentService.getContentFullPath(site, siteRootPrefix + folder.getPath());
                try {
                    NodeRef folderNode = persistenceManagerService.getNodeRef(fullPath);
                    List<FileInfo> children = persistenceManagerService.list(folderNode);
                    DmContentItemTO rootItem = null;
                    if (children != null) {
                        for (FileInfo child : children) {
                            if (child.isFolder()) {
                                DmContentItemTO categoryItem = getCategoryItem(site, child);
                                if (categoryItem != null) {
                                    categoryItem.setCategoryRoot(uri);
                                    categoryItem.setUri(siteRootPrefix + folder.getPath() + "/" + child.getName());
                                    categories.add(categoryItem);
                                }
                            } else {
                                if (DmConstants.INDEX_FILE.equalsIgnoreCase(child.getName())) {
                                    rootItem = getCategoryItem(site, child);
                                }
                            }
                        }
                        // add the root folder (index.xml) at the end since all
                        // URLs start with the URL of root item
                        // this way only those items that do not match at least
                        // one of sub directory URLs would be added to the root
                        if (rootItem != null) {
                            rootItem.setUri(uri);
                            rootItem.setCategoryRoot(uri);
                            categories.add(rootItem);
                        }
                    }
                } catch (Exception e) {
                    // drop the item and continue on
                    logger.error("error while reading directory structure of " + fullPath, e);
                }
            } else {
                DmContentItemTO categoryItem = new DmContentItemTO();
                String timeZone = servicesConfig.getDefaultTimezone(site);
                categoryItem.setTimezone(timeZone);
                //categoryItem.setSandboxId(storeName);
                //categoryItem.setDefaultWebApp(defaultWebApp);
                categoryItem.setName(folder.getName());
                categoryItem.setInternalName(folder.getName());
                categoryItem.setUri(uri);
                categoryItem.setPath(uri);
                categoryItem.setCategoryRoot(uri);
                categories.add(categoryItem);
            }
        }
        return categories;
    }


    /**
     * get a category item given an AVM node
     *
     * @param site
     * @param child
     * @return category item
     */
    protected DmContentItemTO getCategoryItem(String site, FileInfo child) {
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            String fullPath = persistenceManagerService.getNodePath(child.getNodeRef());
            DmContentItemTO contentItem = persistenceManagerService.getContentItem(fullPath, false);
            contentItem.setName(contentItem.getInternalName());
            return contentItem;
        } catch (ServiceException e) {
            // drop the item and continue on
            logger.error("error while reading content item at " + child.getName(), e);
            return null;
        }
    }

    /**
     * add the given node to the go live items
     *
     * @param site
     * @param node
     * @param submitted
     * @param deleted
     * @param launchDate
     * @param categoryItems
     * @param goLiveItems       top level folders that are already added to category items
     * @param comparator
     * @param includeInProgress
     * @param inProgressOnly
     * @param displayPatterns
     * @throws ServiceException
     */
    protected void addToGoLiveItems(String site, NodeRef node, boolean submitted, boolean deleted, Date launchDate,
                                    List<DmContentItemTO> categoryItems, List<String> goLiveItems, DmContentItemComparator comparator,
                                    boolean inProgressOnly, boolean includeInProgress, List<String> displayPatterns) throws ServiceException {
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        if (nodeInfo != null) {
            String nodePath = persistenceManagerService.getNodePath(node);
            if (deleted || !nodeInfo.isFolder()) {
                // if deleted, just add the top level items
                DmPathTO path = new DmPathTO(nodePath);
                // display only if the path matches one of display patterns
                if (deleted || DmUtils.matchesPattern(path.getRelativePath(), displayPatterns)) {
                    if (addToQueue(submitted, inProgressOnly, includeInProgress)) {
                        addToCategoryList(categoryItems, site, node, true, deleted, launchDate, comparator);
                    }
                }
            } else {
                if (!goLiveItems.contains(nodePath)) {
                    // if it is a directory, get child In-progress items
                    List<FileInfo> children = persistenceManagerService.list(node);
                    for (FileInfo child : children) {
                        addToGoLiveItems(site, child.getNodeRef(), submitted, deleted, launchDate, categoryItems, goLiveItems, comparator,
                                inProgressOnly, includeInProgress, displayPatterns);
                    }
                    goLiveItems.add(nodePath);
                }
            }
        }
    }

    /**
     * add the current item to the queue?
     *
     * @param inProgressOnly
     * @param includeInProgress
     * @param includeInProgress
     * @return
     */
    protected boolean addToQueue(boolean submitted, boolean inProgressOnly, boolean includeInProgress) {
        // excluded approved or scheduled items if in-progress items are
        // included. go-live queue case the node is in review
        if (inProgressOnly && submitted) {
            return false;
        }
        // add items in following cases
        // 1) if the item is submitted, add if the flag is not in-progress only
        // 2) if the item is in progress, add if the flag is either in-progress
        // only or include in progress
        if (submitted && !inProgressOnly) {
            return true;
        } else if (!submitted && (inProgressOnly || includeInProgress)) {
            return true;
        }
        return false;
    }

    /**
     * add item to the category list
     *
     * @param categoryItems
     * @param site
     * @param node
     * @param isSubmitted
     * @param deleted
     * @param scheduledDate
     * @param comparator
     * @throws ServiceException
     */
    protected void addToCategoryList(final List<DmContentItemTO> categoryItems, final String site,
                                     final NodeRef node, final boolean isSubmitted, final boolean deleted, final Date scheduledDate,
                                     final DmContentItemComparator comparator) {
        // add only folders or xml files
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        String nodePath = persistenceManagerService.getNodePath(node);
        if (nodeInfo.isFolder() || nodeInfo.getName().contains(DmConstants.XML_PATTERN)) {
            DmContentItemTO itemToAdd = null;
            try {
                itemToAdd = persistenceManagerService.getContentItem(nodePath);
            } catch (ServiceException e) {
                logger.error("Error getting contentItem ["+nodePath+"]",e.getMessage());
                return;
            }
            if (!(itemToAdd.isSubmitted() || itemToAdd.isInProgress())) {
                return;
            }

            itemToAdd.setDeleted(deleted);
            DmContentItemTO found = null;
            String uri = itemToAdd.getUri();
            for (DmContentItemTO categoryItem : categoryItems) {
                String categoryPath = categoryItem.getPath() + "/";
                if (uri.startsWith(categoryPath)) {
                    found = categoryItem;
                    break;
                }
            }
            if (found != null && !found.getUri().equals(itemToAdd.getUri())) {
                found.addChild(itemToAdd, comparator, true);
            }
            // add child nodes
            if (nodeInfo.isFolder()) {
                List<FileInfo> children = persistenceManagerService.list(node);
                if (children != null) {
                    for (FileInfo child : children) {
                        if (!DmConstants.INDEX_FILE.equals(child.getName())) {
                            addToCategoryList(categoryItems, site, child.getNodeRef(), true, deleted, scheduledDate, comparator);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DmContentItemTO> getGoLiveItems(final String site, final String sub,
                                                 final DmContentItemComparator comparator) throws ServiceException {
        return  null;
    }

    protected String getListChangedQuery(String site) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String siteRootPath = servicesConfig.getRepositoryRootPath(site);
        String[] pathSegments = siteRootPath.split("/");
        StringBuilder siteRoot = new StringBuilder();
        for (String segment : pathSegments) {
            if (StringUtils.isNotEmpty(segment)) {
                siteRoot.append("/cm:").append(segment);
            }
        }
        return LIST_CHANGES_QUERY.replace("{site_root}", siteRoot.toString());
    }

    protected String getSubmittedItemsQuery(String site) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String siteRootPath = servicesConfig.getRepositoryRootPath(site);
        String[] pathSegments = siteRootPath.split("/");
        StringBuilder siteRoot = new StringBuilder();
        for (String segment : pathSegments) {
            if (StringUtils.isNotEmpty(segment)) {
                siteRoot.append("/cm:").append(segment);
            }
        }
        return DmConstants.SUBMITTED_ITEMS_QUERY.replace("{site_root}", siteRoot.toString());
    }

    @Override
    public void doDelete(String site,String sub,List<DmDependencyTO> submittedItems, String approver) throws ServiceException {
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param sub
     * @return call result
     * @throws ServiceException
     */
    public void goLive(final String site, String sub, final List<DmDependencyTO> submittedItems, String approver)
            throws ServiceException {
        goLive(site, sub, submittedItems, approver, null);
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param sub
     * @return call result
     * @throws ServiceException
     */
    public void goLive(final String site, String sub, final List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext)
            throws ServiceException {
        List<DmDependencyTO> goLiveItems =  new FastList<DmDependencyTO>();
        // Don't make go live an item if it is new and to be deleted
        DmContentService dmContentService = getService(DmContentService.class);
        for(DmDependencyTO submittedItem : submittedItems) {
            String uri = submittedItem.getUri();
            boolean isNew = dmContentService.isNew(site,uri);
            if(!(submittedItem.isDeleted() && isNew)) {
                goLiveItems.add(submittedItem);
            }
        }
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String user = persistenceManagerService.getCurrentUserName();
        // get web project information
        String assignee = getAssignee(site, sub);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        final String pathPrefix = servicesConfig.getRepositoryRootPath(site);
        final Date now = new Date();

        // group submitted items into packages by their scheculed date
        Map<Date, List<DmDependencyTO>> groupedPackages = new FastMap<Date, List<DmDependencyTO>>();
        for (DmDependencyTO submittedItem : goLiveItems) {
            List<String> taskIds = new FastList<String>();      //find all pending workflows
            //getWorkflowTask(site, null, submittedItem, taskIds);
            // find out how many workflows pending per submitted items
            submittedItem.setWorkflowTasks(taskIds);
            Date scheduledDate = (submittedItem.isNow()) ? null : submittedItem.getScheduledDate();
            if (scheduledDate == null || scheduledDate.before(now)) {
                scheduledDate = now;
            }
            List<DmDependencyTO> goLivePackage = groupedPackages.get(scheduledDate);
            if (goLivePackage == null)
                goLivePackage = new FastList<DmDependencyTO>();
            goLivePackage.add(submittedItem);
            groupedPackages.put(scheduledDate, goLivePackage);
        }
        for (Date scheduledDate : groupedPackages.keySet()) {
            List<DmDependencyTO> goLivePackage = groupedPackages.get(scheduledDate);
            if (goLivePackage != null) {
                // for submit direct, package them together and submit them
                // together as direct submit
                if (scheduledDate.equals(now)) {
                    if (goLivePackage.size() == 1) {
                        DmDependencyTO item = goLivePackage.get(0);
                        List<String> tasks = item.getWorkflowTasks();
                        if (tasks != null && tasks.size() > 1) {
                            // if there is more than one workflow pending
                            approveMultiplePackages(site, null, sub, user, pathPrefix, assignee, null,
                                    goLivePackage);
                        } else {
                            approveSinglePackage(site, null, sub, user, pathPrefix, assignee, null,
                                    goLivePackage.get(0));
                        }
                    } else {
                        // more than one independent item
                        approveMultiplePackages(site, null, sub, user, pathPrefix, assignee, null, goLivePackage);
                    }
                } else {
                    for (DmDependencyTO item : goLivePackage) {
                        approveSinglePackage(site, null, sub, user, pathPrefix, assignee, scheduledDate, item);
                    }
                }
            }
        }

        invokeListeners(submittedItems, site, Operation.GO_LIVE);
    }

    /**
     * approve multiple packages by canceling pending workflows and submitting
     * them to one workflow instance. This should only be used for submit direct
     * case. (might change in future)
     *
     * @param site
     * @param sandbox
     * @param sub
     * @param user
     * @param pathPrefix
     * @param assignee
     * @param scheduledDate
     * @param goLivePackage
     * @throws ServiceException
     */
    protected void approveMultiplePackages(final String site, final String sandbox, final String sub, final String user,
                                           final String pathPrefix, final String assignee, final Date scheduledDate,
                                           final List<DmDependencyTO> goLivePackage) throws ServiceException {
        // cancel workflow if anything is pending
        DmTransactionService dmTransactionService = getService(DmTransactionService.class);
        RetryingTransactionHelper txnHelper = dmTransactionService.getRetryingTransactionHelper();
        /* used to store canceled TaskIds and check whether taskId is already canceled,
          if already canceled then cancelWorkFlow will not be called.
         */
        Set<String> canceledTaskIds = new HashSet<String>();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        for (DmDependencyTO item : goLivePackage) {
            List<String> tasks = item.getWorkflowTasks();
            for (String taskId : tasks) {
                if (!canceledTaskIds.contains(taskId)) {
                    final WorkflowTask task = persistenceManagerService.getTaskById(taskId);
                    RetryingTransactionHelper.RetryingTransactionCallback<String> cancelWorkflowCallBack = new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                        public String execute() throws Throwable {
                            cancelWorkflow(task);
                            return null;
                        }
                    };
                    //PORT txnHelper.doInTransaction(cancelWorkflowCallBack, false, true);
                    canceledTaskIds.add(taskId);
                }
            }
        }
        // attach submitted aspect to all items within this package. Prepare workfow
        final StringBuffer buffer = new StringBuffer();
        RetryingTransactionHelper.RetryingTransactionCallback<List<String>> workflowCallback = new RetryingTransactionHelper.RetryingTransactionCallback<List<String>>() {
            public List<String> execute() throws Throwable {
                List<String> packagePaths = new FastList<String>();
                for (DmDependencyTO item : goLivePackage) {
                    buffer.append(item.getUri() + ", ");
                    List<String> paths = prepareWorkflowSubmission(site, user, item, scheduledDate, item.isSendEmail());
                    packagePaths.addAll(paths);
                }
                return packagePaths;
            }
        };
        // submit to workflow
        List<String> packagePaths = null;//PORT txnHelper.doInTransaction(workflowCallback, false, true);
        String label = buffer.toString();
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }
        boolean submitDirect = (scheduledDate == null) ? true : false;
        submitToWorkflow(site, sub, _submitDirectWorkflowName, assignee, 3, scheduledDate, label, label,
                true, packagePaths);
    }

    /**
     * approve single item by approving the existing workflow or submitting
     * direct
     *
     * @param site
     * @param sandbox
     * @param sub
     * @param user
     * @param pathPrefix
     * @param assignee
     * @param scheduledDate
     * @throws ServiceException
     */
    protected void approveSinglePackage(final String site, final String sandbox, final String sub, final String user,
                                        final String pathPrefix, final String assignee, final Date scheduledDate,
                                        final DmDependencyTO submittedItem) throws ServiceException {
        List<String> tasks = submittedItem.getWorkflowTasks();
        boolean submitDirect = (scheduledDate == null) ? true : false;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (tasks == null || tasks.size() == 0) {
            // if no workflow started, submit items in the package
            // attach submitted aspect to items being submitted
            DmTransactionService dmTransactionService = getService(DmTransactionService.class);
            RetryingTransactionHelper txnHelper = dmTransactionService.getRetryingTransactionHelper();
            RetryingTransactionHelper.RetryingTransactionCallback<List<String>> workflowCallback = new RetryingTransactionHelper.RetryingTransactionCallback<List<String>>() {
                public List<String> execute() throws Throwable {
                    return prepareWorkflowSubmission(site, user, submittedItem, scheduledDate, submittedItem
                            .isSendEmail());
                }
            };
            List<String> paths = null; // PORT txnHelper.doInTransaction(workflowCallback, false, true);
            String label = submittedItem.getUri();
            if (label.length() > 255) {
                label = label.substring(0, 252) + "..";
            }

            submitToWorkflow(site, sub, _submitDirectWorkflowName, assignee, 3, scheduledDate, label,
                    label, true, paths);
        } else {
            // if workflow already started, approve or launch the pending workflow
            Map<QName, Serializable> params = new HashMap<QName, Serializable>(1);
         //PORT    params.put(WCMWorkflowModel.PROP_LAUNCH_DATE, scheduledDate);
            for (String taskId : tasks) {
                WorkflowTask task = persistenceManagerService.getTaskById(taskId);
                WorkflowTransition[] transitions = null; //task.path.node.transitions;
                for (WorkflowTransition transition : transitions) {
                    if (false) { //PORT transition.id.equals(TRNASITION_LAUNCH)) {
                        if (scheduledDate == null || scheduledDate.before(new Date())) {
                            // launch the workflow
                            persistenceManagerService.endTask(taskId, TRNASITION_LAUNCH);
                        } else {
                            // update launch date
                            persistenceManagerService.updateTask(taskId, params, null, null);
                        }
                    } else {
                        // update launch date
                        persistenceManagerService.updateTask(taskId, params, null, null);
                        if (false) { //PORT transition.id.equals(TRNASITION_APPROVE)) {
                            // approve the workflow
                            persistenceManagerService.endTask(taskId, TRNASITION_APPROVE);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<Date, List<DmDependencyTO>> groupByDate(
            List<DmDependencyTO> submittedItems, Date now) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRescheduleRequest(DmDependencyTO dependencyTO,
                                       String site) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void preGoLive(Set<String> uris, GoLiveContext context,Set<String> rescheduledUris) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void preSchedule(Set<String> uris, Date date, GoLiveContext context,Set<String> rescheduledUris) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reject(String site, String sub, List<DmDependencyTO> submittedItems, String reason, String approver) {
    }

    /**
     * remove submitted aspect from the content at the give full path and its dependent items
     *
     * @param site
     * @param fullPath
     * @param workflow
     * @param isAsset
     * @param dmStatus what is the item status after removing submitted aspect?
     */
    protected void removeSubmittedAspect(String site, String fullPath, String workflow, boolean isAsset, String dmStatus) {
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (!isAsset) {
            DmPathTO path = new DmPathTO(fullPath);
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            List<DependencyEntity> dependencyList = dmDependencyService.getDirectDependencies(site, path
                    .getRelativePath());
            if (dependencyList != null) {
                for (DependencyEntity item : dependencyList) {
                    if (!item.getType().equals(DmDependencyService.DEPENDENCY_NAME_DELETE) && dmContentService.isUpdatedOrNew(site, item.getTargetPath())) {
                        boolean isAssetDependency = item.getType().equals(DmDependencyService.DEPENDENCY_NAME_ASSET);
                        String dependencyFullPath = dmContentService.getContentFullPath(site, item.getTargetPath());
                        removeSubmittedAspect(site, dependencyFullPath, workflow, isAssetDependency, dmStatus);
                    }
                }
            }
        }
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        LockStatus lockStatus = persistenceManagerService.getLockStatus(node);
        if (LockStatus.NO_LOCK.equals(lockStatus) || LockStatus.LOCK_OWNER.equals(lockStatus)) {
            persistenceManagerService.removeAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED);
            persistenceManagerService.setProperty(node, CStudioContentModel.PROP_STATUS, dmStatus);
        } else {
            String currentUser = persistenceManagerService.getCurrentUserName();
            String lockOwner = ""; //PORT DefaultTypeConverter.INSTANCE.convert(String.class, persistenceManagerService.getProperty(node, ContentModel.PROP_LOCK_OWNER));
            AuthenticationUtil.setFullyAuthenticatedUser(lockOwner);
            persistenceManagerService.removeAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED);
            persistenceManagerService.setProperty(node, CStudioContentModel.PROP_STATUS, dmStatus);
            AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
        }
    }

    /**
     * update the given content to workflow sandbox
     *
     * @param site
     * @param fullPath
     * @param workflow
     */
    @Override
    public void updateWorkflowSandbox(String site, String fullPath, String workflow) {
    }

    @Override
    public boolean removeFromWorkflow(String site, String sub, String path, boolean cancelWorkflow) {
        Set<String> processedPaths = new FastSet<String>();
        return removeFromWorkflow(site, sub, path, processedPaths, cancelWorkflow);
    }

    protected boolean removeFromWorkflow(String site, String sub, String path, Set<String> processedPaths, boolean cancelWorkflow) {
        // remove submitted aspects from all dependent items
        if (!processedPaths.contains(path)) {
            processedPaths.add(path);
            DmContentService dmContentService = getService(DmContentService.class);
            String fullPath = dmContentService.getContentFullPath(site, path);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                //removeSubmittedAspect(site, fullPath, null, false, DmConstants.DM_STATUS_IN_PROGRESS);
                // cancel workflow if anything is pending
                if (cancelWorkflow) {
                    _cancelWorkflow(site, node);
                }

                DmDependencyService dmDependencyService = getService(DmDependencyService.class);
                DmDependencyTO depItem = dmDependencyService.getDependencies(site, null, path, false, true);
                if (depItem != null) {
                    DependencyRules dependencyRules = new DependencyRules(site, getServicesManager());
                    Set<DmDependencyTO> submittedDeps = dependencyRules.applySubmitRule(depItem);
                    List<String> transitionNodes = new ArrayList<String>();
                    for (DmDependencyTO dependencyTO : submittedDeps) {
                        String depFullPath = dmContentService.getContentFullPath(site, dependencyTO.getUri());
                        removeFromWorkflow(site, sub, dependencyTO.getUri(), processedPaths, cancelWorkflow);
                        ObjectStateService.State state = persistenceManagerService.getObjectState(depFullPath);
                        if (ObjectStateService.State.isScheduled(state) || ObjectStateService.State.isSubmitted(state)) {
                            NodeRef nodeRef = persistenceManagerService.getNodeRef(depFullPath);
                            transitionNodes.add(nodeRef.getId());
                            //persistenceManagerService.transition(depFullPath, ObjectStateService.TransitionEvent.SAVE);
                        }
                    }

                    if (!transitionNodes.isEmpty()) {
                        persistenceManagerService.transitionBulk(transitionNodes, ObjectStateService.TransitionEvent.SAVE, ObjectStateService.State.NEW_UNPUBLISHED_UNLOCKED);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void updateWorkflowSandboxes(String site, String path) {
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        if (node != null) {
            NodeRef liveRepoNode = persistenceManagerService.getNodeRef(getPathFromLiveRepo(fullPath));
            if (liveRepoNode != null) {
                persistenceManagerService.copy(node, liveRepoNode);
            }
            //List<WorkflowTask> tasks = WCMWorkflowUtil.getAssociatedTasksForNode(_workflowService, _avmService, node);
            //List<PublishingEvent> events = _publishingService.getPublishEventsForNode(liveRepoNode);
            //updateWorkflowSandboxes(site, fullPath, events);
        }
    }
    
    protected String getPathFromLiveRepo(String fullPath) {
        Matcher m = DmConstants.DM_REPO_TYPE_PATH_PATTERN.matcher(fullPath);
        if (m.matches()) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.group(1));
            sb.append(m.group(2));
            sb.append(DmConstants.DM_LIVE_REPO_FOLDER);
            sb.append(m.group(4));
            return sb.toString();
        }
        return null;
    }

    @Override
    public List<DmContentItemTO> getScheduledItems(String site, String sub, DmContentItemComparator comparator, DmContentItemComparator subComparator, String filterType) {
        return null;
    }

    protected List<WorkflowTask> getWorkflowTasks() {
        WorkflowTaskQuery query = new WorkflowTaskQuery();

        HashMap<QName, Object> props = new HashMap<QName, Object>(1, 1.0f);

        //props.put(WCMWorkflowModel.PROP_FROM_PATH, fromPath);
        //PORT query.setWorkflowDefinitionName("activiti$cstudioPublishWebContent");
        //query.setProcessCustomProps(props);
        //PORT query.setActive(true);

        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        List<WorkflowTask> tasks = persistenceManagerService.queryTasks(query);
        return tasks;
    }

    protected void _cancelWorkflow(String site, NodeRef node) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmContentService dmContentService = getService(DmContentService.class);
        String currentUser = persistenceManagerService.getCurrentUserName();
        if (node != null) {
            String fullPath = persistenceManagerService.getNodePath(node);
            DmPathTO dmPathTO = new DmPathTO(fullPath);
            List<DmContentItemTO> allItemsToCancel = getWorkflowAffectedPaths(site, dmPathTO.getRelativePath());
            List<String> nodeRefs = new ArrayList<String>();
            for (DmContentItemTO item : allItemsToCancel) {
                try {
                    _deploymentService.cancelWorkflow(site, item.getUri());
                    NodeRef nodeRef = null; //PORT new NodeRef(item.getNodeRef());
                    nodeRefs.add(nodeRef.getId());
                } catch (DeploymentException e) {
                    logger.error("Error occurred while trying to cancel workflow for path [" + dmPathTO.getRelativePath() + "], site " + dmPathTO.getSiteName(), e);
                }
            }
            persistenceManagerService.transitionBulk(nodeRefs, ObjectStateService.TransitionEvent.REJECT, ObjectStateService.State.NEW_UNPUBLISHED_UNLOCKED);

            if (persistenceManagerService.isNew(fullPath) && fullPath.endsWith(DmConstants.INDEX_FILE)) {
                NodeRef parentNodeRef = persistenceManagerService.getPrimaryParent(node).getParentRef();
                FileInfo parentInfo = persistenceManagerService.getFileInfo(parentNodeRef);
                List<FileInfo> files = persistenceManagerService.listFiles(parentNodeRef);
                for (FileInfo file : files) {
                    if (!file.getNodeRef().equals(node)) {
                        ObjectStateService.State state = persistenceManagerService.getObjectState(file.getNodeRef());
                        if (ObjectStateService.State.isScheduled(state) || ObjectStateService.State.isSubmitted(state)) {
                            _cancelWorkflow(site, file.getNodeRef());
                            persistenceManagerService.transition(file.getNodeRef(), ObjectStateService.TransitionEvent.SAVE);
                        }
                    }
                }
                List<FileInfo> folders = persistenceManagerService.listFolders(parentNodeRef);
                for (FileInfo folder : folders) {
                    NodeRef indexNode = persistenceManagerService.getNodeRef(folder.getNodeRef(), DmConstants.INDEX_FILE);
                    if (indexNode != null) {
                        ObjectStateService.State state = persistenceManagerService.getObjectState(indexNode);
                        if (ObjectStateService.State.isScheduled(state) || ObjectStateService.State.isSubmitted(state)) {
                            _cancelWorkflow(site, indexNode);
                            persistenceManagerService.transition(indexNode, ObjectStateService.TransitionEvent.SAVE);
                        }
                    }
                }
            }
        }

    }

    /**
     * add a scheduled item created from the given node to the scheduled items
     * list if the item is not a component or a static asset
     *
     * @param site
     * @param launchDate
     * @param node
     * @param scheduledItems
     * @param comparator
     * @param displayPatterns
     */
    protected void addScheduledItem(String site, Date launchDate, SimpleDateFormat format, NodeRef node,
                                    List<DmContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                    DmContentItemComparator subComparator, String taskId, List<String> displayPatterns, String filterType,List<WorkflowTask> tasks) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String nodePath = persistenceManagerService.getNodePath(node);
        try {
            FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
            if (!nodeInfo.isFolder()) {
                DmPathTO path = new DmPathTO(nodePath);
                    addToScheduledDateList(site, launchDate, format, node,
                            scheduledItems, comparator, subComparator, taskId, displayPatterns, filterType);
                    String relativePath = DmUtils.getRelativePath(nodePath);
                    if(!(relativePath.endsWith("/" + DmConstants.INDEX_FILE) || relativePath.endsWith(DmConstants.XML_PATTERN))) {
                        relativePath = relativePath + "/" + DmConstants.INDEX_FILE;
                    }
                    addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,taskId,displayPatterns,filterType,relativePath,tasks);
                //}
            } else {
                // read child package items
                List<FileInfo> children = persistenceManagerService.list(node);
                for (FileInfo childInfo : children) {
                    addScheduledItem(site, launchDate, format, childInfo.getNodeRef(), scheduledItems, comparator,
                            subComparator, taskId, displayPatterns, filterType,tasks);

                }
            }
        } catch (ServiceException e) {
            if (logger.isErrorEnabled()) {
                logger.error("failed to read " + nodePath + ". " + e.getMessage());
            }
        }
    }

    /**
     * add the given node to the scheduled items list
     *
     * @param site
     * @param launchDate
     * @param format
     * @param node
     * @param scheduledItems
     * @param comparator
     * @param subComparator
     * @param taskId
     * @param displayPatterns
     * @throws ServiceException
     */
    protected void addToScheduledDateList(String site, Date launchDate, SimpleDateFormat format, NodeRef node,
                                          List<DmContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                          DmContentItemComparator subComparator, String taskId, List<String> displayPatterns, String filterType) throws ServiceException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String timeZone = servicesConfig.getDefaultTimezone(site);
        String dateLabel = ContentFormatUtils.formatDate(format, launchDate, timeZone);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String nodePath = persistenceManagerService.getNodePath(node);
        DmPathTO path = new DmPathTO(nodePath);
        // add only if the current node is a file (directories are
        // deployed with index.xml)
        // display only if the path matches one of display patterns
        if (DmUtils.matchesPattern(path.getRelativePath(), displayPatterns)) {
            DmContentItemTO itemToAdd = persistenceManagerService.getContentItem(nodePath, false);
            if (_dmFilterWrapper.accept(site, itemToAdd, filterType)) {
                itemToAdd.setSubmitted(false);
                itemToAdd.setScheduledDate(launchDate);
                itemToAdd.setInProgress(false);
                boolean found = false;
                for (int index = 0; index < scheduledItems.size(); index++) {
                    DmContentItemTO currDateItem = scheduledItems.get(index);
                    // if the same date label found, add the content item to
                    // it non-recursively
                    if (currDateItem.getName().equals(dateLabel)) {
                        currDateItem.addChild(itemToAdd, subComparator, false);
                        found = true;
                        break;
                        // if the date is after the current date, add a new
                        // date item before it
                        // and add the content item to the new date item
                    } else if (itemToAdd.getScheduledDateAsDate().compareTo(currDateItem.getScheduledDateAsDate()) < 0) {
                        DmContentItemTO dateItem = createDateItem(dateLabel, itemToAdd, comparator, timeZone);
                        scheduledItems.add(index, dateItem);
                        found = true;
                        break;
                    }
                }
                // if not found, add to the end of list
                if (!found) {
                    DmContentItemTO dateItem = createDateItem(dateLabel, itemToAdd, comparator, timeZone);
                    scheduledItems.add(dateItem);
                }
            }
        }
    }

    /**
     * create a date category item with the given content item
     *
     * @param name       label of the new date category
     * @param itemToAdd  content item to add to the category
     * @param comparator content item comparator
     * @param timeZone
     * @return date category item
     */
    protected DmContentItemTO createDateItem(String name, DmContentItemTO itemToAdd, DmContentItemComparator comparator, String timeZone) {
        DmContentItemTO dateItem = new DmContentItemTO();
        dateItem.setName(name);
        dateItem.setInternalName(name);
        dateItem.setEventDate(itemToAdd.getScheduledDateAsDate());
        dateItem.setScheduledDate(itemToAdd.getScheduledDateAsDate());
        dateItem.setTimezone(timeZone);
        dateItem.addChild(itemToAdd, comparator, false);
        return dateItem;
    }

    protected void addDependendenciesToSchdeuleList(String site,
                                                    Date launchDate,
                                                    SimpleDateFormat format,
                                                    List<DmContentItemTO>scheduledItems,
                                                    DmContentItemComparator comparator,
                                                    DmContentItemComparator subComparator,
                                                    String taskId,
                                                    List<String> displayPatterns,
                                                    String filterType,
                                                    String relativePath,
                                                    List<WorkflowTask> workFlowTasks) {

        DmDependencyService dmDependencyService = getService(DmDependencyService.class);
        DmDependencyTO dmDependencyTo = dmDependencyService.getDependencies(site, null, relativePath, false, true);

        if (dmDependencyTo != null) {

            List<DmDependencyTO> pages = dmDependencyTo.getPages();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, taskId, displayPatterns, filterType, pages, workFlowTasks);

            List<DmDependencyTO> components = dmDependencyTo.getComponents();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, taskId, displayPatterns, filterType, components, workFlowTasks);

            List<DmDependencyTO> documents = dmDependencyTo.getDocuments();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, taskId, displayPatterns, filterType, documents, workFlowTasks);
        }

    }

    protected void _addDependendenciesToSchdeuleList(String site,
                                                     Date launchDate,
                                                     SimpleDateFormat format,
                                                     List<DmContentItemTO>scheduledItems,
                                                     DmContentItemComparator comparator,
                                                     DmContentItemComparator subComparator,
                                                     String taskId,
                                                     List<String> displayPatterns,
                                                     String filterType,
                                                     List<DmDependencyTO>dependencies,
                                                     List<WorkflowTask>workFlowTasks) {
        if(dependencies != null) {
            DmContentService dmContentService = getService(DmContentService.class);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for(DmDependencyTO dependencyTo:dependencies) {
                if (dmContentService.isNew(site, dependencyTo.getUri())) {
                    String uri = dependencyTo.getUri();
                    String fullPath = dmContentService.getContentFullPath(site, uri);
                    NodeRef node = persistenceManagerService.getNodeRef(fullPath);
                    if(isNodeInScheduledStatus(fullPath)) {
                        addScheduledItem(site,launchDate,format,node,scheduledItems,comparator,subComparator,taskId,displayPatterns,filterType,workFlowTasks);
                        if(dependencyTo.getUri().endsWith(DmConstants.XML_PATTERN)) {
                            addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,taskId,displayPatterns,filterType,uri,workFlowTasks);
                        }
                    }
                }
            }
        }
    }

    protected boolean isNodeInScheduledStatus(String fullPath) {
        try {
            Serializable propValue = getService(PersistenceManagerService.class).getProperty(fullPath, CStudioContentModel.PROP_STATUS);
            if (propValue != null) {
                String status = (String)propValue;
                return DmConstants.DM_STATUS_SCHEDULED.equals(status);
            }
        } catch (Exception e) {
        }
        return false;
    }
    
	@Override
	public void preScheduleDelete(Set<String> _uris, Date _date,
			GoLiveContext _context, Set _rescheduledUris)
			throws ServiceException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<String> preDelete(Set<String> urisToDelete,
			GoLiveContext context, Set<String> rescheduledUris)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean cleanWorkflow(String sandBox, String url, String site,
			Set<DmDependencyTO> dependents) {
		// TODO Auto-generated method stub
		return false;
	}


    /*
    * (non-Javadoc)
    * @see org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService#prepareSubmission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, boolean)
    */
    public void prepareSubmission(NodeRef packageRef, String workflowId, String desc, boolean sendNotice) {

    }

    @Override
    public void postSubmission(NodeRef packageRef, String workflowId,
                                      String desc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateItemStatus(NodeRef packageRef, String status, Date date) {
// PORT AVM?!
        // // find site information
        // StoreRef storeRef = null; //PORT packageRef.getStoreRef();
        // String store = storeRef.getIdentifier();
        // String[] tokens = store.split("--");
        // String site = tokens[0];
        // //String sandbox = _servicesConfig.getSandbox(site);
        // String workflowSandbox =  ""; //PORT WCMUtil.getWorkflowId(storeRef.getIdentifier());

        // if (logger.isDebugEnabled()) {
        //     logger.debug("updating item status for for " + workflowSandbox + " site: "
        //             + site + ", store: " + store);
        // }
        // SearchService searchService = getService(SearchService.class);
        // List<NodeRef> changeSet = searchService.findNodes(CStudioConstants.STORE_REF, getListChangedQuery(site));
        // List<AVMDifference> avmDifferenceList = new ArrayList<AVMDifference>();
        // if (changeSet != null && changeSet.size() > 0) {
        //     for (NodeRef node : changeSet) {
        //         updateItemStatus(site, workflowSandbox, node, status, avmDifferenceList, date);
        //     }
        // }
    }

    /**
     * update status of all items associated submitted in workflow
     *
     * @param site
     * @param workflowSandbox
     * @param node
     * @param status
     * @param date
     */
    protected void updateItemStatus(String site, String workflowSandbox, NodeRef node, String status, Date date) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String workflowPath = persistenceManagerService.getNodePath(node);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        if (nodeInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(node);
            if (children != null) {
                for (FileInfo childInfo : children) {
                    updateItemStatus(site, workflowSandbox, childInfo.getNodeRef(), status, date);
                }
            }
        } else {
            DmPathTO path = new DmPathTO(workflowPath);

            if (!StringUtils.isEmpty(workflowSandbox)) {
                long start=System.currentTimeMillis();
            }
        }

    }

    public void scheduleDeleteSubmission(NodeRef packageRef, String workflowId, String descpription) {
    }

    @Override
    public List<DmContentItemTO> getWorkflowAffectedPaths(String site, String path) {
        return null;
    }

    protected List<DmContentItemTO> getWorkflowAffectedItems(String site, List<String> paths) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmContentService contentService = getService(DmContentService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String siteRootPath = servicesConfig.getRepositoryRootPath(site);
        List<DmContentItemTO> items = new FastList<DmContentItemTO>();

        for (String path : paths) {
            try {
                DmContentItemTO item = persistenceManagerService.getContentItem(contentService.getContentFullPath(site, path));
                items.add(item);
            } catch (ServiceException e) {
                logger.warn("Path [%s] exists in workflow, but content item was not found in the repository for site [%s]", path, site);
            }
        }
        return items;
    }
}
