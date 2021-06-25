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

package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.engine.service.UrlTransformationService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.search.MatchQuery
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

import static org.elasticsearch.index.query.QueryBuilders.boolQuery
import static org.elasticsearch.index.query.QueryBuilders.matchQuery
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import static org.elasticsearch.index.query.QueryBuilders.termQuery
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE = "/page/article"
  static final Map<String, Float> ARTICLE_SEARCH_FIELDS = [
    'subject_t': 1.5f,
    'sections_o.item.section_html': 1.0f
  ]
  static final String[] HIGHLIGHT_FIELDS = ["subject_t", "sections_o.item.section_html"]
  static final int DEFAULT_START = 0
  static final int DEFAULT_ROWS = 10

  def elasticsearch
  UrlTransformationService urlTransformationService

  SearchHelper(elasticsearch, UrlTransformationService urlTransformationService) {
    this.elasticsearch = elasticsearch
    this.urlTransformationService = urlTransformationService
  }

  def search(userTerm, categories, start = DEFAULT_START, rows = DEFAULT_ROWS) {
    def query = boolQuery()

    // Filter by content-type
    query.filter(matchQuery("content-type", ARTICLE_CONTENT_TYPE))

    if (categories) {
      // Filter by categories
      query.filter(getFieldQueryWithMultipleValues("categories_o.item.key", categories))
    }

    if (userTerm) {
      // Check if the user is requesting an exact match with quotes
      def matcher = userTerm =~ /.*("([^"]+)").*/
      if (matcher.matches()) {
        // Using must excludes any doc that doesn't match with the input from the user
        query.must(multiMatchQuery(matcher.group(2))
                    .fields(ARTICLE_SEARCH_FIELDS)
                    .fuzzyTranspositions(false)
                    .autoGenerateSynonymsPhraseQuery(false))

        // Remove the exact match to continue processing the user input
        userTerm = StringUtils.remove(userTerm, matcher.group(1))
      } else {
        // Exclude docs that do not have any optional matches
        query.minimumShouldMatch(1)
      }

      if (userTerm) {
        // Using should makes it optional and each additional match will increase the score of the doc
        query
          // Search for phrase matches including a wildcard at the end and increase the score for this match
          .should(multiMatchQuery(userTerm)
                  .fields(ARTICLE_SEARCH_FIELDS)
                  .type(MatchQuery.Type.PHRASE_PREFIX)
                  .boost(1.5f))
          // Search for matches on individual terms
          .should(multiMatchQuery(userTerm).fields(ARTICLE_SEARCH_FIELDS))
      }
    }

    def highlighter = SearchSourceBuilder.highlight()
    HIGHLIGHT_FIELDS.each{ field -> highlighter.field(field) }

    def builder = searchSource()
      .query(query)
      .from(start)
      .size(rows)
      .highlighter(highlighter)

    def result = elasticsearch.search(new SearchRequest().source(builder))

    if (result) {
      return processUserSearchResults(result)
    } else {
      return []
    }
  }

  def searchArticles(featured, categories, segments, start = DEFAULT_START, rows = DEFAULT_ROWS, additionalCriteria = null) {
    def query = boolQuery()

    query.filter(matchQuery("content-type", ARTICLE_CONTENT_TYPE))

    if (featured) {
      query.filter(termQuery("featured_b", true))
    }
    if (categories) {
      query.filter(getFieldQueryWithMultipleValues("categories_o.item.key", categories))
    }
    if (segments) {
      query.filter(getFieldQueryWithMultipleValues("segments_o.item.key", segments))
    }
    if (additionalCriteria) {
      query.filter(queryStringQuery(additionalCriteria))
    }

    def builder = searchSource()
      .query(query)
      .from(start)
      .size(rows)
      .sort(new FieldSortBuilder("date_dt").order(SortOrder.DESC))

    def result = elasticsearch.search(new SearchRequest().source(builder))

    if (result) {
      return processArticleListingResults(result)
    } else {
      return []
    }
  }

  private def processUserSearchResults(result) {
    def articles = []
    def hits = result.hits.hits

    if (hits) {
      hits.each {hit ->
        def doc = hit.getSourceAsMap()
        def article = [:]
            article.id = doc.objectId
            article.objectId = doc.objectId
            article.path = doc.localId
            article.title = doc.title_t
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)

        if (hit.highlightFields) {
          def articleHighlights = hit.highlightFields.values()*.getFragments().flatten()*.string()
          if (articleHighlights) {
              def highlightValues = []

              articleHighlights.each { value ->
                  highlightValues << value
              }

              article.highlight = StringUtils.join(highlightValues, "... ")
              article.highlight = StringUtils.strip(article.highlight)
          }
        }

        articles << article
      }
    }

    return articles
  }

  private def processArticleListingResults(result) {
    def articles = []
    def documents = result.hits.hits*.getSourceAsMap()

    if (documents) {
      documents.each {doc ->
        def article = [:]
            article.id = doc.objectId
            article.objectId = doc.objectId
            article.path = doc.localId
            article.storeUrl = doc.localId
            article.title = doc.subject_t
            article.summary = doc.summary_t
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)
            article.image = doc.image_s

        articles << article
      }
    }

    return articles
  }

  private def getFieldQueryWithMultipleValues(field, values) {
    if (values.class.isArray()) {
      values = values as List
    }

    if (values instanceof Iterable) {
      values = StringUtils.join((Iterable)values, " ") as String
    } else {
      values = values as String
    }

    return matchQuery(field, values)
  }

}
