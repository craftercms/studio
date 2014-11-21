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

import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;

import org.craftercms.cstudio.alfresco.service.api.Rendition;

/**
 * represents a rendition
 */
public class RenditionImpl implements Rendition {

	protected String _name;
	protected String _folder;
	protected String _path;
	protected String _targetLocation;
	protected String _mimetype;
	protected InputStream _content;
	protected NodeRef _nodeRef;
	protected String _contentType;
	
	protected boolean _isParent;
	protected boolean _addToContainer;
	

	/**
	 * default constructor
	 */
	public RenditionImpl() {

	}

	public InputStream getRenditionContent() {
		return _content;
	}

	public String getRenditionFileName() {
		return _name;
	}

	public String getRenditionFolder() {
		return _folder;
	}
	
	public String getRenditionMimeType() {
		return _mimetype;
	}

	public String getRenditionPath() {
		return _path;
	}

	public void setMimeType(String mimetype) {
		_mimetype = mimetype;
	}

	public void setRenditionContent(InputStream stream) {
		_content = stream;
	}

	public void setRenditionFileName(String name) {
		_name = name;
	}
	
	public void setRenditionFolder(String folder) {
		_folder = folder;
	}

	public void setRenditionPath(String path) {
		_path = path;
	}

	public boolean isParent() {
		return _isParent;
	}

	public void setParent(boolean parent) {
		_isParent = parent;
	}

	public boolean isAddToContainer() {
		return _addToContainer;
	}

	public void setAddToContainer(boolean addToContainer) {
		_addToContainer = addToContainer;
	}

	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this._nodeRef = nodeRef;
	}

	/**
	 * @return the nodeRef
	 */
	public NodeRef getNodeRef() {
		return _nodeRef;
	}

	/**
	 * @return the targetLocation
	 */
	public String getTargetLocation() {
		return _targetLocation;
	}

	/**
	 * @param targetLocation the targetLocation to set
	 */
	public void setTargetLocation(String targetLocation) {
		this._targetLocation = targetLocation;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this._contentType = contentType;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return _contentType;
	}

}
