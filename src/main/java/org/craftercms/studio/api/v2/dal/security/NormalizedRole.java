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

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNullElse;

/**
 * Normalized role to deal with case-insensitive while working with roles/permissions mappings
 * @param name the name of the role
 */
public record NormalizedRole(String name) {
    public NormalizedRole(String name) {
        this.name = requireNonNullElse(name, StringUtils.EMPTY).toLowerCase();
    }

    @Override
    public String toString() {
        return name;
    }
}
