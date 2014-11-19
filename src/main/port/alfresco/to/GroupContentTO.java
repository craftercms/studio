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
package org.craftercms.cstudio.alfresco.to;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * represents group type
 * 
 * @author hyanghee
 *
 */
public class GroupContentTO {

	protected QName _type;
	protected NodeRef _nodeRef;
	protected Map<String, List<String>> _properties;

	/**
	 * @return the properties
	 */
	public Map<String, List<String>> getProperties() {
		return _properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, List<String>> properties) {
		this._properties = properties;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(QName type) {
		this._type = type;
	}

	/**
	 * @return the type
	 */
	public QName getType() {
		return _type;
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

}
