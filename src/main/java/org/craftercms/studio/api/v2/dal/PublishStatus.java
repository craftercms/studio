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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PublishStatus {

    public static final String READY = "ready";
    public static final String QUEUED = "queued";
    public static final String PROCESSING = "processing";
    public static final String PUBLISHING = "publishing";
    public static final String STOPPED = "stopped";
    public static final String ERROR = "error";

    @JsonIgnore
    private long id;
    private boolean enabled;
    @JsonIgnore
    private int enabledAsInt;
    private String status;
    private String lockOwner;
    private String lockTTL;

    private String publishingTarget;
    private String submissionId;
    private int numberOfItems;
    private int totalItems;

    private boolean published;

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.enabledAsInt = this.enabled? 1 : 0;
    }

    @JsonIgnore
    public int getEnabledAsInt() {
        return enabledAsInt;
    }

    @JsonIgnore
    public void setEnabledAsInt(int enabledAsInt) {
        this.enabledAsInt = enabledAsInt;
        this.enabled = enabledAsInt > 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getLockTTL() {
        return lockTTL;
    }

    public void setLockTTL(String lockTTL) {
        this.lockTTL = lockTTL;
    }

    public String getPublishingTarget() {
        return publishingTarget;
    }

    public void setPublishingTarget(String publishingTarget) {
        this.publishingTarget = publishingTarget;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
