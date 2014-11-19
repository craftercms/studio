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


public class SiteContentTypePathsTO implements TimeStamped {

	/** content type path configuration **/
	protected List<ContentTypePathTO> _configs = null;
	/** last updated date of this configuration **/
	protected Date _lastUpdated = null;
	
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
	 * @return the configs
	 */
	public List<ContentTypePathTO> getConfigs() {
		return _configs;
	}

	/**
	 * @param configs the configs to set
	 */
	public void setConfigs(List<ContentTypePathTO> configs) {
		this._configs = configs;
	}

}
