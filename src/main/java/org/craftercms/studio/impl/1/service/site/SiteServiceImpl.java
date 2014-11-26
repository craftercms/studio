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
package org.craftercms.cstudio.impl.service.site;

import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.dom4j.Document;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.  
 * This service provides access to site configuration
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService {
	
	/**
	 * given a site ID return the configuration as a document
	 * This method allows extensions to add additional properties to the configuration that
	 * are not made available through the site configuration object
	 * @param site the name of the site
	 * @return a Document containing the entire site configuration
	 */
	public Document getSiteConfiguration(String site) 
	throws SiteConfigNotFoundException {
		return _siteServiceDAL.getSiteConfiguration(site);
	}

	/** getter site service dal */
	public SiteServiceDAL getSiteService() { return _siteServiceDAL; }
	/** setter site service dal */
	public void setSiteServiceDAL(SiteServiceDAL service) { _siteServiceDAL = service; }

	protected SiteServiceDAL _siteServiceDAL;
}
