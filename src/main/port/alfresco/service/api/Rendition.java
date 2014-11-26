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

import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The following interface represents a rendition
 * 
 */
public interface Rendition {


	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(NodeRef nodeRef);

	/**
	 * @return the nodeRef
	 */
	public NodeRef getNodeRef();

	/**
	 * 
	 * @return return the target root location where the rendition should be stored
	 */
	public String getTargetLocation();
	
	/**
	 * set the target location
	 * 
	 * @param targetLocation
	 */
	public void setTargetLocation(String targetLocation);
	
	/**
	 * @return the path the rendition should be stored at (without the file
	 *         name)
	 */
	public String getRenditionPath();

	/**
	 * set the path
	 * 
	 * @param path
	 *            path of the rendition
	 */
	public void setRenditionPath(String path);

	/**
	 * @return the file name of the rendition
	 */
	public String getRenditionFileName();

	/**
	 * set the name of the file storing the rendition
	 * 
	 * @param name
	 *            file name
	 */
	public void setRenditionFileName(String name);

	/**
	 * @return the folder of the rendition
	 */
	public String getRenditionFolder();
	
	/**
	 * set the name of the folder where rendition will be stored
	 * 
	 * @param folder
	 * 				folder name
	 */
	public void setRenditionFolder(String folder);
	
	/**
	 * @return the file mimetype of the rendition
	 */
	public String getRenditionMimeType();

	/**
	 * set the mimetype value
	 * 
	 * @param mimetype
	 *            mime type
	 */
	public void setMimeType(String mimetype);

	/**
	 * @return content of the rendition
	 */
	public InputStream getRenditionContent();
	
	/**
	 * set the content value for the rendition
	 * @param stream
	 */
	public void setRenditionContent(InputStream stream);
	
	/**
	 * @return whether rendition is parent or not
	 */
	public boolean isParent();

	/**
	 * set if this rendition is parent or not
	 * @param parent
	 */
	public void setParent(boolean parent);

	/**
	 * @return whether rendition has to be added to container or not
	 */
	public boolean isAddToContainer();

	/**
	 * set if rendition has to be added to container or not
	 * @param addToContainer
	 */
	public void setAddToContainer(boolean addToContainer);
	
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType);

	/**
	 * @return the contentType
	 */
	public String getContentType();

}
