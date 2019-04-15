/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

public class EnvironmentConfigTO implements TimeStamped,Serializable {

    private static final long serialVersionUID = -8009424857607808612L;
    /** preview server URL **/
	protected String previewServerUrl = "";
	protected String previewEngineServerUrl = StringUtils.EMPTY;
    protected String graphqlServerUrl = StringUtils.EMPTY;
	/** authoring server URL **/
	protected String authoringServerUrl = "";
	/** live server url **/
	protected String liveServerUrl = "";
	/** admin email address **/
	protected String adminEmailAddress;
	/** the last updated date of this configuration **/
	protected ZonedDateTime lastUpdated;
    /** publisihng targets configuration **/
    protected List<PublishingTargetTO> publishingTargets = new ArrayList<PublishingTargetTO>();

	protected boolean openDropdown = false;

    protected String previewDeploymentEndpoint = null;


	public String getAdminEmailAddress() {
		return adminEmailAddress;
	}

	public void setAdminEmailAddress(String adminEmailAddress) {
		this.adminEmailAddress = adminEmailAddress;
	}

    public boolean getOpenDropdown() {
		return openDropdown;
	}

	public void setOpenDropdown(boolean openDropdown) {
		this.openDropdown = openDropdown;
	}

    public String getLiveServerUrl() {
		return liveServerUrl;
	}

	public void setLiveServerUrl(String liveServerUrl) {
		this.liveServerUrl = liveServerUrl;
	}

	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @param previewServerUrl the previewServerUrl to set
	 */
	public void setPreviewServerUrl(String previewServerUrl) {
		this.previewServerUrl = previewServerUrl;
	}

	/**
	 * @return the previewServerUrl
	 */
	public String getPreviewServerUrl() {
		return previewServerUrl;
	}

    public String getPreviewEngineServerUrl() {
        return previewEngineServerUrl;
    }

    public void setPreviewEngineServerUrl(String previewEngineServerUrl) {
        this.previewEngineServerUrl = previewEngineServerUrl;
    }

    public String getGraphqlServerUrl() {
        return graphqlServerUrl;
    }

    public void setGraphqlServerUrl(String graphqlServerUrl) {
        this.graphqlServerUrl = graphqlServerUrl;
    }

    /**
	 * @param authoringServerUrl the authoringServerUrl to set
	 */
	public void setAuthoringServerUrl(String authoringServerUrl) {
		this.authoringServerUrl = authoringServerUrl;
	}

	/**
	 * @return the authoringServerUrl
	 */
	public String getAuthoringServerUrl() {
		return authoringServerUrl;
	}


    public List<PublishingTargetTO> getPublishingTargets() { return publishingTargets; }
    public void setPublishingTargets(List<PublishingTargetTO> publishingTargets) { this.publishingTargets = publishingTargets; }

    public String getPreviewDeploymentEndpoint() {
        return previewDeploymentEndpoint;
    }

    public void setPreviewDeploymentEndpoint(String previewDeploymentEndpoint) {
        this.previewDeploymentEndpoint = previewDeploymentEndpoint;
    }
}
