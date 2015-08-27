/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.job.CronJobContext;

import scripts.api.SiteServices
import scripts.api.impl.content.ContentMonitoring

def context = applicationContext

def user = context.get("cstudioDeployContentToEnvironmentJobs").userName 
def pw = context.get("cstudioDeployContentToEnvironmentJobs").password

def ticket = sercurityService.authenticate(user, pw)

if (StringUtils.isNotEmpty(ticket)) {
    CronJobContext cronJobContext = new CronJobContext(ticket)
    CronJobContext.setCurrent(cronJobContext)
	
	ContentMonitoring.doMonitoringForAllSites(context)
    
    CronJobContext.clear()
} 
else {
	logger.error("Not able to authenticate user for cron job.")
}
