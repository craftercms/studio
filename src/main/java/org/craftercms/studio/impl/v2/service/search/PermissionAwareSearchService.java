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

package org.craftercms.studio.impl.v2.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.search.elasticsearch.ElasticsearchWrapper;
import org.craftercms.search.elasticsearch.impl.client.AbstractElasticsearchClientWrapper;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ElasticsearchWrapper} specific for authoring indexes
 * @author joseross
 */
public class PermissionAwareSearchService extends AbstractElasticsearchClientWrapper {

    /**
     * The suffix to append to the site name
     */
    protected String indexSuffix;

    /**
     * The name of the field to filter paths
     */
    protected String pathFieldName;

    @ConstructorProperties({"client", "indexSuffix", "pathFieldName"})
    public PermissionAwareSearchService(ElasticsearchClient client, String indexSuffix, String pathFieldName) {
        super(client);
        this.indexSuffix = indexSuffix;
        this.pathFieldName = pathFieldName;
    }

    /**
     * Perform a search operation for the given site
     * @param siteId the site id
     * @param allowedPaths the paths that should be included in the results
     * @param request the search request
     * @return the search response
     */
    public <T> SearchResponse<T> search(String siteId, List<String> allowedPaths, SearchRequest request,
                                        Class<T> documentClass) throws IOException {
        return super.search(request, documentClass, Map.of("siteId", siteId, "allowedPaths", allowedPaths));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RequestUpdates getRequestUpdates(SearchRequest request, Map<String, Object> parameters) {
        RequestUpdates updates = new RequestUpdates();
        String siteId = (String) parameters.get("siteId");
        List<String> allowedPaths = (List<String>) parameters.get("allowedPaths");

        //TODO: Implement locale in Studio too? for now just query all existing aliases
        updates.setIndex(List.of(siteId + indexSuffix + "*"));

        //TODO: Prevent running the search without allowedPaths
        if(CollectionUtils.isNotEmpty(allowedPaths)) {
            addFilters(request, allowedPaths, updates);
        }

        return updates;
    }

    protected void addFilters(SearchRequest request, List<String> allowedPaths, RequestUpdates updates) {
        Query originalQuery = request.query();
        BoolQuery.Builder updatedQuery = new BoolQuery.Builder();
        if(originalQuery.isBool()) {
            copyQuery(originalQuery.bool(),updatedQuery);
        } else {
            updatedQuery.must(originalQuery);
        }

        //TODO: Check if allowedPaths will be regexes already
        allowedPaths.forEach(path -> updatedQuery.filter(f -> f
            .regexp(r -> r
                .field(pathFieldName)
                .value(path + ".*")
            )
        ));

        updates.setQuery(updatedQuery.build()._toQuery());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> SearchResponse<T> search(SearchRequest request, Class<T> docClass, Map<String, Object> parameters)
            throws IOException {
        // Prevent execution of requests without permission filters
        throw new UnsupportedOperationException();
    }

}
