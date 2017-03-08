package org.craftercms.sites.editorial

import org.apache.commons.lang3.StringUtils
import org.craftercms.search.service.SearchService

class SearchHelper {

  SearchService searchService

  def SearchHelper(SearchService searchService) {
    this.searchService = searchService
  }

  def searchArticles(tags, audience) {
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
    def queryStr = "content-type:\"/page/article\" AND ${tagsQueryStr} AND ${audienceQueryStr}"
    def query = searchService.createQuery()

    query.setQuery(queryStr)
    query.addParam("sort", "date_dt desc")

    return searchService.search(query)
  }

}
