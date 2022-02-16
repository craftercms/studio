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

package org.craftercms.studio.api.v2.service.search.internal;

import java.util.List;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;

/**
 * Provides access to Elasticsearch for authoring indexes
 * @author joseross
 */
public interface SearchServiceInternal {

    /**
     * Performs a search operation for a given site
     * @param siteId the site id
     * @param allowedPaths list of paths that should be included in the results
     * @param params the search parameters
     * @return the search results
     * @throws ServiceLayerException if there is any error executing the search in Elasticsearch
     */
    SearchResult search(String siteId, List<String> allowedPaths, SearchParams params) throws ServiceLayerException;

}
