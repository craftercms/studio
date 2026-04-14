/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.content;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentMonitorConfigTO;
import org.craftercms.studio.api.v2.content.ContentMonitor;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.search.SearchService;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResultItem;
import org.craftercms.studio.model.site.AllSitesMonitors;
import org.craftercms.studio.model.site.SiteMonitor;
import org.slf4j.Logger;

import java.beans.ConstructorProperties;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.craftercms.studio.api.v2.dal.Site.State.READY;
import static org.craftercms.studio.impl.v1.util.ContentUtils.getAuthoringUrl;
import static org.opensearch.core.common.util.CollectionUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of {@link ContentMonitor}
 */
public class ContentMonitorImpl implements ContentMonitor {
	private final static Logger logger = getLogger(ContentMonitorImpl.class);

	private final ServicesConfig servicesConfig;
	private final SearchService searchService;
	private final SiteDAO siteDao;
	private final NotificationService notificationService;

	@ConstructorProperties({"searchService", "servicesConfig", "siteDao", "notificationService"})
	public ContentMonitorImpl(SearchService searchService, ServicesConfig servicesConfig,
							  SiteDAO siteDao, NotificationService notificationService) {
		this.searchService = searchService;
		this.servicesConfig = servicesConfig;
		this.siteDao = siteDao;
		this.notificationService = notificationService;
	}

	@Override
	public Collection<SiteMonitor> monitorSite(String siteId) throws ServiceLayerException {
		Collection<SiteMonitor> results = new ArrayList<>();
		ContentMonitorConfigTO monitorConfig = servicesConfig.getMonitorConfig(siteId);
		for (ContentMonitorConfigTO.ContentMonitorTO monitor : monitorConfig.getMonitors()) {
			if (isEmpty(monitor.paths())) {
				logger.warn("No paths configured for monitor '{}', skipping", monitor.name());
				continue;
			}
			if (StringUtils.isEmpty(monitor.query())) {
				logger.warn("No query configured for monitor '{}', skipping", monitor.name());
				continue;
			}
			SearchParams searchParams = new SearchParams();
			searchParams.setQuery(monitor.query());
			Collection<SiteMonitor.SiteMonitorPath> monitorPaths = new ArrayList<>(monitor.paths().size());
			List<SearchResultItem> items = searchService.search(siteId, searchParams).getItems();
			for (ContentMonitorConfigTO.MonitorPathTO path : monitor.paths()) {
				List<SearchResultItem> matchingItems = items.stream()
						.filter(item -> StringUtils.isEmpty(path.pattern()) || item.getPath().matches(path.pattern()))
						.toList();
				if (!matchingItems.isEmpty()) {
					List<SiteMonitor.SiteMonitorItem> list = new ArrayList<>();
					for (SearchResultItem item : matchingItems) {
						SiteMonitor.SiteMonitorItem siteMonitorItem = new SiteMonitor.SiteMonitorItem(item.getPath(), path.name(),
								getAuthoringUrl(servicesConfig, siteId, item.getPreviewUrl()));
						list.add(siteMonitorItem);
					}
					SiteMonitor.SiteMonitorPath monitorPath = new SiteMonitor.SiteMonitorPath(path.name(), path.emails(), path.emailTemplate(), list);
					monitorPaths.add(monitorPath);
				}
			}
			if (!monitorPaths.isEmpty()) {
				SiteMonitor monitorResult = new SiteMonitor();
				monitorResult.setName(monitor.name());
				monitorResult.setPaths(monitorPaths);
				results.add(monitorResult);
			}
		}
		return results;
	}

	@Override
	public AllSitesMonitors monitorAllSites() {
		Collection<AllSitesMonitors.SiteMonitors> sites = new ArrayList<>();
		siteDao.getSitesByState(READY).forEach(site -> {
			try {
				Collection<SiteMonitor> siteMonitors = monitorSite(site.getSiteId());
				sites.add(new AllSitesMonitors.SiteMonitors(site.getSiteId(), siteMonitors));
			} catch (ServiceLayerException e) {
				logger.error("Failed to monitor site '{}'", site.getSiteId(), e);
			}
		});

		return new AllSitesMonitors(sites);
	}

	@Override
	public void monitorAndNotify() {
		AllSitesMonitors allSitesMonitors = monitorAllSites();
		if (isEmpty(allSitesMonitors.sites())) {
			logger.info("No content monitor matches found for any site");
		} else {
			logger.info("Content monitor matches found for {} sites, sending notifications", allSitesMonitors.sites().size());
			allSitesMonitors.sites().forEach(site -> site.monitors().forEach(monitor -> monitor.getPaths().forEach(path -> {
				if (isEmpty(path.items())) {
					logger.info("No items matching path pattern '{}' for monitor '{}' in site '{}', skipping notification",
							path.name(), monitor.getName(), site.siteId());
					return;
				}

				Collection<String> emails = getEmails(path);
				if(isEmpty(emails)) {
					logger.warn("No emails configured for path pattern '{}' for monitor '{}' in site '{}', skipping notification",
							path.name(), monitor.getName(), site.siteId());
					return;
				}
				notificationService.notify(site.siteId(), emails,
						path.emailTemplate(),
						Pair.of("monitorName", monitor.getName()),
						Pair.of("items", path.items()));
			})));
		}
	}

	/**
	 * Helper method to get the list of emails from the comma separated string configured for the path pattern
	 */
	private Collection<String> getEmails(SiteMonitor.SiteMonitorPath path) {
		if (StringUtils.isEmpty(path.emails())) {
			return emptyList();
		}
		return Arrays.stream(path.emails().split(",")).toList();
	}
}
