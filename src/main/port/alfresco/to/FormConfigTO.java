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
/**
 * 
 */
package org.craftercms.cstudio.alfresco.to;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * This class store widget configuration
 * 
 * @author Sweta Chalasani
 *
 */
public class FormConfigTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -8210688745885714879L;

	Map<String, WidgetConfigMappingTO> _widgetsMapping = null;
	
	WidgetConfigMappingTO _defaultWidgetConfig = null;
	
	/** the last updated date of site configuration **/
	protected Date _lastUpdated = null;

	/**
	 * @return the widgetsMapping
	 */
	public Map<String, WidgetConfigMappingTO> getWidgetsMapping() {
		return _widgetsMapping;
	}

	/**
	 * @param widgetsMapping the widgetsMapping to set
	 */
	public void setWidgetsMapping(Map<String, WidgetConfigMappingTO> widgetsMapping) {
		this._widgetsMapping = widgetsMapping;
	}

	/**
	 * @return the defaultWidgetConfig
	 */
	public WidgetConfigMappingTO getDefaultWidgetConfig() {
		return _defaultWidgetConfig;
	}

	/**
	 * @param defaultWidgetConfig the defaultWidgetConfig to set
	 */
	public void setDefaultWidgetConfig(WidgetConfigMappingTO defaultWidgetConfig) {
		this._defaultWidgetConfig = defaultWidgetConfig;
	}

	/**
	 * @param lastUpdated
	 *            the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return _lastUpdated;
	}
}
