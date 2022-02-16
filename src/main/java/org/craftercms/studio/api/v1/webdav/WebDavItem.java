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

package org.craftercms.studio.api.v1.webdav;

/**
 * Holds the basic information about WebDAV resources.
 * @author joseross
 */
public class WebDavItem {

    /**
     * Display name of the resource.
     */
    protected String name;

    /**
     * Full URL of the resource.
     */
    protected String url;

    /**
     * Indicates if the resource is a folder.
     */
    protected boolean folder;

    public WebDavItem(final String name, final String url, final boolean folder) {
        this.name = name;
        this.url = url;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isFolder() {
        return folder;
    }

}
