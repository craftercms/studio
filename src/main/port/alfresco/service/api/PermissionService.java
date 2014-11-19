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

import java.util.List;
import java.util.Set;

/**
 * Provides services for getting permissions from the config file based on user and groups
 * 
 * @author swetachalasani
 *
 */
public interface PermissionService {

	/**
	 * Get user permissions to read/write/delete
	 * 
	 * @param site
	 * @param path
	 * @param user
	 * @param groups
	 * @return list of permissions
	 */
	
	public Set<String> getUserPermissions(String site, String path, String user, List<String> groups);
	
	/**
	 * get user roles 
	 * 
	 * @param site
	 * @param user
	 * @return list of roles
	 */
	public Set<String> getUserRoles(String site, String user);

	/**
	 * get all roles from the site 
	 * 
	 * @param site
	 * @return
	 */
	public Set<String> getRoles(String site);
	
}
