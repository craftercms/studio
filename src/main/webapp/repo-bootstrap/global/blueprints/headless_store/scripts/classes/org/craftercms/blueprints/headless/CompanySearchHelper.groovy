package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class CompanySearchHelper extends SearchHelper {
	
	def CompanySearchHelper(searchService, siteItemService) {
		super(searchService, siteItemService)
		filter("content-type:\"/component/company\"")
		sortBy("name_s asc")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: item.objectId.text,
			name: item.name_s.text,
			logo: item.logo_s.text,
			website: item.website_s.text,
			email: item.email_s.text,
			phone: item.phone_s.text,
			description: item.description_html.text,
            itemUrl: doc.localId
		]
	}
	
}