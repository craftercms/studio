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
package org.craftercms.cstudio.alfresco.util.api;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.SearchColumnTO;

public interface SearchResultExtractor {

	/**
	 * extract search result items from the given set of node references
	 * 
	 * @param site
	 * @param nodeRefs
	 * @param columns
	 * @param ascending 
	 * @param sort 
	 * @param pageSize 
	 * @param page 
	 * @return search result items
	 * @throws ServiceException 
	 */
	List extract(String site, List<NodeRef> nodeRefs, List<SearchColumnTO> columns, String sort, boolean ascending,
			int page, int pageSize) throws ServiceException;

}
