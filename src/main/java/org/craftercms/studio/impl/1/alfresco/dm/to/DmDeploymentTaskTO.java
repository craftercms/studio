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
package org.craftercms.cstudio.alfresco.dm.to;

import java.util.List;
import org.craftercms.cstudio.api.to.ContentItemTO;

public class DmDeploymentTaskTO {

    /** the name of task specified by the creator **/
    protected String _internalName;

    /** the number of deployment items in the task **/
    protected int _numOfChildren;

    /** navigation child content items **/
    protected List<ContentItemTO> _children = null;

    protected String endpoint;


    public String getInternalName() {
        return _internalName;
    }

    public void setInternalName(String internalName) {
        _internalName = internalName;
    }

    public int getNumOfChildren() {
        return _numOfChildren;
    }

    public void setNumOfChildren(int numOfChildren) {
        _numOfChildren = numOfChildren;
    }

    public List<ContentItemTO> getChildren() {
        return _children;
    }

    public void setChildren(List<ContentItemTO> children) {
        this._children = children;
    }
}
