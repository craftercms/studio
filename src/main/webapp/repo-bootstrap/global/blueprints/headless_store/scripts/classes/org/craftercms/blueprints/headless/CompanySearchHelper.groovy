package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class CompanySearchHelper extends SearchHelper {
	
	def CompanySearchHelper(searchService) {
		super(searchService)
		filter("content-type:\"/component/company\"")
		sortBy("name_s asc")
	}
	
	def processItem(doc) {
		[
			id: doc.objectId,
			name: doc.name_s,
			logo: doc.logo_s,
			website: doc.website_s,
			email: doc.email_s,
			phone: doc.phone_s,
			description: doc.description_html,
            itemUrl: doc.localId
		]
	}
	
}