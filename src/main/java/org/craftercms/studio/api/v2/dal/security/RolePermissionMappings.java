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

import java.util.*;

/**
 * Maps roles to item available actions.
 * Instances will keep a map of rules to available actions for a given role.
 * It will also keep a set of site wide permissions. These are meant to be merged for all roles and then be used
 * to determine site-wide actions. e.g.: a user can be assigned a role with PUBLISH_REQUEST permission and another
 * one with PUBLISH_APPROVE permission. Such user would get the PUBLISH available action.
 */
public class RolePermissionMappings {

	private final Map<String, Long> ruleContentItemPermissions = new HashMap<>();
	private final Set<String> siteWidePermissions = new HashSet<>();


	public void addRuleContentItemPermissionsMapping(String rule, Long contentItemAvailableActions) {
		ruleContentItemPermissions.put(rule, contentItemAvailableActions);
	}

	public void addSiteWidePermissions(final List<String> permissions) {
		this.siteWidePermissions.addAll(permissions);
	}


	public Map<String, Long> getRuleContentItemPermissions() {
		return ruleContentItemPermissions;
	}

	public Collection<String> getSiteWidePermissions() {
		return siteWidePermissions;
	}
}
