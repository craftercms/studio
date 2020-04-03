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

package org.craftercms.studio.api.v1.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class ItemMetadata implements Serializable {
    private static final long serialVersionUID = 8859492095343542092L;

    public static final String PROP_SITE = "site";
    public static final String PROP_PATH = "path";
    public static final String PROP_NEW_PATH = "newPath";
    public static final String PROP_NAME = "name";
    public static final String PROP_MODIFIER = "modifier";
    public static final String PROP_OWNER = "owner";
    public static final String PROP_CREATOR = "creator";
    public static final String PROP_FIRST_NAME = "firstName";
    public static final String PROP_LAST_NAME = "lastName";
    public static final String PROP_LOCK_OWNER = "lockOwner";
    public static final String PROP_EMAIL = "email";
    public static final String PROP_RENAMED = "renamed";
    public static final String PROP_OLD_URL = "oldUrl";
    public static final String PROP_DELETE_URL = "deleteUrl";
    public static final String PROP_IMAGE_WIDTH = "imageWidth";
    public static final String PROP_IMAGE_HEIGHT = "imageHeight";
    public static final String PROP_APPROVED_BY = "approvedBy";
    public static final String PROP_SUBMITTED_BY = "submittedBy";
    public static final String PROP_SUBMITTED_FOR_DELETION = "submittedForDeletion";
    public static final String PROP_SEND_EMAIL = "sendEmail";
    public static final String PROP_SUBMISSION_COMMENT = "submissionComment";
    public static final String PROP_LAUNCH_DATE = "launchDate";
    public static final String PROP_COMMIT_ID = "commitId";
    public static final String PROP_SUBMITTED_TO_ENVIRONMENT = "submittedToEnvironment";

    public static final String PROP_LABEL = "label";
    public static final String PROP_CONTENT_TYPE_ID = "contentTypeId";
    public static final String PROP_PREVIEW_URL = "previewUrl";
    public static final String PROP_SYSTEM_TYPE = "systemType";
    public static final String PROP_MIME_TYPE = "mimeType";
    public static final String PROP_STATE = "state";
    public static final String PROP_DISABLED = "disabled";
    public static final String PROP_LOCALE_CODE = "localeCode";
    public static final String PROP_TRANSLATION_SOURCE_ID = "translationSourceId";
    public static final String PROP_CREATED_DATE = "createdDate";
    public static final String PROP_LAST_MODIFIED_DATE = "lastModifiedDate";
    public static final String PROP_SIZE_IN_BYTES = "sizeInBytes";


    protected int id;
    protected String site;
    protected String path;
    protected String name;
    protected ZonedDateTime lastModifiedDate;
    protected String modifier;
    protected String owner;
    protected String creator;
    protected String firstName;
    protected String lastName;
    protected String lockOwner;
    protected String email;
    protected int renamed;
    protected String oldUrl;
    protected String deleteUrl;
    protected int imageWidth;
    protected int imageHeight;
    protected String approvedBy;
    protected String submittedBy;
    protected int submittedForDeletion;
    protected int sendEmail;
    protected String submissionComment;
    protected ZonedDateTime launchDate;
    protected String commitId;
    protected String submittedToEnvironment;

    private String label;
    private String contentTypeId;
    private String previewUrl;
    private String systemType;
    private String mimeType;
    private int state;
    private boolean disabled;
    private String localeCode;
    private String translationSourceId;
    private ZonedDateTime createdDate;
    private long sizeInBytes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRenamed() {
        return renamed;
    }

    public void setRenamed(int renamed) {
        this.renamed = renamed;
    }

    public String getOldUrl() {
        return oldUrl;
    }

    public void setOldUrl(String oldUrl) {
        this.oldUrl = oldUrl;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public int getSubmittedForDeletion() {
        return submittedForDeletion;
    }

    public void setSubmittedForDeletion(int submittedForDeletion) {
        this.submittedForDeletion = submittedForDeletion;
    }

    public int getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(int sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getSubmissionComment() {
        return submissionComment;
    }

    public void setSubmissionComment(String submissionComment) {
        this.submissionComment = submissionComment;
    }

    public ZonedDateTime getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(ZonedDateTime launchDate) {
        this.launchDate = launchDate;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(final String commitId) {
        this.commitId = commitId;
    }

    public String getSubmittedToEnvironment() {
        return submittedToEnvironment;
    }

    public void setSubmittedToEnvironment(String submittedToEnvironment) {
        this.submittedToEnvironment = submittedToEnvironment;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @JsonProperty("disabled")
    public boolean isDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getDisabledAsInt() {
        return disabled ? 1 : 0;
    }

    public void setDisabledAsInt(int disabled) {
        this.disabled = disabled > 0;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getTranslationSourceId() {
        return translationSourceId;
    }

    public void setTranslationSourceId(String translationSourceId) {
        this.translationSourceId = translationSourceId;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
