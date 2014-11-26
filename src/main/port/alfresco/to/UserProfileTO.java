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
import java.util.List;
import java.util.Map;

public class UserProfileTO implements Serializable {
	/**
	 * 
	 */
	protected static final long serialVersionUID = -9094094494095838444L;
	
	/** contextual user role **/
	protected String _contextual;
	/** user roles **/
	protected Map<String, List<String>> _userRoles = null;
	/** user information **/
	protected Map<String, String> _profile = null;

	/**
	 * @return the userRoles
	 */
	public Map<String, List<String>> getUserRoles() {
		return _userRoles;
	}

	/**
	 * @param userRoles
	 *            the userRoles to set
	 */
	public void setUserRoles(Map<String, List<String>> userRoles) {
		this._userRoles = userRoles;
	}

	/**
	 * @return the profile
	 */
	public Map<String, String> getProfile() {
		return _profile;
	}

	/**
	 * @param profile
	 *            the profile to set
	 */
	public void setProfile(Map<String, String> profile) {
		this._profile = profile;
	}

	/**
	 * @param contextual the contextual to set
	 */
	public void setContextual(String contextual) {
		this._contextual = contextual;
	}

	/**
	 * @return the contextual
	 */
	public String getContextual() {
		return _contextual;
	}

}
