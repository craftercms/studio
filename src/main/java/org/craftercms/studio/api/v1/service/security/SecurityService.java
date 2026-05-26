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

package org.craftercms.studio.api.v1.service.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;

import java.util.Map;
import java.util.Set;

/**
 * @author Dejan Brkic
 */
public interface SecurityService {

	Map<String, Object> getUserProfile(String user) throws ServiceLayerException, UserNotFoundException;

	Set<String> getUserPermissions(String site, String path, String user) throws SiteNotFoundException;

	/**
	 * Check if given user is site admin
	 *
	 * @param username user
	 * @param site     site identifier
	 * @return true if user belongs to admin group
	 */
	boolean isSiteAdmin(String username, String site);
}
