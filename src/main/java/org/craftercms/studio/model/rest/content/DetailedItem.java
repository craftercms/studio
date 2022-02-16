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

package org.craftercms.studio.model.rest.content;

import org.craftercms.studio.api.v2.dal.Item;

import java.time.ZonedDateTime;

public class DetailedItem {

    private long id;
    private String label;
    private Long parentId;
    private String contentTypeId;
    private String path;
    private String previewUrl;
    private String systemType;
    private String mimeType;
    private Long state;
    private String lockOwner;
    private String localeCode;
    private Long translationSourceId;
    private long availableActions;
    private Sandbox sandbox;
    private Environment staging;
    private Environment live;
    private int childrenCount = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public Long getTranslationSourceId() {
        return translationSourceId;
    }

    public void setTranslationSourceId(Long translationSourceId) {
        this.translationSourceId = translationSourceId;
    }

    public long getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(long availableActions) {
        this.availableActions = availableActions;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public Environment getStaging() {
        return staging;
    }

    public void setStaging(Environment staging) {
        this.staging = staging;
    }

    public Environment getLive() {
        return live;
    }

    public void setLive(Environment live) {
        this.live = live;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public static DetailedItem getInstance(Item item) {
        DetailedItem instance = new DetailedItem();
        instance.sandbox = new Sandbox();
        instance.staging = new Environment();
        instance.live = new Environment();

        instance.id = item.getId();
        instance.label = item.getLabel();
        instance.parentId = item.getParentId();
        instance.contentTypeId = item.getContentTypeId();
        instance.path = item.getPath();
        instance.previewUrl = item.getPreviewUrl();
        instance.systemType = item.getSystemType();
        instance.mimeType = item.getMimeType();
        instance.state = item.getState();
        instance.lockOwner = item.getLockOwner();
        instance.localeCode = item.getLocaleCode();
        instance.translationSourceId = item.getTranslationSourceId();
        instance.sandbox.creator = item.getCreator();
        instance.sandbox.dateCreated = item.getCreatedOn();
        instance.sandbox.modifier = item.getModifier();
        instance.sandbox.dateModified = item.getLastModifiedOn();
        instance.sandbox.commitId = item.getCommitId();
        instance.sandbox.sizeInBytes = item.getSize();
        instance.availableActions = item.getAvailableActions();
        instance.childrenCount = item.getChildrenCount();

        return instance;
    }

    public static DetailedItem getInstance(org.craftercms.studio.api.v2.dal.DetailedItem item) {
        DetailedItem instance = new DetailedItem();
        instance.sandbox = new Sandbox();
        instance.staging = new Environment();
        instance.live = new Environment();

        instance.id = item.getId();
        instance.label = item.getLabel();
        instance.parentId = item.getParentId();
        instance.contentTypeId = item.getContentTypeId();
        instance.path = item.getPath();
        instance.previewUrl = item.getPreviewUrl();
        instance.systemType = item.getSystemType();
        instance.mimeType = item.getMimeType();
        instance.state = item.getState();
        instance.lockOwner = item.getLockOwner();
        instance.localeCode = item.getLocaleCode();
        instance.translationSourceId = item.getTranslationSourceId();
        instance.sandbox.creator = item.getCreator();
        instance.sandbox.dateCreated = item.getCreatedOn();
        instance.sandbox.modifier = item.getModifier();
        instance.sandbox.dateModified = item.getLastModifiedOn();
        instance.sandbox.commitId = item.getCommitId();
        instance.sandbox.sizeInBytes = item.getSize();
        instance.availableActions = item.getAvailableActions();
        instance.childrenCount = item.getChildrenCount();

        instance.staging.dateScheduled = item.getStagingScheduledDate();
        instance.staging.datePublished = item.getStagingPublishedOn();
        instance.staging.publisher = item.getStagingUsername();
        instance.staging.commitId = item.getStagingCommitId();
        instance.live.dateScheduled = item.getLiveScheduledDate();
        instance.live.datePublished = item.getLivePublishedOn();
        instance.live.publisher = item.getLiveUsername();
        instance.live.commitId = item.getLiveCommitId();

        return instance;
    }

    public static class Environment {
        private ZonedDateTime dateScheduled;
        private ZonedDateTime datePublished;
        private String publisher;
        private String commitId;

        public ZonedDateTime getDateScheduled() {
            return dateScheduled;
        }

        public void setDateScheduled(ZonedDateTime dateScheduled) {
            this.dateScheduled = dateScheduled;
        }

        public ZonedDateTime getDatePublished() {
            return datePublished;
        }

        public void setDatePublished(ZonedDateTime datePublished) {
            this.datePublished = datePublished;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getCommitId() {
            return commitId;
        }

        public void setCommitId(String commitId) {
            this.commitId = commitId;
        }
    }

    public static class Sandbox {
        private String creator;
        private ZonedDateTime dateCreated;
        private String modifier;
        private ZonedDateTime dateModified;
        private String commitId;
        private long sizeInBytes;

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public ZonedDateTime getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(ZonedDateTime dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public ZonedDateTime getDateModified() {
            return dateModified;
        }

        public void setDateModified(ZonedDateTime dateModified) {
            this.dateModified = dateModified;
        }

        public String getCommitId() {
            return commitId;
        }

        public void setCommitId(String commitId) {
            this.commitId = commitId;
        }

        public long getSizeInBytes() {
            return sizeInBytes;
        }

        public void setSizeInBytes(long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
        }
    }
}
