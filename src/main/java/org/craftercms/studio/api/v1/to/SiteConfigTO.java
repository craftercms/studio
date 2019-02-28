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


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * This class stores Site Configuration
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class SiteConfigTO implements Serializable {

	/**
	 *
	 */
	protected static final long serialVersionUID = 3411780412457597813L;

    /**
     * WEM project name
     */
    protected String wemProject;

    /**
     * site name
     */
    protected String siteName = null;

	/**
	 * site display name
	 */
	protected String name = null;

	/** the last updated date of site configuration **/
	protected ZonedDateTime lastUpdated = null;

    /** web project configuration if the site is dm-based **/
    protected RepositoryConfigTO repositoryConfig = null;

	/** default timezone **/
	protected String timezone = null;

	protected boolean stagingEnvironmentEnabled;

	/** staging environment **/
	protected String stagingEnvironment;

	/** live environment **/
	protected String liveEnvironment;

    /** sandbox branch **/
    protected String sandboxBranch = null;

    /**
     * Configuration for the range facets in search
     */
    protected Map<String, FacetTO> facets;

    /**
     * @return the WEM project
     */
    public String getWemProject() {
        return wemProject;
    }

    public void setWemProject(String wemProject) {
        this.wemProject = wemProject;
    }

    public String getSiteName() {
        return siteName;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

    public RepositoryConfigTO getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(RepositoryConfigTO repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTimezone() {
		return timezone;
	}

    public String getSandboxBranch() {
        return sandboxBranch;
    }

    public void setSandboxBranch(String sandboxBranch) {
        this.sandboxBranch = sandboxBranch;
    }

    public String getStagingEnvironment() {
        return stagingEnvironment;
    }

    public void setStagingEnvironment(String stagingEnvironment) {
        this.stagingEnvironment = stagingEnvironment;
    }

    public String getLiveEnvironment() {
        return liveEnvironment;
    }

    public void setLiveEnvironment(String liveEnvironment) {
        this.liveEnvironment = liveEnvironment;
    }

    public boolean isStagingEnvironmentEnabled() {
        return stagingEnvironmentEnabled;
    }

    public void setStagingEnvironmentEnabled(boolean stagingEnvironmentEnabled) {
        this.stagingEnvironmentEnabled = stagingEnvironmentEnabled;
    }

    public Map<String, FacetTO> getFacets() {
        return facets;
    }

    public void setFacets(final Map<String, FacetTO> facets) {
        this.facets = facets;
    }

}
