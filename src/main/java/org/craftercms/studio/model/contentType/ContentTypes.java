/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.model.contentType;

import org.craftercms.studio.model.rest.Result;

import java.util.Collection;

/**
 * Contains a collection of {@link ContentTypeConfigFiles} objects.
 */
public class ContentTypes extends Result {
    private final Collection<ContentTypeConfigFiles> types;

    public ContentTypes(Collection<ContentTypeConfigFiles> types) {
        this.types = types;
    }

    public Collection<ContentTypeConfigFiles> getTypes() {
        return types;
    }

}
