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

import org.craftercms.commons.security.permissions.DefaultPermission;

import java.util.Objects;

public class PermissionOrOwnership extends DefaultPermission {

    protected boolean owner;

    @Override
    public boolean isAllowed(String action) {
        return owner || super.isAllowed(action);
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PermissionOrOwnership that = (PermissionOrOwnership) o;

        if (!Objects.equals(allowedActions, that.allowedActions)) {
            return false;
        }

        return owner == that.owner;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(owner) + (allowedActions != null ? allowedActions.hashCode() : 0);
    }
}
