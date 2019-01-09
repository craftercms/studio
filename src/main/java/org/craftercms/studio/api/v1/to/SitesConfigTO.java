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
import java.util.HashMap;
import java.util.Map;

/**
 * contains overall sites configuration
 *
 * @author hyanghee
 *
 */
public class SitesConfigTO implements TimeStamped, Serializable {

	/** the sites drop-down menu items **/
	protected Map<String, String> _sitesMenu;
	/** the preset keys and site types mapping **/
	protected Map<String, String> _siteTypes;
	/**  the site names and repository types mapping **/
	protected Map<String, String> _repositoryTypes;
	/** the site names and site types mapping **/
	protected Map<String, String> _siteInstanceTypes = new HashMap<String, String>();
	/** the path to where all sites are stored **/
	protected String _sitesLocation = "";
	/** last updated date **/
	protected ZonedDateTime _lastUpdated;

	public ZonedDateTime getLastUpdated() {
		return this._lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @param sitesMenu the sitesMenu to set
	 */
	public void setSitesMenu(Map<String, String> sitesMenu) {
		this._sitesMenu = sitesMenu;
	}

	/**
	 * @return the sitesMenu
	 */
	public Map<String, String> getSitesMenu() {
		return _sitesMenu;
	}

	/**
	 * @param siteTypes the siteTypes to set
	 */
	public void setSiteTypes(Map<String, String> siteTypes) {
		this._siteTypes = siteTypes;
	}

	/**
	 * @return the siteTypes
	 */
	public Map<String, String> getSiteTypes() {
		return _siteTypes;
	}

	/**
	 * @param siteInstanceTypes the siteInstanceTypes to set
	 */
	public void setSiteInstanceTypes(Map<String, String> siteInstanceTypes) {
		this._siteInstanceTypes = siteInstanceTypes;
	}

	/**
	 * @return the siteInstanceTypes
	 */
	public Map<String, String> getSiteInstanceTypes() {
		return _siteInstanceTypes;
	}

	/**
	 * @param sitesLocation the sitesLocation to set
	 */
	public void setSitesLocation(String sitesLocation) {
		this._sitesLocation = sitesLocation;
	}

	/**
	 * @return the sitesLocation
	 */
	public String getSitesLocation() {
		return _sitesLocation;
	}

	/**
	 * @param repositoryTypes the repositoryTypes to set
	 */
	public void setRepositoryTypes(Map<String, String> repositoryTypes) {
		this._repositoryTypes = repositoryTypes;
	}

	/**
	 * @return the repositoryTypes
	 */
	public Map<String, String> getRepositoryTypes() {
		return _repositoryTypes;
	}

	/**
	 * get the site type of the given site preset
	 * @param preset
	 * @return site type
	 */
	public String getSiteType(String preset) {
		if (_siteTypes != null) {
			return _siteTypes.get(preset);
		} else {
			return null;
		}
	}

	/**
	 * get the repository type of the give site
	 *
	 * @param site
	 * @return the repository type
	 */
	public String getRepositoryType(String site) {
		if (_repositoryTypes != null) {
			return _repositoryTypes.get(site);
		} else {
			return null;
		}
	}

	/**
	 * get the site type of the given site instance
	 *
	 * @param site
	 * @return site type
	 */
	public String getSiteInstanceType(String site) {
		return _siteInstanceTypes.get(site);
	}

	/**
	 * add an entry of site instance and site type
	 *
	 * @param site
	 * @param siteType
	 */
	public void addSiteInstanceType(String site, String siteType) {
		_siteInstanceTypes.put(site, siteType);
	}
}
