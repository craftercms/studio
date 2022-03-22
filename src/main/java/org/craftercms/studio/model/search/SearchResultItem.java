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

package org.craftercms.studio.model.search;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Holds the data of a single search result
 * @author joseross
 */
public class SearchResultItem {

    /**
     * The path of the file
     */
    protected String path;

    /**
     * The name of the item
     */
    protected String name;

    /**
     * The type of the file
     */
    protected String type;

    /**
     * The mime type of the file
     */
    protected String mimeType;

    /**
     * The preview URL of the file
     */
    protected String previewUrl;

    /**
     * The last user that modified the file
     */
    protected String lastModifier;

    /**
     * The last date that the file was modified
     */
    protected Instant lastModified;

    /**
     * The size of the file, in bytes
     */
    protected long size;

    /**
     * Snippets relevant to the search query
     */
    protected List<String> snippets;

    /**
     * Additional fields requested
     */
    protected Map<String, Object> additionalFields;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
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

    public List<String> getSnippets() {
        return snippets;
    }

    public void setSnippets(final List<String> snippets) {
        this.snippets = snippets;
    }

    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }

}
