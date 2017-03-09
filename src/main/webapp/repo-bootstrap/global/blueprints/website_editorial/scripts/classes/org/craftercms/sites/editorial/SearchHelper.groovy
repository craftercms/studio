package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.search.service.SearchService

class SearchHelper {

  static final String ARTICLE_CONTENT_TYPE = "/page/article"
  static final String ARTICLE_CONTENT_TYPE_QUERY_STR = "content-type:\"${ARTICLE_CONTENT_TYPE}\""

  SearchService searchService

  def SearchHelper(SearchService searchService) {
    this.searchService = searchService
  }

  def searchArticlesByFeatured() {
    def queryStr = "${ARTICLE_CONTENT_TYPE_QUERY_STR} AND featured_b:true"
    def query = createSortedArticleQuery(queryStr)
	def result = searchService.search(query)
    
    if (result) {
    	return result.response.documents
    } else {
    	return []
    }
  }

  def searchArticlesByTagsAndAudience(tags, audience) {
    def tagsStr = StringUtils.join((Iterable<String>)tags, " OR ")
    def audienceStr = StringUtils.join((Iterable<String>)audience, " OR ")

    if (!tagsStr) {
      tagsStr = "*"
    }
    if (!audienceStr) {
      audienceStr = "*"
    }

    def tagsQueryStr = "tags.item.key:\"${tagsStr}\""
    def audienceQueryStr = "audience.item.key:\"${audienceStr}\""
    def queryStr = "${ARTICLE_CONTENT_TYPE_QUERY_STR} AND ${tagsQueryStr} AND ${audienceQueryStr}"
    def query = createSortedArticleQuery(queryStr)
	def result = searchService.search(query)
    
	if (result) {
    	return result.response.documents
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

}
