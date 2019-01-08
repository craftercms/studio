package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class PostSearchHelper extends SearchHelper {
	
	def PostSearchHelper(elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("content-type:\"/component/post\"")
		sortBy("createdDate_dt", "desc")
	}
	
	def getAuthors(doc) {
		if(doc.authors.item instanceof Map) {
			return doc.authors.item.component.name_s
		} else {
			def authors = []
			doc.authors.item.each { author ->
				authors << author.component.name_s
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
			title: item.title_s,
			authors: getAuthors(doc),
			categories: getCategories(doc),
			tags: getTags(doc),
			date: item.createdDate_dt.toInstant() as String,
			body: item.body_html,
			featuredImage: item.featuredImage_s,
			itemUrl: doc.localId
		]
	}
	
}