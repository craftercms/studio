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

public class ClusterSiteRecord {

    private long clusterNodeId;
    private String clusterNodeLocalAddress;
    private long siteId;
    private String siteIdString;
    private String nodeLastCommitId;
    private String nodeLastVerifiedGitlogCommitId;
    private String state;
    private int publishedRepoCreated;

    public long getClusterNodeId() {
        return clusterNodeId;
    }

    public void setClusterNodeId(long clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    public String getClusterNodeLocalAddress() {
        return clusterNodeLocalAddress;
    }

    public void setClusterNodeLocalAddress(String clusterNodeLocalAddress) {
        this.clusterNodeLocalAddress = clusterNodeLocalAddress;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public String getSiteIdString() {
        return siteIdString;
    }

    public void setSiteIdString(String siteIdString) {
        this.siteIdString = siteIdString;
    }

    public String getNodeLastCommitId() {
        return nodeLastCommitId;
    }

    public void setNodeLastCommitId(String nodeLastCommitId) {
        this.nodeLastCommitId = nodeLastCommitId;
    }

    public String getNodeLastVerifiedGitlogCommitId() {
        return nodeLastVerifiedGitlogCommitId;
    }

    public void setNodeLastVerifiedGitlogCommitId(String nodeLastVerifiedGitlogCommitId) {
        this.nodeLastVerifiedGitlogCommitId = nodeLastVerifiedGitlogCommitId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPublishedRepoCreated() {
        return publishedRepoCreated;
    }

    public void setPublishedRepoCreated(int publishedRepoCreated) {
        this.publishedRepoCreated = publishedRepoCreated;
    }
}
