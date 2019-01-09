/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.to;

import org.craftercms.studio.api.v1.to.ContentItemTO;

import java.io.Serializable;
import java.util.List;

public class DmDeploymentTaskTO implements Serializable {

    private static final long serialVersionUID = 432422778157179373L;
    /** the name of task specified by the creator **/
    protected String internalName;

    /** the number of deployment items in the task **/
    protected int numOfChildren;

    /** navigation child content items **/
    protected List<ContentItemTO> children = null;

    protected String endpoint;


    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getNumOfChildren() {
        return numOfChildren;
    }

    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren = numOfChildren;
    }

    public List<ContentItemTO> getChildren() {
        return children;
    }

    public void setChildren(List<ContentItemTO> children) {
        this.children = children;
    }

    public String getEndpoint() { return endpoint; }

    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
}
