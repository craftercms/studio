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

package org.craftercms.studio.api.v1.dal;

import java.time.ZonedDateTime;

public class SiteFeed {

    public static final String STATE_INITIALIZING = "INITIALIZING";
    public static final String STATE_READY = "READY";
    public static final String STATE_DELETED = "DELETED";

    protected long id;
    protected String siteUuid;
    protected String siteId;
    protected String name;
    protected String description;
    protected int deleted;
    protected String liveUrl;
    protected String lastCommitId;
    protected int publishingEnabled;
    protected String publishingStatus;
    protected String lastVerifiedGitlogCommitId;
    protected String sandboxBranch;
    protected int publishedRepoCreated;
    protected String publishingLockOwner;
    protected ZonedDateTime publishingLockHeartbeat;
    protected String state;
    protected String lastSyncedGitlogCommitId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSiteUuid() {
        return siteUuid;
    }

    public void setSiteUuid(String siteUuid) {
        this.siteUuid = siteUuid;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public int getPublishingEnabled() {
        return publishingEnabled;
    }

    public void setPublishingEnabled(int publishingEnabled) {
        this.publishingEnabled = publishingEnabled;
    }

    public String getPublishingStatus() {
        return publishingStatus;
    }

    public void setPublishingStatus(String publishingStatus) {
        this.publishingStatus = publishingStatus;
    }

    public String getLastVerifiedGitlogCommitId() {
        return lastVerifiedGitlogCommitId;
    }

    public void setLastVerifiedGitlogCommitId(String lastVerifiedGitlogCommitId) {
        this.lastVerifiedGitlogCommitId = lastVerifiedGitlogCommitId;
    }

    public String getSandboxBranch() {
        return sandboxBranch;
    }

    public void setSandboxBranch(String sandboxBranch) {
        this.sandboxBranch = sandboxBranch;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public boolean isSiteDeleted() {
        return deleted != 0;
    }

    public int getPublishedRepoCreated() {
        return publishedRepoCreated;
    }

    public void setPublishedRepoCreated(int publishedRepoCreated) {
        this.publishedRepoCreated = publishedRepoCreated;
    }

    public boolean isSitePublishedRepoCreated() {
        return publishedRepoCreated > 0;
    }

    public String getPublishingLockOwner() {
        return publishingLockOwner;
    }

    public void setPublishingLockOwner(String publishingLockOwner) {
        this.publishingLockOwner = publishingLockOwner;
    }

    public ZonedDateTime getPublishingLockHeartbeat() {
        return publishingLockHeartbeat;
    }

    public void setPublishingLockHeartbeat(ZonedDateTime publishingLockHeartbeat) {
        this.publishingLockHeartbeat = publishingLockHeartbeat;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLastSyncedGitlogCommitId() {
        return lastSyncedGitlogCommitId;
    }

    public void setLastSyncedGitlogCommitId(String lastSyncedGitlogCommitId) {
        this.lastSyncedGitlogCommitId = lastSyncedGitlogCommitId;
    }
}
