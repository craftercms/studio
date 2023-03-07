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

package org.craftercms.studio.api.v1.exception;

/**
 * Exception thrown when a blob pointer exists in the repository
 * but the actual asset does not exist in the blob store.
 */
public class BlobNotFoundException extends ServiceLayerException {

    protected String path;
    protected String site;

    public BlobNotFoundException(final String path, final String site, final String message) {
        super(message);
        this.path = path;
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public String getSite() {
        return site;
    }
}
