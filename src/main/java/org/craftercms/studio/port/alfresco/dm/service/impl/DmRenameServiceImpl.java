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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceException;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.cache.Scope;
import org.craftercms.cstudio.alfresco.cache.cstudioCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.listener.DmWorkflowListener;
import org.craftercms.cstudio.alfresco.dm.service.api.*;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.workflow.GoLiveContext;
import org.craftercms.cstudio.alfresco.dm.workflow.MultiChannelPublishingContext;
import org.craftercms.cstudio.alfresco.dm.workflow.WorkflowProcessor;
import org.craftercms.cstudio.alfresco.dm.workflow.operation.SubmitLifeCycleOperation;
import org.craftercms.cstudio.alfresco.dm.workflow.operation.presubmit.PreGoLiveOperation;
import org.craftercms.cstudio.alfresco.dm.workflow.operation.presubmit.PreScheduleOperation;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class DmRenameServiceImpl extends AbstractRegistrableService implements DmRenameService {

    private static final Logger logger = LoggerFactory.getLogger(DmRenameServiceImpl.class);

    protected DmWorkflowListener _listener;
    public DmWorkflowListener getListener() {
        return _listener;
    }
    public void setListener(DmWorkflowListener listener) {
        this._listener = listener;
    }

    protected WorkflowProcessor _workflowProcessor;
    public WorkflowProcessor getWorkflowProcessor() {
        return _workflowProcessor;
    }
    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) {
        this._workflowProcessor = workflowProcessor;
    }

    protected String _submitDirectWorkflowName;
    public String getSubmitDirectWorkflowName() {
        return _submitDirectWorkflowName;
    }
    public void setSubmitDirectWorkflowName(String submitDirectWorkflowName) {
        this._submitDirectWorkflowName = submitDirectWorkflowName;
    }

    protected cstudioCacheManager _cache;
    public cstudioCacheManager getCacheManager() {
        return _cache;
    }
    public void setCacheManager(cstudioCacheManager cache) {
        this._cache = cache;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmRenameService.class, this);
    }

    /**
     *
     * If cut/paste or rename is reverted (i.e pasted back in the original location where it belongs to then return true
     *
     * @param site
     * @param cutPath
     * @param pastePath
     * @return
     */
    @Override
    public boolean isRevertRename(String site, String cutPath, String pastePath) {
        if(StringUtils.isEmpty(cutPath))
            return false;

        pastePath = pastePath.replace("//", "/"); //workaround sometimes we can two // in the url which creates problem during comparison

        String originalUrl = getOldUrl(site, cutPath);
        if (StringUtils.isNotEmpty(originalUrl) && originalUrl.equals(getIndexFilePath(pastePath))) {
            if (logger.isDebugEnabled())
                logger.debug("Revert rename case for path: " + pastePath + ", original URL: " + originalUrl);
            return true;
        }
        return false;
    }

    /**
     * If the node has been live and renamed then oldUrl will return the original url in sandbox before it has been renamed
     *
     * @param site
     * @param cutPath
     * @return
     */
    protected String getOldUrl(String site,String cutPath){
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullCutPath = dmContentService.getContentFullPath(site, cutPath);
        NodeRef cutPathNode = persistenceManagerService.getNodeRef(fullCutPath);
        String originalUrl = (String)persistenceManagerService.getProperty(cutPathNode, CStudioContentModel.PROP_RENAMED_OLD_URL);
        return originalUrl;

    }

    protected String getIndexFilePath(String path){
        if(!path.endsWith(DmConstants.XML_PATTERN)){
            path = path + "/" + DmConstants.INDEX_FILE;
        }
        return path;
    }

    /**
     * Is provided node renamed?
     *
     */
    @Override
    public boolean isItemRenamed(String site, DmDependencyTO item) {

        if (item.getUri().endsWith(DmConstants.XML_PATTERN) || !item.getUri().contains(".")) {
            return isItemRenamed(site, item.getUri());
        } else {
            // if not xml or a folder, skip checking if renamed
            return false;
        }
    }

    /**
     * Is provided node renamed?
     * we always look into index.xml for node properties
     *
     */
    @Override
    public boolean isItemRenamed(String site, String uri){
        NodeService nodeService = getService(NodeService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String fullPath = servicesConfig.getRepositoryRootPath(site) + uri;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        return nodeService.hasAspect(nodeRef, CStudioContentModel.ASPECT_RENAMED);
    }

    /**
     * Always returns node of the index.xml
     */
    protected NodeRef getIndexNode(final String site, String uri) {
        return getNode(site, getIndexFilePath(uri));
    }

    protected NodeRef getNode(final String site, String uri) {
        NodeRef node;
        try{
            DmContentService dmContentService = getService(DmContentService.class);
            String fullPath = dmContentService.getContentFullPath(site, uri);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            node = persistenceManagerService.getNodeRef(fullPath);
        }catch(AVMNotFoundException e){
            return null;
        }
        return node;
    }

    /**
     * GoLive on the renamed node
     */
    @Override
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver) throws ServiceException {
        goLive(site, sub, submittedItems, approver, null);
    }

    /**
     * GoLive on the renamed node
     */
    @Override
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException {
        long start = System.currentTimeMillis();

        try {
            Date now = new Date();
            DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
            Map<Date, List<DmDependencyTO>> groupedPackages = dmWorkflowService.groupByDate(submittedItems, now);

            for (Date scheduledDate : groupedPackages.keySet()) {
                submitWorkflow(site, sub, groupedPackages.get(scheduledDate),now, scheduledDate, approver, mcpContext);
            }

        } catch (ContentNotFoundException e) {
            throw new ServiceException("Error during go live",e);
        } catch (org.craftercms.cstudio.alfresco.service.exception.ServiceException e) {
            throw new ServiceException("Error during go live",e);
        }
        long end = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Total go live time on rename item = " + (end - start));
        }

    }

    /**
     *
     * Prepares and starts workflow
     *
     * Reverts any child nodes which are not in the same version as staging since only URL changes has to be pushed to staging.
     * A copy of the new version is placed in a temp location and recovered once we push things to workflow
     *
     */
    protected void submitWorkflow(final String site, final String sub, final List<DmDependencyTO> submittedItems, Date now, Date scheduledDate,
                                  final String approver, MultiChannelPublishingContext mcpContext) throws ServiceException, org.craftercms.cstudio.alfresco.service.exception.ServiceException{

        final String assignee = DmUtils.getAssignee(site, sub);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        final String pathPrefix = servicesConfig.getRepositoryRootPath(site);
        final PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        final List<String> paths = new FastList<String>();
        final List<String> dependenices = new FastList<String>();
        Date launchDate = scheduledDate.equals(now) ? null : scheduledDate;
        final boolean isScheduled = launchDate==null ? false:true;

        //label will keep track of all nodes that has been reverted to staging version and used during postStagingSubmission
        final StringBuilder label = new StringBuilder();
        label.append(isScheduled ? DmConstants.SCHEDULE_RENAME_WORKFLOW_PREFIX : DmConstants.RENAME_WORKFLOW_PREFIX);
        label.append(":");
        final Set<String> rescheduledUris = new HashSet<String>();
        DmTransactionService dmTransactionService = getService(DmTransactionService.class);
        RetryingTransactionHelper helper = dmTransactionService.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>(){
            @Override
            public Object execute() throws Throwable {
                for (DmDependencyTO submittedItem : submittedItems) {
                    String workflowLabel = getWorkflowPaths(site, sub, submittedItem, pathPrefix, paths, dependenices, isScheduled, rescheduledUris);
                    label.append(workflowLabel);
                    label.append(",");
                }
                return null;
            }
        },false,true);

        Set<String>uris = new HashSet<String>();
        Map<String, String> submittedBy = new FastMap<String, String>();
        DmContentService dmContentService = getService(DmContentService.class);
        DmPublishService dmPublishService = getService(DmPublishService.class);
        SearchService searchService = getService(SearchService.class);
        for (String path : paths) {
            String uri = path.substring(pathPrefix.length());
            uris.add(uri);
            DmUtils.addToSubmittedByMapping(persistenceManagerService, dmContentService, searchService, site, uri, submittedBy, approver);
            dmPublishService.cancelScheduledItem(site, uri);
        }
        GoLiveContext context = new GoLiveContext(approver, site);
        SubmitLifeCycleOperation operation = null;
        DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
        if (launchDate == null){
            operation = new PreGoLiveOperation(dmWorkflowService, uris, context, rescheduledUris);
        }else{
            //uri will not be have dependencies
            for (String dependency: dependenices) {
                String uri = dependency.substring(pathPrefix.length());
                uris.add(uri);
                DmUtils.addToSubmittedByMapping(persistenceManagerService, dmContentService, searchService, site, uri, submittedBy, approver);
            }
            operation = new PreScheduleOperation(dmWorkflowService, uris,launchDate, context, rescheduledUris);
        }
        _workflowProcessor.addToWorkflow(site, paths,launchDate,_submitDirectWorkflowName, label.toString(), operation, approver, mcpContext);
        if (logger.isDebugEnabled()) {
            logger.debug("Go live rename: paths posted " + paths + "for workflow scheduled at : " + launchDate);
        }
    }

    /**
     * get site root (e.g. /www/avm_webapps/ROOT)
     *
     */
    protected String getSiteRoot(String site) {
        return site;
    }

    /**
     *
     * Compute the paths to be moved and paths to be deleted from Staging
     *
     * @throws org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException
     */
    protected String getWorkflowPaths(final String site, final String sub, DmDependencyTO submittedItem,
                                      final String pathPrefix, final List<String> paths, List<String> dependenices, boolean isScheduled, Set<String> rescheduledUris) throws ContentNotFoundException, ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("GoLive on renamed node " + submittedItem.getUri());
        }
        List <String> childUris = new FastList<String>();
        String submittedUri = submittedItem.getUri();
        List<String> submittedChildUris = getSubmittedChildUri(submittedItem);
        // handles the file content submission
        if (submittedUri.endsWith(DmConstants.XML_PATTERN) && !submittedUri.endsWith(DmConstants.INDEX_FILE)) {
            childUris.add(submittedUri);
        } else {
            getChildrenUri(site, getNode(site, DmUtils.getParentUrl(submittedItem.getUri())),childUris);
        }
        StringBuilder label = new StringBuilder();
        label.append(DmUtils.getParentUrl(submittedUri));
        for (String uri :childUris){
            //find all child items that are already live and revert the sandbox to staging version
            String oldStagingUri = getStoredStagingUri(site, uri);
            /*
            if (oldStagingUri != null){
                if (isRenameDeleteTag(site, uri)){
                    // handles the file content submission
                    if (submittedUri.endsWith(DmConstants.XML_PATTERN) && !submittedUri.endsWith(DmConstants.INDEX_FILE)) {
                        pathsToRemove.add(pathPrefix + oldStagingUri);
                    } else {
                        //submit the old url for delete in staging
                        String folderToRemoveInStaging = DmUtils.getParentUrl(oldStagingUri);
                        pathsToRemove.add(pathPrefix+ folderToRemoveInStaging);
                    }
                }
            } */

            if(submittedChildUris.contains(uri) || submittedItem.getUri().equals(uri)){
                //if child is one of the submitted item then add itself and references
                paths.add(pathPrefix + uri);
                List<String> refPaths = getReferencePaths(site, uri, submittedItem, pathPrefix, rescheduledUris);
                dependenices.addAll(refPaths);
                if (!isScheduled && refPaths != null && refPaths.size() > 0) { //Update dependencies during prestaging submission for dependenices
                    paths.addAll(refPaths);
                }
            }
        }
        return label.toString();
    }

    protected List<String> getSubmittedChildUri(DmDependencyTO submittedItem) {
        List<String> childUri = new FastList<String>();
        if(submittedItem.getChildren()!=null){
            for(DmDependencyTO child:submittedItem.getChildren()){
                childUri.add(child.getUri());
            }
        }
        return childUri;
    }

    protected List<String> getChildrenUri(String site, NodeRef node, List<String> paths){
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        if (nodeInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(node);
            for (FileInfo child : children) {
                getChildrenUri(site, child.getNodeRef(), paths);
            }
        } else {
            addChildUri(site, node, paths);
        }
        return paths;
    }

    protected void addChildUri(String site, NodeRef node, List<String> paths){
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullPath = persistenceManagerService.getNodePath(node);
        if(fullPath.endsWith(DmConstants.XML_PATTERN)){
            DmPathTO path = new DmPathTO(fullPath);
            String childUri = path.getRelativePath();
            paths.add(childUri);
        }
    }

    /**
     *
     * Renamed node will have content in a different location in staging.
     * Code below access the appropariate node property and returns the equivanlent staging url
     */

    protected String getStoredStagingUri(String site,String uri) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, getIndexFilePath(uri));
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        if (nodeRef == null) {
            fullPath = fullPath.replace(String.format("/%s", DmConstants.INDEX_FILE), "");
            nodeRef = persistenceManagerService.getNodeRef(fullPath);
        }
        if (nodeRef != null) {
            Serializable val = persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_RENAMED_OLD_URL);
            if(val != null){
                return (String)val;
            }
        }
        return null;
    }

    /**
     * Cut/paste a tree. Cut any child in this hirarchy and paste it somewhere outside.
     * There is a limitation that we cannot push a deleted child to staging wiothout pushing the deleted parent.
     * In such scenario we need to push the deleted parent as well. This api returns false in such case
     * @param site
     * @param uri
     * @return false if we push the renamed child without pushing the renamed parent.
     */
    protected boolean isRenameDeleteTag(String site,String uri) {
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, getIndexFilePath(uri));
        Serializable val = getService(PersistenceManagerService.class).getProperty(fullPath, CStudioContentModel.PROP_RENAMED_DELETE_URL);
        return val!=null && (Boolean)val;
    }

    /**
     * Get depedency for a given uri
     *
     */
    protected List<String> getReferencePaths(final String site, String uri, DmDependencyTO submittedItem,String pathPrefix, Set<String> rescheduledUris) throws ServiceException{
        //TODO figure out a better way to do this
        DmDependencyTO to = null;
        List<String> depedencyPaths = new FastList<String>();
        if(uri.equals(submittedItem.getUri())){
            to = submittedItem;
        }else{
            if(submittedItem.getChildren()==null)
                return null;
            for(DmDependencyTO depedencyTo:submittedItem.getChildren()){
                if(uri.equals(depedencyTo.getUri())){
                    to = depedencyTo;
                    break;
                }
            }
        }
        DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
        if(dmWorkflowService.isRescheduleRequest(to, site)){
            rescheduledUris.add(to.getUri());
        }

        _listener.postGolive(site, to);
        DmContentService dmContentService = getService(DmContentService.class);
        DependencyRules rule = new DependencyRules(site, getServicesManager());
        Set<DmDependencyTO> dependencyTOSet;
        dependencyTOSet = rule.applySubmitRule(to);
        for (DmDependencyTO dependencyTO : dependencyTOSet) {
            depedencyPaths.add(pathPrefix+dependencyTO.getUri());
            _listener.postGolive(site, dependencyTO);
        }

        return depedencyPaths;
    }

    /**
     * Move the node and all its descendant to the target path. Status of the descendant node will be maintained
     *
     * Adds two node property
     * 	PROP_RENAMED_OLDURL - for the node and all its descendant with the staging url
     * 	PROP_RENAMED_DELETEFLAG - to indicate if the url has to be submitted for delete during golive
     *
     *  @param sourcePath - source uri
     *  @param targetPath - destination uri
     *  @param createFolder - not used
     *
     * @throws ContentNotFoundException
     */
    @Override
    public void rename(String site, String sub, String sourcePath, String targetPath,	boolean createFolder) throws ServiceException, ContentNotFoundException {
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        long start = System.currentTimeMillis();
        // get index path
        if (!sourcePath.endsWith(DmConstants.XML_PATTERN)) {
            sourcePath = (sourcePath.endsWith("/")) ? sourcePath + DmConstants.INDEX_FILE : sourcePath + "/" + DmConstants.INDEX_FILE;
        }
        // get index path
        if (!targetPath.endsWith(DmConstants.XML_PATTERN)) {
            targetPath = (targetPath.endsWith("/")) ? targetPath + DmConstants.INDEX_FILE : targetPath + "/" + DmConstants.INDEX_FILE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Rename - starting to move contents from source:"+sourcePath+" to destination: "+targetPath);
        }

        String user = AuthenticationUtil.getFullyAuthenticatedUser();

        //source paths
        DmContentService dmContentService = getService(DmContentService.class);
        String srcOrgFullPath = dmContentService.getContentFullPath(site, sourcePath);
        String dstOrgFullPath = dmContentService.getContentFullPath(site, targetPath);
        String srcFullPath = srcOrgFullPath;
        if(srcFullPath.endsWith(DmConstants.INDEX_FILE)){
            srcFullPath = DmUtils.getParentUrl(srcFullPath);
        }
        String srcNodeName = DmUtils.getPageName(srcFullPath);
        String srcNodeParentUrl= DmUtils.getParentUrl(srcFullPath);
        try{

            //destination paths
            String dstFullPath = dstOrgFullPath;
            if(dstFullPath.endsWith(DmConstants.INDEX_FILE)){
                dstFullPath = DmUtils.getParentUrl(dstFullPath);
            }
            if (dstFullPath == null) {
                throw new ServiceException("Error while moving content. " + targetPath + " is not valid.");
            }
            String dstNodeName = DmUtils.getPageName(dstFullPath);
            String dstNodeParentUrl = DmUtils.getParentUrl(dstFullPath);

            preRenameCleanWorkFlow(site,sub,sourcePath);

            if (srcNodeParentUrl.equalsIgnoreCase(dstNodeParentUrl)) {
                FileInfo srcNodeInfo = persistenceManagerService.getFileInfo(srcFullPath);
                if (srcNodeInfo != null && srcNodeInfo.isFolder() && dstFullPath.endsWith(DmConstants.XML_PATTERN)) {
                    persistenceManagerService.move(persistenceManagerService.getNodeRef(srcOrgFullPath),
                        persistenceManagerService.getNodeRef(dstNodeParentUrl), dstNodeName);
                        persistenceManagerService.deleteNode(srcFullPath);
                } else if (srcNodeInfo != null && !srcNodeInfo.isFolder()
                                && !dstFullPath.endsWith(DmConstants.XML_PATTERN)) {
                    Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
                    nodeProperties.put(ContentModel.PROP_NAME, dstNodeName);
                    NodeRef parentNode = persistenceManagerService.createNewFolder(persistenceManagerService
                        .getNodeRef(dstNodeParentUrl), dstNodeName, nodeProperties);
                    persistenceManagerService.move(persistenceManagerService.getNodeRef(srcFullPath), parentNode,
                        DmConstants.INDEX_FILE);
                } else {
                    persistenceManagerService.rename(persistenceManagerService.getNodeRef(srcFullPath), dstNodeName);
                }
            } else {
                persistenceManagerService.move(persistenceManagerService.getNodeRef(srcFullPath), persistenceManagerService.getNodeRef(dstNodeParentUrl), dstNodeName);
            }

            NodeRef node = persistenceManagerService.getNodeRef(dstFullPath);
            if (node == null) {
                throw new ContentNotFoundException("Error while moving content " + dstFullPath + " does not exist.");
            }

            String renamedUrl = getService(PersistenceManagerService.class).getNodePath(node);
            DmPathTO fileDmPath = new DmPathTO(renamedUrl);
            String renamedUri = fileDmPath.getRelativePath();
            String storedStagingUri = getStoredStagingUri(site, renamedUri);
            persistenceManagerService.updateObjectPath(node, renamedUri);

            if(storedStagingUri == null){
                addRenameUriDeleteProperty(renamedUrl);
            }

            //update cache and add node property to all children with oldurl if required
            postRenameUpdateStatus(user, site, targetPath, sourcePath, true);

         

            // run through the lifecycle service
            Map<String, String> params = new FastMap<String, String>();
            params.put(DmConstants.KEY_SOURCE_PATH, sourcePath);
            params.put(DmConstants.KEY_TARGET_PATH, targetPath);
            params.put(DmConstants.KEY_SOURCE_FULL_PATH, srcOrgFullPath);
            params.put(DmConstants.KEY_TARGET_FULL_PATH, dstOrgFullPath);
            NodeRef dstOrgNodeRef = persistenceManagerService.getNodeRef(dstOrgFullPath);
            if (dstOrgNodeRef == null) {
                dstOrgFullPath = dstOrgFullPath.replace("/" + DmConstants.INDEX_FILE, "");
                dstOrgNodeRef = persistenceManagerService.getNodeRef(dstOrgFullPath);
            }
            String contentType = null;
            if (dstOrgNodeRef != null) {
                Serializable contentTypeValue = persistenceManagerService.getProperty(dstOrgFullPath, CStudioContentModel.PROP_CONTENT_TYPE);
                contentType = (contentTypeValue != null) ? (String)contentTypeValue : null;
            }
            DmContentLifeCycleService dmContentLifeCycleService = getService(DmContentLifeCycleService.class);
            dmContentLifeCycleService.process(site, user, targetPath, contentType,
                    DmContentLifeCycleService.ContentLifeCycleOperation.RENAME, params);
        } catch (FileNotFoundException e) {
            throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        } catch (AVMBadArgumentException e) {
            throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        } catch (AVMNotFoundException e) {
            throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        } finally{
            AuthenticationUtil.setFullyAuthenticatedUser(user);
        }

        long end = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Total time to rename = " + (end - start));
        }
    }

    /**
     * Remove any old uri from the workflow and puts it them to in progress
     *
     * @param site
     * @param sub
     * @param path
     */
    protected void preRenameCleanWorkFlow(String site,String sub,String path ){
        if(path.endsWith(DmConstants.INDEX_FILE)){
            path = DmUtils.getParentUrl(path);
        }
        NodeRef node = getNode(site, path);
        List<String> childUris = new FastList<String>();
        if (node != null) {
            getChildrenUri(site, node, childUris);
        }
        try{
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
            List<String> transitionNodes = new ArrayList<String>();
            for(String childUri: childUris){
                dmWorkflowService.removeFromWorkflow(site, sub, childUri, true);
                NodeRef nodeRef = getIndexNode(site, childUri);
                if (nodeRef != null) {
                    transitionNodes.add(nodeRef.getId());
                }
            }
            if (!transitionNodes.isEmpty()) {
                persistenceManagerService.transitionBulk(transitionNodes, ObjectStateService.TransitionEvent.SAVE, ObjectStateService.State.NEW_UNPUBLISHED_UNLOCKED);
            }
        }catch(Exception e){
            throw new ServiceException("Error during clean workflow",e);
        }
    }

    protected void addRenameUriDeleteProperty(String filePath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(getIndexFilePath(filePath));
        if (nodeRef == null) {
            nodeRef = persistenceManagerService.getNodeRef(filePath);
        }
        if (nodeRef != null) {
            persistenceManagerService.setProperty(nodeRef, CStudioContentModel.PROP_RENAMED_DELETE_URL, "true");
        }
    }

    /**
     *
     * Updates acitivity dashboard, invalidates cache and adds node properties
     *
     * @param site
     * @param path
     * @throws ContentNotFoundException
     */
    protected void postRenameUpdateStatus(String user, String site, String path, String oldPath,boolean addNodeProperty) throws ContentNotFoundException{

        //we'll need to work with index.xml
        path = getIndexFilePath(path);
        oldPath = getIndexFilePath(oldPath);

        DmContentService dmContentService = getService(DmContentService.class);
        String srcFullPath = dmContentService.getContentFullPath(site, path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(srcFullPath);
        List<String> transitionNodes = new ArrayList<String>();
        if (node == null) {
            srcFullPath = srcFullPath.replace("/" + DmConstants.INDEX_FILE, "");
            oldPath = oldPath.replace("/" + DmConstants.INDEX_FILE, "");
            node = persistenceManagerService.getNodeRef(srcFullPath);
        }
        //change last modified property to the current user
        persistenceManagerService.setProperty(node, CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
        if (srcFullPath.endsWith(DmConstants.INDEX_FILE)) {
            // if the file is index.xml, update child contnets
            String parentNodePath = DmUtils.getParentUrl(srcFullPath);
            NodeRef parent = persistenceManagerService.getNodeRef(parentNodePath);
            updateChildNodes(site, parent, oldPath, path, addNodeProperty, user, false);
        } else {
            // for the file content, update the file itself
            NodeRef parent = persistenceManagerService.getNodeRef(srcFullPath);
            updateChildNodes(site, parent, oldPath, path, addNodeProperty, user, true);
        }
        List<String> childUris = new FastList<String>();
        if (node != null) {
            transitionNodes.add(node.getId());
            getChildrenUri(site, node, childUris);
        }
        for (String childUri : childUris) {
            NodeRef childNode = persistenceManagerService.getNodeRef(childUri);
            if (childNode != null) {
                transitionNodes.add(childNode.getId());
            }
        }
        if (!transitionNodes.isEmpty()) {
            persistenceManagerService.transitionBulk(transitionNodes, ObjectStateService.TransitionEvent.SAVE, ObjectStateService.State.NEW_UNPUBLISHED_UNLOCKED);
        }
    }


    /**
     * update child node with olduri property and invalidate cache for the old uri
     *
     * @param node
     * @param parentOldPath
     * @param parentNewPath
     */
    protected void updateChildNodes(String site, NodeRef node, String parentOldPath, String parentNewPath, boolean addNodeProperty, String user, boolean fileContent) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        if (nodeInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(node);
            for (FileInfo child : children) {
                updateChildNodes(site, child.getNodeRef(), parentOldPath, parentNewPath, addNodeProperty, user, fileContent);
            }
        } else {
            DmContentService dmContentService = getService(DmContentService.class);
            ActivityService activityService = getService(ActivityService.class);
            Map<String,String> extraInfo = new HashMap<String,String>();
            String relativePath = new DmPathTO(persistenceManagerService.getNodePath(node)).getRelativePath();
            addNodePropertyToChildren(site, persistenceManagerService.getNodePath(node), parentNewPath, parentOldPath, addNodeProperty, user,
                fileContent);
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, dmContentService.getContentType(site, getIndexFilePath(relativePath)));
            activityService.postActivity(site, user, getIndexFilePath(relativePath), ActivityService.ActivityType.UPDATED, extraInfo);
        }
    }

    protected void addNodePropertyToChildren(String site, String fullPath, String renamedPath, String oldPath, boolean addNodeProperty, String user, boolean fileContent){
        DmContentService dmContentService = getService(DmContentService.class);
        DmPathTO path = new DmPathTO(fullPath);
        String childUri = path.getRelativePath();
        String oldUri = (fileContent) ? oldPath : childUri.replace(DmUtils.getParentUrl(renamedPath), DmUtils.getParentUrl(oldPath));
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(node);

        if (addNodeProperty && getStoredStagingUri(site,childUri) == null && fileContent) {
            persistenceManagerService.addAspect(node, CStudioContentModel.ASPECT_RENAMED, new HashMap< QName, Serializable>());
            if (nodeProperties.get(CStudioContentModel.PROP_RENAMED_OLD_URL) == null) {
                nodeProperties.put(CStudioContentModel.PROP_RENAMED_OLD_URL, oldUri);
            }
        } else {
            String indexFilePath = getIndexFilePath(fullPath);
            NodeRef indexNode = persistenceManagerService.getNodeRef(indexFilePath);
            persistenceManagerService.addAspect(indexNode, CStudioContentModel.ASPECT_RENAMED, new HashMap<QName, Serializable>());
            if (nodeProperties.get(CStudioContentModel.PROP_RENAMED_OLD_URL) == null) {
                nodeProperties.put(CStudioContentModel.PROP_RENAMED_OLD_URL, oldUri);
            }
        }
        persistenceManagerService.updateObjectPath(node, path.getRelativePath());
        nodeProperties.put(ContentModel.PROP_MODIFIER, user);
        nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
        persistenceManagerService.setProperties(node, nodeProperties);


        //dependencies also has to be moved post rename
        try{
            Document document = dmContentService.getContentXml(site, null, childUri);
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            Map<String, Set<String>> globalDeps = new FastMap<String, Set<String>>();
            dmDependencyService.extractDependencies(site, childUri, document, globalDeps);
        }catch(Exception e){
            throw new ServiceException("Error during extracting dependency of "+childUri,e);
        }
        updateGoLiveQueue(site,fullPath,oldUri);
        updateActivity(childUri,oldUri,site);
    }

    protected void updateGoLiveQueue(String site,String newFullPath, String oldUri){
        GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY, site);
        /** cached go live queue disabled **/
        if (queue != null) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            DmContentItemTO to;
            try {
                to = persistenceManagerService.getContentItem(newFullPath);
                if(to != null){
                    queue.add(to);
                    queue.remove(oldUri);
                }
            } catch (Exception e) {
                logger.warn("Exception during updateGoLive Queue",e);
            }
        }
    }

    protected void updateActivity(String newUrl, String oldUrl, String site){
        if (logger.isDebugEnabled()) {
            logger.debug("Updating activity url post rename:"+newUrl);
        }
        ActivityService activityService = getService(ActivityService.class);
        activityService.renameContentId(oldUrl, newUrl,site);
    }

    /**
     * updateWorkflow with additional urls during Rename GO Live
     *
     *  Called during prestaging submssion
     *
     */
    @Override
    public void updateWorkflow(String site, String workFlowDescription) {
        long start = System.currentTimeMillis();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        SearchService searchService = getService(SearchService.class);
        List<NodeRef> changeSet = DmUtils.getChangeSet(searchService, servicesConfig, site);
        if (changeSet != null && changeSet.size() > 0) {
            DmContentService dmContentService = getService(DmContentService.class);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (NodeRef workFlowNode : changeSet) {
                DmPathTO path = new DmPathTO(persistenceManagerService.getNodePath(workFlowNode));
                List<String> submittedChildPaths = new FastList<String>();
                getChildrenUri(site, workFlowNode, submittedChildPaths);

                try {
                    String fullPath = dmContentService.getContentFullPath(site, path.getRelativePath());
                    if (!fullPath.endsWith(DmConstants.XML_PATTERN)) {
                        fullPath += "/" + DmConstants.INDEX_FILE;
                    }
                    //persistenceManagerService.removeAspect(workFlowNode, CStudioContentModel.ASPECT_RENAMED);
                    //updateWorkFlowSandbox(site, path.getRelativePath(), submittedChildPaths);
                    //updateWorkflowSandboxWithDiff(site, fullPath);

                } catch (Exception e) {
                    logger.error("failed to update workflow sandbox : ", e);
                }
            }
        }
        long end = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Total pre-staging processing on Renamed Item = " + (end - start));
        }
    }

    /**
     * Update the workflowSandbox with additional URI. Also revert to staging version if child is not submitted for go live by the User
     *
     * @param site
     * @param renamedPath
     * @param submittedUri
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     */
    protected void updateWorkFlowSandbox (String site, String renamedPath, List<String> submittedUri) throws org.craftercms.cstudio.alfresco.service.exception.ServiceException{

        List<String> childUris = new FastList<String>();
        getChildrenUri(site, getNode(site, renamedPath), childUris);
        DmContentService dmContentService = getService(DmContentService.class);
        
        for (String uri :childUris){
            //only for the child items that are not submitted to go live by the user
            if(!submittedUri.contains(uri)){
                //find all child items that are already live and revert the sandbox to staging version
                String depFullPath = dmContentService.getContentFullPath(site, uri);
                getService(PersistenceManagerService.class).removeAspect(depFullPath, CStudioContentModel.ASPECT_RENAMED);
            }
        }
    }

    /**
     * Actions to be performed post submission
     * Put the temp copy back. The uris will be coming in from the workflow
     */
    @Override
    public void postSubmission(String site, String workFlowDescription) {
        // do nothing. should not touch the site after workflow is completed
    }
}
