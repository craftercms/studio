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
class PostSearchHelper extends SearchHelper {
	
	def PostSearchHelper(elasticsearch, siteItemService) {
		super(elasticsearch, siteItemService)
		filter("content-type:\"/component/post\"")
		sortBy("createdDate_dt", "desc")
	}
	
	def getAuthors(doc) {
		if(doc.authors_o.item instanceof Map) {
			return doc.authors_o.item.component.name_s
		} else {
			def authors = []
			doc.authors_o.item.each { author ->
				authors << author.component.name_s
			}
			return authors
		}
	}
	
	
	def getCategories(doc) {
		getTaxonomyValues(doc, "categories_o")
	}
	
	def getTags(doc) {
		getTaxonomyValues(doc, "tags_o")
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
