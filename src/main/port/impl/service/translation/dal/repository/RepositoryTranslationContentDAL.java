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
package org.craftercms.cstudio.impl.service.translation.dal.repository;

import java.io.InputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.craftercms.cstudio.api.repository.*;
import org.craftercms.cstudio.api.service.translation.*;
import org.craftercms.cstudio.impl.service.translation.dal.*;

/**
 * get source content/save transalted content from and to a content repository implementation
 * @author russdanner
 */
public class RepositoryTranslationContentDAL extends AbstractTranslationContentDAL {

	/**
	 * @return true if target has sourcePath in project
	 */
	protected boolean isSourcePathInTarget(String targetSite, String sourcePath) {
		return _contentRepository.contentExists(targetSite, sourcePath);
	}

	/**
	 * retrieve the source content
	 */
	public InputStream getContent(String site, String path) {
		return _contentRepository.getContent(site, site, "work-area", path);
	}

	/**
	 * update site with translated content and update linking
	 */
	public void updateSiteWithTranslatedContent(String site, String path, InputStream content) {
		
		_contentRepository.writeContent(site, site, "work-area", path, content);
	}
	
	/** getter for content repository */
	public ContentRepository getContentRepository() { return _contentRepository; }
	/** setter for content repository */
	public void setContentRepository(ContentRepository repo) { _contentRepository = repo; }
	
	protected ContentRepository _contentRepository;
}