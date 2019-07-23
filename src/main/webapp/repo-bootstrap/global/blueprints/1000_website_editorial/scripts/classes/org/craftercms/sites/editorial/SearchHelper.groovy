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

package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.engine.service.UrlTransformationService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE_QUERY = "content-type:\"/page/article\""
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
    def q = "${ARTICLE_CONTENT_TYPE_QUERY}"

    if (userTerm) {
      if(!userTerm.contains(" ")) {
        userTerm = "${userTerm}~1 OR *${userTerm}*"
      }
      def userTermQuery = "(subject_t:(${userTerm}) OR sections_o.item.section_html:(${userTerm}))"

      q = "${q} AND ${userTermQuery}"
    }
    if (categories) {
      def categoriesQuery = getFieldQueryWithMultipleValues("categories_o.item.key", categories)

      q = "${q} AND ${categoriesQuery}"
    }

    def highlighter = SearchSourceBuilder.highlight()
    HIGHLIGHT_FIELDS.each{ field -> highlighter.field(field) }

    def builder = new SearchSourceBuilder()
      .query(QueryBuilders.queryStringQuery(q))
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
    def q = "${ARTICLE_CONTENT_TYPE_QUERY}"

    if (featured) {
      q = "${q} AND featured_b:true"
    }
    if (categories) {
      def categoriesQuery = getFieldQueryWithMultipleValues("categories_o.item.key", categories)

      q = "${q} AND ${categoriesQuery}"
    }
    if (segments) {
      def segmentsQuery = getFieldQueryWithMultipleValues("segments_o.item.key", segments)

      q = "${q} AND ${segmentsQuery}"
    }
    if (additionalCriteria) {
      q = "${q} AND ${additionalCriteria}"
    }
    
    def builder = new SearchSourceBuilder()
      .query(QueryBuilders.queryStringQuery(q))
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
      values = "(" + StringUtils.join((Iterable)values, " OR ") + ")"
    } else {
      values = "\"${values}\""
    }

    return "${field}:${values}"
  }

}
