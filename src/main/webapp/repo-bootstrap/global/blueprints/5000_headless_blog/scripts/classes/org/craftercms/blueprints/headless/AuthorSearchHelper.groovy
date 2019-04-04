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
class AuthorSearchHelper extends SearchHelper {
	
	def AuthorSearchHelper(elasticsearch, siteItemService) {
		super(elasticsearch, siteItemService)
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