package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class CategorySearchHelper extends SearchHelper {
	
	def CategorySearchHelper(searchService) {
		super(searchService)
	}
	
	def init() {
		filter("content-type:\"/component/categories\"")
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