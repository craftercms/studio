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

package org.craftercms.studio.api.v1.service.site;

import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;

import java.util.List;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 *
 * @author russdanner
 */
public interface SiteService {

	/**
	 * Check if site already exists
	 *
	 * @param site site ID
	 * @return true if site exists, false otherwise
	 */
	boolean exists(String site);

	/**
	 * Get total number of sites that user is allowed access to for given username
	 *
	 * @param username username
	 * @return number of sites
	 * @throws UserNotFoundException user not found
	 * @throws ServiceLayerException general service error
	 */
	int getSitesPerUserTotal(String username) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Get sites that user is allowed access to for current user
	 *
	 * @param start  start position for pagination
	 * @param number number of sites per page
	 * @return the list of sites
	 * @throws ServiceLayerException general service error
	 * @throws UserNotFoundException user not found
	 */
	List<SiteFeed> getSitesPerUser(int start, int number) throws UserNotFoundException,
			ServiceLayerException;

	/**
	 * Get sites that user is allowed access to for given username
	 *
	 * @param username username
	 * @param start    start position for pagination
	 * @param number   number of sites per page
	 * @return the list of sites
	 * @throws UserNotFoundException user not found
	 * @throws ServiceLayerException general service error
	 */
	List<SiteFeed> getSitesPerUser(String username, int start, int number) throws UserNotFoundException,
		ServiceLayerException;
}
