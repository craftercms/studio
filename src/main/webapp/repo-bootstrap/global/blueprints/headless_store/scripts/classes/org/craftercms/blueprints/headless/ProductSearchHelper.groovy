/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.blueprints.headless

import groovy.util.logging.Slf4j

@Slf4j
class ProductSearchHelper extends SearchHelper {
	
	def ProductSearchHelper(elasticSearch, siteItemService) {
		super(elasticSearch, siteItemService)
		filter("content-type:\"/component/product\"")
		sortBy("createdDate_dt", "desc")
	}	
	
	def getCategories(doc) {
		getTaxonomyValues(doc, "categories")
	}
	
	def getTags(doc) {
		getTaxonomyValues(doc, "tags")
	}
	
	def processItem(doc) {
		def item = super.processItem(doc)
		[
			id: doc.objectId,
			name: item.name_s,
			company: doc.company.item.component.name_s,
			categories: getCategories(doc),
			tags: getTags(doc),
			date: item.createdDate_dt.toInstant() as String,
			description: item.description_html,
			image: item.image_s,
			price: item.price_d,
            itemUrl: doc.localId
		]
	}
	
}