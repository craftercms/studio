/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.service.search;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;

/**
 * Provides access to ElasticSearch for authoring indexes
 * @author joseross
 */
public interface SearchService {

    /**
     * Performs a search operation for a given site
     * @param siteId the id of the site
     * @param params the parameters for the search
     * @return the search results
     * @throws AuthenticationException if there is an error checking the current user
     * @throws ServiceLayerException if there is any error executing the search in ElasticSearch
     */
    SearchResult search(String siteId, SearchParams params) throws AuthenticationException, ServiceLayerException;

}
