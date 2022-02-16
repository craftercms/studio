/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.service.site.dal;

import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteConfigNotFoundException;
import org.craftercms.studio.impl.v1.service.site.AbstractSiteServiceDAL;
import org.dom4j.Document;


/**
 * use content repository as data access layer for site configuration
 * @author russdanner
 */
public class RepositorySiteServiceDAL extends AbstractSiteServiceDAL {

	private String configFilePath = "/site-config.xml";

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	/**
	 * given a site ID return the configuration as a document
	 * This method allows extensions to add additional properties to the configuration that
	 * are not made available through the site configuration object
	 * @param site the name of the site
	 * @return a Document containing the entire site configuration
	 */
	public Document getSiteConfiguration(String site)
	throws SiteConfigNotFoundException {
		Document retConfigDoc = null;
		
		try {
			retConfigDoc = _contentService.getContentAsDocument(site, configFilePath);
		}
		catch(Exception err) {
			throw new SiteConfigNotFoundException();
		}
		
		return retConfigDoc;
	}

	/** getter for content service */
	public ContentService getContentService() { return _contentService; }
	/** setter for content service */
	public void setContentService(ContentService service) { _contentService = service; }
	
	protected ContentService _contentService;
}
