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

package org.craftercms.studio.model.site;

import java.util.Collection;

/**
 * Includes the information about content matching site monitor queries for all sites.
 *
 * @param sites the collection of site monitors, one for each site
 */
public record AllSitesMonitors(Collection<SiteMonitors> sites) {
	/**
	 * Includes the information about content matching a site monitor query for a site.
	 *
	 * @param siteId   the site id
	 * @param monitors the collection of monitors for the site, one for each monitor query configured for the site
	 */
	public record SiteMonitors(String siteId, Collection<SiteMonitor> monitors) {
	}
}
