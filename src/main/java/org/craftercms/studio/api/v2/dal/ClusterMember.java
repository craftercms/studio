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

public class ClusterMember {

    public enum State {
        REGISTRATION_INCOMPLETE,
        ACTIVE
    }

    private long id;
    private String localIp;
    private State state;
    private String gitUrl;
    private String gitRemoteName;
    private String gitAuthType;
    private String gitUsername;
    private String gitPassword;
    private String gitToken;
    private String gitPrivateKey;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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

    public String getGitUsername() {
        return gitUsername;
    }

    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getGitToken() {
        return gitToken;
    }

    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }

    public String getGitPrivateKey() {
        return gitPrivateKey;
    }

    public void setGitPrivateKey(String gitPrivateKey) {
        this.gitPrivateKey = gitPrivateKey;
    }
}
