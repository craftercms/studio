/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package scripts.api.impl.content
import groovy.util.logging.Log

import javax.mail.*
import javax.mail.internet.*
import java.util.regex.Pattern
import java.util.regex.Matcher

@Log
class ContentMonitoring {

	static doMonitoringForAllSites(context, logger) {
		def results = []
		def siteService = context.get("cstudioSiteServiceSimple") 
		def sites = siteService.getAllAvailableSites()

		for(int i=0; i<sites.size(); i++) {
			def result = [:]
			def site = sites[i]
			result.siteId = site
			result.contentMonitoring = doContentMonitoringForSite(context, site, logger)
			results.add(result)
		}

		return results;
	}

	/**
	 * given a site ID, perform the monitoring specified in the site-config.xml
	 * @context application context
	 * @site the id of the site to do monitoring for
	 * @return a list of notifications that were made
	 */
	static doContentMonitoringForSite(context, site, logger) {
		logger.info("monitoring for expired content for site: " + site)

		def results = [:]
		def searchService = context.get("crafter.searchService")
		def notificationService = context.get("cstudioNotificationService") 
		def siteService = context.get("cstudioSiteServiceSimple") 
		
		def config = siteService.getConfiguration(site, "/site-config.xml", false);

		if(config.contentMonitoring != null && config.contentMonitoring.monitor !=null) {

			if([Collection, Object[]].any { it.isAssignableFrom(config.contentMonitoring.monitor.getClass()) } == false) {
				// there is only one monitor
				def onlyMonitor = config.contentMonitoring.monitor
				config.contentMonitoring.monitor = []
				config.contentMonitoring.monitor[0] = onlyMonitor
			}

			for(int m=0; m<config.contentMonitoring.monitor.size(); m++) {
				def monitor = config.contentMonitoring.monitor[m]
                                def authoringBaseUrl = config.contentMonitoring.authoringBaseUrl
                                def siteId = config.name

				logger.info("executing monitor: " + monitor.name)

				if(monitor.paths !=null && monitor.paths.path!=null) {
					if([Collection, Object[]].any { it.isAssignableFrom(monitor.paths.path.getClass()) } == false) {
						// there is only one path
						def onlyPath = monitor.paths.path
						monitor.paths.path = []
						monitor.paths.path[0] = onlyPath
					}			

					results.monitors = []
					def queryStatement = monitor.query
					 
					def query = searchService.createQuery()
					query = query.setQuery(queryStatement)
                                        query = query.setRows(10000)
					 
					def executedQuery = searchService.search(query)   
					def itemsFound = executedQuery.response.numFound    
					def items = executedQuery.response.documents
					logger.info("Content monitor (" + monitor.name + ") found " + itemsFound + " | " + items.size() )

					for(int p=0; p<monitor.paths.path.size(); p++) {
						// there are paths, query for items and then match against paths patterns
						def path = monitor.paths.path[p]
						def monitorPathResult = [:]
						monitorPathResult.name = path.name
						monitorPathResult.notify = path.notifyEmail
                                                monitorPathResult.itemsStr = ""

						def monitorPathResultItems = []

						for(int i=0; i<itemsFound; i++) {
							// iterate over the items and prepare notifications
							def item = items.get(i)
                                                    
	                             if(item != null) {
	                               def notifyItem = [:]
	                               notifyItem.id = item.localId
	                               notifyItem.internalName = item["internal-name"]

	                               if(item.localId =~ Pattern.compile(path.pattern)) {
	                                    monitorPathResultItems.add(notifyItem)
	                                  
	                                    if(notifyItem.id.contains("/site/website")) {

	                                       def uri = notifyItem.id.replace("/site/website", "").replace("/index.xml","").replace(".xml", "")

	                                       monitorPathResult.itemsStr += 
	                                          "<li class='notifyItem'>" +
	                                            "<a href='" + authoringBaseUrl + "/preview/#/?page=" + uri + "&site=" + siteId  + "'>" +
	                                              notifyItem.internalName + 
	                                            "</a>" +
	                                          "</li>"
	                                    }
	                                    else {
	                                       monitorPathResult.itemsStr += 
	                                          "<li class='notifyItem'>" +
	                                            "Component: "+
	                                              notifyItem.internalName + 
	                                            "" +
	                                          "</li>"
	                                    }

	                               }
	                           }
	                           else {
	                              logger.info("Content monitor: " + monitor.name + " query ("+queryStatement+")  item " + i + " is null")
	                           }						
	                    }

						if(monitorPathResultItems.size() > 0) {
							results.monitors.add(monitorPathResult)

                            // create a list of items to send in the email
							logger.info("Content monitor: " + monitor.name + " Sending notification ("+path.notificationMessageId+")")
							notificationService.sendGenericNotification(
								site, 
								"/site/website/index.xml",  //PATHS ARE IN PARAMS BUT THIS SERVICE NEEDS SOMETHING 
								path.notifyEmail, 
								path.notifyEmail, 
								path.notificationMessageId, 
								monitorPathResult)
						}

					} //end looping over paths


				} // if no paths to monitor, don't do anything
			} // end looping through site monitors
		}
		else {
			logger.info("no expired items to report")
		}

		return results 
	}
}
