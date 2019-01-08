package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

@Slf4j
class SearchHelper {
	
	protected def elasticSearch
	protected def siteItemService
	protected def queryStr = "*:*"
	protected def builder
	protected def filters = []
	protected def sort
	
	def SearchHelper(elasticSearch, siteItemService) {
		this.elasticSearch = elasticSearch
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
		def results = elasticSearch.search(new SearchRequest().source(builder))
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