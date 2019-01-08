package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class AuthorSearchHelper extends SearchHelper {
	
	def AuthorSearchHelper(elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("content-type:\"/component/author\"")
		sortBy("name_s", "desc")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: item.objectId.text,
			name: item.name_s,
			photo: item.photo_s,
			website: item.website_s,
			photo: item.photo_s,
			biography: item.biography_html,
			itemUrl: doc.localId
		]
	}
	
}