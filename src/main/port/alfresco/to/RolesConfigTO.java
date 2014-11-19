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

import java.util.Date;
import java.util.Map;

/**
 * site roles configuration 
 * 
 * @author hyanghee
 *
 */
public class RolesConfigTO implements TimeStamped {

	protected String _topGroup;
	protected Map<String, RoleConfigTO> _roles;
	protected Date _lastUpdated;
	
	/**
	 * @return the topGroup
	 */
	public String getTopGroup() {
		return _topGroup;
	}

	/**
	 * @param topGroup
	 *            the topGroup to set
	 */
	public void setTopGroup(String topGroup) {
		this._topGroup = topGroup;
	}

	/**
	 * @return the roles
	 */
	public Map<String, RoleConfigTO> getRoles() {
		return _roles;
	}

	/**
	 * @param roles
	 *            the roles to set
	 */
	public void setRoles(Map<String, RoleConfigTO> roles) {
		this._roles = roles;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.to.TimeStamped#getLastUpdated()
	 */
	public Date getLastUpdated() {
		return _lastUpdated;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.to.TimeStamped#setLastUpdated(java.util.Date)
	 */
	public void setLastUpdated(Date lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

}
