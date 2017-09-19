package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class AuthorSearchHelper extends SearchHelper {
	
	def AuthorSearchHelper(searchService, siteItemService) {
		super(searchService, siteItemService)
		filter("content-type:\"/component/author\"")
		sortBy("name_s desc")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: item.objectId.text,
			name: item.name_s.text,
			photo: item.photo_s.text,
			website: item.website_s.text,
			photo: item.photo_s.text,
			biography: item.biography_html.text,
			itemUrl: doc.localId
		]
	}
	
}