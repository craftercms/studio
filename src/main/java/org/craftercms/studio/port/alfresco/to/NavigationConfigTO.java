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
package org.craftercms.cstudio.alfresco.to;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * contains site navigation configuration
 * 
 * @author hyanghee
 *
 */
public class NavigationConfigTO implements TimeStamped {

	/** site **/
	protected String _site;
	/** navigation menu item mapping **/
	protected Map<String, NavMenuTO> _menus;

    protected Map<String, List<NavigationRootMenuTo>> rootMenus;

	/** last updated date **/
	protected Date _lastUpdated;
	
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
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this._site = site;
	}


	/**
	 * @return the site
	 */
	public String getSite() {
		return _site;
	}


	/**
	 * @param menus the menus to set
	 */
	public void setMenus(Map<String, NavMenuTO> menus) {
		this._menus = menus;
	}


	/**
	 * @return the menus
	 */
	public Map<String, NavMenuTO> getMenus() {
		return _menus;
	}

    public Map<String, List<NavigationRootMenuTo>> getRootMenus() {
        return rootMenus;
    }

    public void setRootMenus(Map<String, List<NavigationRootMenuTo>> rootMenus) {
        this.rootMenus = rootMenus;
    }
}
