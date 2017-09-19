package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class SearchHelper {
	
	protected def searchService
	protected def siteItemService
	protected def queryStr = "*:*"
	protected def query
	
	def SearchHelper(searchService, siteItemService) {
		this.searchService = searchService
		this.siteItemService = siteItemService
		query = searchService.createQuery()
	}
	
	def query(String q) {
		queryStr = q
		this
	}
	
	def filter(String fq) {
		query.addFilterQuery(fq)
		this
	}
	
	def sortBy(String sort) {
		query.addParam("sort", sort)
		this
	}
	
	def from(int start) {
		query.setStart(start)
		this
	}
	
	def to(int rows) {
		query.setRows(rows)
		this
	}
	
	def getItems() {
		query.setQuery(queryStr)
		log.info("Running query: {}", query)
		def results = searchService.search(query)
		processResults(results)
	}
	
	def processResults(results) {
		def res = [:]
		res.total = results.response.numFound
		res.items = results.response.documents.collect { doc ->
			processItem(doc)
		}
		res
	}
	
	def processItem(document) {
		// Returns all fields by default.
		siteItemService.getSiteItem(document.localId)
	}
	
	def getTaxonomyValues(doc, field) {
		def keyField = "${field}.item.key"
		def valField = "${field}.item.value_smv"
		def values = []
		if(doc[valField]) {
			if(doc[valField] instanceof String) {
				values << [
					label: doc[valField],
					value: doc[keyField]
				]
			} else {
				for(def i = 0; i < doc[valField].size(); i++) {
					values << [
						label: doc[valField][i],
						value: doc[keyField][i]
					]
				}
			}
		}
		values
	}
	
}