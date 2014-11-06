import groovy.json.JsonSlurper

// /cstudio/wcm/search/search?site=globalcom
// example incoming {"contentTypes": [ ],"includeAspects": [ ],"excludeAspects": [ ],"keyword": "c", "page": "1","pageSize": "20","sortBy": "","sortAscending": "true","filters": [], "columns":[] }
// example result {"resultCount":10,"pageTotal":1,"resultPerPage":20,"searchFailed":false,"objectList":[


def results = [:]
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

def searchParams = new JsonSlurper().parseText( body )

// get search options
def site = params.site
def keywords = searchParams.keyword
def page = searchParams.page.toInteger()
def pageSize = searchParams.pageSize.toInteger()

// build query
def queryStatement = "crafterSite:\"" + site + "\" "
queryStatement    += "AND " + keywords + " "

// can't support filters for images at this time because images are not indexed
//[Object { qname="cm:content.mimetype", value="image/*", useWildCard="true"}]
for(int f=0; f<searchParams.filters.size; f++){
	def filter = searchParams.filters[f]
	queryStatement    += " AND " + filter.qname + ":" + filter.value + " "
}

results.query = queryStatement

def query = searchService.createQuery()
query = query.setQuery(queryStatement)
query = query.setRows(pageSize)
query = query.setStart(page)

if(searchParams.sortBy != "") {
	def order = (searchParams.sortAscending == "true") ? "asc" : "desc"
	def sort = searchParams.sortBy

	if(searchParams.sortBy == "cstudio-core:internalName,cstudio-core:title"
	|| searchParams.sortBy == "cm:created") {
		sort = "id"// bug with title, internal name, filename, last modified as arrays so cant sort on them
	}

	query = query.addParam("sort", sort + " " + order)
}

try {
	def executedQuery = searchService.search(query)   
	def queryResults = executedQuery.response.documents

	results.resultCount = executedQuery.response.numFound   
	results.pageTotal = (results.resultCount < pageSize) ? 1 : results.resultCount / pageSize
	results.resultPerPage = pageSize
	results.searchFailed = false

	for(int i = 0; i < queryResults.size; i++) {
		def result = [:]
		def item = queryResults[i] // use the baseline object

		// map known properties from solr index
		item.name = item["file-name"]
		item.internalName = item["internal-name"]
		item.contentType = item["content-type"]
		item.path = item["localId"]
		item.lastEditDate = item.lastModifiedDate 
		//item.lastEditDateAsString = "2014-11-04T09:36:03"
		//item.eventDate = item.lastModifiedDate  
	/*	item.lastEditDate = [:]
			item.lastEditDate.date = 3
			item.lastEditDate.day = 1
			item.lastEditDate.hours = 10
			item.lastEditDate.minutes = 19
			item.lastEditDate.month = 10
			item.lastEditDate.seconds = 42
			item.lastEditDate.time = 1415027982411
			item.lastEditDate.timezoneOffset = 300
			item.lastEditDate.year = 114*/

		item.floating = (item["placeInNav"]!=null           ? (item["placeInNav"] == "true") : true)
		item.previewable = (item["content-type"]!=null      ? (item["content-type"].indexOf("page") != -1) : false)
		item.component = (item["content-type"]!=null        ? (item["content-type"].indexOf("page") == -1) : true)
		item.contentType = (item["disabled"]!=null          ? (item["disabled"] == "true") : false)
		item.contentType = (item["hideInAuthoring"]!=null   ? (item["hideInAuthoring"] == "true") : false)
		item.uri = item.localId 

		item.browserUri = item.localId.replace("/site/website", "").replace("/index.xml", "")

		item.asset = (item["file-name"].indexOf(".xml") == -1)
		item.document = false
		item.container = false // folders are not in the index
		item.deleted = false // it's in the index
		item.defaultWebApp = "/wem-projects/"+site+"/"+site+"/work-area"

		// fake required properties
		item.inFlight = false
		item.inProgress = false
		item.scheduled = false
		item.submitted = false
		item.submittedForDeletion = false
		item.live = true
		item.new = false

		item.lockOwner = ""
		item.user = ""
	    item.userFirstName = ""	
		item.userLastName = ""
		item.nodeRef = ""
		item.metaDescription = "" //oneweb

		result.item = item
		results.objectList[i] = result
	}
}
catch(err) {
	results.searchFailed = true
	results.error = "" + err
}

return results
