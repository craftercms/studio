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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.model.rest.Person;

import java.time.ZonedDateTime;

public class SandboxItem {

    private long id;
    private String label;
    private Long parentId;
    private String contentTypeId;
    private String path;
    private String previewUrl;
    private String systemType;
    private String mimeType;
    private long state;
    private Person lockOwner;
    private String localeCode;
    private Long translationSourceId;
    private Person creator;
    private ZonedDateTime dateCreated;
    private Person modifier;
    private ZonedDateTime dateModified;
    private Person submitter;
    private ZonedDateTime dateSubmitted;
    private String commitId;
    private long sizeInBytes;
    private long availableActions;
    private int childrenCount = 0;
    private String previousPath;

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

    public Person getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(Person lockOwner) {
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

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
        this.creator = creator;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Person getModifier() {
        return modifier;
    }

    public void setModifier(Person modifier) {
        this.modifier = modifier;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public ZonedDateTime getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(ZonedDateTime dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
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

    public long getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(long availableActions) {
        this.availableActions = availableActions;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    @JsonIgnore
    public String getPreviousPath() {
        return previousPath;
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    public static SandboxItem getInstance(Item item) {
        SandboxItem instance = new SandboxItem();

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
        instance.creator = item.getCreator();
        instance.dateCreated = item.getCreatedOn();
        instance.modifier = item.getModifier();
        instance.dateModified = item.getLastModifiedOn();
        instance.submitter = item.getSubmitter();
        instance.dateSubmitted = item.getSubmittedOn();
        instance.commitId = item.getCommitId();
        instance.sizeInBytes = item.getSize();
        instance.availableActions = item.getAvailableActions();
        instance.childrenCount = item.getChildrenCount();
        instance.previousPath = item.getPreviousPath();

        return instance;
    }
}
