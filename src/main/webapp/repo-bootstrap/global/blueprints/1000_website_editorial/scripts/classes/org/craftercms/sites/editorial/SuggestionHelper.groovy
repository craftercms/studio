/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import org.opensearch.client.opensearch.core.SearchRequest
import org.craftercms.search.opensearch.client.OpenSearchClientWrapper

class SuggestionHelper {
	
	static final String DEFAULT_CONTENT_TYPE_QUERY = "content-type:\"/page/article\""
	static final String DEFAULT_SEARCH_FIELD = "subject_t"

	OpenSearchClientWrapper searchClient
	
	String contentTypeQuery = DEFAULT_CONTENT_TYPE_QUERY
	String searchField = DEFAULT_SEARCH_FIELD
	
	SuggestionHelper(searchClient) {
		this.searchClient = searchClient
	}
	
	def getSuggestions(String term) {
		def queryStr = "${contentTypeQuery} AND ${searchField}:*${term}*"
		def result = searchClient.search(SearchRequest.of(r -> r
			.query(q -> q
				.queryString(s -> s
					.query(queryStr)
				)
			)
		), Map)

		return process(result)
	}
	
	def process(result) {
		def processed = result.hits.hits*.source().collect { doc ->
			doc[searchField]
		}
		return processed
	}
	
}
