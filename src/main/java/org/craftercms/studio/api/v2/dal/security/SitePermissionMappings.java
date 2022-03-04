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
    private Map<String, RolePermissionMappings> rolePermissions = new HashMap<String, RolePermissionMappings>();
    private Map<String, List<String>> groupToRolesMapping = new HashMap<String, List<String>>();

    public long getAvailableActions(String username, List<Group> groups, String path) {
        List<String> rolesList = new ArrayList<String>();
        List<String> userRoles = groupToRolesMapping.get(username);
        if (CollectionUtils.isNotEmpty(userRoles)) {
            CollectionUtils.addAll(rolesList, userRoles);
        }
        groups.forEach(g -> {
            List<String> groupRoles = groupToRolesMapping.get(g.getGroupName());
            if (CollectionUtils.isNotEmpty(groupRoles)) {
                CollectionUtils.addAll(rolesList, groupRoles);
            }
        });

        long availableActions = 0L;
        for (String role : rolesList) {
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

    public void addGroupToRolesMapping(String group, List<String> roles) {
        groupToRolesMapping.put(group, roles);
    }

    public void addRoleToGroupMapping(String group, String role) {
        List<String> roles = groupToRolesMapping.get(group);
        if (Objects.isNull(roles)) {
            roles = new ArrayList<String>();
            groupToRolesMapping.put(group, roles);
        }
        roles.add(role);
    }

    public List<String> getRolesForGroup(String group) {
        return this.groupToRolesMapping.get(group);
    }

    public void addRolePermissionMapping(String role, RolePermissionMappings rolePermissionMappings) {
        rolePermissions.put(role, rolePermissionMappings);
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Map<String, RolePermissionMappings> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(Map<String, RolePermissionMappings> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
