package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class PostSearchHelper extends SearchHelper {
	
	def PostSearchHelper(searchService, siteItemService) {
		super(searchService, siteItemService)
		filter("content-type:\"/component/post\"")
		sortBy("createdDate_dt desc")
	}
	
	def getAuthors(doc) {
		if(doc["authors.item.component.name_s"]) {
			return doc["authors.item.component.name_s"]
		} else {
			def authors = []
			for(def i = 0; i < doc["authors.item.key"].size(); i++) {
				authors << doc["authors.item.component.name_smv"][i]
			}
			return authors
		}
	}
	
	
	def getCategories(doc) {
		getTaxonomyValues(doc, "categories")
	}
	
	def getTags(doc) {
		getTaxonomyValues(doc, "tags")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		return [
			id: item.objectId.text,
			title: item.title_s.text,
			authors: getAuthors(doc),
			categories: getCategories(doc),
			tags: getTags(doc),
			date: item.createdDate_dt.toInstant() as String,
			body: item.body_html.text,
			featuredImage: item.featuredImage_s.text,
			itemUrl: doc.localId
		]
	}
	
}