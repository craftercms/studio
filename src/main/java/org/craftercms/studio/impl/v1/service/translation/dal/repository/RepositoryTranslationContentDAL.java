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
package org.craftercms.studio.impl.v1.service.translation.dal.repository;

import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.impl.v1.service.translation.dal.AbstractTranslationContentDAL;

/**
 * get source content/save transalted content from and to a content repository implementation
 * @author russdanner
 */
public class RepositoryTranslationContentDAL extends AbstractTranslationContentDAL {

	/**
	 * @return true if target has sourcePath in project
	 */
	protected boolean isSourcePathInTarget(String targetSite, String sourcePath) {
		return _contentService.contentExists(targetSite, sourcePath);
	}

	/**
	 * retrieve the source content
	 */
	public InputStream getContent(String site, String path) throws ContentNotFoundException {
		return _contentService.getContent(site, path);
	}

	/**
	 * update site with translated content and update linking
	 */
	public void updateSiteWithTranslatedContent(String site, String path, InputStream content) throws ServiceException {
		
		_contentService.writeContent(site, path, content);
	}
	
	/** getter for content repository */
	public ContentService getContentService() { return _contentService; }
	/** setter for content repository */
	public void setContentService(ContentService service) { _contentService = service; }
	
	protected ContentService _contentService;
}
