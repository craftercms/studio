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

import java.io.Serializable;

public class RemoteRepository implements Serializable {

    private static final long serialVersionUID = -5031083831374591061L;

    private long id;
    private String siteId;
    private String remoteName;
    private String remoteUrl;
    private String authenticationType;
    private String remoteUsername;
    private String remotePassword;
    private String remoteToken;
    private String remotePrivateKey;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSiteId() { return siteId; }
    public void setSiteId(String siteId) { this.siteId = siteId; }

    public String getRemoteName() { return remoteName; }
    public void setRemoteName(String remoteName) { this.remoteName = remoteName; }

    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }

    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }

    public String getRemoteUsername() { return remoteUsername; }
    public void setRemoteUsername(String remoteUsername) { this.remoteUsername = remoteUsername; }

    public String getRemotePassword() { return remotePassword; }
    public void setRemotePassword(String remotePassword) { this.remotePassword = remotePassword; }

    public String getRemoteToken() { return remoteToken; }
    public void setRemoteToken(String remoteToken) { this.remoteToken = remoteToken; }

    public String getRemotePrivateKey() { return remotePrivateKey; }
    public void setRemotePrivateKey(String remotePrivateKey) { this.remotePrivateKey = remotePrivateKey; }

}
