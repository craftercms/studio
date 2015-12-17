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


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SiteTO carries the configuration for a given share site There is some overlap
 * with the SiteConfigTO. These will need to be resolved
 */
public class SiteTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5106269319021769281L;
	
	/** site id **/
	protected String _site;
	/** wcm web project **/
	protected String _webProject;
	/** collaborative sandbox **/
	protected String _collabSandbox;
	/** live url **/
	protected String _liveUrl;
	/** authoring url **/
	protected String _authoringUrl;
	/** form server url **/
	protected String _formServerUrl;
	/** preview url **/
	protected String _previewUrl;
	/** authoring url **/
	protected String _authoringUrlPattern;
	/** preview url **/
	protected String _previewUrlPattern;
	/** cookie domain **/
	protected String _cookieDomain;
	/** environment value **/
	protected String _environment;
	/** admin email **/
	protected String _adminEmail;
	/** admin email **/
	protected boolean _openSiteDropdown;
    /** repository root path **/
    protected String _repositoryRootPath;
    /** publisihng channels group configuration **/
    protected Map<String, PublishingChannelGroupConfigTO> _publishingChannelGroupConfigs = new HashMap<String, PublishingChannelGroupConfigTO>();

    protected Map<String, DeploymentEndpointConfigTO> deploymentEndpointConfigs = new HashMap<String, DeploymentEndpointConfigTO>();
	
	/**
	 * @return the site
	 */
	public String getSite() {
		return _site;
	}
	/**
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this._site = site;
	}
	/**
	 * @return the openSiteDropdown flag
	 */
	public boolean getOpenSiteDropdown() {
		return _openSiteDropdown;
	}
	/**
	 * @param openSiteDropdown flag to set
	 */
	public void setOpenSiteDropdown(boolean openSiteDropdown) {
		this._openSiteDropdown = openSiteDropdown;
	}

	/**
	 * @return the webProject
	 */
	public String getWebProject() {
		return _webProject;
	}
	/**
	 * @param webProject the webProject to set
	 */
	public void setWebProject(String webProject) {
		this._webProject = webProject;
	}
	/**
	 * @return the collabSandbox
	 */
	public String getCollabSandbox() {
		return _collabSandbox;
	}
	/**
	 * @param collabSandbox the collabSandbox to set
	 */
	public void setCollabSandbox(String collabSandbox) {
		this._collabSandbox = collabSandbox;
	}
	/**
	 * @return the authoringUrl
	 */
	public String getAuthoringUrl() {
		return _authoringUrl;
	}
	/**
	 * @param authoringUrl the authoringUrl to set
	 */
	public void setAuthoringUrl(String authoringUrl) {
		this._authoringUrl = authoringUrl;
	}
	/**
	 * @return the authoringUrl
	 */
	public String getFormServerUrl() {
		return _formServerUrl;
	}
	/**
	 * @param formServerUrl the authoringUrl to set
	 */
	public void setFormServerUrl(String formServerUrl) {
		this._formServerUrl = formServerUrl;
	}
	/**
	 * @return the previewUrl
	 */
	public String getPreviewUrl() {
		return _previewUrl;
	}
	/**
	 * @param previewUrl the previewUrl to set
	 */
	public void setPreviewUrl(String previewUrl) {
		this._previewUrl = previewUrl;
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
	 * @return the environment
	 */
	public String getEnvironment() {
		return _environment;
	}
	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(String environment) {
		this._environment = environment;
	}

	/**
	 * @return the adminEmail
	 */
	public String getAdminEmail() {
		return _adminEmail;
	}
	/**
	 * @param adminEmail the adminEmail to set
	 */
	public void setAdminEmail(String adminEmail) {
		this._adminEmail = adminEmail;
	}

	/**
	 * @return the liveUrl
	 */
	public String getLiveUrl() {
		return _liveUrl;
	}
	/**
	 * @param liveUrl the liveUrl to set
	 */
	public void setLiveUrl(String liveUrl) {
		this._liveUrl = liveUrl;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return _site + ":" + _environment;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object target) {
		if (this == target) return true;
		if (!(target instanceof SiteTO)) return false;
		SiteTO targetSite = (SiteTO) target;
		return this.toString().equals(targetSite.toString());
	}
	/**
	 * @return the authoringUrlPattern
	 */
	public String getAuthoringUrlPattern() {
		return _authoringUrlPattern;
	}
	/**
	 * @param authoringUrlPattern the authoringUrlPattern to set
	 */
	public void setAuthoringUrlPattern(String authoringUrlPattern) {
		this._authoringUrlPattern = authoringUrlPattern;
	}
	/**
	 * @return the previewUrlPattern
	 */
	public String getPreviewUrlPattern() {
		return _previewUrlPattern;
	}
	/**
	 * @param previewUrlPattern the previewUrlPattern to set
	 */
	public void setPreviewUrlPattern(String previewUrlPattern) {
		this._previewUrlPattern = previewUrlPattern;
	}

    public String getRepositoryRootPath() {
        return _repositoryRootPath;
    }

    public void setRepositoryRootPath(String repositoryRootPath) {
        this._repositoryRootPath = repositoryRootPath;
    }

    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs() {
        return _publishingChannelGroupConfigs;
    }

    public void setPublishingChannelGroupConfigs(Map<String, PublishingChannelGroupConfigTO> publishingChannelGroupConfigs) {
        this._publishingChannelGroupConfigs = publishingChannelGroupConfigs;
    }

    public Map<String, DeploymentEndpointConfigTO> getDeploymentEndpointConfigs() {
        return deploymentEndpointConfigs;
    }

    public void setDeploymentEndpointConfigs(Map<String, DeploymentEndpointConfigTO> deploymentEndpointConfigs) {
        this.deploymentEndpointConfigs = deploymentEndpointConfigs;
    }
}
