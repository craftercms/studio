package scripts.api.impl.search

import groovy.json.JsonSlurper

/**
 * search implementation
 */
class SolrSearch {

    /**
     * Creates a query for all possible combinations of the given keywords
     * @param keywords
     * @param field
     */
    static expandTextSearch(String keywords, String field = null) {
        def query = "\"$keywords\" OR *$keywords* OR ($keywords)"
        if (field) {
            query = "$field: ($query)"
        }
        query
    }

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
        if (keywords && keywords != "") {
            def localIdQuery = expandTextSearch(keywords, "localId")
            def internalNameQuery = expandTextSearch(keywords, "internal-name")
            def titleQuery = expandTextSearch(keywords, "title")
            def textQuery = expandTextSearch(keywords, "_text_main_")
            queryStatement += " AND ($localIdQuery OR $internalNameQuery OR $titleQuery OR $textQuery)"
        }

        // can't support filters for images at this time because images are not indexed
        //[Object { qname="cm:content.mimetype", value="image/*", useWildCard="true"}]
        for (int f = 0; f < searchParams.filters.size; f++) {
            def filter = searchParams.filters[f]
            queryStatement += " AND " + filter.qname + ":" + filter.value + " "
        }

        results.query = queryStatement

        def query = searchService.createQuery()
        query = query.setQuery(queryStatement)
        query = query.setRows(pageSize)
        query = query.setStart(page * pageSize)

        if (searchParams.sortBy != "") {
            def order = (searchParams.sortAscending == "true") ? "asc" : "desc"

            if ("cstudio-core:internalName".equals(searchParams.sortBy)) {
                sort = "internal-name"
            } else if ("cstudio-core:title".equals(searchParams.sortBy)) {
                sort = "title"
            } else if ("cm:created".equals(searchParams.sortBy)) {
                sort = "createdDate_dt"
            } else {
                sort = searchParams.sortBy
            }

            query = query.addParam("sort", sort + " " + order)
        } else {
            query = query.addParam("sort", "score desc")
        }

        try {
            // Site name will be the index ID
            def executedQuery = searchService.search(site, query)
            def queryResults = executedQuery.response.documents

            results.resultCount = executedQuery.response.numFound
            results.pageTotal = (results.resultCount < pageSize) ? 1 : results.resultCount / pageSize
            results.resultPerPage = pageSize
            results.searchFailed = false

            for (int i = 0; i < queryResults.size; i++) {
                try {
                    def result = [:]
                    def item = queryResults[i] // use the baseline object
                    item.path = item["localId"]
                    item.name = item["localId"]
                    item.internalName = item["internal-name"]
                    item.contentType = item["content-type"]
                    item.path = item["localId"]
                    item.lastEditDate = item.lastModifiedDate
                    result.item = item
                    if (item.internalName == null) {
                        item.internalName = item.name
                    }
                    results.objectList[results.objectList.size] = result

                }
                catch (err) {
                    // item could not be built
                }
            }
        }
        catch (err) {
            results.searchFailed = true
            results.error = "" + err
        }

        return results

    }
}
