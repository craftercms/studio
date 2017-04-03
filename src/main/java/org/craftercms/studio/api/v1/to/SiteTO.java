/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
	protected String site;
	/** live url **/
	protected String liveUrl;
	/** authoring url **/
	protected String authoringUrl;
	/** preview url **/
	protected String previewUrl;
	/** environment value **/
	protected String environment;
	/** admin email **/
	protected String adminEmail;
	/** admin email **/
	protected boolean openSiteDropdown;

    protected Map<String, DeploymentEndpointConfigTO> deploymentEndpointConfigs = new HashMap<String, DeploymentEndpointConfigTO>();
	
	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}
	/**
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}
	/**
	 * @return the openSiteDropdown flag
	 */
	public boolean getOpenSiteDropdown() {
		return openSiteDropdown;
	}
	/**
	 * @param openSiteDropdown flag to set
	 */
	public void setOpenSiteDropdown(boolean openSiteDropdown) {
		this.openSiteDropdown = openSiteDropdown;
	}

	/**
	 * @return the authoringUrl
	 */
	public String getAuthoringUrl() {
		return authoringUrl;
	}
	/**
	 * @param authoringUrl the authoringUrl to set
	 */
	public void setAuthoringUrl(String authoringUrl) {
		this.authoringUrl = authoringUrl;
	}

	/**
	 * @return the previewUrl
	 */
	public String getPreviewUrl() {
		return previewUrl;
	}
	/**
	 * @param previewUrl the previewUrl to set
	 */
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return environment;
	}
	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * @return the adminEmail
	 */
	public String getAdminEmail() {
		return adminEmail;
	}
	/**
	 * @param adminEmail the adminEmail to set
	 */
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	/**
	 * @return the liveUrl
	 */
	public String getLiveUrl() {
		return liveUrl;
	}
	/**
	 * @param liveUrl the liveUrl to set
	 */
	public void setLiveUrl(String liveUrl) {
		this.liveUrl = liveUrl;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return site + ":" + environment;
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

    public Map<String, DeploymentEndpointConfigTO> getDeploymentEndpointConfigs() {
        return deploymentEndpointConfigs;
    }

    public void setDeploymentEndpointConfigs(Map<String, DeploymentEndpointConfigTO> deploymentEndpointConfigs) {
        this.deploymentEndpointConfigs = deploymentEndpointConfigs;
    }
}
