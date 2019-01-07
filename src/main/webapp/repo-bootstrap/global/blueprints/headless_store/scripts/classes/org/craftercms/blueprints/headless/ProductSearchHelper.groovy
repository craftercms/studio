package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class ProductSearchHelper extends SearchHelper {
	
	def ProductSearchHelper(elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("content-type:\"/component/product\"")
		sortBy("createdDate_dt", "desc")
	}	
	
	def getCategories(doc) {
		getTaxonomyValues(doc, "categories")
	}
	
	def getTags(doc) {
		getTaxonomyValues(doc, "tags")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: doc.objectId,
			name: item.name_s,
			company: doc.company.item.component.name_s,
			categories: getCategories(doc),
			tags: getTags(doc),
			date: item.createdDate_dt.toInstant() as String,
			description: item.description_html,
			image: item.image_s,
			price: item.price_d,
            itemUrl: doc.localId
		]
	}
	
}