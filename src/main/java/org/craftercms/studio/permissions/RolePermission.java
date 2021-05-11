/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.permissions;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.commons.security.permissions.Permission;

import java.util.Set;

public class RolePermission implements Permission {

    public static final String ANY_ROLE = "*";
    protected Set<String> allowedRoles;

    @Override
    public boolean isAllowed(String role) {
        return CollectionUtils.isNotEmpty(allowedRoles) &&
                (allowedRoles.contains(ANY_ROLE) || allowedRoles.contains(role));
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{allowedRoles=" + allowedRoles + '}';
    }
}
