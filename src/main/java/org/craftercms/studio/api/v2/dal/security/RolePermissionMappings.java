/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

public class RolePermissionMappings {

    private String role;
    private Map<String, Long> ruleContentItemPermissions = new HashMap<String, Long>();

    public void addRuleContentItemPermissionsMapping(String rule, Long contentItemAvailableActions) {
        ruleContentItemPermissions.put(rule, contentItemAvailableActions);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, Long> getRuleContentItemPermissions() {
        return ruleContentItemPermissions;
    }

    public void setRuleContentItemPermissions(Map<String, Long> ruleContentItemPermissions) {
        this.ruleContentItemPermissions = ruleContentItemPermissions;
    }
}
