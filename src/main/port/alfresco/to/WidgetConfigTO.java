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
/**
 * 
 */
package org.craftercms.cstudio.alfresco.to;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Sweta Chalasani
 *
 */
public class WidgetConfigTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -6081954659903273323L;
	
	List<Map<String, String>> _properties = null;

	/**
	 * @return the properties
	 */
	public List<Map<String, String>> getProperties() {
		return _properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(List<Map<String, String>> properties) {
		this._properties = properties;
	}
}
