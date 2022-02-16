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

import java.time.ZonedDateTime;

public class ClusterMember {

    public enum State {
        REGISTRATION_INCOMPLETE,
        ACTIVE,
        INACTIVE
    }

    private long id;
    private String localAddress;
    private State state;
    private ZonedDateTime heartbeat;
    private String gitUrl;
    private String gitRemoteName;
    private String gitAuthType;
    private String gitUsername;
    private String gitPassword;
    private String gitToken;
    private String gitPrivateKey;
    private int available;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ZonedDateTime getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(ZonedDateTime heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitRemoteName() {
        return gitRemoteName;
    }

    public void setGitRemoteName(String gitRemoteName) {
        this.gitRemoteName = gitRemoteName;
    }

    public String getGitAuthType() {
        return gitAuthType;
    }

    public void setGitAuthType(String gitAuthType) {
        this.gitAuthType = gitAuthType;
    }

    @JsonIgnore
    public String getGitUsername() {
        return gitUsername;
    }

    @JsonIgnore
    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    @JsonIgnore
    public String getGitPassword() {
        return gitPassword;
    }

    @JsonIgnore
    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    @JsonIgnore
    public String getGitToken() {
        return gitToken;
    }

    @JsonIgnore
    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }

    @JsonIgnore
    public String getGitPrivateKey() {
        return gitPrivateKey;
    }

    @JsonIgnore
    public void setGitPrivateKey(String gitPrivateKey) {
        this.gitPrivateKey = gitPrivateKey;
    }

    @JsonIgnore
    public int getAvailable() {
        return available;
    }

    @JsonIgnore
    public void setAvailable(int available) {
        this.available = available;
    }
}
