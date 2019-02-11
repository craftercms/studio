/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.tuple.Pair
import org.craftercms.studio.model.search.SearchParams

import static org.craftercms.studio.api.v1.constant.StudioConstants.SEARCH_ENGINE_ELASTIC_SEARCH

class ContentMonitoring {

	static doMonitoringForAllSites(context, logger) {
		def results = []
		def siteService = context.get("cstudioSiteServiceSimple")
		def sites = siteService.getAllAvailableSites()

		sites.each { site ->
			def result = [:]
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
		logger.debug("monitoring for expired content for site: " + site)

		def results = [:]

		def searchService = context.get("searchServiceInternal")
		def notificationService = context.get("cstudioNotificationService")
		def siteService = context.get("cstudioSiteServiceSimple")

        def siteFeed = siteService.getSite(site);
        if (siteFeed.searchEngine == SEARCH_ENGINE_ELASTIC_SEARCH) {

        }
		def config = siteService.getConfiguration(site, "/site-config.xml", "", false);

		if(config.contentMonitoring != null && config.contentMonitoring.monitor != null) {
			if(config.contentMonitoring.monitor instanceof Map) {
				// there is only one monitor
				config.contentMonitoring.monitor = [ config.contentMonitoring.monitor ]
			}

			config.contentMonitoring.monitor.each { monitor ->
				def authoringBaseUrl = siteService.getAuthoringServerUrl(site)

				logger.info("executing monitor: ${monitor.name}")

				if(monitor.paths !=null && monitor.paths.path!=null) {
					if(monitor.paths.path instanceof Map) {
						// there is only one path
						monitor.paths.path = [ monitor.paths.path ]
					}

					results.monitors = []
					def queryStatement = monitor.query

                    def searchParams = new SearchParams()
                    searchParams.query = queryStatement
                    searchParams.limit = 10000

					def executedQuery = searchService.search(site, Collections.emptyList(), searchParams)
					def itemsFound = executedQuery.total
					def items = executedQuery.items
					logger.info("content monitor (${monitor.name}) found $itemsFound items")

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
									internalName : item["internal-name"]
							]
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
