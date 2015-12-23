/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.listener.DmWorkflowListener;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmContentLifeCycleService;
import org.craftercms.studio.api.v1.service.content.DmRenameService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyRules;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.impl.v1.content.pipeline.DmWorkflowProcessor;
import org.craftercms.studio.impl.v1.service.workflow.WorkflowProcessor;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreGoLiveOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreScheduleOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.SubmitLifeCycleOperation;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;

import java.util.*;

public class DmRenameServiceImpl extends AbstractRegistrableService implements DmRenameService {

    private static final Logger logger = LoggerFactory.getLogger(DmRenameServiceImpl.class);



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
     *//*
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
     *//*
    protected String getOldUrl(String site,String cutPath){
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullCutPath = dmContentService.getContentFullPath(site, cutPath);
        NodeRef cutPathNode = persistenceManagerService.getNodeRef(fullCutPath);
        String originalUrl = (String)persistenceManagerService.getProperty(cutPathNode, CStudioContentModel.PROP_RENAMED_OLD_URL);
        return originalUrl;
    }*/

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
        return objectMetadataManager.isRenamed(site, uri);
    }

    /**
     * Always returns node of the index.xml
     *//*
    protected NodeRef getIndexNode(final String site, String uri) {
        return getNode(site, getIndexFilePath(uri));
    }


    /**
     * GoLive on the renamed node
     *//*
    @Override
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver) throws ServiceException {
                goLive(site, sub, submittedItems, approver, null);
    }

    /**
     * GoLive on the renamed node
     */
    @Override
    public void goLive(String site, List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException {
                long start = System.currentTimeMillis();

        try {
            Date now = new Date();
            Map<Date, List<DmDependencyTO>> groupedPackages = workflowService.groupByDate(submittedItems, now);

            for (Date scheduledDate : groupedPackages.keySet()) {
                submitWorkflow(site, groupedPackages.get(scheduledDate),now, scheduledDate, approver, mcpContext);
            }

        } catch (ContentNotFoundException e) {
            throw new ServiceException("Error during go live",e);
        } catch (ServiceException e) {
            throw new ServiceException("Error during go live",e);
        }
        long end = System.currentTimeMillis();
        logger.debug("Total go live time on rename item = " + (end - start));


    }

    /**
     *
     * Prepares and starts workflow
     *
     * Reverts any child nodes which are not in the same version as staging since only URL changes has to be pushed to staging.
     * A copy of the new version is placed in a temp location and recovered once we push things to workflow
     *
     */
    protected void submitWorkflow(final String site, final List<DmDependencyTO> submittedItems, Date now, Date scheduledDate,
                                  final String approver, MultiChannelPublishingContext mcpContext) throws ServiceException{

        final String assignee = "" ;//DmUtils.getAssignee(site, sub);
        final List<String> paths = new ArrayList<>();
        final List<String> dependenices = new ArrayList<>();
        Date launchDate = scheduledDate.equals(now) ? null : scheduledDate;
        final boolean isScheduled = launchDate == null ? false : true;
        String pathPrefix = "/wem-projects/" + site + "/" + site + "/work-area";

        //label will keep track of all nodes that has been reverted to staging version and used during postStagingSubmission
        final StringBuilder label = new StringBuilder();
        label.append(isScheduled ? DmConstants.SCHEDULE_RENAME_WORKFLOW_PREFIX : DmConstants.RENAME_WORKFLOW_PREFIX);
        label.append(":");
        final Set<String> rescheduledUris = new HashSet<String>();
        for (DmDependencyTO submittedItem : submittedItems) {
            String workflowLabel = getWorkflowPaths(site, submittedItem, pathPrefix, paths, dependenices, isScheduled, rescheduledUris);
            label.append(workflowLabel);
            label.append(",");
        }

        Set<String>uris = new HashSet<String>();
        Map<String, String> submittedBy = new HashMap<>();
        for (String path : paths) {
            String uri = path.substring(pathPrefix.length());
            uris.add(uri);
            //DmUtils.addToSubmittedByMapping(persistenceManagerService, dmContentService, searchService, site, uri, submittedBy, approver);
            dmPublishService.cancelScheduledItem(site, uri);
        }
        GoLiveContext context = new GoLiveContext(approver, site);
        SubmitLifeCycleOperation operation = null;
        if (launchDate == null){
            operation = new PreGoLiveOperation(workflowService, uris, context, rescheduledUris);
        }else{
            //uri will not be have dependencies
            for (String dependency: dependenices) {
                String uri = dependency.substring(pathPrefix.length());
                uris.add(uri);
                //DmUtils.addToSubmittedByMapping(persistenceManagerService, dmContentService, searchService, site, uri, submittedBy, approver);
            }
            operation = new PreScheduleOperation(workflowService, uris,launchDate, context, rescheduledUris);
        }
        workflowProcessor.addToWorkflow(site, paths, launchDate, label.toString(), operation, approver, mcpContext);
        logger.debug("Go live rename: paths posted " + paths + "for workflow scheduled at : " + launchDate);
    }

    /**
     * get site root (e.g. /www/avm_webapps/ROOT)
     *
     *//*
    protected String getSiteRoot(String site) {
        return site;
    }

    /**
     *
     * Compute the paths to be moved and paths to be deleted from Staging
     *
     * @throws org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException
     */
    protected String getWorkflowPaths(final String site, DmDependencyTO submittedItem,
                                      final String pathPrefix, final List<String> paths, List<String> dependenices, boolean isScheduled, Set<String> rescheduledUris) throws ContentNotFoundException, ServiceException {

        logger.debug("GoLive on renamed node " + submittedItem.getUri());

        List <String> childUris = new ArrayList<>();
        String submittedUri = submittedItem.getUri();
        List<String> submittedChildUris = getSubmittedChildUri(submittedItem);
        // handles the file content submission
        if (submittedUri.endsWith(DmConstants.XML_PATTERN) && !submittedUri.endsWith(DmConstants.INDEX_FILE)) {
            childUris.add(submittedUri);
        } else {
            getChildrenUri(site, ContentUtils.getParentUrl(submittedItem.getUri()),childUris);
        }
        StringBuilder label = new StringBuilder();
        label.append(ContentUtils.getParentUrl(submittedUri));
        for (String uri :childUris){
            //find all child items that are already live and revert the sandbox to staging version
            String oldStagingUri = objectMetadataManager.getOldPath(site, uri);
            
            if (oldStagingUri != null){
                if (isRenameDeleteTag(site, uri)){
                    // handles the file content submission
                    if (submittedUri.endsWith(DmConstants.XML_PATTERN) && !submittedUri.endsWith(DmConstants.INDEX_FILE)) {
                       //PORT  pathsToRemove.add(pathPrefix + oldStagingUri);
                    } else {
                        //submit the old url for delete in staging
                        String folderToRemoveInStaging = ContentUtils.getParentUrl(oldStagingUri);
                       //PORT  pathsToRemove.add(pathPrefix+ folderToRemoveInStaging);
                    }
                }
            } 

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
        List<String> childUri = new ArrayList<>();
        if(submittedItem.getChildren()!=null){
            for(DmDependencyTO child:submittedItem.getChildren()){
                childUri.add(child.getUri());
            }
        }
        return childUri;
    }

    protected List<String> getChildrenUri(String site, String path, List<String> paths){
        ContentItemTO itemTree = contentService.getContentItemTree(site, path, 1);
        if (itemTree.getNumOfChildren() > 0) {
            for (ContentItemTO child : itemTree.getChildren()) {
                getChildrenUri(site, child.getUri(), paths);
            }
        } else {
            paths.add(itemTree.getUri());
        }
        return paths;
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
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, uri);
        return StringUtils.isEmpty(metadata.getDeleteUrl());
    }

    /**
     * Get depedency for a given uri
     *
     */
    protected List<String> getReferencePaths(final String site, String uri, DmDependencyTO submittedItem,String pathPrefix, Set<String> rescheduledUris) throws ServiceException{
        //TODO figure out a better way to do this
        DmDependencyTO to = null;
        List<String> depedencyPaths = new ArrayList<>();
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
        if(workflowService.isRescheduleRequest(to, site)){
            rescheduledUris.add(to.getUri());
        }

        dmWorkflowListener.postGolive(site, to);
        DependencyRules rule = new DependencyRules(site);
        rule.setContentService(contentService);
        rule.setObjectStateService(objectStateService);
        Set<DmDependencyTO> dependencyTOSet;
        dependencyTOSet = rule.applySubmitRule(to);
        for (DmDependencyTO dependencyTO : dependencyTOSet) {
            depedencyPaths.add(pathPrefix+dependencyTO.getUri());
            dmWorkflowListener.postGolive(site, dependencyTO);
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
    public void rename(String site, String sourcePath, String targetPath,	boolean createFolder) throws ServiceException, ContentNotFoundException {
        long start = System.currentTimeMillis();
        // get index path
        if (!sourcePath.endsWith(DmConstants.XML_PATTERN)) {
            sourcePath = (sourcePath.endsWith("/")) ? sourcePath + DmConstants.INDEX_FILE : sourcePath + "/" + DmConstants.INDEX_FILE;
        }
        // get index path
        if (!targetPath.endsWith(DmConstants.XML_PATTERN)) {
            targetPath = (targetPath.endsWith("/")) ? targetPath + DmConstants.INDEX_FILE : targetPath + "/" + DmConstants.INDEX_FILE;
        }
        logger.debug("Rename - starting to move contents from source:"+sourcePath+" to destination: "+targetPath);

        String user = securityService.getCurrentUser();

        //source paths
        String srcOrgFullPath = contentService.expandRelativeSitePath(site, sourcePath);
        String dstOrgFullPath = contentService.expandRelativeSitePath(site, targetPath);
        String srcFullPath = srcOrgFullPath;
        if(srcFullPath.endsWith(DmConstants.INDEX_FILE)){
            srcFullPath = ContentUtils.getParentUrl(srcFullPath);
        }
        String srcNodeName = ContentUtils.getPageName(srcFullPath);
        String srcNodeParentUrl= ContentUtils.getParentUrl(srcFullPath);
        try{

            //destination paths
            String dstFullPath = dstOrgFullPath;
            if(dstFullPath.endsWith("/" + DmConstants.INDEX_FILE)){
                dstFullPath = ContentUtils.getParentUrl(dstFullPath);
            }
            if (dstFullPath == null) {
                throw new ServiceException("Error while moving content. " + targetPath + " is not valid.");
            }
            String dstNodeName = ContentUtils.getPageName(dstFullPath);
            String dstNodeParentUrl = ContentUtils.getParentUrl(dstFullPath);

            preRenameCleanWorkFlow(site, sourcePath);

            String dstNodeParentPath = contentService.getRelativeSitePath(site, dstNodeParentUrl);
            String dstPath = contentService.getRelativeSitePath(site, dstFullPath);

            if (srcNodeParentUrl.equalsIgnoreCase(dstNodeParentUrl)) {
                ContentItemTO srcItem = contentService.getContentItem(srcFullPath);
                if (srcItem != null && srcItem.isFolder() && dstFullPath.endsWith(DmConstants.XML_PATTERN)) {
                    contentService.moveContent(site, contentService.getRelativeSitePath(site, srcFullPath), targetPath);
                } else if (srcItem != null && !srcItem.isFolder()
                                && !dstFullPath.endsWith(DmConstants.XML_PATTERN)) {
                    contentService.createFolder(site, dstNodeParentPath, dstNodeName);
                    contentService.moveContent(site, contentService.getRelativeSitePath(site, srcFullPath), dstPath);
                } else {
                    contentService.moveContent(site, contentService.getRelativeSitePath(site, srcFullPath), ContentUtils.getParentUrl(dstPath), ContentUtils.getPageName(dstPath));
                }
            } else {
                if (dstPath.endsWith("/" + DmConstants.INDEX_FILE)) {
                    dstPath = dstPath.replace("/" + DmConstants.INDEX_FILE, "");
                    dstNodeParentPath = ContentUtils.getParentUrl(dstPath);
                    dstNodeName = ContentUtils.getPageName(dstPath);
                    if (!contentService.contentExists(site, dstPath)) {
                        contentService.createFolder(site, dstNodeParentPath, dstNodeName);
                    }
                    contentService.moveContent(site, contentService.getRelativeSitePath(site, srcFullPath), dstPath, DmConstants.INDEX_FILE);
                } else {
                    dstNodeParentPath = ContentUtils.getParentUrl(dstPath);
                    dstNodeName = ContentUtils.getPageName(dstPath);
                    if (!contentService.contentExists(site, dstNodeParentPath)) {
                        contentService.createFolder(site, ContentUtils.getParentUrl(dstNodeParentPath), ContentUtils.getPageName(dstNodeParentPath));
                    }
                    contentService.moveContent(site, contentService.getRelativeSitePath(site, srcFullPath), dstNodeParentPath, dstNodeName);
                }
            }

            ContentItemTO item = contentService.getContentItem(site, contentService.getRelativeSitePath(site, dstFullPath));
            if (item == null) {
                throw new ContentNotFoundException("Error while moving content " + dstFullPath + " does not exist.");
            }

            String renamedUri = item.getUri();
            String storedStagingUri = objectMetadataManager.getOldPath(site, renamedUri);
            objectStateService.updateObjectPath(site, sourcePath, renamedUri);

            if(storedStagingUri == null){
                addRenameUriDeleteProperty(site, renamedUri);
            }

            //update cache and add node property to all children with oldurl if required
            postRenameUpdateStatus(user, site, targetPath, sourcePath, true);

         

            // run through the lifecycle service
            Map<String, String> params = new HashMap<>();
            params.put(DmConstants.KEY_SOURCE_PATH, sourcePath);
            params.put(DmConstants.KEY_TARGET_PATH, targetPath);
            params.put(DmConstants.KEY_SOURCE_FULL_PATH, srcOrgFullPath);
            params.put(DmConstants.KEY_TARGET_FULL_PATH, dstOrgFullPath);
            /*
            NodeRef dstOrgNodeRef = persistenceManagerService.getNodeRef(dstOrgFullPath);
            if (dstOrgNodeRef == null) {
                dstOrgFullPath = dstOrgFullPath.replace("/" + DmConstants.INDEX_FILE, "");
                dstOrgNodeRef = persistenceManagerService.getNodeRef(dstOrgFullPath);
            }
            String contentType = null;

            if (dstOrgNodeRef != null) {
                Serializable contentTypeValue = persistenceManagerService.getProperty(dstOrgFullPath, CStudioContentModel.PROP_CONTENT_TYPE);
                contentType = (contentTypeValue != null) ? (String)contentTypeValue : null;
            }*/
            String contentTypeClass = contentService.getContentTypeClass(site, contentService.getRelativeSitePath(site, dstOrgFullPath));
            dmContentLifeCycleService.process(site, user, targetPath, contentTypeClass,
                    DmContentLifeCycleService.ContentLifeCycleOperation.RENAME, params);
        //} catch (FileNotFoundException e) {
        //    throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        //PORT } catch (AVMBadArgumentException e) {
        //     throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        //PORT } catch (AVMNotFoundException e) {
        //    throw new ContentNotFoundException("Error while moving " + sourcePath +" to "+targetPath, e);
        } finally{
            //AuthenticationUtil.setFullyAuthenticatedUser(user);
        }

        long end = System.currentTimeMillis();
        logger.debug("Total time to rename = " + (end - start));
    }


    /**
     * Remove any old uri from the workflow and puts it them to in progress
     *
     * @param site
     * @param path
     */
    protected void preRenameCleanWorkFlow(String site, String path ) throws ServiceException {
        if(path.endsWith(DmConstants.INDEX_FILE)){
            path = ContentUtils.getParentUrl(path);
        }
        ContentItemTO item = contentService.getContentItem(site, path);
        List<String> childUris = new ArrayList<>();
        if (item != null) {
            getChildrenUri(site, item.getUri(), childUris);
        }
        try{
            List<String> transitionNodes = new ArrayList<String>();
            for(String childUri: childUris){
                workflowService.removeFromWorkflow(site, childUri, true);
                ContentItemTO childItem = contentService.getContentItem(site, getIndexFilePath(childUri));
                if (childItem != null) {
                    transitionNodes.add(childUri);
                }
            }
            if (!transitionNodes.isEmpty()) {
                objectStateService.transitionBulk(site, transitionNodes, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE, org.craftercms.studio.api.v1.service.objectstate.State.NEW_UNPUBLISHED_UNLOCKED);
            }
        }catch(Exception e){
            logger.error("Error during clean workflow",e);
            throw new ServiceException("Error during clean workflow",e);
        }
    }

    protected void addRenameUriDeleteProperty(String site, String relativePath) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ObjectMetadata.PROP_DELETE_URL, true);
        objectMetadataManager.setObjectMetadata(site, relativePath, properties);
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

        String srcFullPath = contentService.expandRelativeSitePath(site, path);
        ContentItemTO itemTO = contentService.getContentItem(site, path);
        List<String> transitionItems = new ArrayList<String>();
        if (itemTO == null) {
            srcFullPath = srcFullPath.replace("/" + DmConstants.INDEX_FILE, "");
            oldPath = oldPath.replace("/" + DmConstants.INDEX_FILE, "");
            itemTO = contentService.getContentItem(srcFullPath);
        }
        //change last modified property to the current user
        //persistenceManagerService.setProperty(node, CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
        if (srcFullPath.endsWith(DmConstants.INDEX_FILE)) {
            // if the file is index.xml, update child contnets

            String parentNodePath = ContentUtils.getParentUrl(srcFullPath);
            String parentRelativePath = contentService.getRelativeSitePath(site, parentNodePath);
            ContentItemTO parentItem = contentService.getContentItem(site, parentRelativePath);
            updateChildItems(site, parentItem, oldPath, path, addNodeProperty, user, false);
        } else {
            // for the file content, update the file itself
            ContentItemTO parentItem = contentService.getContentItem(srcFullPath);
            updateChildItems(site, parentItem, oldPath, path, addNodeProperty, user, true);
        }
        List<String> childUris = new ArrayList<>();
        if (itemTO != null) {
            transitionItems.add(itemTO.getUri());
            getChildrenUri(site, itemTO.getUri(), childUris);
        }
        for (String childUri : childUris) {
            ContentItemTO childItem = contentService.getContentItem(site, childUri);
            if (childItem != null) {
                transitionItems.add(childItem.getUri());
            }
        }
        if (!transitionItems.isEmpty()) {
            objectStateService.transitionBulk(site, transitionItems, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE, org.craftercms.studio.api.v1.service.objectstate.State.NEW_UNPUBLISHED_UNLOCKED);
        }
    }


    /**
     * update child node with olduri property and invalidate cache for the old uri
     *
     * @param node
     * @param parentOldPath
     * @param parentNewPath
     */
    protected void updateChildItems(String site, ContentItemTO node, String parentOldPath, String parentNewPath, boolean addNodeProperty, String user, boolean fileContent) {
        ContentItemTO itemTree = contentService.getContentItemTree(site, node.getUri(), 1);
        if (itemTree.getNumOfChildren() > 0) {
            for (ContentItemTO child : itemTree.getChildren()) {
                updateChildItems(site, child, parentOldPath, parentNewPath, addNodeProperty, user, fileContent);
            }
        } else {
            Map<String,String> extraInfo = new HashMap<String,String>();
            String relativePath = contentService.getRelativeSitePath(site, node.getUri());
            addItemPropertyToChildren(site, relativePath, parentNewPath, parentOldPath, addNodeProperty, user,
                    fileContent);
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, contentService.getContentTypeClass(site, getIndexFilePath(relativePath)));
            activityService.postActivity(site, user, getIndexFilePath(relativePath), ActivityService.ActivityType.UPDATED, extraInfo);
        }
    }

    protected void addItemPropertyToChildren(String site, String relativePath, String renamedPath, String oldPath, boolean addNodeProperty, String user, boolean fileContent){

        String oldUri = (fileContent) ? oldPath : relativePath.replace(ContentUtils.getParentUrl(renamedPath), ContentUtils.getParentUrl(oldPath));
        objectStateService.updateObjectPath(site, oldUri, relativePath);
        objectMetadataManager.updateObjectPath(site, oldUri, relativePath);
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, relativePath);
        if (metadata == null) {
            metadata = new ObjectMetadata();
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        if (addNodeProperty && StringUtils.isEmpty(metadata.getOldUrl()) && fileContent) {
            properties.put(ObjectMetadata.PROP_RENAMED, 1);
            if (StringUtils.isEmpty(metadata.getOldUrl())) {
                properties.put(ObjectMetadata.PROP_OLD_URL, oldUri);
            }
        } else {
            String indexRelativePath = getIndexFilePath(relativePath);
            metadata = objectMetadataManager.getProperties(site, indexRelativePath);
            if (metadata == null) {
                metadata = new ObjectMetadata();
            }
            properties.put(ObjectMetadata.PROP_RENAMED, 1);
            if (StringUtils.isEmpty(metadata.getOldUrl())) {
                properties.put(ObjectMetadata.PROP_OLD_URL, oldUri);
            }
        }
        properties.put(ObjectMetadata.PROP_MODIFIER, user);
        objectMetadataManager.setObjectMetadata(site, relativePath, properties);


        //dependencies also has to be moved post rename
        try{
            Document document = contentService.getContentAsDocument(contentService.expandRelativeSitePath(site, relativePath));
            Map<String, Set<String>> globalDeps = new HashMap<String, Set<String>>();
            dmDependencyService.extractDependencies(site, relativePath, document, globalDeps);
        }catch(Exception e){
            logger.error("Error during extracting dependency of " + relativePath, e);
        }
        updateGoLiveQueue(site, relativePath, oldUri);
        updateActivity(site, oldUri, relativePath);
    }

    protected void updateGoLiveQueue(String site,String newFullPath, String oldUri){
        //GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY, site);
        /** cached go live queue disabled **//*
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
        }*/
    }

    protected void updateActivity(String site, String oldUrl, String newUrl){
        logger.debug("Updating activity url post rename:"+newUrl);
        activityService.renameContentId(site, oldUrl, newUrl);
    }

    /**
     * updateWorkflow with additional urls during Rename GO Live
     *
     *  Called during prestaging submssion
     *
     *//*
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
     *//*
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
     *//*
    @Override
    public void postSubmission(String site, String workFlowDescription) {
        // do nothing. should not touch the site after workflow is completed
    }*/


    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public WorkflowService getWorkflowService() { return workflowService; }
    public void setWorkflowService(WorkflowService workflowService) { this.workflowService = workflowService; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

    public DmContentLifeCycleService getDmContentLifeCycleService() { return dmContentLifeCycleService; }
    public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) { this.dmContentLifeCycleService = dmContentLifeCycleService; }

    public DmWorkflowListener getDmWorkflowListener() { return dmWorkflowListener; }
    public void setDmWorkflowListener(DmWorkflowListener dmWorkflowListener) { this.dmWorkflowListener = dmWorkflowListener; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }

    public WorkflowProcessor getWorkflowProcessor() { return workflowProcessor; }
    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) { this.workflowProcessor = workflowProcessor; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    protected SecurityService securityService;
    protected ContentService contentService;
    protected ObjectStateService objectStateService;
    protected WorkflowService workflowService;
    protected ActivityService activityService;
    protected DmContentLifeCycleService dmContentLifeCycleService;
    protected DmWorkflowListener dmWorkflowListener;
    protected DmPublishService dmPublishService;
    protected WorkflowProcessor workflowProcessor;
    protected ObjectMetadataManager objectMetadataManager;
    protected DmDependencyService dmDependencyService;
}
