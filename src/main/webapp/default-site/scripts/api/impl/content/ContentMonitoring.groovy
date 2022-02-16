/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import org.apache.commons.lang3.tuple.Pair
import org.craftercms.studio.model.search.SearchParams

class ContentMonitoring {

	static SERVICES_CONFIG_BEAN = "cstudioServicesConfig"
	static SITE_SERVICE_BEAN = "cstudioSiteServiceSimple"
	static NOTIFICATION_SERVICE_BEAN = "cstudioNotificationService"
	static SEARCH_SERVICE_BEAN = "searchServiceInternal"
	static CONFIGURATION_SERVICE_BEAN = "configurationService"

	static doMonitoringForAllSites(context, logger) {
		def results = []
		def siteService = context.get(SITE_SERVICE_BEAN)
		def sites = siteService.getAllAvailableSites()

		sites.each { site ->
			def result = [:]
			result.siteId = site
			result.contentMonitoring = doContentMonitoringForSite(context, site, logger)
			results.add(result)
		}

		return results
	}

	/**
	 * given a site ID, perform the monitoring specified in the site-config.xml
	 * @context application context
	 * @site the id of the site to do monitoring for
	 * @return a list of notifications that were made
	 */
	static doContentMonitoringForSite(context, site, logger) {
		logger.debug("monitoring for expired content for site: " + site)

		def results = [:]

		def searchService = context.get(SEARCH_SERVICE_BEAN)
		def notificationService = context.get(NOTIFICATION_SERVICE_BEAN)
		def servicesConfig = context.get(SERVICES_CONFIG_BEAN)
		def configurationService = context.get(CONFIGURATION_SERVICE_BEAN)

		def config = configurationService.legacyGetConfiguration(site, "site-config.xml")

		if(config.contentMonitoring != null && config.contentMonitoring.monitor != null) {
			if(config.contentMonitoring.monitor instanceof Map) {
				// there is only one monitor
				config.contentMonitoring.monitor = [ config.contentMonitoring.monitor ]
			}

			results.monitors = []
			config.contentMonitoring.monitor.each { monitor ->
				def authoringBaseUrl = servicesConfig.getAuthoringUrl(site)

				logger.debug("executing monitor: ${monitor.name}")

				if(monitor.paths !=null && monitor.paths.path!=null) {
					if(monitor.paths.path instanceof Map) {
						// there is only one path
						monitor.paths.path = [ monitor.paths.path ]
					}

					def queryStatement = monitor.query

                    def searchParams = new SearchParams()
                    searchParams.query = queryStatement
                    searchParams.limit = 10000

					def executedQuery = searchService.search(site, Collections.emptyList(), searchParams)
					def itemsFound = executedQuery.total
					def items = executedQuery.items
					logger.debug("content monitor (${monitor.name}) found $itemsFound items")

					monitor.paths.path.each { path ->
						// there are paths, query for items and then match against paths patterns
						def monitorPathResult = [:]
						monitorPathResult.name = path.name
						monitorPathResult.emails = path.emails
						monitorPathResult.items = []
						// iterate over the items and prepare notifications
						items.findAll { it && it.path =~ path.pattern }.each { item ->
							def notifyItem = [
									id : item.path,
									internalName : item.name
							]
							//TODO: Move this logic to search service, maybe add a 'renderUrl' to all items?
							if(notifyItem.id.contains("/site/website")) {
								def uri = notifyItem.id.replace("/site/website", "").replace("/index.xml","").replace(".xml", "")
								notifyItem.url = "$authoringBaseUrl/preview/#/?page=$uri&site=$site".toString()
							}
							monitorPathResult.items << notifyItem
						}

						if(monitorPathResult.items) {
							results.monitors.add(monitorPathResult)
							logger.info("content monitor: ${monitor.name} Sending notification (${path.emailTemplate})")
							notificationService.notify(
									site,
									path.emails.split(",") as List,
									path.emailTemplate,
									new Locale(path.locale ?: "en"),
									Pair.of("monitorName", monitor.name),
									Pair.of("items", monitorPathResult.items)
							)
						}
					} //end looping over paths
				} // if no paths to monitor, don't do anything
			} // end looping through site monitors
		}
		else {
			logger.debug("no items to report")
		}
		return results
	}
}
