import groovy.json.JsonException
import groovy.json.JsonSlurper

// /cstudio/wcm/search/search?site=globalcom
// example incoming {"contentTypes": [ ],"includeAspects": [ ],"excludeAspects": [ ],"keyword": "c", "page": "1","pageSize": "20","sortBy": "","sortAscending": "true","filters": [], "columns":[] }
// example result {"resultCount":10,"pageTotal":1,"resultPerPage":20,"searchFailed":false,"objectList":[

import scripts.api.ContentServices;

def results = [:]
try {
    results.resultCount = 0
    results.pageTotal = 1
    results.resultPerPage = 20
    results.searchFailed = false
    results.objectList = []

// get post body
    def reader = request.getReader()
    def body = ""

    def content = reader.readLine()
    while (content != null) {
        body += content
        content = reader.readLine()
    }

    def searchParams = new JsonSlurper().parseText(body)

// get search options
    def site = request.getParameter("site")
    def keywords = searchParams.keyword
    def page = ((searchParams.page.toInteger()) - 1) // UI is 1 based
    def pageSize = searchParams.pageSize.toInteger()
    def sort = searchParams.sortBy

    def context = ContentServices.createContext(applicationContext, request)

    results = ContentServices.search(site, keywords, searchParams, sort, page, pageSize, context)
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return results
