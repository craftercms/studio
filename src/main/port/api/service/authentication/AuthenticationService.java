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
package org.craftercms.cstudio.api.service.authentication;

import java.lang.reflect.Method;

/**
 * Authentication service provides all services related to authenticating users and jobs
 * @author russdanner
 */
public interface AuthenticationService  {

	/**
	 * perform operation as a specific user
	 * @param userName the name of the user account performing the operation
	 * @param obj the object that contains the method to executre
	 * @param work the method that represents the work to perform
	 * @param args any number of arguments to pass to the method
	 */
	Object runAs(String userName, Object obj, Method work, Object ... args);

    String getCurrentUser();

    String getAdministratorUser();
}
