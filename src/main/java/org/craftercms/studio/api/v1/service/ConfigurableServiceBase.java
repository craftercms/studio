/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.service;

import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.to.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


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
	protected String configPath;

	/**
	 * configuration file name
	 */
	protected String configFileName;

	
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
		TimeStamped config = getConfigurationById(key);
		if (config == null) {
		 	return true;
		} else {
            String siteConfigFullPath =  getConfigFullPath(key);
            if (contentRepository.contentExists(siteConfigFullPath)) {
                Date modifiedDate = contentRepository.getModifiedDate(siteConfigFullPath);
                if (modifiedDate == null) {
                    return false;
                } else {
                    return modifiedDate.after(config.getLastUpdated());
                }
            } else {
                removeConfiguration(key);
                return true;
            }
		 }
	}

    protected abstract String getConfigFullPath(String key);

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
	protected abstract TimeStamped getConfigurationById(final String key);

    /**
     * remove configuration for the given key
     *
     * @param key
     */
    protected abstract void removeConfiguration(final String key);
	
	/**
	 * set configuration path
	 * 
	 * @param configPath
	 */
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	/**
	 * set configuration file name
	 * 
	 * @param configFileName
	 */
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    protected ContentRepository contentRepository;
}
