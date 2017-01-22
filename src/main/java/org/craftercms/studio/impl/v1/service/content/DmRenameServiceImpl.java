/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
import org.craftercms.core.service.CacheService;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.listener.DmWorkflowListener;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmRenameService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyRules;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
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
     * Is provided node renamed?
     * we always look into index.xml for node properties
     *
     */
    protected boolean isItemRenamed(String site, String uri){
        return objectMetadataManager.isRenamed(site, uri);
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
            }
            operation = new PreScheduleOperation(workflowService, uris,launchDate, context, rescheduledUris);
        }
        workflowProcessor.addToWorkflow(site, paths, launchDate, label.toString(), operation, approver, mcpContext);
        logger.debug("Go live rename: paths posted " + paths + "for workflow scheduled at : " + launchDate);
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
        }
        paths.add(itemTree.getUri());
        return paths;
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

    protected ContentService contentService;
    protected ObjectStateService objectStateService;
    protected WorkflowService workflowService;
    protected DmWorkflowListener dmWorkflowListener;
    protected DmPublishService dmPublishService;
    protected WorkflowProcessor workflowProcessor;
    protected ObjectMetadataManager objectMetadataManager;

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public WorkflowService getWorkflowService() { return workflowService; }
    public void setWorkflowService(WorkflowService workflowService) { this.workflowService = workflowService; }

    public DmWorkflowListener getDmWorkflowListener() { return dmWorkflowListener; }
    public void setDmWorkflowListener(DmWorkflowListener dmWorkflowListener) { this.dmWorkflowListener = dmWorkflowListener; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }


    public WorkflowProcessor getWorkflowProcessor() { return workflowProcessor; }
    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) { this.workflowProcessor = workflowProcessor; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }


}
