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
package org.craftercms.studio.api.v1.to;


import java.io.Serializable;
import java.time.ZonedDateTime;

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

	/** staging environment **/
	protected String stagingEnvironment;

	/** live environment **/
	protected String liveEnvironment;

    /**
     * @return the WEM project
     */
    public String getWemProject() {
        return wemProject;
    }

    /**
     * @param wemProject
     *            the WEM project to set
     */
    public void setWemProject(String wemProject) {
        this.wemProject = wemProject;
    }

    /**
     * @return the name
     */
    public String getSiteName() {
        return siteName;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param lastUpdated
	 *            the lastUpdated to set
	 */
	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the lastUpdated
	 */
	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

    /**
     * @return the repositoryConfig
     */
    public RepositoryConfigTO getRepositoryConfig() {
        return repositoryConfig;
    }

    /**
     * @param repositoryConfig the webProjectConfig to set
     */
    public void setRepositoryConfig(RepositoryConfigTO repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
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
}
