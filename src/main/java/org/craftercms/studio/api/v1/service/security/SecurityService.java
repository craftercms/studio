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

package org.craftercms.studio.api.v1.service.security;

import jakarta.validation.Valid;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.security.NormalizedRole;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dejan Brkic
 */
public interface SecurityService {

	/**
	 * Returns the username of the current user OR NULL if no user is authenticated
	 *
	 * @return current user
	 * @deprecated use {@link org.craftercms.studio.impl.v2.utils.security.SecurityUtils#getCurrentUsername()} instead
	 */
	@Deprecated
	String getCurrentUser();

	/**
	 * Returns the {@link Authentication} for the current user or null if not user is authenticated.
	 *
	 * @return authentication
	 * @deprecated use {@link org.craftercms.studio.impl.v2.utils.security.SecurityUtils#getAuthentication()} instead
	 */
	@Deprecated
	Authentication getAuthentication();

	Set<String> getUserRoles(String site);

	@Valid Collection<NormalizedRole> getUserRoles(String site, String user);

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

	/**
	 * Check if given user has system_admin role
	 *
	 * @param username user
	 * @return true if user is system_admin, false otherwise
	 */
	boolean isSystemAdmin(String username);

	List<NormalizedRole> getUserGlobalRoles(long userId, String username)
		throws ServiceLayerException, UserNotFoundException;
}
