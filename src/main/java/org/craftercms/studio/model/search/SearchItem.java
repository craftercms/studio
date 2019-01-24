/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.model.search;

import java.time.Instant;

/**
 * @author joseross
 */
public class SearchItem {

    protected String path;
    protected String type;
    protected String previewUrl;
    protected String lastModifier;

    protected Instant lastModified;
    protected long size;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(final String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(final String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

}
