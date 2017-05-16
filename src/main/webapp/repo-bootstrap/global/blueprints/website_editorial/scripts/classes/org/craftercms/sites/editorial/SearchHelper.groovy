package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.search.service.SearchService
import org.craftercms.engine.service.UrlTransformationService

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE_QUERY = "content-type:\"/page/article\""
  static final String[] HIGHLIGHT_FIELDS = ["subject", "sections.item.section_html"]
  static final int DEFAULT_START = 0
  static final int DEFAULT_ROWS = 10

  SearchService searchService
  UrlTransformationService urlTransformationService

  SearchHelper(SearchService searchService, UrlTransformationService urlTransformationService) {
    this.searchService = searchService
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

    def query = searchService.createQuery()
        query.setQuery(q)
        query.setStart(start)
        query.setRows(rows)
        query.setHighlight(true)
        query.setHighlightFields(HIGHLIGHT_FIELDS)

    def result = searchService.search(query)

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

    def query = createSortedArticleQuery(q, start, rows)
    def result = searchService.search(query)

    if (result) {
      return processArticleListingResults(result)
    } else {
      return []
    }
  }

  private def createSortedArticleQuery(q, start, rows) {
    def query = searchService.createQuery()
        query.setQuery(q)
        query.addParam("sort", "date_dt desc")
        query.setStart(start)
        query.setRows(rows)

    return query
  }

  private def processUserSearchResults(result) {
    def articles = []
    def documents = result.response.documents
    def highlighting = result.highlighting

    if (documents) {
      documents.each {doc ->
        def article = [:]
            article.title = doc.title
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)

        if (highlighting) {
          def articleHighlights = highlighting[doc.id]
          if (articleHighlights) {
              def highlightValues = []

              articleHighlights.values().each { value ->
                  if (value instanceof Iterable) {
                    value = ((Iterable) value).iterator().next()
                  }

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
    def documents = result.response.documents

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
