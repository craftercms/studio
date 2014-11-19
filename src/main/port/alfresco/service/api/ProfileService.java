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

import org.alfresco.service.cmr.repository.NodeRef;

import org.craftercms.cstudio.alfresco.to.UserProfileTO;

/**
 * Provides user-related information and configuration.
 * 
 * @author hyanghee
 *
 */
public interface ProfileService {

	/**
	 * assign role to user given user id is and role
	 * 
	 * @param role
	 * @param user
	 * @return true if the user role is assigned
	 */
	public boolean assignUserRole(String user, String role, String site);

	/**
	 * remove role assigned to user given user id and role
	 * 
	 * @param role
	 * @param user
	 * @return true if the user role is removed
	 */
	public boolean removeUserRole(String user, String role, String site);

	/**
	 * get user profile given user id
	 * 
	 * @param user
	 * @param site
	 * @param populateRoles
	 * 			populate user roles?
	 * @return user profile
	 */
	public UserProfileTO getUserProfile(String user, String site, boolean populateRoles);

	/**
	 * check user role
	 * 
	 * @param user
	 * @param site
	 * @param role
	 * @return true if the user has the role
	 */
	public boolean checkUserRole(String user, String site, String role);
	
	/**
	 * get a user node reference
	 * 
	 * @param user
	 * @return user ref
	 */
	public NodeRef getUserRef(String user);

}
