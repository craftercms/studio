package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.search.service.SearchService
import org.craftercms.engine.service.UrlTransformationService

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE = "/page/article"
  static final String ARTICLE_CONTENT_TYPE_QUERY_STR = "content-type:\"${ARTICLE_CONTENT_TYPE}\""

  SearchService searchService
  UrlTransformationService urlTransformationService

  SearchHelper(SearchService searchService, UrlTransformationService urlTransformationService) {
    this.searchService = searchService
    this.urlTransformationService = urlTransformationService
  }

  def searchArticles(featured, category, segment, start = 0, rows = 10, additionalCriteria = null) {
    def queryStr = "${ARTICLE_CONTENT_TYPE_QUERY_STR}"

    if (featured) {
      queryStr = "${queryStr} AND featured_b:true"
    }
    if (category) {
      def categoryQueryStr = getFieldQueryWithMultipleValues("categories.item.key", category)

      queryStr = "${queryStr} AND ${categoryQueryStr}"
    }
    if (segment) {
      def segmentQueryStr = getFieldQueryWithMultipleValues("segments.item.key", segment)

      queryStr = "${queryStr} AND ${segmentQueryStr}"
    }
    if (additionalCriteria) {
      queryStr = "${queryStr} AND ${additionalCriteria}"
    }

    def query = createSortedArticleQuery(queryStr, start, rows)
    def result = searchService.search(query)

    if (result) {
      return processArticleDocuments(result.response.documents)
    } else {
      return []
    }
  }

  private def createSortedArticleQuery(queryStr, start, rows) {
    def query = searchService.createQuery()
        query.setQuery(queryStr)
        query.addParam("sort", "date_dt desc")
        query.setStart(start)
        query.setRows(rows)

    return query
  }

  private def processArticleDocuments(documents) {
    def articles = []

    if (documents) {
      documents.each {doc ->
        def article = [:]
            article.title = doc.title
            article.summary = doc.summary
            article.url = urlTransformationService.transform("storeUrlToRenderUrl", doc.localId)
            article.image = doc.image

        articles << article
      }
    }

    return articles
  }

  private def getFieldQueryWithMultipleValues(field, values) {
    if (values instanceof Iterable) {
      values = "(" + StringUtils.join((Iterable)values, " OR ") + ")"
    } else {
      values = "\"${values}\""
    }

    return "${field}:${values}"
  }

}
