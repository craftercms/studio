package org.craftercms.sites.editorial

import org.craftercms.search.service.SearchService

class SuggestionHelper {
	
	static final String DEFAULT_CONTENT_TYPE_QUERY = "content-type:\"/page/article\""
	static final String DEFAULT_SEARCH_FIELD = "subject"
	
	SearchService searchService
	
	String contentTypeQuery = DEFAULT_CONTENT_TYPE_QUERY
	String searchField = DEFAULT_SEARCH_FIELD
	
	SuggestionHelper(SearchService searchService) {
		this.searchService = searchService
	}
	
	def getSuggestions(String term) {
		def queryStr = "${contentTypeQuery} AND ${searchField}:*${term}*"
		def query = searchService.createQuery()
		query.setQuery(queryStr)
		def result = searchService.search(query)
		return process(result)
	}
	
	def process(result) {
		def processed = result.response.documents.collect { doc ->
			doc[searchField]
		}
		return processed
	}
	
}