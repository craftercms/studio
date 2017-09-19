package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class TaxonomySearchHelper extends SearchHelper {
	
	def TaxonomySearchHelper(String name, searchService, siteItemService) {
		super(searchService, siteItemService)
		filter("file-name: \"${name}.xml\"")
		filter("content-type:\"/taxonomy\"")
	}
	
	def processItem(doc) {
		def values = []
		for(def i = 0; i < doc["items.item.value"].size(); i++) {
			values << [
				label: doc["items.item.value"][i],
				value: doc["items.item.key"][i]
			]
		}
		values
	}
	
	def processResults(results) {
		def res = [:]
		res.items = []
		results.response.documents.each { doc ->
			res.items.addAll(processItem(doc))
		}
		res.total = res.items.size()
		res
	}
	
}