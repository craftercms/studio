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
package org.craftercms.cstudio.alfresco.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.craftercms.cstudio.alfresco.service.api.Rendition;
import org.craftercms.cstudio.alfresco.service.api.RenditionContainer;

/**
 * Manage a related group of renditions
 */
public class RenditionContainerImpl implements RenditionContainer {

	protected List<Rendition> _renditions;

	/**
	 * 
	 */
	public RenditionContainerImpl() {
		_renditions = new ArrayList<Rendition>();
	}

	/**
	 * get all renditions on hand
	 * 
	 * @return a list of renditions
	 */
	public List<Rendition> getRenditions() {
		return _renditions;
	}

	/**
	 * add a rendition to the collection
	 * 
	 * @param rendition
	 *            rendition to add
	 */
	public void addRendition(Rendition rendition) {
		_renditions.add(rendition);
	}
	
	/**
	 * remove a list of renditions from the collection
	 * 
	 * @param renditions
	 */	
	public void removeRendition(List<Rendition> renditions) {
		_renditions.removeAll(renditions);
	}
}
