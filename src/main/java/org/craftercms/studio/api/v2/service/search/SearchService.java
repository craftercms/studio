/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.search;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;

/**
 * Provides access to OpenSearch for authoring indexes
 *
 * @author joseross
 */
public interface SearchService {
    /**
     * The default maximum number of terms for fuzzy queries expands to.
     */
    int DEFAULT_MAX_EXPANSIONS = 50;

    /**
     * Performs a search operation for a given site
     *
     * @param siteId        the id of the site
     * @param params        the parameters for the search
     * @param maxExpansions Specifies the maximum number of terms for fuzzy queries expands to.
     * @return the search results
     * @throws ServiceLayerException if there is any error executing the search in OpenSearch
     */
    SearchResult search(String siteId, SearchParams params, int maxExpansions) throws ServiceLayerException;

    /**
     * Performs a search operation for a given site
     *
     * @param siteId the id of the site
     * @param params the parameters for the search
     * @return the search results
     * @throws ServiceLayerException if there is any error executing the search in OpenSearch
     */
    default SearchResult search(String siteId, SearchParams params) throws ServiceLayerException {
        return search(siteId, params, DEFAULT_MAX_EXPANSIONS);
    }

}
