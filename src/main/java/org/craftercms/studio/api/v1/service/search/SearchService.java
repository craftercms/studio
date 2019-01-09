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

package org.craftercms.studio.api.v1.service.search;

import org.craftercms.studio.api.v1.exception.ServiceException;

/**
 * Created by Sumer Jabri on 2/15/17.
 */
public interface SearchService {

	/**
	 * Create a new index (core) in Crafter Search for a site
	 *
	 * @param siteId the Site ID for the site to build the index for
	 * @throws ServiceException
	 */
	void createIndex(String siteId) throws ServiceException;

	/**
	 * Delete a search index (core) in Crafter Search for a site
	 *
	 * @param siteId the Site ID for the site to delete the index for
	 * @throws ServiceException
	 */
	public void deleteIndex(final String siteId) throws ServiceException;
}