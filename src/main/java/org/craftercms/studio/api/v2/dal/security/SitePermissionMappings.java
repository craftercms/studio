/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v2.dal.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SitePermissionMappings {

    private String siteId;
    private Map<NormalizedRole, RolePermissionMappings> rolePermissions = new HashMap<>();
    private final Map<NormalizedGroup, List<NormalizedRole>> groupToRolesMapping = new HashMap<>();

    public long getAvailableActions(String username, List<Group> groups, String path) {
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

        long availableActions = 0L;
        for (NormalizedRole role : rolesList) {
            RolePermissionMappings rolePermissionMappings = rolePermissions.get(role);
            Map<String, Long> rulePermissions = rolePermissionMappings.getRuleContentItemPermissions();
            for (Map.Entry<String, Long> entry : rulePermissions.entrySet()) {
                Pattern pattern = Pattern.compile(entry.getKey());
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    availableActions = availableActions | entry.getValue();
                }
            }
        }
        return availableActions;
    }

    public void addGroupToRolesMapping(NormalizedGroup group, List<NormalizedRole> roles) {
        groupToRolesMapping.put(group, roles);
    }

    public void addRoleToGroupMapping(String group, String role) {
        NormalizedGroup normalizedGroup = new NormalizedGroup(group);
        List<NormalizedRole> roles = groupToRolesMapping.get(normalizedGroup);
        if (Objects.isNull(roles)) {
            roles = new ArrayList<>();
            groupToRolesMapping.put(normalizedGroup, roles);
        }
        roles.add(new NormalizedRole(role));
    }

    public List<NormalizedRole> getRolesForGroup(String group) {
        return this.groupToRolesMapping.get(new NormalizedGroup(group));
    }

    public void addRolePermissionMapping(String role, RolePermissionMappings rolePermissionMappings) {
        rolePermissions.put(new NormalizedRole(role), rolePermissionMappings);
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Map<NormalizedRole, RolePermissionMappings> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(Map<NormalizedRole, RolePermissionMappings> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
