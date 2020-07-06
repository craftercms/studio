/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.search.elasticsearch.ElasticsearchWrapper;
import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Implementation of {@link ElasticsearchWrapper} specific for authoring indexes
 * @author joseross
 */
public class PermissionAwareSearchService extends AbstractElasticsearchWrapper {

    /**
     * The suffix to append to the site name
     */
    protected String indexSuffix;

    /**
     * The name of the field to filter paths
     */
    protected String pathFieldName;

    public PermissionAwareSearchService(RestHighLevelClient client, String indexSuffix, String pathFieldName) {
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
     * @throws IOException if there is an error connecting to Elasticsearch
     */
    public SearchResponse search(String siteId, List<String> allowedPaths, SearchRequest request) throws IOException {
        return search(siteId, allowedPaths, request, RequestOptions.DEFAULT);
    }

    /**
     * Perform a search operation for the given site
     * @param siteId the site id
     * @param allowedPaths the paths that should be included in the results
     * @param request the search request
     * @param options the request options
     * @return the search response
     * @throws IOException if there is an error connecting to Elasticsearch
     */
    public SearchResponse search(String siteId, List<String> allowedPaths, SearchRequest request,
                                 RequestOptions options) throws IOException {
        //TODO: Implement locale in Studio too? for now just query all existing aliases
        request.indices(siteId + indexSuffix + "*");

        //TODO: Prevent running the search without allowedPaths
        if(CollectionUtils.isNotEmpty(allowedPaths)) {
            updateFilters(request, allowedPaths);
        }

        return client.search(request, options);
    }

    protected void updateFilters(SearchRequest request, List<String> allowedPaths) {
        QueryBuilder query = request.source().query();
        BoolQueryBuilder boolQuery;
        if(query instanceof BoolQueryBuilder) {
            boolQuery = (BoolQueryBuilder) query;
        } else {
            boolQuery = QueryBuilders.boolQuery().must(query);
        }

        //TODO: Check if allowedPaths will be regexes already
        allowedPaths.forEach(path -> boolQuery.filter(QueryBuilders.regexpQuery(pathFieldName, path + ".*")));

        request.source().query(boolQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateIndex(final SearchRequest request) {
        // do nothing, this method will not be used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResponse search(final SearchRequest request, final RequestOptions options) {
        // Prevent execution of requests without permission filters
        throw new UnsupportedOperationException();
    }

}
