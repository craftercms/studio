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

package org.craftercms.studio.model.blobstore;

import java.util.List;

/**
 * Blob store configuration details.
 */
public class BlobStoreDetails {
    private String id;
    private String type;
    private String pattern;
    List<Mapping> mappings;
    private boolean readOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public static class Mapping {
        private final String publishingTarget;
        private final String storeTarget;
        private final String prefix;

        public Mapping(String publishingTarget, String storeTarget, String prefix) {
            this.publishingTarget = publishingTarget;
            this.storeTarget = storeTarget;
            this.prefix = prefix;
        }

        public String getPublishingTarget() {
            return publishingTarget;
        }

        public String getStoreTarget() {
            return storeTarget;
        }

        public String getPrefix() {
            return prefix;
        }
    }

}
