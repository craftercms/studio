/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v1.service.security.SecurityService;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_PASSWORD;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_USERNAME;

public abstract class RepositoryJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryJob.class);

	/**
	 * authenticate and then delegate execution to protected method: executeAsSignedInUser()
	 */
    public void execute() {

    	String ticket = sercurityService.authenticate(getUserName(), getPassword());
        if (StringUtils.isNotEmpty(ticket)) {
            CronJobContext cronJobContext = new CronJobContext(ticket, getUserName());
            CronJobContext.setCurrent(cronJobContext);
            executeAsSignedInUser();
            CronJobContext.clear();
            sercurityService.logout();
        } else {
            logger.error("Not able to authenticate user for cron job.");
        }
    }

    /**
     * method is called after user is authenticated
     */
    protected abstract void executeAsSignedInUser();


	public String getUserName() {
		return studioConfiguration.getProperty(JOB_USERNAME);
	}

	public String getPassword() {
		return studioConfiguration.getProperty(JOB_PASSWORD);
	}

	private SecurityService sercurityService;
	public SecurityService getSecurityService() { return sercurityService; }
	public void setSecurityService(SecurityService sercurityService) { this.sercurityService = sercurityService; }

	protected StudioConfiguration studioConfiguration;
    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}
