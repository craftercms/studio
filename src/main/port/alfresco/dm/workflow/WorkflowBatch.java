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

import org.craftercms.cstudio.alfresco.dm.workflow.operation.SubmitLifeCycleOperation;

import java.util.*;

public class WorkflowBatch {

    protected String _workflowName;

    protected Set<String> _paths = new HashSet<String>();

    protected List<SubmitLifeCycleOperation> _preSubmitOperations = new ArrayList<SubmitLifeCycleOperation>();

    protected Date _launchDate;

    protected String _label;

    protected String _approvedBy;

    protected MultiChannelPublishingContext _mcpContext;

    public WorkflowBatch(Date launchDate, String workflowName, String label, String approvedBy, MultiChannelPublishingContext mcpContext) {
        this._launchDate = launchDate;
        this._workflowName = workflowName;
        this._label=label;
        this._approvedBy = approvedBy;
        this._mcpContext = mcpContext;
    }

    public String getWorkflowName() {
        return _workflowName;
    }

    public List<SubmitLifeCycleOperation> getPreSubmitOperations() {
        return _preSubmitOperations;
    }

    public Set<String> getPaths() {
        return _paths;
    }

    public void add(Collection<String> path) {
        _paths.addAll(path);
    }

    public Date getLaunchDate() {
        return _launchDate;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        this._label = label;
    }

    public String getApprovedBy() {
        return _approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this._approvedBy = approvedBy;
    }

    public void addOperation(SubmitLifeCycleOperation preSubmitOperation) {
        _preSubmitOperations.add(preSubmitOperation);
    }

    public MultiChannelPublishingContext getMultiChannelPublishingContext() {
        return _mcpContext;
    }
}
