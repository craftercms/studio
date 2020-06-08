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
class SearchHelper {
	
	protected def searchService
	protected def siteItemService
	protected def queryStr = "*:*"
	protected def queryObj
	
	def SearchHelper(searchService, siteItemService) {
		this.searchService = searchService
		this.siteItemService = siteItemService
		queryObj = searchService.createQuery()
	}
	
	def query(String q) {
		queryStr = q
		this
	}
	
	def filter(String fq) {
		queryObj.addFilterQuery(fq)
		this
	}
	
	def sortBy(String sort) {
		queryObj.addParam("sort", sort)
		this
	}
	
	def from(int start) {
		queryObj.setStart(start)
		this
	}
	
	def to(int rows) {
		queryObj.setRows(rows)
		this
	}
	
	def getItems() {
		queryObj.setQuery(queryStr)
		log.info("Running query: {}", queryObj)
		def results = searchService.search(queryObj)
		processResults(results)
	}
	
	def processResults(results) {
		def res = [:]
		res.total = results.response.numFound
		res.items = results.response.documents.collect { doc ->
			processItem(doc)
		}
		res
	}
	
	def processItem(document) {
		// Returns all fields by default.
		siteItemService.getSiteItem(document.localId)
	}
	
	def getTaxonomyValues(doc, field) {
		def keyField = "${field}.item.key"
		def valField = "${field}.item.value_smv"
		def values = []
		if(doc[valField]) {
			if(doc[valField] instanceof String) {
				values << [
					label: doc[valField],
					value: doc[keyField]
				]
			} else {
				for(def i = 0; i < doc[valField].size(); i++) {
					values << [
						label: doc[valField][i],
						value: doc[keyField][i]
					]
				}
			}
		}
		values
	}
	
}