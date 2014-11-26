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

/**
 * DM to WCM Publish Rendition configuration object
 * 
 * @author hyanghee
 *
 */
public class RenditionTO implements Serializable {
	
	/**
	 * 
	 */
	protected static final long serialVersionUID = 7206349500049100624L;

	/**
	 * deploy location in sandbox
	 */
	protected String _location;
	
	/**
	 * script to be executed upon deployment
	 */
	protected String _script;

	/**
	 * @return the location
	 */
	public String getLocation() {
		return _location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this._location = location;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return _script;
	}

	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this._script = script;
	}

}
