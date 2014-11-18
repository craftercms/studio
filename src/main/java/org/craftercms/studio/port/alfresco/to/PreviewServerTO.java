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

/**
 * Preview Server Configuration
 */
public class PreviewServerTO {

	String _serverName;
	String _serverPort;
	String _serverBaseContext;

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return _serverName;
	}

	/**
	 * @param serverName
	 *            the serverName to set
	 */
	public void setServerName(String serverName) {
		_serverName = serverName;
	}

	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return _serverPort;
	}

	/**
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		_serverPort = serverPort;
	}

	/**
	 * @return the serverBaseContext
	 */
	public String getServerBaseContext() {
		return _serverBaseContext;
	}

	/**
	 * @param serverBaseContext
	 *            the serverBaseContext to set
	 */
	public void setServerBaseContext(String serverBaseContext) {
		_serverBaseContext = serverBaseContext;
	}
}
