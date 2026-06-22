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

package org.craftercms.studio.api.v2.dal.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import static org.craftercms.studio.api.v1.constant.StudioConstants.ADMIN_NORMALIZED_ROLE;
import org.craftercms.studio.api.v2.dal.Group;
import static org.craftercms.studio.api.v2.dal.security.NormalizedRole.WILDCARD_ROLE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.mapSiteWidePermissionsToItemAvailableActions;
import static org.craftercms.studio.api.v2.security.publish.PublishPackageAvailableActions.mapSiteWidePermissionsToPackageAvailableActions;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

/**
 * Mapping of user groups to available actions.
 * Instances will keep a map of groups to roles and a map of roles to {@link RolePermissionMappings} for a given site.
 * The {@link RolePermissionMappings} can then be used to retrieve the available actions.
 */
public class SitePermissionMappings {

	private final Map<NormalizedRole, RolePermissionMappings> rolePermissions = new HashMap<>();
	private final Map<NormalizedGroup, List<NormalizedRole>> groupToRolesMapping = new HashMap<>();

	/**
	 * Get the available actions for a given user and path.
	 * The available actions are calculated by merging the available actions for all roles the user belongs to.
	 *
	 * @param username username of the user
	 * @param groups   groups the user belongs to
	 * @param path     path of the content
	 * @return available actions bitmap
	 */
	public long getAvailableActions(String username, List<Group> groups, String path) {
		List<NormalizedRole> rolesList = getRolesForUser(username, groups);
		long availableActions = 0L;
		for (NormalizedRole role : rolesList) {
			RolePermissionMappings rolePermissionMappings = rolePermissions.get(role);
			if (rolePermissionMappings != null) {
				availableActions |= rolePermissionMappings.getActionsForPath(path);
			}
		}
		return availableActions;
	}

	/**
	 * Get the site wide available actions for a given user.
	 * Site-wide actions are performed on the item-level, but are allowed on site-wide
	 * permissions. e.g.: publish_request permission will allow PUBLISH_REQUEST
	 * action for any item in the site
	 *
	 * @param username username of the user
	 * @param groups groups the user belongs to
	 * @return available actions bitmap
	 */
	public long getSiteWideItemAvailableActions(String username, List<Group> groups) {
		return mapSiteWidePermissionsToItemAvailableActions(getSiteWidePermissions(username, groups));
	}

	/**
	 * Get the actions a user has permissions to perform on publish packages
	 *
	 * @param username username of the user
	 * @param groups groups the user belongs to
	 * @return available actions bitmap
	 */
	public long getPublishPackageAvailableActions(String username, List<Group> groups) {
		return mapSiteWidePermissionsToPackageAvailableActions(getSiteWidePermissions(username, groups));
	}

	/**
	 * Check if the user is a site admin
	 * @param username username of the user
	 * @param groups groups the user belongs to
	 * @return true if the user is a site admin, false otherwise
	 */
	public boolean isSiteAdmin(String username, Collection<Group> groups) {
		return getRolesForUser(username, groups).contains(ADMIN_NORMALIZED_ROLE);
	}

	/**
	 * Get the site-wide permissions for a given user.
	 *
	 * @param username username of the user
	 * @param groups   the groups the user belongs to
	 * @return list of permissions
	 */
	private Collection<String> getSiteWidePermissions(String username, Collection<Group> groups) {
		List<NormalizedRole> rolesList = getRolesForUser(username, groups);
		Set<String> permissions = new HashSet<>();
		for (NormalizedRole role : rolesList) {
			RolePermissionMappings rolePermissionMappings = rolePermissions.get(role);
			if (rolePermissionMappings != null) {
				permissions.addAll(rolePermissionMappings.getSiteWidePermissions());
			}
		}
		return permissions;
	}

	private List<NormalizedRole> getRolesForUser(String username, Collection<Group> groups) {
		List<NormalizedRole> rolesList = new ArrayList<>();
		List<NormalizedRole> userRoles = groupToRolesMapping.get(new NormalizedGroup(username));
		if (CollectionUtils.isNotEmpty(userRoles)) {
			CollectionUtils.addAll(rolesList, userRoles);
		}

		groups.forEach(g -> {
			List<NormalizedRole> groupRoles = groupToRolesMapping.get(new NormalizedGroup(g.getGroupName()));
			if (CollectionUtils.isNotEmpty(groupRoles)) {
				CollectionUtils.addAll(rolesList, groupRoles);
			}
		});

		// Add wildcard role to the roles list so it matches the wildcard rule
		if (!rolesList.isEmpty()) {
			rolesList.add(WILDCARD_ROLE);
		}
		return rolesList;
	}

	/**
	 * Get all permissions for a given user.
	 * @param username username of the user
	 * @param groups groups the user belongs to
	 * @param isSystemAdmin true if the user is a system admin, false otherwise
	 * @return list of permissions
	 */
	public Set<String> getUserPermissions(String username, Collection<Group> groups, boolean isSystemAdmin) {
		Set<String> permissions = new HashSet<>();
		if (isSystemAdmin) {
			rolePermissions.values().forEach(rolePermissionMappings -> {
				permissions.addAll(rolePermissionMappings.getAllPermissions());
			});
		} else {
			List<NormalizedRole> roles = getRolesForUser(username, groups);
			if (CollectionUtils.isNotEmpty(roles)) {
				roles.forEach(role -> {
					RolePermissionMappings rolePermissionMappings = rolePermissions.get(role);
					if (rolePermissionMappings != null) {
						permissions.addAll(rolePermissionMappings.getAllPermissions());
					}
				});
			} else {
				permissions.add(PERMISSION_CONTENT_READ);
			}
		}
		return permissions;
	}

	/**
	 * Get the permissions for a given user and path.
	 *
	 * @param username username of the user
	 * @param groups groups the user belongs to
	 * @param path path of the content
	 * @param isSystemAdmin true if the user is a system admin, false otherwise
	 * @return set of permissions
	 */
	public Set<String> getUserPermissions(String username, Collection<Group> groups, String path, boolean isSystemAdmin) {
		Set<String> permissions = new HashSet<>();
		if (isSystemAdmin) {
			rolePermissions.values().forEach(rolePermissionMappings -> {
				permissions.addAll(rolePermissionMappings.getPermissionsForPath(path));
			});
		} else {
			List<NormalizedRole> roles = getRolesForUser(username, groups);
			if (CollectionUtils.isNotEmpty(roles)) {
				roles.forEach(role -> {
					RolePermissionMappings rolePermissionMappings = rolePermissions.get(role);
					if (rolePermissionMappings != null) {
						permissions.addAll(rolePermissionMappings.getPermissionsForPath(path));
					}
				});
			} else {
				permissions.add(PERMISSION_CONTENT_READ);
			}
		}
		return permissions;
	}

	public void addGroupToRolesMapping(NormalizedGroup group, List<NormalizedRole> roles) {
		groupToRolesMapping.put(group, roles);
	}

	public void addRolePermissionMapping(String role, RolePermissionMappings rolePermissionMappings) {
		rolePermissions.put(new NormalizedRole(role), rolePermissionMappings);
	}
}
