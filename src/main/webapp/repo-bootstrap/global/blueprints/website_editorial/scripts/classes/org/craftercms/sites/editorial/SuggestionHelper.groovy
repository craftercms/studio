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