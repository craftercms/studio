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

package org.craftercms.studio.permissions;

import org.craftercms.commons.security.permissions.Permission;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositePermission implements Permission {

    protected Set<Permission> permissions;

    @Override
    public boolean isAllowed(String action) {
        return permissions.stream().allMatch(p -> p.isAllowed(action));
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public CompositePermission addPermission(Permission permission) {
        if (Objects.nonNull(permission)) {
            if (Objects.isNull(permissions)) {
                permissions = new HashSet<Permission>();
            }
            permissions.add(permission);
        }
        return this;
    }

    public CompositePermission addPermission(CompositePermission compositePermission, Permission permission) {
        if (Objects.nonNull(compositePermission)) {
            if (Objects.nonNull(permission)) {
                compositePermission.addPermission(permission);
            }
        } else {
            if (Objects.nonNull(permission)) {
                CompositePermission cp = new CompositePermission();
                return cp.addPermission(permission);
            }
        }
        return compositePermission;
    }

    @Override
    public String toString() {
        return permissions.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompositePermission that = (CompositePermission) o;
        return permissions.stream().anyMatch(p -> !that.permissions.contains(p));
    }

    @Override
    public int hashCode() {
        return permissions != null? permissions.hashCode() : 0;
    }
}
