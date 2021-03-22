/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import okhttp3.OkHttpClient.Builder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;

public class Item {

    private static final Logger logger = LoggerFactory.getLogger(Item.class);

    private long id;
    private long siteId;
    private String siteName;
    private String path;
    private String previewUrl;
    private long state;
    private Long ownedBy;
    private String owner;
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
    private int disabledAsInt;
    private boolean disabled;
    private String localeCode;
    private Long translationSourceId = null;
    private long size;
    private Long parentId = null;
    private String commitId;
    private long availableActions;
    private String previousPath;
    private int ignoredAsInt;
    private boolean ignored;

    public Item() { }

    private Item(Builder builder) {
        id = builder.id;
        siteId = builder.siteId;
        siteName = builder.siteName;
        path = builder.path;
        previewUrl = builder.previewUrl;
        state = builder.state;
        ownedBy = builder.ownedBy;
        owner = builder.owner;
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
        disabledAsInt = builder.disabledAsInt;
        disabled = builder.disabled;
        localeCode = builder.localeCode;
        translationSourceId = builder.translationSourceId;
        size = builder.size;
        parentId = builder.parentId;
        commitId = builder.commitId;
        availableActions = builder.availableActions;
        previousPath = builder.previousPath;
        ignoredAsInt = builder.ignoredAsInt;
        ignored = builder.ignored;
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

    public Long getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(Long ownedBy) {
        this.ownedBy = ownedBy;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public int getDisabledAsInt() {
        return disabledAsInt;
    }

    public void setDisabledAsInt(int disabledAsInt) {
        this.disabledAsInt = disabledAsInt;
        this.disabled = disabledAsInt > 0;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.disabledAsInt = disabled ? 1 : 0;
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

    public void populateProperties(Map<String, Object> properties) {
        properties.forEach((propertyName, value) -> {
            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(propertyName, this.getClass());
                Method setter = pd.getWriteMethod();
                try {
                    setter.invoke(this, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        });
    }

    public void populateProperties2(Map<String, Object> properties) {
        properties.forEach((propertyName, value) -> {
                    switch (propertyName) {
                        case Properties.ID:
                            setId((Long) value);
                            break;
                        case Properties.SITE_ID:
                            setSiteId((Long) value);
                            break;
                        case Properties.SITE_NAME:
                            setSiteName((String) value);
                            break;
                        case Properties.PATH:
                            setPath((String) value);
                            break;
                        case Properties.PREVIEW_URL:
                            setPreviewUrl((String) value);
                            break;
                        case Properties.STATE:
                            setState((Long) value);
                            break;
                        case Properties.OWNED_BY:
                            setOwnedBy((Long) value);
                            break;
                        case Properties.OWNER:
                            setOwner((String) value);
                            break;
                        case Properties.CREATED_BY:
                            setCreatedBy((Long) value);
                            break;
                        case Properties.CREATOR:
                            setCreator((String) value);
                            break;
                        case Properties.CREATED_ON:
                            setCreatedOn((ZonedDateTime) value);
                            break;
                        case Properties.LAST_MODIFIED_BY:
                            setLastModifiedBy((Long) value);
                            break;
                        case Properties.MODIFIER:
                            setModifier((String) value);
                            break;
                        case Properties.LAST_MODIFIED_ON:
                            setLastModifiedOn((ZonedDateTime) value);
                            break;
                        case Properties.LAST_PUBLISHED_ON:
                            setLastPublishedOn((ZonedDateTime) value);
                            break;
                        case Properties.LABEL:
                            setLabel((String) value);
                            break;
                        case Properties.CONTENT_TYPE_ID:
                            setContentTypeId((String) value);
                            break;
                        case Properties.SYSTEM_TYPE:
                            setSystemType((String) value);
                            break;
                        case Properties.MIME_TYPE:
                            setMimeType((String) value);
                            break;
                        case Properties.DISABLED:
                            setDisabled((Boolean) value);
                            break;
                        case Properties.LOCALE_CODE:
                            setLocaleCode((String) value);
                            break;
                        case Properties.TRANSLATION_SOURCE_ID:
                            setTranslationSourceId((Long) value);
                            break;
                        case Properties.SIZE:
                            setSize((Long) value);
                            break;
                        case Properties.PARENT_ID:
                            setParentId((Long) value);
                            break;
                        case Properties.COMMIT_ID:
                            setCommitId((String) value);
                            break;
                        case Properties.AVAILABLE_ACTIONS:
                            setAvailableActions((Long) value);
                            break;
                        case Properties.PREVIOUS_PATH:
                            setPreviousPath((String) value);
                            break;
                        case Properties.IGNORED:
                            setIgnored((Boolean) value);
                            break;
                    }
                });
    }

    public static final class Builder {
        private long id;
        private long siteId;
        private String siteName;
        private String path;
        private String previewUrl;
        private long state;
        private Long ownedBy;
        private String owner;
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
        private int disabledAsInt;
        private boolean disabled;
        private String localeCode;
        private Long translationSourceId;
        private long size;
        private Long parentId = null;
        private String commitId;
        private Long availableActions;
        private String previousPath;
        private int ignoredAsInt;
        private boolean ignored;

        public Builder() { }

        public static Builder buildFromClone(Item item) {
            Builder clone = new Builder();
            clone.siteId = item.siteId;
            clone.siteName = item.siteName;
            clone.path = item.path;
            clone.previewUrl = item.previewUrl;
            clone.state = item.state;
            clone.ownedBy = item.ownedBy;
            clone.owner = item.owner;
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
            clone.disabledAsInt = item.disabledAsInt;
            clone.disabled = item.disabled;
            clone.localeCode = item.localeCode;
            clone.translationSourceId = item.translationSourceId;
            clone.size = item.size;
            clone.parentId = item.parentId;
            clone.commitId = item.commitId;
            clone.availableActions = item.availableActions;
            clone.previousPath = item.previousPath;
            clone.ignoredAsInt = item.ignoredAsInt;
            clone.ignored = item.ignored;
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

        public Builder withOwnedBy (Long ownedBy) {
            this.ownedBy = ownedBy;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
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

        public Builder withDisabledAsInt(int disabledAsInt) {
            this.disabledAsInt = disabledAsInt;
            this.disabled = disabledAsInt > 0;
            return this;
        }

        public Builder withDisabled(boolean disabled) {
            this.disabled = disabled;
            this.disabledAsInt = disabled ? 1 : 0;
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

    public static class Properties {
        public static final String ID = "id";
        public static final String SITE_ID = "siteId";
        public static final String SITE_NAME = "siteName";
        public static final String PATH = "path";
        public static final String PREVIEW_URL = "previewUrl";
        public static final String STATE =  "state";
        public static final String OWNED_BY = "ownedBy";
        public static final String OWNER = "owner";
        public static final String CREATED_BY = "createdBy";
        public static final String CREATOR = "creator";
        public static final String CREATED_ON = "createdOn";
        public static final String LAST_MODIFIED_BY = "lastModifiedBy";
        public static final String MODIFIER = "modifier";
        public static final String LAST_MODIFIED_ON = "lastModifiedOn";
        public static final String LAST_PUBLISHED_ON = "lastPublishedOn";
        public static final String LABEL = "label";
        public static final String CONTENT_TYPE_ID = "contentTypeId";
        public static final String SYSTEM_TYPE = "systemType";
        public static final String MIME_TYPE = "mimeType";
        public static final String DISABLED = "disabled";
        public static final String LOCALE_CODE = "localeCode";
        public static final String TRANSLATION_SOURCE_ID = "translationSourceId";
        public static final String SIZE = "size";
        public static final String PARENT_ID = "parentId";
        public static final String COMMIT_ID = "commitId";
        public static final String AVAILABLE_ACTIONS = "availableActions";
        public static final String PREVIOUS_PATH = "previousPath";
        public static final String IGNORED = "ignored";
    }
}
