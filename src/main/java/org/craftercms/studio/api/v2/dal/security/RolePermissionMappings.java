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

import org.craftercms.studio.permissions.StudioPermissionsConstants;

import java.util.*;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.mapPermissionsToContentItemAvailableActions;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.SITE_WIDE_RULE_REGEXES;

/**
 * Maps role rules to item available actions.
 * Instances will keep a map of rules to available actions for a given role.
 * It will also keep a set of site wide permissions. These are meant to be merged for all roles and then be used
 * to determine site-wide actions. e.g.: a user can be assigned a role with PUBLISH_REQUEST permission and another
 * one with PUBLISH_APPROVE permission. Such user would get the PUBLISH available action.
 */
public class RolePermissionMappings {

	// Rule path -> available actions
	private final Map<Pattern, Long> ruleContentItemPermissions = new HashMap<>();
	private final Set<String> siteWidePermissions = new HashSet<>();

	/**
	 * Add a rule to this role mappings
	 *
	 * @param ruleRegex   regex to match the content item paths
	 * @param permissions granted permissions for the rule
	 */
	public void addRuleContentItemPermissionsMapping(final String ruleRegex, final Collection<String> permissions) {
		Pattern pattern = Pattern.compile(ruleRegex);
		ruleContentItemPermissions.put(pattern, mapPermissionsToContentItemAvailableActions(permissions));
		if (SITE_WIDE_RULE_REGEXES.stream().anyMatch(ruleRegex::equals)) {
			this.siteWidePermissions.addAll(permissions);
		}
	}

	/**
	 * Get the available actions for a given path.
	 *
	 * @param path path of the content
	 * @return available actions bitmap. This is calculated
	 * by combining the available actions for all rules that match the path.
	 */
	public long getActionsForPath(final String path) {
		return ruleContentItemPermissions.entrySet().stream()
			.filter(entry -> entry.getKey().matcher(path).matches())
			.mapToLong(Map.Entry::getValue)
			.reduce(0L, (a, b) -> a | b);
	}

	/**
	 * Get the site wide permissions for this role.
	 * The site-wide permissions are the ones found in rules matching {@link StudioPermissionsConstants#SITE_WIDE_RULE_REGEXES}
	 *
	 * @return list of permissions
	 */
	public Collection<String> getSiteWidePermissions() {
		return siteWidePermissions;
	}
}
