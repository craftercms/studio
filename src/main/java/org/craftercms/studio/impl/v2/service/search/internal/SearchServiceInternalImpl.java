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
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.search.internal.SearchServiceInternal;
import org.craftercms.studio.impl.v2.service.search.PermissionAwareSearchService;
import org.craftercms.studio.model.search.SearchFacet;
import org.craftercms.studio.model.search.SearchItem;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author joseross
 */
public class SearchServiceInternalImpl implements SearchServiceInternal {

    public static final String CONFIG_KEY_FACETS_FIELDS = "studio.search.facets.fields";
    public static final String CONFIG_KEY_FACETS_RANGES = "studio.search.facets.ranges";
    public static final String CONFIG_KEY_TYPES = "studio.search.types";

    public static final String CONFIG_KEY_FACET_NAME = "name";
    public static final String CONFIG_KEY_FACET_FIELD = "field";
    public static final String CONFIG_KEY_FACET_OFFSET = "offset";
    public static final String CONFIG_KEY_FACET_INTERVAL = "interval";

    public static final String CONFIG_KEY_TYPE_FIELD = CONFIG_KEY_FACET_FIELD;
    public static final String CONFIG_KEY_TYPE_NAME = CONFIG_KEY_FACET_NAME;
    public static final String CONFIG_KEY_TYPE_MATCHES = "matches";

    protected String pathFieldName;
    protected String lastEditFieldName;
    protected String lastEditorFieldName;
    protected String sizeFieldName;

    protected String[] searchFields;
    protected String[] highlightFields;

    protected int snippetSize;
    protected int numberOfSnippets;

    protected String defaultType;

    protected PermissionAwareSearchService elasticSearchService;

    protected StudioConfiguration studioConfiguration;

    protected List<AggregationBuilder> aggregations;

    protected Map<String, HierarchicalConfiguration<ImmutableNode>> facets;
    protected Map<String, HierarchicalConfiguration<ImmutableNode>> types;

    @Required
    public void setPathFieldName(final String pathFieldName) {
        this.pathFieldName = pathFieldName;
    }

    @Required
    public void setLastEditFieldName(final String lastEditFieldName) {
        this.lastEditFieldName = lastEditFieldName;
    }

    @Required
    public void setLastEditorFieldName(final String lastEditorFieldName) {
        this.lastEditorFieldName = lastEditorFieldName;
    }

    @Required
    public void setSizeFieldName(final String sizeFieldName) {
        this.sizeFieldName = sizeFieldName;
    }

    @Required
    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    @Required
    public void setSearchFields(final String[] searchFields) {
        this.searchFields = searchFields;
    }

    @Required
    public void setHighlightFields(final String[] highlightFields) {
        this.highlightFields = highlightFields;
    }

    @Required
    public void setSnippetSize(final int snippetSize) {
        this.snippetSize = snippetSize;
    }

    @Required
    public void setNumberOfSnippets(final int numberOfSnippets) {
        this.numberOfSnippets = numberOfSnippets;
    }

    @Required
    public void setElasticSearchService(final PermissionAwareSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void init() {
        loadFacetsFromConfiguration(studioConfiguration);
        loadTypesFromConfiguration(studioConfiguration);
    }

    protected void loadFacetsFromConfiguration(final StudioConfiguration studioConfiguration) {
        facets = new HashMap<>();
        aggregations = new LinkedList<>();

        List<HierarchicalConfiguration<ImmutableNode>> facetsConfig =
            studioConfiguration.getSubConfigs(CONFIG_KEY_FACETS_FIELDS);

        if(CollectionUtils.isNotEmpty(facetsConfig)) {
            facetsConfig.forEach(facet -> {
                facets.put(facet.getString(CONFIG_KEY_FACET_NAME), facet);

                aggregations.add(AggregationBuilders
                    .terms(facet.getString(CONFIG_KEY_FACET_NAME))
                    .field(facet.getString(CONFIG_KEY_FACET_FIELD))
                    .minDocCount(1));
            });
        }

        facetsConfig = studioConfiguration.getSubConfigs(CONFIG_KEY_FACETS_RANGES);

        if(CollectionUtils.isNotEmpty(facetsConfig)) {
            facetsConfig.forEach(facet -> {
                facets.put(facet.getString(CONFIG_KEY_FACET_NAME), facet);

                aggregations.add(AggregationBuilders
                    .histogram(facet.getString(CONFIG_KEY_FACET_NAME))
                    .field(facet.getString(CONFIG_KEY_FACET_FIELD))
                    .offset(facet.getDouble(CONFIG_KEY_FACET_OFFSET))
                    .interval(facet.getDouble(CONFIG_KEY_FACET_INTERVAL))
                    .minDocCount(1)
                    .keyed(true));
            });
        }
    }

    protected void loadTypesFromConfiguration(final StudioConfiguration studioConfiguration) {
        types = new HashMap<>();

        List<HierarchicalConfiguration<ImmutableNode>> typesConfig =
            studioConfiguration.getSubConfigs(CONFIG_KEY_TYPES);

        typesConfig.forEach(type -> types.put(type.getString(CONFIG_KEY_TYPE_NAME), type));

    }

    //TODO: Implement highlights
    protected SearchItem processHit(Map<String, Object> source, Map<String, HighlightField> highlights) {
        SearchItem item = new SearchItem();
        item.setPath((String) source.get(pathFieldName));
        item.setLastModified(Instant.parse((String) source.get(lastEditFieldName)));
        item.setLastModifier(source.get(lastEditorFieldName).toString());
        item.setSize(Long.parseLong(source.get(sizeFieldName).toString()));
        item.setType(getItemType(source));
        item.setSnippets(getItemSnippets(highlights));
        return item;
    }

    @Override
    public SearchResult search(final String siteId, final List<String> allowedPaths, final SearchParams params)
        throws IOException {

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if(StringUtils.isNotEmpty(params.getQuery())) {
            query.should(QueryBuilders.regexpQuery(pathFieldName, ".*" + params.getQuery() + ".*"));
            query.should(QueryBuilders.multiMatchQuery(params.getQuery(), searchFields));
        }

        if(MapUtils.isNotEmpty(params.getFilters())) {
            params.getFilters().forEach((filter, value) -> {
                if(facets.containsKey(filter)) {
                    HierarchicalConfiguration<ImmutableNode> facetConfig = facets.get(filter);
                    if(facetConfig.containsKey(CONFIG_KEY_FACET_INTERVAL)) {
                        query.filter(QueryBuilders
                            .rangeQuery(facetConfig.getString(CONFIG_KEY_FACET_FIELD))
                            .gte(value)
                            .lt(((Number)value).doubleValue() + facetConfig.getDouble(CONFIG_KEY_FACET_INTERVAL)));
                    } else {
                        query.filter(QueryBuilders.matchQuery(facetConfig.getString(CONFIG_KEY_FACET_FIELD), value));
                    }
                }
            });
        }

        SearchSourceBuilder builder = new SearchSourceBuilder()
            .query(query)
            .from(params.getOffset())
            .size(params.getLimit())
            .sort(params.getSortBy(), SortOrder.valueOf(params.getSortOrder()));

        if(ArrayUtils.isNotEmpty(highlightFields)) {
            HighlightBuilder highlight = SearchSourceBuilder.highlight();
            for (String field : highlightFields) {
                highlight.field(field, snippetSize, numberOfSnippets);
            }
            builder.highlighter(highlight);
        }

        aggregations.forEach(builder::aggregation);

        SearchRequest request = new SearchRequest()
            .source(builder);

        SearchResponse response = elasticSearchService.search(siteId, allowedPaths, request);

        SearchResult result = new SearchResult();
        result.setTotal(response.getHits().getTotalHits());

        List<SearchItem> items = Stream.of(response.getHits().getHits())
            .map(hit -> processHit(hit.getSourceAsMap(), hit.getHighlightFields()))
            .collect(Collectors.toList());

        result.setItems(items);

        result.setFacets(processAggregations(response));

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<SearchFacet> processAggregations(final SearchResponse response) {
        List<SearchFacet> facets = new LinkedList<>();
        Aggregations aggregations = response.getAggregations();
        if(aggregations != null) {
            aggregations.getAsMap().forEach((name, aggregation) -> {
                SearchFacet facet = new SearchFacet();
                facet.setName(name);
                Map values = new TreeMap();
                if(aggregation instanceof Terms) {
                    Terms terms = (Terms) aggregation;
                    terms.getBuckets().forEach(bucket -> values.put(bucket.getKey(), bucket.getDocCount()));


                } else if(aggregation instanceof Histogram) {
                    Histogram histogram = (Histogram) aggregation;
                    histogram.getBuckets().forEach(bucket -> values.put(bucket.getKey(), bucket.getDocCount()));
                }
                if(MapUtils.isNotEmpty(values)) {
                    facet.setValues(values);
                    facets.add(facet);
                }
            });
        }
        return facets;
    }

    protected String getItemType(Map<String, Object> source) {
        if(MapUtils.isNotEmpty(types)) {
            for (HierarchicalConfiguration<ImmutableNode> typeConfig : types.values()) {
                String fieldName = typeConfig.getString(CONFIG_KEY_TYPE_FIELD);
                if(source.containsKey(fieldName)) {
                    String fieldValue = source.get(fieldName).toString();
                    if (StringUtils.isNotEmpty(fieldValue) &&
                            fieldValue.matches(typeConfig.getString(CONFIG_KEY_TYPE_MATCHES))) {
                        return typeConfig.getString(CONFIG_KEY_TYPE_NAME);
                    }
                }
            }
        }
        return defaultType;
    }

    protected List<String> getItemSnippets(Map<String, HighlightField> highlights) {
        if(MapUtils.isNotEmpty(highlights)) {
            List<String> snippets = new LinkedList<>();
            highlights.values().forEach(highlight -> {
                for(Text text : highlight.getFragments()) {
                    snippets.add(text.string());
                }
            });
            return snippets;
        }
        return null;
    }

}
