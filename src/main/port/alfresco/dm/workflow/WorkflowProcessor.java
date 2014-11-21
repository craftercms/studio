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
package org.craftercms.cstudio.alfresco.dm.workflow;



import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.workflow.operation.SubmitLifeCycleOperation;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService.State;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WorkflowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    protected static final int PRIORITY = 3;

    protected Set<String> _inflightItems = new HashSet<String>();

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public synchronized boolean isInFlight(String path) {
        return _inflightItems.contains(path);
    }

    /**
     * $ToDO get Rid of Rename dependencies on label
     *
     * @param site
     * @param paths
     * @param launchDate
     * @param workflowName
     * @param label
     * @param operation
     * @param approvedBy
     */
    public synchronized void addToWorkflow(String site, List<String> paths, Date launchDate, String workflowName,
                                           String label, SubmitLifeCycleOperation operation, String approvedBy, MultiChannelPublishingContext mcpContext) {
        _inflightItems.addAll(paths);
        WorkflowBatch workflowBatch = createBatch(paths, launchDate, workflowName, label, operation, approvedBy, mcpContext);
        execute(site, workflowBatch);
    }

    protected WorkflowBatch createBatch(Collection<String> paths, Date launchDate, String workflowName, String label,
                                        SubmitLifeCycleOperation preSubmitOperation, String approvedBy, MultiChannelPublishingContext mcpContext) {

        WorkflowBatch batch = new WorkflowBatch(launchDate, workflowName, label, approvedBy, mcpContext);
        batch.add(paths);
        batch.addOperation(preSubmitOperation);
        return batch;
    }

    protected void execute(String site, WorkflowBatch workflowBatch) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (logger.isDebugEnabled()) {
            logger.debug("[WORKFLOW] executing Go Live Processor for " + site);
        }
        try {
            DmTransactionService dmTransactionService = getServicesManager().getService(DmTransactionService.class);
            DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
            AuthenticationUtil.setFullyAuthenticatedUser(workflowBatch.getApprovedBy());
            final String assignee = DmUtils.getAssignee(site, null);     // Who is the current task owner
            String workflowName = workflowBatch.getWorkflowName();
            try {
                List<SubmitLifeCycleOperation> preSubmitOperations = workflowBatch.getPreSubmitOperations();
                for (final SubmitLifeCycleOperation preSubmitOperation : preSubmitOperations) {
                    preSubmitOperation.execute();
                }
                if (logger.isDebugEnabled())
                    logger.debug("[WORKFLOW] submitting " + workflowBatch.getPaths() + " to workflow");
                if (!workflowBatch.getPaths().isEmpty()) {
                    dmWorkflowService.submitToWorkflow(site, null, workflowName, assignee,
                            PRIORITY, workflowBatch.getLaunchDate(), workflowBatch.getLabel(), workflowBatch.getLabel(),
                            true, new ArrayList<String>(workflowBatch.getPaths()), workflowBatch.getMultiChannelPublishingContext());
                }

            } finally {
                this._inflightItems.removeAll(workflowBatch.getPaths());
            }
        } catch (Exception e) {
            this._inflightItems.removeAll(workflowBatch.getPaths());
            logger.debug("Rolling Back states of "+workflowBatch.getPaths());
            rollbackOnError(site,workflowBatch.getPaths());
            logger.error("[WORKFLOW] Error submitting workflow", e);
        }
        AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
        if (logger.isDebugEnabled()) {
            logger.debug("[WORKFLOW] exiting Go Live Processor for " + site);
        }
    }
    
	private void rollbackOnError(String site, Set<String> allPaths) {
		PersistenceManagerService persistenceManagerService = getServicesManager()
				.getService(PersistenceManagerService.class);
		for (String fullPath : allPaths) {
			try {
 
				if (persistenceManagerService.exists(fullPath)) {
				  persistenceManagerService.setSystemProcessing(fullPath, false);
				}
			} catch (Exception ex) {
				logger.error("Unable to rollback " + fullPath, ex);
			}
		}
	}

    public void removeInFilghtItem(String path) {
        this._inflightItems.remove(path);
    }
}
