/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.service.workflow;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.impl.v1.service.workflow.operation.SubmitLifeCycleOperation;

import java.time.ZonedDateTime;
import java.util.*;

public class WorkflowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    protected static final int PRIORITY = 3;

    protected Set<String> inflightItems = new HashSet<String>();


    public synchronized boolean isInFlight(String path) {
        return inflightItems.contains(path);
    }

    /**
     * $ToDO get Rid of Rename dependencies on label
     *
     * @param site
     * @param paths
     * @param launchDate
     * @param label
     * @param operation
     * @param approvedBy
     */
    public synchronized void addToWorkflow(String site, List<String> paths, ZonedDateTime launchDate,
                                           String label, SubmitLifeCycleOperation operation, String approvedBy, MultiChannelPublishingContext mcpContext) {
        inflightItems.addAll(paths);
        WorkflowBatch workflowBatch = createBatch(paths, launchDate,  label, operation, approvedBy, mcpContext);
        execute(site, workflowBatch);
    }

    protected WorkflowBatch createBatch(Collection<String> paths, ZonedDateTime launchDate, String label,
                                        SubmitLifeCycleOperation preSubmitOperation, String approvedBy, MultiChannelPublishingContext mcpContext) {

        WorkflowBatch batch = new WorkflowBatch(launchDate, label, approvedBy, mcpContext);
        batch.add(paths);
        batch.addOperation(preSubmitOperation);
        return batch;
    }

    protected void execute(String site, WorkflowBatch workflowBatch) {
        String currentUser = securityService.getCurrentUser();
        logger.debug("[WORKFLOW] executing Go Live Processor for " + site);

        try {

            //final String assignee = Cont.getAssignee(site, null);     // Who is the current task owner
            try {
                List<SubmitLifeCycleOperation> preSubmitOperations = workflowBatch.getPreSubmitOperations();
                for (final SubmitLifeCycleOperation preSubmitOperation : preSubmitOperations) {
                    preSubmitOperation.execute();
                }
                logger.debug("[WORKFLOW] submitting " + workflowBatch.getPaths() + " to workflow");
                if (!workflowBatch.getPaths().isEmpty()) {
                    dmPublishService.publish(site, new ArrayList<String>(workflowBatch.getPaths()), workflowBatch.getLaunchDate(), workflowBatch.getMultiChannelPublishingContext());
                }

            } finally {
                this.inflightItems.removeAll(workflowBatch.getPaths());
            }
        } catch (Exception e) {
            this.inflightItems.removeAll(workflowBatch.getPaths());
            logger.debug("Rolling Back states of "+workflowBatch.getPaths());
            rollbackOnError(site,workflowBatch.getPaths());
            logger.error("[WORKFLOW] Error submitting workflow", e);
        }
        //AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
        logger.debug("[WORKFLOW] exiting Go Live Processor for " + site);

    }
    
	private void rollbackOnError(String site, Set<String> allPaths) {

		for (String relativePath : allPaths) {
			try {
				if (contentService.contentExists(site, relativePath)) {
				  objectStateService.setSystemProcessing(site, relativePath, false);
				}
			} catch (Exception ex) {
				logger.error("Unable to rollback site " + site + " path " + relativePath, ex);
			}
		}
	}

    public void removeInFlightItem(String path) {
        this.inflightItems.remove(path);
    }

    protected WorkflowService workflowService;
    protected ContentService contentService;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected DmPublishService dmPublishService;
    protected SecurityService securityService;

    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public WorkflowService getWorkflowService() { return workflowService; }
    public void setWorkflowService(WorkflowService workflowService) { this.workflowService = workflowService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }
}
