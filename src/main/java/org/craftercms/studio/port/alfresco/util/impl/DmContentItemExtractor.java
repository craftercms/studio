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

import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.util.api.ContentItemExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DmContentItemExtractor implements ContentItemExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DmContentItemExtractor.class);

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /*
    * (non-Javadoc)
    * @see org.craftercms.cstudio.alfresco.util.api.ContentItemExtractor#extractContent(java.lang.String, java.lang.String)
    */
	public Serializable extractContent(String site, String id) {
        if (!StringUtils.isEmpty(id)) {
            try {
                PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
                ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
                String fullPath = servicesConfig.getRepositoryRootPath(site) + id;
                return persistenceManagerService.getContentItem(fullPath, false);
            } catch (ContentNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.error(id + " is not found in " + site);
                }
            }catch (ServiceException e) {
                logger.error(id + " is not found in " + site);
            }
        } else {
            logger.error("cotnent path must be provided.");
        }
        return null;
	}

}
