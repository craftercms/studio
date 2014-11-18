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

import java.io.Serializable;
import java.util.Map;

import org.craftercms.cstudio.alfresco.util.ContentUtils;

/**
 * This class contains content properties
 * 
 * @author hyanghee 
 * 
 */
public class ContentItemTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 5770441050004336480L;
	
	/**
	 * content properties
	 */
	protected Map<String, Serializable> _properties;

	/**
	 * content node type
	 */
	protected String _contentType;

	/**
	 * content node ref, a default property in search result
	 */
	protected String _nodeRef;

	/**
	 * get properties
	 * 
	 * @return properties
	 */
	public Map<String, Serializable> getProperties() {
		return _properties;
	}

	/**
	 * set properties
	 * 
	 * @param properties
	 */
	public void setProperties(Map<String, Serializable> properties) {
		_properties = properties;
	}

	/**
	 * get content type
	 * 
	 * @return contentType
	 */
	public String getContentType() {
		return _contentType;
	}

	/**
	 * set content type
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		_contentType = contentType;
	}

	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(String nodeRef) {
		this._nodeRef = nodeRef;
	}

	/**
	 * @return the nodeRef
	 */
	public String getNodeRef() {
		return _nodeRef;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ContentItemTO)) {
			return false;
		}
		ContentItemTO item = (ContentItemTO) object;
		// if the properties are the same, it's the same item
		return ContentUtils.areEqual((Serializable) this._properties, (Serializable) item.getProperties());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 17;
		if (this._properties != null) {
			result = 31 * result + this._properties.hashCode();
		}
        return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (_properties == null) ? "" : _properties.toString();
	}
	
	/*
	 * Converts object to JSON Escaped String
	 * Specialized for Web-script use.  
	 */
	public String jsonEscape(Serializable s) {
		String str = s.toString();
		
		return ContentUtils.jsonEscape(str);
	}

}
