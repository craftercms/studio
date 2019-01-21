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

package org.craftercms.studio.impl.v2.service.search.internal;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.craftercms.studio.api.v2.service.search.internal.SearchServiceInternal;
import org.craftercms.studio.impl.v2.service.search.PermissionAwareSearchService;
import org.craftercms.studio.model.search.SearchItem;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author joseross
 */
public class SearchServiceInternalImpl implements SearchServiceInternal {

    protected String pathFieldName;

    protected PermissionAwareSearchService elasticSearchService;

    @Required
    public void setPathFieldName(final String pathFieldName) {
        this.pathFieldName = pathFieldName;
    }

    @Required
    public void setElasticSearchService(final PermissionAwareSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    protected SearchItem processHit(SearchHit hit) {
        SearchItem item = new SearchItem();
        item.setId(hit.field(pathFieldName).getValue());
        return item;
    }

    @Override
    public SearchResult search(final String siteId, final List<String> allowedPaths, final SearchParams params)
        throws IOException {

        BoolQueryBuilder query = QueryBuilders.boolQuery()
            .must(QueryBuilders.multiMatchQuery(params.getQuery(), pathFieldName));

        SearchSourceBuilder builder = new SearchSourceBuilder()
            .query(query)
            .from(params.getOffset())
            .size(params.getLimit())
            .sort(params.getSort().getField(), SortOrder.valueOf(params.getSort().getOrder().name().toLowerCase()))
            .highlighter(SearchSourceBuilder.highlight());

        SearchRequest request = new SearchRequest()
            .source(builder);

        SearchResponse response = elasticSearchService.search(siteId, allowedPaths, request);

        SearchResult result = new SearchResult();
        result.setTotal(response.getHits().getTotalHits());

        List<SearchItem> items = Stream.of(response.getHits().getHits())
            .map(this::processHit)
            .collect(Collectors.toList());

        result.setItems(items);

        return result;
    }


}
