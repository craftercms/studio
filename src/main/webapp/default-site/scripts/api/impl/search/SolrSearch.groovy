package scripts.api.impl.search

import groovy.json.JsonSlurper

/**
 * search implementation
 */
class SolrSearch {
 
	/** 
	 * search the repository
	 * @param site - the project ID	 
	 * @param keywords - keywords
	 * @param filters - Filters object (document based)
	 * @param sort - sort object
	 * @param page - page to start on
	 * @param resultsPerPage - items to return per page
	 */
	static search(site, keywords, searchParams, sort, page, pageSize, context) {
		def results = [:]
		results.objectList = []

		def searchService = context.applicationContext.get("crafter.searchService")

		// build query
		def queryStatement = "crafterSite:\"" + site + "\" "
		if(keywords && keywords != "") {
			queryStatement    += " *" + keywords + "* "
		}

		// can't support filters for images at this time because images are not indexed
		//[Object { qname="cm:content.mimetype", value="image/*", useWildCard="true"}]
		for(int f=0; f<searchParams.filters.size; f++){
			def filter = searchParams.filters[f]
			queryStatement    += " AND " + filter.qname + ":"+ filter.value + " "
		}

		results.query = queryStatement

		def query = searchService.createQuery()
		query = query.setQuery(queryStatement)
		query = query.setRows(pageSize)
		query = query.setStart(page)

		if(searchParams.sortBy != "") {
			def order = (searchParams.sortAscending == "true") ? "asc" : "desc"

			if(searchParams.sortBy == "cstudio-core:internalName,cstudio-core:title"
			|| searchParams.sortBy == "cm:created") {
				sort = "id"// bug with title, internal name, filename, last modified as arrays so cant sort on them
			}

			query = query.addParam("sort", sort + " " + order)
		}
        else {
              	query = query.addParam("sort", "internal-name_s asc ")
        }

		try {
 			def executedQuery = searchService.search(query)   
			def queryResults = executedQuery.response.documents

			results.resultCount = executedQuery.response.numFound   
			results.pageTotal = (results.resultCount < pageSize) ? 1 : results.resultCount / pageSize
			results.resultPerPage = pageSize
			results.searchFailed = false

			for(int i = 0; i < queryResults.size; i++) {
				try {
				def result = [:]
				def item = queryResults[i] // use the baseline object
				item.path = item["localId"]
				item.name = item["localId"]
				// map known properties from solr index
//				item.name = item["file-name"]
				item.internalName = item["internal-name"]
				item.contentType = item["content-type"]
				item.path = item["localId"]
				item.lastEditDate = item.lastModifiedDate 
				//item.lastEditDateAsString = "2014-11-04T09:36:03"
				//item.eventDate = item.lastModifiedDate  

				result.item = item
				if(item.internalName != null) {
					results.objectList[results.objectList.size] = result
				}

				}
				catch(err) {
					// item could not be built
				}
			}
		}
		catch(err) {
			results.searchFailed = true
			results.error = "" + err
		}

		return results

	}
}	
