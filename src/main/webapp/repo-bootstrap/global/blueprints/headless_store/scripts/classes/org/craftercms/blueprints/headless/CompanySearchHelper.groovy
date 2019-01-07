package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class CompanySearchHelper extends SearchHelper {
	
	def CompanySearchHelper(elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("content-type:\"/component/company\"")
		sortBy("name_s", "asc")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: item.objectId.text,
			name: item.name_s,
			logo: item.logo_s,
			website: item.website_s,
			email: item.email_s,
			phone: item.phone_s,
			description: item.description_html,
            itemUrl: doc.localId
		]
	}
	
}