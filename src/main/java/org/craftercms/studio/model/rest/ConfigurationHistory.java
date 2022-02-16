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

package org.craftercms.studio.model.rest;

import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.ContentItemVersion;

import java.util.List;

public class ConfigurationHistory {

    private ContentItemTO item;
    private List<ContentItemVersion> versions;

    public ContentItemTO getItem() {
        return item;
    }

    public void setItem(ContentItemTO item) {
        this.item = item;
    }

    public List<ContentItemVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ContentItemVersion> versions) {
        this.versions = versions;
    }
}
