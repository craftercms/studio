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
class TaxonomySearchHelper extends SearchHelper {
	
	def TaxonomySearchHelper(String name, elasticsearch, siteItemService) {
		super(elasticsearch, siteItemService)
		filter("file-name: \"${name}.xml\"")
		filter("content-type:\"/taxonomy\"")
	}
	
	def processItem(doc) {
		if(doc.items.item instanceof List) {
			return doc.items.item.collect { item ->
				[
					label: item.value,
					value: item.key
				]
			}
		} else {
			return [
				label: doc.items.item.value,
				value: doc.items.item.key
			]
		}
	}
	
	def processResults(results) {
		def res = [:]
		res.items = []
		results.hits.hits.each { doc ->
			res.items.addAll(processItem(doc.getSourceAsMap()))
		}
		res.total = res.items.size()
		res
	}
	
}