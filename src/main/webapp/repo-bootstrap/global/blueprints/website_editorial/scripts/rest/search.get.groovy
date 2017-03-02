def category = params.category ? params.category : "*"
def queryStr = "content-type:\"/page/article\" AND categories.item.key:\"${category}\""

println queryStr

def query = searchService.createQuery()
	query.setQuery(queryStr)
    
return searchService.search(query)