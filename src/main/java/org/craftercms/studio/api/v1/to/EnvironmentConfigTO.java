/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.to;

import java.util.*;

public class EnvironmentConfigTO implements TimeStamped {

	/** preview server URL **/
	protected String _previewServerUrl = "";
	/** authoring server URL **/
	protected String _authoringServerUrl = "";
	/** preview server URL **/
	protected String _previewServerUrlPattern = "";
	/** authoring server URL **/
	protected String _authoringServerUrlPattern = "";
	/** form server URL **/
	protected String _formServerUrlPattern = "";
	/** live server url **/
	protected String _liveServerUrl = "";
	/** admin email address **/
	protected String _adminEmailAddress;
	/** cookie domain **/
	protected String _cookieDomain;
	/** the last updated date of this configuration **/
	protected Date _lastUpdated;
    /** publisihng channels configuration **/
    protected Map<String, PublishingChannelGroupConfigTO> _publishingChannelGroupConfigs = new HashMap<String, PublishingChannelGroupConfigTO>();

	protected boolean _openDropdown = false;

    protected PublishingChannelGroupConfigTO _liveEnvironmentPublishingGroup = null;

    protected String _previewDeploymentEndpoint = null;

	
	public String getAdminEmailAddress() {
		return _adminEmailAddress;
	}

	public void setAdminEmailAddress(String adminEmailAddress) {
		this._adminEmailAddress = adminEmailAddress;
	}

    public boolean getOpenDropdown() {
		return _openDropdown;
	}

	public void setOpenDropdown(boolean openDropdown) {
		this._openDropdown = openDropdown;
	}

    public String getLiveServerUrl() {
		return _liveServerUrl;
	}

	public void setLiveServerUrl(String liveServerUrl) {
		this._liveServerUrl = liveServerUrl;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.to.TimeStamped#getLastUpdated()
	 */
	public Date getLastUpdated() {
		return _lastUpdated;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.to.TimeStamped#setLastUpdated(java.util.Date)
	 */
	public void setLastUpdated(Date lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @param previewServerUrl the previewServerUrl to set
	 */
	public void setPreviewServerUrl(String previewServerUrl) {
		this._previewServerUrl = previewServerUrl;
	}

	/**
	 * @return the previewServerUrl
	 */
	public String getPreviewServerUrl() {
		return _previewServerUrl;
	}

	/**
	 * @param authoringServerUrl the authoringServerUrl to set
	 */
	public void setAuthoringServerUrl(String authoringServerUrl) {
		this._authoringServerUrl = authoringServerUrl;
	}

	/**
	 * @return the authoringServerUrl
	 */
	public String getAuthoringServerUrl() {
		return _authoringServerUrl;
	}
	
	/**
	 * @return the orbeonServerUrl
	 */
	public String getFormServerUrlPattern() {
		return _formServerUrlPattern;
	}
	
	/**
	 * Sets the orbeonServerUrl
	 */
	public void setFormServerUrlPattern(String formUrl) {
		_formServerUrlPattern = formUrl;
	}

	/**
	 * @return the cookieDomain
	 */
	public String getCookieDomain() {
		return _cookieDomain;
	}

	/**
	 * @param cookieDomain the cookieDomain to set
	 */
	public void setCookieDomain(String cookieDomain) {
		this._cookieDomain = cookieDomain;
	}

	/**
	 * @return the previewServerUrlPattern
	 */
	public String getPreviewServerUrlPattern() {
		return _previewServerUrlPattern;
	}

	/**
	 * @param previewServerUrlPattern the previewServerUrlPattern to set
	 */
	public void setPreviewServerUrlPattern(String previewServerUrlPattern) {
		this._previewServerUrlPattern = previewServerUrlPattern;
	}

	/**
	 * @return the authoringServerUrlPattern
	 */
	public String getAuthoringServerUrlPattern() {
		return _authoringServerUrlPattern;
	}

	/**
	 * @param authoringServerUrlPattern the authoringServerUrlPattern to set
	 */
	public void setAuthoringServerUrlPattern(String authoringServerUrlPattern) {
		this._authoringServerUrlPattern = authoringServerUrlPattern;
	}

    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs() {
        return _publishingChannelGroupConfigs;
    }

    public void setPublishingChannelGroupConfigs(Map<String, PublishingChannelGroupConfigTO> publishingChannelGroupConfigs) {
        this._publishingChannelGroupConfigs = publishingChannelGroupConfigs;
    }

    public PublishingChannelGroupConfigTO getLiveEnvironmentPublishingGroup() {
        return _liveEnvironmentPublishingGroup;
    }

    public void setLiveEnvironmentPublishingGroup(PublishingChannelGroupConfigTO liveEnvironmentPublishingGroup) {
        this._liveEnvironmentPublishingGroup = liveEnvironmentPublishingGroup;
    }

    public String getPreviewDeploymentEndpoint() {
        return _previewDeploymentEndpoint;
    }

    public void setPreviewDeploymentEndpoint(String previewDeploymentEndpoint) {
        this._previewDeploymentEndpoint = previewDeploymentEndpoint;
    }
}
