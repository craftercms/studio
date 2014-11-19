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
package org.craftercms.cstudio.alfresco.dm.util;

import org.craftercms.cstudio.alfresco.dm.workflow.WorkflowBatch;

public class WorkflowLock {

    protected boolean _isEnded = false;

    protected WorkflowBatch _batch;

    public boolean isEnded() {
        return _isEnded;
    }

    public void setEnded(boolean ended) {
        _isEnded = ended;
    }

    public WorkflowBatch getBatch() {
        return _batch;
    }

    public void setCurrentBatch(WorkflowBatch batch) {
        this._batch = batch;
    }
}
