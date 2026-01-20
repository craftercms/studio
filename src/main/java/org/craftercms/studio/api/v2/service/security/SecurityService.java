/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.studio.api.v2.dal.security.NormalizedRole;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Provides security related services
 */
public interface SecurityService {

	/**
	 * Get user permissions for given site
	 *
	 * @param siteId   crafter site Id
	 * @param username user
	 * @param roles    roles the user is assigned to
	 * @return list of user permissions
	 */
	List<String> getUserPermission(String siteId, String username, Collection<NormalizedRole> roles) throws ExecutionException;

}
