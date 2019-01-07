package org.craftercms.sites.editorial

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

class SuggestionHelper {
	
	static final String DEFAULT_CONTENT_TYPE_QUERY = "content-type:\"/page/article\""
	static final String DEFAULT_SEARCH_FIELD = "subject"
	
	def elasticSearch
	
	String contentTypeQuery = DEFAULT_CONTENT_TYPE_QUERY
	String searchField = DEFAULT_SEARCH_FIELD
	
	SuggestionHelper(elasticSearch) {
		this.elasticSearch = elasticSearch
	}
	
	def getSuggestions(String term) {
		def queryStr = "${contentTypeQuery} AND ${searchField}:*${term}*"
		def builder = new SearchSourceBuilder()
      .query(QueryBuilders.queryStringQuery(queryStr))
    
    def result = elasticSearch.search(new SearchRequest().source(builder))

		return process(result)
	}
	
	def process(result) {
		def processed = result.hits.hits*.getSourceAsMap().collect { doc ->
			doc[searchField]
		}
		return processed
	}
	
}