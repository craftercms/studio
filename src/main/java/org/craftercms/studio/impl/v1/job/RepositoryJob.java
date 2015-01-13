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
package org.craftercms.studio.impl.v1.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v1.service.security.SecurityService;

public abstract class RepositoryJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryJob.class);

	/**
	 * authenticate and then delegate execution to protected method: executeAsSignedInUser()
	 */
    public void execute() {

    	SecurityService securityService = this.getSecurityService();
    	String username = this.getUserName();
    	String password = this.getPassword();

    	sercurityService.authenticate(username, password);

    	executeAsSignedInUser();
    }

    /**
     * method is called after user is authenticated 
     */
    protected abstract void executeAsSignedInUser();

	private String username;	
	public String getUserName() {
		return username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	private String password;	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private SecurityService sercurityService;
	public SecurityService getSecurityService() {
		return sercurityService;
	}

	public void setSecurityService(SecurityService sercurityService) {
		this.sercurityService = sercurityService;
	}	

}
