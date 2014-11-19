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
package org.craftercms.cstudio.alfresco.dm.service.api;

import org.craftercms.cstudio.alfresco.service.api.SearchService;

/**
 * Provide methods for searching content in DM
 *
 * @author Dejan Brkic
 *
 */
public interface DmSearchService extends SearchService {

    /**
     * Is indexing required for the given site
     *
     * @param site
     * @return true if the last index time is older than the index life time
     */
    public boolean isIndexingRequired(String site);

    /**
     * synchronously index given site
     *
     * @param site
     */
    public void indexSite(String site);
}
