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

package org.craftercms.studio.model.rest.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.studio.model.rest.Person;

import java.time.ZonedDateTime;

public class Activity {

    private long id;
    private Person person;
    private String actionType;
    private ZonedDateTime actionTimestamp;

    protected Object target;

    protected Item item;

    @JsonProperty("package")
    protected Package publishPackage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public ZonedDateTime getActionTimestamp() {
        return actionTimestamp;
    }

    public void setActionTimestamp(ZonedDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Package getPublishPackage() {
        return publishPackage;
    }

    public void setPublishPackage(Package publishPackage) {
        this.publishPackage = publishPackage;
    }

    //TODO: Populate with metadata once Publishing Packages are fully implemented
    public static class Package {

        protected String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    public static class Item {

        protected long id;
        protected String path;
        protected String label;
        protected String previewUrl;
        protected String systemType;

        // Backup fields used only when an item has been deleted
        @JsonIgnore
        protected String recordedPath;
        @JsonIgnore
        protected String recordedLabel;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getPath() {
            return path != null? path : recordedPath;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getLabel() {
            return label != null? label : recordedLabel;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        public String getSystemType() {
            return systemType;
        }

        public void setSystemType(String systemType) {
            this.systemType = systemType;
        }

        public String getRecordedPath() {
            return recordedPath;
        }

        public void setRecordedPath(String recordedPath) {
            this.recordedPath = recordedPath;
        }

        public String getRecordedLabel() {
            return recordedLabel;
        }

        public void setRecordedLabel(String recordedLabel) {
            this.recordedLabel = recordedLabel;
        }
    }

}
