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
package org.craftercms.studio.api.v2.utils.spring.context;

/**
 * Service used to track the state of each site in the bootstrap process.
 * Implementations should expect a multi-threaded environment, as the bootstrap process is executed in parallel for each site,
 * and API calls to check the state of the site can be made from different threads.
 */
public interface SiteBootstrapStateProvider {
	/**
	 * Marks the site as ready, meaning that it has finished its bootstrap process and is ready to be used.
	 *
	 * @param siteId the id of the site
	 */
	void markSiteAsReady(String siteId);

	/**
	 * Checks if the site is ready, meaning that it has finished its bootstrap process and is ready to be used.
	 *
	 * @param siteId the id of the site
	 * @return true if the site is ready
	 */
	boolean isSiteReady(String siteId);
}
