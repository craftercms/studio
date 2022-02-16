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

package org.craftercms.studio.api.v2.dal;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Objects;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;

public class Item {

    private static final Logger logger = LoggerFactory.getLogger(Item.class);

    private long id;
    private long siteId;
    private String siteName;
    private String path;
    private String previewUrl;
    private long state;
    private Long lockedBy;
    private String lockOwner;
    private Long createdBy = null;
    private String creator;
    private ZonedDateTime createdOn;
    private Long lastModifiedBy = null;
    private String modifier;
    private ZonedDateTime lastModifiedOn;
    private ZonedDateTime lastPublishedOn;
    private String label;
    private String contentTypeId;
    private String systemType;
    private String mimeType;
    private String localeCode;
    private Long translationSourceId = null;
    private long size;
    private Long parentId = null;
    private String commitId;
    private long availableActions;
    private String previousPath;
    private int ignoredAsInt;
    private boolean ignored;
    private int childrenCount = 0;

    public Item() { }

    private Item(Builder builder) {
        id = builder.id;
        siteId = builder.siteId;
        siteName = builder.siteName;
        path = builder.path;
        previewUrl = builder.previewUrl;
        state = builder.state;
        lockedBy = builder.lockedBy;
        lockOwner = builder.lockOwner;
        createdBy = builder.createdBy;
        creator = builder.creator;
        createdOn = builder.createdOn;
        lastModifiedBy = builder.lastModifiedBy;
        modifier = builder.modifier;
        lastModifiedOn = builder.lastModifiedOn;
        lastPublishedOn = builder.lastPublishedOn;
        label = builder.label;
        contentTypeId = builder.contentTypeId;
        systemType = builder.systemType;
        mimeType = builder.mimeType;
        localeCode = builder.localeCode;
        translationSourceId = builder.translationSourceId;
        size = builder.size;
        parentId = builder.parentId;
        commitId = builder.commitId;
        availableActions = builder.availableActions;
        previousPath = builder.previousPath;
        ignoredAsInt = builder.ignoredAsInt;
        ignored = builder.ignored;
        childrenCount = builder.childrenCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
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

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public Long getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Long getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public ZonedDateTime getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(ZonedDateTime lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public ZonedDateTime getLastPublishedOn() {
        return lastPublishedOn;
    }

    public void setLastPublishedOn(ZonedDateTime lastPublishedOn) {
        this.lastPublishedOn = lastPublishedOn;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public long getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(long availableActions) {
        this.availableActions = availableActions;
    }

    public String getPreviousPath() {
        return previousPath;
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    public int getIgnoredAsInt() {
        return ignoredAsInt;
    }

    public void setIgnoredAsInt(int ignoredAsInt) {
        this.ignoredAsInt = ignoredAsInt;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public static Item getInstance(DetailedItem item) {
        if (Objects.isNull(item)) {
            return null;
        }

        Item instance = new Item();

        instance.id = item.getId();
        instance.siteId = item.getSiteId();
        instance.siteName = item.getSiteName();
        instance.path = item.getPath();
        instance.previewUrl = item.getPreviewUrl();
        instance.state = item.getState();
        instance.lockedBy = item.getLockedBy();
        instance.lockOwner = item.getLockOwner();
        instance.createdBy = item.getCreatedBy();
        instance.creator = item.getCreator();
        instance.createdOn = item.getCreatedOn();
        instance.lastModifiedBy = item.getLastModifiedBy();
        instance.modifier = item.getModifier();
        instance.lastModifiedOn = item.getLastModifiedOn();
        instance.lastPublishedOn = item.getLastPublishedOn();
        instance.label = item.getLabel();
        instance.contentTypeId = item.getContentTypeId();
        instance.systemType = item.getSystemType();
        instance.mimeType = item.getMimeType();
        instance.localeCode = item.getLocaleCode();
        instance.translationSourceId = item.getTranslationSourceId();
        instance.size = item.getSize();
        instance.parentId = item.getParentId();
        instance.commitId = item.getCommitId();
        instance.availableActions = item.getAvailableActions();
        instance.previousPath = item.getPreviousPath();
        instance.ignoredAsInt = item.getIgnoredAsInt();
        instance.ignored = item.isIgnored();
        instance.childrenCount = item.getChildrenCount();

        return instance;
    }

    public static final class Builder {
        private long id;
        private long siteId;
        private String siteName;
        private String path;
        private String previewUrl;
        private long state;
        private Long lockedBy;
        private String lockOwner;
        private Long createdBy;
        private String creator;
        private ZonedDateTime createdOn;
        private Long lastModifiedBy;
        private String modifier;
        private ZonedDateTime lastModifiedOn;
        private ZonedDateTime lastPublishedOn;
        private String label;
        private String contentTypeId;
        private String systemType;
        private String mimeType;
        private String localeCode;
        private Long translationSourceId;
        private long size;
        private Long parentId = null;
        private String commitId;
        private Long availableActions;
        private String previousPath;
        private int ignoredAsInt;
        private boolean ignored;
        private int childrenCount = 0;

        public Builder() { }

        public static Builder buildFromClone(Item item) {
            Builder clone = new Builder();
            clone.siteId = item.siteId;
            clone.siteName = item.siteName;
            clone.path = item.path;
            clone.previewUrl = item.previewUrl;
            clone.state = item.state;
            clone.lockedBy = item.lockedBy;
            clone.lockOwner = item.lockOwner;
            clone.createdBy = item.createdBy;
            clone.creator = item.creator;
            clone.createdOn = item.createdOn;
            clone.lastModifiedBy = item.lastModifiedBy;
            clone.modifier = item.modifier;
            clone.lastModifiedOn = item.lastModifiedOn;
            clone.lastPublishedOn = item.lastPublishedOn;
            clone.label = item.label;
            clone.contentTypeId = item.contentTypeId;
            clone.systemType = item.systemType;
            clone.mimeType = item.mimeType;
            clone.localeCode = item.localeCode;
            clone.translationSourceId = item.translationSourceId;
            clone.size = item.size;
            clone.parentId = item.parentId;
            clone.commitId = item.commitId;
            clone.availableActions = item.availableActions;
            clone.previousPath = item.previousPath;
            clone.ignoredAsInt = item.ignoredAsInt;
            clone.ignored = item.ignored;
            clone.childrenCount = item.childrenCount;
            return clone;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withSiteId(long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder withSiteName(String siteName) {
            this.siteName = siteName;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
            return this;
        }

        public Builder withState(long state) {
            this.state = state;
            return this;
        }

        public Builder withLockedBy(Long lockedBy) {
            this.lockedBy = lockedBy;
            return this;
        }

        public Builder withLockOwner(String lockOwner) {
            this.lockOwner = lockOwner;
            return this;
        }

        public Builder withCreatedBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder withCreatedOn(ZonedDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withLastModifiedBy(Long lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Builder withModifier(String modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder withLastModifiedOn(ZonedDateTime lastModifiedOn) {
            this.lastModifiedOn = lastModifiedOn;
            return this;
        }

        public Builder withLastPublishedOn(ZonedDateTime lastPublishedOn) {
            this.lastPublishedOn = lastPublishedOn;
            return this;
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withContentTypeId(String contentTypeId) {
            this.contentTypeId = contentTypeId;
            return this;
        }

        public Builder withSystemType(String systemType) {
            this.systemType = systemType;
            return this;
        }

        public Builder withMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withLocaleCode(String localeCode) {
            this.localeCode = localeCode;
            return this;
        }

        public Builder withTranslationSourceId(Long translationSourceId) {
            this.translationSourceId = translationSourceId;
            return this;
        }

        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        public Builder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder withCommitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public Builder withPreviousPath(String previousPath) {
            this.previousPath = previousPath;
            return this;
        }

        public Builder withIgnoredAsInt(int ignoredAsInt) {
            this.ignoredAsInt = ignoredAsInt;
            this.ignored = ignoredAsInt > 0;
            return this;
        }

        public Builder withIgnored(boolean ignored) {
            this.ignored = ignored;
            this.ignoredAsInt = ignored ? 1 : 0;
            return this;
        }

        public Item build() {
            String fileName = FilenameUtils.getName(this.path);
            if (ArrayUtils.contains(IGNORE_FILES, fileName)) {
                this.ignoredAsInt = 1;
                this.ignored = true;
            }
            return new Item(this);
        }
    }
}
