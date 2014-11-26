/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.service.deployment;

import java.util.ArrayList;
import java.util.List;

//implementation detail
// document
public class PublishingTargetItem {	

	// default
    private static final String VERSION_URL = "http://localhost:9191/api/1/version";

    public String getId() { return _id; }
    public void setId(String id) { this._id = id; }

    public String getName() { return _name; }
    public void setName(String name) { this._name = name; }

    public String getType() { return _type; }
    public void setType(String type) { this._type = type; }

    public String getServerUrl() { return _serverUrl; }
    public void setServerUrl(String serverUrl) { this._serverUrl = serverUrl; }

    public String getStatusUrl() { return _statusUrl; }
    public void setStatusUrl(String statusUrl) { this._statusUrl = statusUrl; }

    public String getVersionUrl() { return _versionUrl; }
    public void setVersionUrl(String versionUrl) { this._versionUrl = versionUrl; }

    public String getPassword() { return _password; }
    public void setPassword(String password) { this._password = password; }

    public String getTarget() { return _target; }
    public void setTarget(String target) { this._target = target; }

    public boolean isSendMetadata() { return _sendMetadata; }
    public void setSendMetadata(boolean sendMetadata) { this._sendMetadata = sendMetadata; }

    public List<String> getExcludePattern() { return _excludePattern; }
    public void setExcludePattern(List<String> excludePattern) { this._excludePattern = excludePattern; }

    public List<String> getIncludePattern() { return _includePattern; }
    public void setIncludePattern(List<String> includePattern) { this._includePattern = includePattern; }

    public int getBucketSize() { return _bucketSize; }
    public void setBucketSize(int bucketSize) { this._bucketSize = bucketSize; }

    public int getChunkSize() { return _chunkSize; }
    public void setChunkSize(int chunkSize) { this._chunkSize = chunkSize; }

    public void addEnvironment(String environment) {
        this._environments.add(environment);
    }

    public void removeEnvironment(String environment) {
        this._environments.remove(environment);
    }

    public List<String> getEnvironments() {
        return _environments;
    }

    public void setEnvironments(List<String> environments) {
        this._environments = environments;
    }

    public String getSiteId() { return _siteId; }
    public void setSiteId(String siteId) { this._siteId = siteId; }

    protected String _id;
    protected String _name;
    protected String _type;
    protected String _serverUrl;
    protected String _statusUrl;
    protected String _versionUrl = VERSION_URL;
    protected String _password;
    protected String _target;
    protected boolean _sendMetadata;
    protected List<String> _excludePattern;
    protected List<String> _includePattern;
    protected int _bucketSize;
    protected String _siteId;

    /** for future use */
    protected int _chunkSize;

    protected List<String> _environments = new ArrayList<String>();
}
