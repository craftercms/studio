package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.search.service.SearchService
import org.craftercms.engine.service.UrlTransformationService

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE = "/page/article"
  static final String ARTICLE_CONTENT_TYPE_QUERY_STR = "content-type:\"${ARTICLE_CONTENT_TYPE}\""

  SearchService searchService
  UrlTransformationService urlTransformationService

  def SearchHelper(SearchService searchService, UrlTransformationService urlTransformationService) {
    this.searchService = searchService
    this.urlTransformationService = urlTransformationService
  }

  def searchArticles(featured, category, segment) {
    def queryStr = "${ARTICLE_CONTENT_TYPE_QUERY_STR}"

    if (featured) {
      queryStr = "${queryStr} AND featured_b:true"
    }
    if (category) {
      queryStr = "${queryStr} AND categories.item.key:\"${category}\""
    }
    if (segment) {
      queryStr = "${queryStr} AND segments.item.key:\"${segment}\""
    }

    def query = createSortedArticleQuery(queryStr)
    def result = searchService.search(query)

    if (result) {
      return processArticleDocuments(result.response.documents)
    } else {
      return []
    }
  }

  private def createSortedArticleQuery(queryStr) {
    def query = searchService.createQuery()

    query.setQuery(queryStr)
    query.addParam("sort", "date_dt desc")

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

}
