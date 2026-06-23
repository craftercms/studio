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

package org.craftercms.studio.api.v2.security;

import java.util.Collection;
import java.util.Set;

/**
 * Read-only view of role rules mapped to item available actions.
 */
public interface RolePermissionMappings {

	/**
	 * Get the available actions for a given path.
	 *
	 * @param path path of the content
	 * @return available actions bitmap. This is calculated
	 * by combining the available actions for all rules that match the path.
	 */
	long getActionsForPath(String path);

	/**
	 * Get the permissions for a given path.
	 *
	 * @param path path of the content
	 * @return set of permissions
	 */
	Set<String> getPermissionsForPath(String path);

	/**
	 * Get the site wide permissions for this role.
	 *
	 * @return list of permissions
	 */
	Collection<String> getSiteWidePermissions();

	/**
	 * Get all permissions configured for this role.
	 *
	 * @return collection of permissions
	 */
	Collection<String> getAllPermissions();

}
