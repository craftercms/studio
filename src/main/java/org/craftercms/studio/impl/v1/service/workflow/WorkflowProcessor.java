/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.impl.v1.service.workflow.operation.SubmitLifeCycleOperation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WorkflowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    protected Set<String> inflightItems = new HashSet<String>();

    protected DmPublishService dmPublishService;
    protected ItemServiceInternal itemServiceInternal;

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
                                           String label, SubmitLifeCycleOperation operation, String approvedBy,
                                           MultiChannelPublishingContext mcpContext) {
        inflightItems.addAll(paths);
        WorkflowBatch workflowBatch = createBatch(paths, launchDate,  label, operation, approvedBy, mcpContext);
        execute(site, workflowBatch);
    }

    protected WorkflowBatch createBatch(Collection<String> paths, ZonedDateTime launchDate, String label,
                                        SubmitLifeCycleOperation preSubmitOperation, String approvedBy,
                                        MultiChannelPublishingContext mcpContext) {

        WorkflowBatch batch = new WorkflowBatch(launchDate, label, approvedBy, mcpContext);
        batch.add(paths);
        batch.addOperation(preSubmitOperation);
        return batch;
    }

    protected void execute(String site, WorkflowBatch workflowBatch) {
        logger.debug("[WORKFLOW] executing Go Live Processor for " + site);

        try {
            try {
                List<SubmitLifeCycleOperation> preSubmitOperations = workflowBatch.getPreSubmitOperations();
                for (final SubmitLifeCycleOperation preSubmitOperation : preSubmitOperations) {
                    if (Objects.nonNull(preSubmitOperation)) {
                        preSubmitOperation.execute();
                    }
                }
                logger.debug("[WORKFLOW] submitting " + workflowBatch.getPaths() + " to workflow");
                if (!workflowBatch.getPaths().isEmpty()) {
                    dmPublishService.publish(site, new ArrayList<String>(workflowBatch.getPaths()),
                            workflowBatch.getLaunchDate(), workflowBatch.getMultiChannelPublishingContext());
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
        logger.debug("[WORKFLOW] exiting Go Live Processor for " + site);

    }
    
	private void rollbackOnError(String site, Set<String> allPaths) {
        List<String> paths = new ArrayList<String>();
        paths.addAll(allPaths);
        itemServiceInternal.setSystemProcessingBulk(site, paths, false);
	}

    public DmPublishService getDmPublishService() {
        return dmPublishService;
    }

    public void setDmPublishService(DmPublishService dmPublishService) {
        this.dmPublishService = dmPublishService;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
