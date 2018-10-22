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

public class Cluster {

    String remoteName;
    String remoteUsername;
    String remotePassword;
    String remoteToken;
    String remotePrivateKey;
    String remoteUrl;
    String authenticationType;

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    public String getRemoteToken() {
        return remoteToken;
    }

    public void setRemoteToken(String remoteToken) {
        this.remoteToken = remoteToken;
    }

    public String getRemotePrivateKey() {
        return remotePrivateKey;
    }

    public void setRemotePrivateKey(String remotePrivateKey) {
        this.remotePrivateKey = remotePrivateKey;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
}
