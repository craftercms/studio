/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.util.api;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;

/**
 * load content item properties upon generating content items
 */
public interface ContentPropertyLoader {

    /**
     * get the name of this loader
     *
     * @return
     */
    public String getName();

    /**
     * is the content item eligible to load properties?
     *
     * @param item
     * @return
     */
    //public boolean isEligible(AVMNodeDescriptor node, DmContentItemTO item);
    public boolean isEligible(NodeRef node, DmContentItemTO item);

    /**
     * load properties into the item given
     *
     * @param item
     */
    //public void loadProperties(AVMNodeDescriptor node, DmContentItemTO item);
    public void loadProperties(NodeRef node, DmContentItemTO item);
}
