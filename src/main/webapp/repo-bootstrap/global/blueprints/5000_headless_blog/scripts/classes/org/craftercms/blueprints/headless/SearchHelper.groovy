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

package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

@Slf4j
class SearchHelper {
	
	protected def elasticsearch
	protected def siteItemService
	protected def queryStr = "*:*"
	protected def builder
	protected def filters = []
	protected def sort
	
	def SearchHelper(elasticsearch, siteItemService) {
		this.elasticsearch = elasticsearch
		this.siteItemService = siteItemService
		builder = new SearchSourceBuilder()
	}
	
	def query(String q) {
		queryStr = q
		this
	}
	
	def filter(String fq) {
		filters << fq
		this
	}
	
	def sortBy(String field, String order) {
		sort = new FieldSortBuilder(field).order(SortOrder.valueOf(order.toUpperCase()))
		this
	}
	
	def from(int start) {
		builder.from(start)
		this
	}
	
	def to(int rows) {
		builder.size(rows)
		this
	}
	
	def getItems() {
		if(sort) {
			builder.sort(sort)
		}
		def queryBuilder
		if(filters) {
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(queryStr))
			filters.each{ filter -> queryBuilder.filter(QueryBuilders.queryStringQuery(filter)) }
		} else {
			queryBuilder = QueryBuilders.queryStringQuery(queryStr)
		}
		builder.query(queryBuilder)
		log.info("Running query: {}", builder)
		def results = elasticsearch.search(new SearchRequest().source(builder))
		processResults(results)
	}
	
	def processResults(results) {
		def res = [:]
		res.total = results.hits.totalHits
		res.items = results.hits.hits.collect { doc ->
			processItem(doc.getSourceAsMap())
		}
		res
	}
	
	def processItem(document) {
		// Returns all fields by default.
		siteItemService.getSiteItem(document.localId)
	}
	
	def getTaxonomyValues(doc, field) {
		def values = []
		if(doc[field]?.item) {
			if(doc[field].item instanceof Map) {
				values << [
					label: doc[field].item.value_smv,
					value: doc[field].item.key
				]
			} else {
				doc[field].item.each { item ->
					values << [
						label: item.value_smv,
						value: item.key
					]
				}
			}
		}
		values
	}
	
}