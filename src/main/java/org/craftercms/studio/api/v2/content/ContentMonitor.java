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

package org.craftercms.studio.api.v2.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.site.AllSitesMonitors;
import org.craftercms.studio.model.site.SiteMonitor;

import java.util.Collection;

/**
 * Provides operations to monitor the content of a site or all sites, based on the content monitor queries configured for the site(s).
 */
public interface ContentMonitor {
	/**
	 * Get the site content monitors from configuration and execute them, returning the
	 * matching results for each site monitor.
	 *
	 * @param siteId the site id
	 * @return the list of results for the site monitoring
	 */
	Collection<SiteMonitor> monitorSite(String siteId) throws ServiceLayerException;

	/**
	 * Get the content monitors for all sites and execute them, returning the matching results for each site monitor.
	 *
	 * @return the list of results for all site monitoring
	 */
	AllSitesMonitors monitorAllSites();

	/**
	 * Get the content monitors for all sites and execute them, sending notifications for the matching results based on the configuration of each monitor.
	 */
	void monitorAndNotify();
}
