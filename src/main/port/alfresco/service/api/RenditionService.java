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
package org.craftercms.cstudio.alfresco.service.api;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This service will generate a rendition for the content
 * 
 * @author videepkumar1
 * 
 */
public interface RenditionService {

	/**
	 * @return an empty collection of renditions
	 */
	public RenditionContainer createRenditionContainer();

	/**
	 * create a rendition object
	 * 
	 * @return a new rendition instance
	 */
	public Rendition createRendition();

	/**
	 * Execute a script to create or add to a collection of renditions
	 * 
	 * @param actionedUponNodeRef
	 * @param targetLocation
	 * @param scriptLocation
	 * @param scriptName
	 * @return Object - the result
	 */
	public RenditionContainer generateRendition(NodeRef actionedUponNodeRef,
			String targetLocation, String scriptLocation, String scriptName,
			RenditionContainer renditionContainer);

}
