/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class ClusterMember {

    private long id;
    private String clusterId;
    private String clusterMemberName;
    private String clusterMemberIp;
    private ZonedDateTime clusterMemberTimestamp;
    private String remoteName;
    private String remoteUrl;
    private String authenticationType;
    private String remoteUsername;
    private String remotePassword;
    private String remoteToken;
    private String remotePrivateKey;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterMemberName() {
        return clusterMemberName;
    }

    public void setClusterMemberName(String clusterMemberName) {
        this.clusterMemberName = clusterMemberName;
    }

    public String getClusterMemberIp() {
        return clusterMemberIp;
    }

    public void setClusterMemberIp(String clusterMemberIp) {
        this.clusterMemberIp = clusterMemberIp;
    }

    public ZonedDateTime getClusterMemberTimestamp() {
        return clusterMemberTimestamp;
    }

    public void setClusterMemberTimestamp(ZonedDateTime clusterMemberTimestamp) {
        this.clusterMemberTimestamp = clusterMemberTimestamp;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    @JsonProperty("gitUrl")
    public String getRemoteUrl() {
        return remoteUrl;
    }

    @JsonProperty("gitUrl")
    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    @JsonProperty("gitAuthType")
    public String getAuthenticationType() {
        return authenticationType;
    }

    @JsonProperty("gitAuthType")
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    @JsonProperty("gitUsername")
    public String getRemoteUsername() {
        return remoteUsername;
    }

    @JsonProperty("gitUsername")
    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    @JsonProperty("gitPassword")
    public String getRemotePassword() {
        return remotePassword;
    }

    @JsonProperty("gitPassword")
    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    @JsonProperty("gitToken")
    public String getRemoteToken() {
        return remoteToken;
    }

    @JsonProperty("gitToken")
    public void setRemoteToken(String remoteToken) {
        this.remoteToken = remoteToken;
    }

    @JsonProperty("gitPrivateKey")
    public String getRemotePrivateKey() {
        return remotePrivateKey;
    }

    @JsonProperty("gitPrivateKey")
    public void setRemotePrivateKey(String remotePrivateKey) {
        this.remotePrivateKey = remotePrivateKey;
    }
}
