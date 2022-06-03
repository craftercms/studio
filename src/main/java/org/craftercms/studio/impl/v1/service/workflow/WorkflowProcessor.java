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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    protected Set<String> inflightItems = new HashSet<>();

    protected DmPublishService dmPublishService;
    protected ItemServiceInternal itemServiceInternal;

    public synchronized boolean isInFlight(String path) {
        return inflightItems.contains(path);
    }

    /**
     * Add items to workflow
     *
     * @param site the site
     * @param paths paths to items to add
     * @param launchDate date and time when items are to go live
     * @param label the label to use for submission
     * @param operation
     * @param approvedBy username of the user that approved the submission
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
        logger.debug("Execute the Go Live Processor in site '{}'", site);

        try {
            try {
                List<SubmitLifeCycleOperation> preSubmitOperations = workflowBatch.getPreSubmitOperations();
                for (final SubmitLifeCycleOperation preSubmitOperation : preSubmitOperations) {
                    if (Objects.nonNull(preSubmitOperation)) {
                        preSubmitOperation.execute();
                    }
                }
                logger.debug("Submit the paths '{}' to workflow in site '{}'", workflowBatch.getPaths(), site);
                if (!workflowBatch.getPaths().isEmpty()) {
                    dmPublishService.publish(site, new ArrayList<>(workflowBatch.getPaths()),
                            workflowBatch.getLaunchDate(), workflowBatch.getMultiChannelPublishingContext());
                }

            } finally {
                this.inflightItems.removeAll(workflowBatch.getPaths());
            }
        } catch (Exception e) {
            this.inflightItems.removeAll(workflowBatch.getPaths());
            logger.error("Failed to add the paths '{}' to workflow in site '{}'. Will rollback the item states.",
                    workflowBatch.getPaths(), site, e);
            rollbackOnError(site,workflowBatch.getPaths());
        }
        logger.debug("Go Live processor finished executing for site '{}'", site);

    }
    
	private void rollbackOnError(String site, Set<String> allPaths) {
        List<String> paths = new ArrayList<>();
        paths.addAll(allPaths);
        itemServiceInternal.setSystemProcessingBulk(site, paths, false);
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
