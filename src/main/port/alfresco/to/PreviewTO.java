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
 * Preview Configuration 
 * 
 * @author hyanghee
 *
 */
public class PreviewTO {
	
	/** preview server name **/
	protected String _serverName = null;
	/** preview server port **/
	protected String _serverPort = null;
	/** preview server URL context **/
	protected String _urlContext = null;
	
	/**
	 * return the preview server URL
	 * @return preview server URL
	 */
	public String getPreviewServerUrl() {
		return _serverName + ":" + _serverPort + _urlContext;
	}
	
	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return _serverName;
	}
	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this._serverName = serverName;
	}
	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return _serverPort;
	}
	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		this._serverPort = serverPort;
	}
	/**
	 * @return the urlContext
	 */
	public String getUrlContext() {
		return _urlContext;
	}
	/**
	 * @param urlContext the urlContext to set
	 */
	public void setUrlContext(String urlContext) {
		this._urlContext = urlContext;
	}
}
