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
package org.craftercms.cstudio.alfresco.util.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.craftercms.cstudio.alfresco.util.api.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImportServiceBase extends AbstractRegistrableService implements ImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceBase.class);
    
	/**
     * repository configuration file name
     * 
     */
    protected String _configFileName;

    /*
     * (non-Javadoc)
     * @see org.craftercms.cstudio.alfresco.util.api.ImportService#ImportDmRepository(boolean, java.lang.String)
     */
    public void runImport(boolean importFromFilePath, String buildDataLocation) { 
    	String filePath = buildDataLocation + "/" + _configFileName;
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("[IMPORTSERVICE] loading import configuration at " + filePath);
    	}
        InputStream in = null;
        try {
    		in = this.getResourceStream(filePath, importFromFilePath);
    		if (in != null) {
	            SAXReader reader = new SAXReader();
	            Document document = reader.read(in);
	            setupRepository(document, importFromFilePath, buildDataLocation);
    		}
        } catch (Exception e) {
            LOGGER.error("[IMPORTSERVICE] Failed to load the repository configuration file: " + filePath, e);
        } finally {
            ContentUtils.release(in);
        }
    }
    
    /**
     * set up the repository 
     * 
     * @param document
     * @param importFromFilePath
     * @param buildDataLocation
     * @return
     * @throws Exception
     */
    protected abstract void setupRepository(Document document, boolean importFromFilePath, String buildDataLocation) throws Exception;
    
    /**
     * get a resource stream of the given file
     *  
     * @param filePath
     * @param importFromFilePath
     * @return
     */
    protected InputStream getResourceStream(String filePath, boolean importFromFilePath) {
        InputStream in = null;
        if (importFromFilePath) {
            try {
                in = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                LOGGER.error("[IMPORTSERVICE] File does not exist at " + filePath);
            }
        } else {
            in = getClass().getClassLoader().getResourceAsStream(filePath);
        }
        return in;
    }

    /**
     * get the resource url for import
     * 
     * @param filePath
     * @param importFromFilePath
     * @return
     */
    protected URL getResourceUrl(String filePath, boolean importFromFilePath) {
        URL resourceUrl;
        if(importFromFilePath) {
            try {
                resourceUrl = new File(filePath).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Not able to find " + filePath);
            }
        } else {
            resourceUrl = getClass().getClassLoader().getResource(filePath);
        }
        return resourceUrl;
    }

    
	/**
	 * @return the configFileName
	 */
	public String getConfigFileName() {
		return _configFileName;
	}

	/**
	 * @param configFileName the configFileName to set
	 */
	public void setConfigFileName(String configFileName) {
		this._configFileName = configFileName;
	}


}
