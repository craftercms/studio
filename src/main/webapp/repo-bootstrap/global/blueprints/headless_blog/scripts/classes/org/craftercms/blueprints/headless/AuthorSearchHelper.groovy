package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class AuthorSearchHelper extends SearchHelper {
	
	def AuthorSearchHelper(searchService) {
		super(searchService)
		filter("content-type:\"/component/author\"")
		sortBy("name_s desc")
	}
	
	def processItem(doc) {
		[
			id: doc.objectId,
			name: doc.name_s,
			photo: doc.photo_s,
			website: doc.website_s,
			photo: doc.photo_s,
			biography: doc.biography_html
		]
	}
	
}