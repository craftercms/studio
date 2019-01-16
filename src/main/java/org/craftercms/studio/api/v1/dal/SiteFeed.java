/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

public class SiteFeed {

    protected long id;
    protected String siteUuid;
    protected String siteId;
    protected String name;
    protected String description;
    protected String status;
    protected int deleted;
    protected String liveUrl;
    protected String lastCommitId;
    protected int publishingEnabled;
    protected String publishingStatusMessage;
    protected String lastVerifiedGitlogCommitId;
    protected String sandboxBranch;
    protected String searchEngine;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPublishingStatusMessage() {
        return publishingStatusMessage;
    }

    public void setPublishingStatusMessage(String publishingStatusMessage) {
        this.publishingStatusMessage = publishingStatusMessage;
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

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
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
}
