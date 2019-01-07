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
  static final String[] HIGHLIGHT_FIELDS = ["subject", "sections.item.section_html"]
  static final int DEFAULT_START = 0
  static final int DEFAULT_ROWS = 10

  def elasticSearch
  UrlTransformationService urlTransformationService

  SearchHelper(elasticSearch, UrlTransformationService urlTransformationService) {
    this.elasticSearch = elasticSearch
    this.urlTransformationService = urlTransformationService
  }

  def search(userTerm, categories, start = DEFAULT_START, rows = DEFAULT_ROWS) {
    def q = "${ARTICLE_CONTENT_TYPE_QUERY}"

    if (userTerm) {
      if(!userTerm.contains(" ")) {
        userTerm = "${userTerm}~1 OR *${userTerm}*"
      }
      def userTermQuery = "(subject:(${userTerm}) OR sections.item.section_html:(${userTerm}))"

      q = "${q} AND ${userTermQuery}"
    }
    if (categories) {
      def categoriesQuery = getFieldQueryWithMultipleValues("categories.item.key", categories)

      q = "${q} AND ${categoriesQuery}"
    }

    def highlighter = SearchSourceBuilder.highlight()
    HIGHLIGHT_FIELDS.each{ field -> highlighter.field(field) }

    def builder = new SearchSourceBuilder()
      .query(QueryBuilders.queryStringQuery(q))
      .from(start)
      .size(rows)
      .highlighter(highlighter)
    
    def result = elasticSearch.search(new SearchRequest().source(builder))

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
      def categoriesQuery = getFieldQueryWithMultipleValues("categories.item.key", categories)

      q = "${q} AND ${categoriesQuery}"
    }
    if (segments) {
      def segmentsQuery = getFieldQueryWithMultipleValues("segments.item.key", segments)

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
    
    def result = elasticSearch.search(new SearchRequest().source(builder))

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
            article.title = doc.title
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)

        if (hit.highlightFields) {
          println hit.highlightFields.values()*.getFragments().flatten()*.string()
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
            article.title = doc.subject
            article.summary = doc.summary
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)
            article.image = doc.image

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
