package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class TaxonomySearchHelper extends SearchHelper {
	
	def TaxonomySearchHelper(String name, elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("file-name: \"${name}.xml\"")
		filter("content-type:\"/taxonomy\"")
	}
	
	def processItem(doc) {
		if(doc.items.item instanceof List) {
			return doc.items.item.collect { item ->
				[
					label: item.value,
					value: item.key
				]
			}
		} else {
			return [
				label: doc.items.item.value,
				value: doc.items.item.key
			]
		}
	}
	
	def processResults(results) {
		def res = [:]
		res.items = []
		results.hits.hits.each { doc ->
			res.items.addAll(processItem(doc.getSourceAsMap()))
		}
		res.total = res.items.size()
		res
	}
	
}