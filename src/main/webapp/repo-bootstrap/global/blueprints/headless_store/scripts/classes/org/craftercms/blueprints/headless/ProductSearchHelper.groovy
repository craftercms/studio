package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class ProductSearchHelper extends SearchHelper {
	
	def ProductSearchHelper(searchService) {
		super(searchService)
		filter("content-type:\"/component/product\"")
		sortBy("createdDate_dt desc")
	}	
	
	def getCategories(doc) {
		getTaxonomyValues(doc, "categories")
	}
	
	def getTags(doc) {
		getTaxonomyValues(doc, "tags")
	}
	
	def processItem(doc) {
		[
			id: doc.objectId,
			name: doc.name_s,
			company: doc["company.item.component.name_s"],
			categories: getCategories(doc),
			tags: getTags(doc),
			date: doc.createdDate_dt,
			description: doc.description_html,
			image: doc.image_s,
			price: doc.price_d
		]
	}
	
}