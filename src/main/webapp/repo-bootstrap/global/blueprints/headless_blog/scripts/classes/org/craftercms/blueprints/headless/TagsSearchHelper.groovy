package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class TagsSearchHelper extends CategorySearchHelper {
	
	def TagsSearchHelper(searchService) {
		super(searchService)
	}
	
	def init() {
		filter("content-type:\"/component/tags\"")
	}
	
}