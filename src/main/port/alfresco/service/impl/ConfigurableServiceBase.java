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
package org.craftercms.cstudio.alfresco.service.impl;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.to.TimeStamped;

/**
 * A service base class that loads a configuration file and refreshes
 * configuration when the file is updated
 * 
 * @author hyanghee
 * 
 */
public abstract class ConfigurableServiceBase extends AbstractRegistrableService {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableServiceBase.class);

	/**
	 * the location where to find the configuration file
	 */
	protected String _configPath;

	/**
	 * configuration file name
	 */
	protected String _configFileName;

	
	/**
	 * check if configuration is updated and load the latest configuration if needed
	 * 
	 * @param key
	 */
	protected void checkForUpdate(final String key) {
		if (isConfigUpdated(key)) {
			loadConfiguration(key);
		}
	} 

	/**
	 * is the notification configuration updated?
	 * 
	 * @param key
	 * @return true if configuration hasn't been loaded or modified after loading
	 */
	protected boolean isConfigUpdated(final String key) {
		TimeStamped config = getConfiguration(key);
		if (config == null) {
			return true;
		} else {
			NodeRef configRef = getConfigRef(key);
            if (configRef != null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			    Serializable modifiedDateVal = persistenceManagerService.getProperty(configRef, ContentModel.PROP_MODIFIED);
                if (modifiedDateVal == null) return false;
                Date modifiedDate = (Date)modifiedDateVal;
			    return modifiedDate.after(config.getLastUpdated());
            } else {
                removeConfiguration(key);
                return true;
            }
		}
	}
	
	/**
	 * load configuration for the given key
	 * 
	 * @param key
	 */
	protected abstract void loadConfiguration(final String key);

	/**
	 * get configuration for the given key
	 * 
	 * @param key
	 * @return timpStamped configuration object
	 */
	protected abstract TimeStamped getConfiguration(final String key);

    /**
     * remove configuration for the given key
     *
     * @param key
     */
    protected abstract void removeConfiguration(final String key);



	/**
	 * find the configuration file node
	 * 
	 * @param key
	 * @return nodeRef
	 */
	protected abstract NodeRef getConfigRef(final String key);
	
	/**
	 * set configuration path
	 * 
	 * @param configPath
	 */
	public void setConfigPath(String configPath) {
		this._configPath = configPath;
	}

	/**
	 * set configuration file name
	 * 
	 * @param configFileName
	 */
	public void setConfigFileName(String configFileName) {
		this._configFileName = configFileName;
	}

}
