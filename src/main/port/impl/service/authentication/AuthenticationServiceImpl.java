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
package org.craftercms.cstudio.impl.service.authentication;

import org.craftercms.cstudio.api.repository.*;
import org.craftercms.cstudio.api.service.authentication.*;
import java.lang.reflect.Method;

/**
 * Authentication service provides all services related to authenticating users and jobs
 * uses repository as authentication
 * @author russdanner
 */
public class AuthenticationServiceImpl implements AuthenticationService  {

	/**
	 * perform operation as a specific user
	 * @param userName the name of the user account performing the operation
	 * @param obj the object that contains the method to executre
	 * @param work the method that represents the work to perform
	 * @param args any number of arguments to pass to the method
	 */
	public Object runAs(String userName, Object obj, Method work, Object ... args) {
		return _contentRepository.runAs(userName, obj, work, args);
	}

    @Override
    public String getCurrentUser() {
        return _contentRepository.getCurrentUser();
    }

    @Override
    public String getAdministratorUser() {
        return _contentRepository.getAdministratorUser();
    }

    /** getter for content repository */
	public ContentRepository getContentRepository() { return _contentRepository; }
	/** setter for content repository */
	public void setContentRepository(ContentRepository repo) { _contentRepository = repo; }
	
	protected ContentRepository _contentRepository;
}
