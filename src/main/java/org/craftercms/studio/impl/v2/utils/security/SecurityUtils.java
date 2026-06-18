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

package org.craftercms.studio.impl.v2.utils.security;

import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.model.AuthenticatedUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security utilities
 */
public class SecurityUtils {

	/**
	 * Returns the username of the current user
	 *
	 * @return username of the current user, or null if no user is authenticated
	 */
	public static String getCurrentUsername() {
		String username = null;
		var context = SecurityContextHolder.getContext();
		// SecurityContext.getContext() is never null
		var auth = context.getAuthentication();

		if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
			username = auth.getName();
		}

		return username;
	}

	/**
	 * Returns the {@link Authentication} for the current user or null if not user is authenticated.
	 *
	 * @return authentication
	 */
	public static Authentication getAuthentication() {
		var context = SecurityContextHolder.getContext();
		if (context != null) {
			return context.getAuthentication();
		}
		return null;
	}

	/**
	 * Returns the {@link AuthenticatedUser} for the current user
	 *
	 * @return currently authenticated user
	 * @throws AuthenticationException if no user is authenticated
	 */
	public static AuthenticatedUser getCurrentUser() throws AuthenticationException {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication != null) {
			return (AuthenticatedUser) authentication.getPrincipal();
		}
		throw new AuthenticationException("User should be authenticated");
	}
}
