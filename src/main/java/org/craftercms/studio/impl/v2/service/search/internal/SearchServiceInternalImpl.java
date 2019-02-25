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
import java.util.LinkedHashMap;
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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.search.internal.SearchServiceInternal;
import org.craftercms.studio.impl.v2.service.search.PermissionAwareSearchService;
import org.craftercms.studio.model.search.SearchFacet;
import org.craftercms.studio.model.search.SearchFacetRange;
import org.craftercms.studio.model.search.SearchResultItem;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link SearchServiceInternal}
 * @author joseross
 */
public class SearchServiceInternalImpl implements SearchServiceInternal {

    public static final String CONFIG_KEY_FACETS_FIELDS = "studio.search.facets.fields";
    public static final String CONFIG_KEY_FACETS_RANGES = "studio.search.facets.ranges";
    public static final String CONFIG_KEY_TYPES = "studio.search.types";

    public static final String CONFIG_KEY_FACET_NAME = "name";
    public static final String CONFIG_KEY_FACET_FIELD = "field";
    public static final String CONFIG_KEY_FACET_RANGE = "ranges";
    public static final String CONFIG_KEY_FACET_RANGE_FROM = "from";
    public static final String CONFIG_KEY_FACET_RANGE_TO = "to";

    public static final String CONFIG_KEY_TYPE_FIELD = CONFIG_KEY_FACET_FIELD;
    public static final String CONFIG_KEY_TYPE_NAME = CONFIG_KEY_FACET_NAME;
    public static final String CONFIG_KEY_TYPE_MATCHES = "matches";

    public static final String FACET_RANGE_MIN = "min";
    public static final String FACET_RANGE_MAX = "max";

    public static final String DEFAULT_MIME_TYPE = "application/xml";

    /**
     * Name of the field for paths
     */
    protected String pathFieldName;

    /**
     * Name of the field for internal name
     */
    protected String internalNameFieldName;

    /**
     * Name of the field for last edit date
     */
    protected String lastEditFieldName;

    /**
     * Name of the field for last edit user
     */
    protected String lastEditorFieldName;

    /**
     * Name of the field for size
     */
    protected String sizeFieldName;

    /**
     * Name of the field for mimeType
     */
    protected String mimeTypeName;

    /**
     * List of fields to include during searching
     */
    protected String[] searchFields;

    /**
     * List of fields to include during highlighting
     */
    protected String[] highlightFields;

    /**
     * Number of characters to include for snippets
     */
    protected int snippetSize;

    /**
     * Number of snippets to generate for each file
     */
    protected int numberOfSnippets;

    /**
     * Default label used for unknown file types
     */
    protected String defaultType;

    /**
     * The ElasticSearch service
     */
    protected PermissionAwareSearchService elasticSearchService;

    /**
     * The Studio configuration
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * List of {@link AggregationBuilder} to include in all searches
     */
    protected List<AggregationBuilder> aggregations;

    /**
     * Configurations for facets
     */
    protected Map<String, HierarchicalConfiguration<ImmutableNode>> facets;

    /**
     * Configurations for types
     */
    protected Map<String, HierarchicalConfiguration<ImmutableNode>> types;

    @Required
    public void setPathFieldName(final String pathFieldName) {
        this.pathFieldName = pathFieldName;
    }

    @Required
    public void setInternalNameFieldName(final String internalNameFieldName) {
        this.internalNameFieldName = internalNameFieldName;
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
    public void setMimeTypeName(final String mimeTypeName) {
        this.mimeTypeName = mimeTypeName;
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

    /**
     * Loads facets & type mapping from the configuration
     */
    public void init() {
        loadFacetsFromConfiguration(studioConfiguration);
        loadTypesFromConfiguration(studioConfiguration);
    }

    /**
     * Loads the facets from the given configuration
     * @param studioConfiguration the studio configuration
     */
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
                    .minDocCount(1)
                    .size(1000));
            });
        }

        facetsConfig = studioConfiguration.getSubConfigs(CONFIG_KEY_FACETS_RANGES);

        if(CollectionUtils.isNotEmpty(facetsConfig)) {
            facetsConfig.forEach(facet -> {
                facets.put(facet.getString(CONFIG_KEY_FACET_NAME), facet);

                RangeAggregationBuilder rangeAggregation = AggregationBuilders
                    .range(facet.getString(CONFIG_KEY_FACET_NAME))
                    .field(facet.getString(CONFIG_KEY_FACET_FIELD))
                    .keyed(true);

                for(HierarchicalConfiguration<ImmutableNode> range :
                    facet.configurationsAt(CONFIG_KEY_FACET_RANGE)) {
                    if(range.containsKey(CONFIG_KEY_FACET_RANGE_TO) && range.containsKey(CONFIG_KEY_FACET_RANGE_FROM)) {
                        rangeAggregation.addRange(range.getDouble(CONFIG_KEY_FACET_RANGE_FROM),
                            range.getDouble(CONFIG_KEY_FACET_RANGE_TO));
                    } else if(range.containsKey(CONFIG_KEY_FACET_RANGE_FROM)) {
                        rangeAggregation.addUnboundedFrom(range.getDouble(CONFIG_KEY_FACET_RANGE_FROM));
                    } else {
                        rangeAggregation.addUnboundedTo(range.getDouble(CONFIG_KEY_FACET_RANGE_TO));
                    }

                }

                aggregations.add(rangeAggregation);
            });
        }
    }

    /**
     * Loads the type mapping from the given configuration
     * @param studioConfiguration the studio configuration
     */
    protected void loadTypesFromConfiguration(final StudioConfiguration studioConfiguration) {
        types = new HashMap<>();

        List<HierarchicalConfiguration<ImmutableNode>> typesConfig =
            studioConfiguration.getSubConfigs(CONFIG_KEY_TYPES);

        typesConfig.forEach(type -> types.put(type.getString(CONFIG_KEY_TYPE_NAME), type));

    }

    /**
     * Maps the information from ElasticSearch for a single {@link SearchResultItem}
     * @param source the fields returned by ElasticSearch
     * @param highlights the highlights returned by ElasticSearch
     * @return the search item object
     */
    //TODO: Implement previewUrl for supported types
    protected SearchResultItem processSearchHit(Map<String, Object> source, Map<String, HighlightField> highlights) {
        SearchResultItem item = new SearchResultItem();
        item.setPath((String) source.get(pathFieldName));
        item.setName((String) source.get(internalNameFieldName));
        item.setLastModified(Instant.parse((String) source.get(lastEditFieldName)));
        item.setLastModifier(source.get(lastEditorFieldName).toString());
        item.setSize(Long.parseLong(source.get(sizeFieldName).toString()));
        item.setType(getItemType(source));
        item.setMimeType(getMimeType(source));
        item.setSnippets(getItemSnippets(highlights));
        return item;
    }

    /**
     * Adds the required filters based on the given parameters
     * @param query the query to update
     * @param params the parameters to add
     */
    @SuppressWarnings("unchecked")
    protected void updateFilters(BoolQueryBuilder query, SearchParams params) {
        params.getFilters().forEach((filter, value) -> {
            if(facets.containsKey(filter)) {
                HierarchicalConfiguration<ImmutableNode> facetConfig = facets.get(filter);
                String fieldName = facetConfig.getString(CONFIG_KEY_FACET_FIELD);
                if(facetConfig.containsKey(CONFIG_KEY_FACET_RANGE)) {
                    RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(fieldName);
                    if(value instanceof Map) {
                        Map<String, Number> range = (Map<String, Number>) value;
                        rangeQuery
                            .gte(range.get(FACET_RANGE_MIN))
                            .lte(range.get(FACET_RANGE_MAX));
                    }
                    query.filter(rangeQuery);
                } else {
                    query.filter(QueryBuilders.matchQuery(fieldName, value));
                }
            }
        });
    }

    /**
     * Adds the configured highlighting to the given builder
     * @param builder the search builder to update
     */
    protected void updateHighlighting(SearchSourceBuilder builder) {
        HighlightBuilder highlight = SearchSourceBuilder.highlight();
        for (String field : highlightFields) {
            highlight.field(field, snippetSize, numberOfSnippets);
        }
        builder.highlighter(highlight);
    }

    /**
     * Maps the ElasticSearch {@link SearchResponse} to a {@link SearchResult} object
     * @param response the response to map
     * @return the search result object
     */
    protected SearchResult processResults(SearchResponse response) {
        SearchResult result = new SearchResult();
        result.setTotal(response.getHits().getTotalHits());

        List<SearchResultItem> items = Stream.of(response.getHits().getHits())
            .map(hit -> processSearchHit(hit.getSourceAsMap(), hit.getHighlightFields()))
            .collect(Collectors.toList());

        result.setItems(items);

        result.setFacets(processAggregations(response));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResult search(final String siteId, final List<String> allowedPaths, final SearchParams params)
        throws ServiceLayerException {

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if(StringUtils.isNotEmpty(params.getQuery())) {
            query.must(QueryBuilders.queryStringQuery(params.getQuery()));
        }

        if(StringUtils.isNotEmpty(params.getKeywords())) {
            query.must(
                QueryBuilders.boolQuery()
                    .should(QueryBuilders.regexpQuery(pathFieldName, ".*" + params.getKeywords() + ".*"))
                    .should(QueryBuilders.multiMatchQuery(params.getKeywords(), searchFields))
            );
        }

        if(MapUtils.isNotEmpty(params.getFilters())) {
            updateFilters(query, params);
        }

        SearchSourceBuilder builder = new SearchSourceBuilder()
            .query(query)
            .from(params.getOffset())
            .size(params.getLimit())
            .sort(getSortFieldName(params.getSortBy()), SortOrder.fromString(params.getSortOrder()));

        if(ArrayUtils.isNotEmpty(highlightFields)) {
            updateHighlighting(builder);
        }

        aggregations.forEach(builder::aggregation);

        SearchRequest request = new SearchRequest()
            .source(builder);

        try {
            SearchResponse response = elasticSearchService.search(siteId, allowedPaths, request);
            return processResults(response);
        } catch (IOException e) {
            throw new ServiceLayerException("Error connecting to ElasticSearch", e);
        } catch (Exception e) {
            throw new ServiceLayerException("Error executing search in ElasticSearch", e);
        }
    }

    /**
     * Maps the field name from the configured facets, if its not found returns the same value.
     * @param name the facet name
     * @return name of the field to sort
     */
    protected String getSortFieldName(String name) {
        String field = name;
        if(facets.containsKey(field)) {
            field = facets.get(field).getString(CONFIG_KEY_FACET_FIELD);
        }
        return field;
    }

    /**
     * Maps the ElasticSearch aggregations to {@link SearchFacet} objects
     * @param response the ElasticSearch response to map
     * @return the list of search facet objects
     */
    @SuppressWarnings("unchecked")
    private List<SearchFacet> processAggregations(final SearchResponse response) {
        List<SearchFacet> facets = new LinkedList<>();
        Aggregations aggregations = response.getAggregations();
        if(aggregations != null) {
            aggregations.getAsMap().forEach((name, aggregation) -> {
                SearchFacet facet = new SearchFacet();
                facet.setName(name);
                Map values = null;
                if(aggregation instanceof Terms) {
                    values = new LinkedHashMap();
                    Terms terms = (Terms) aggregation;
                    for(Terms.Bucket bucket : terms.getBuckets()) {
                        values.put(bucket.getKey(), bucket.getDocCount());
                    }
                } else if(aggregation instanceof Range) {
                    values = new TreeMap();
                    Range range = (Range) aggregation;
                    for(Range.Bucket bucket : range.getBuckets()) {
                        SearchFacetRange rangeValues = new SearchFacetRange();
                        rangeValues.setCount(bucket.getDocCount());
                        rangeValues.setFrom((Number) bucket.getFrom());
                        rangeValues.setTo((Number) bucket.getTo());
                        values.put(bucket.getKey(), rangeValues);
                    }
                    facet.setRange(true);
                }
                if(MapUtils.isNotEmpty(values)) {
                    facet.setValues(values);
                    facets.add(facet);
                }
            });
        }
        return facets;
    }

    /**
     * Maps the item type for the given source based on the configuration
     * @param source the source to map
     * @return the item type
     */
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

    /**
     * Maps the ElasticSearch highlighting to simple text snippets
     * @param highlights the highlighting to map
     * @return the list of snippets
     */
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

    /**
     * Finds the mime type for the given item
     * @param source the item to map
     * @return the mime type
     */
    protected String getMimeType(Map<String, Object> source) {
        if(source.containsKey(mimeTypeName)) {
            return source.get(mimeTypeName).toString();
        } else {
            return DEFAULT_MIME_TYPE;
        }
    }

}
