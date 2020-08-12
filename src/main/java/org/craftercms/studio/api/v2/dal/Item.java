/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import java.time.ZonedDateTime;

public class Item {
    private long id;
    private long siteId;
    private String siteName;
    private String path;
    private String previewUrl;
    private long state;
    private long ownedBy;
    private String owner;
    private long createdBy;
    private String creator;
    private ZonedDateTime createdOn;
    private long lastModifiedBy;
    private String modifier;
    private ZonedDateTime lastModifiedOn;
    private String label;
    private String contentTypeId;
    private String systemType;
    private String mimeType;
    private int disabledAsInt;
    private boolean disabled;
    private String localeCode;
    private long translationSourceId;
    private long size;
    private Long parentId = null;
    private String commitId;

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

    public long getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(long ownedBy) {
        this.ownedBy = ownedBy;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
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

    public long getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(long lastModifiedBy) {
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

    public long getTranslationSourceId() {
        return translationSourceId;
    }

    public void setTranslationSourceId(long translationSourceId) {
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

    public static Item cloneItem(Item item) {
        Item clone = new Item();
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
        return clone;
    }
}
