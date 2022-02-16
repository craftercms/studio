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


import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.impl.v1.service.workflow.operation.SubmitLifeCycleOperation;

import java.time.ZonedDateTime;
import java.util.*;

public class WorkflowBatch {

    protected Set<String> paths = new HashSet<String>();

    protected List<SubmitLifeCycleOperation> preSubmitOperations = new ArrayList<SubmitLifeCycleOperation>();

    protected ZonedDateTime launchDate;

    protected String label;

    protected String approvedBy;

    protected MultiChannelPublishingContext mcpContext;

    public WorkflowBatch(ZonedDateTime launchDate, String label, String approvedBy, MultiChannelPublishingContext mcpContext) {
        this.launchDate = launchDate;
        this.label=label;
        this.approvedBy = approvedBy;
        this.mcpContext = mcpContext;
    }

    public List<SubmitLifeCycleOperation> getPreSubmitOperations() {
        return preSubmitOperations;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void add(Collection<String> path) {
        paths.addAll(path);
    }

    public ZonedDateTime getLaunchDate() {
        return launchDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void addOperation(SubmitLifeCycleOperation preSubmitOperation) {
        preSubmitOperations.add(preSubmitOperation);
    }

    public MultiChannelPublishingContext getMultiChannelPublishingContext() {
        return mcpContext;
    }
}
