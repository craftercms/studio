def category = params.category ? params.category : "*"
def audience = params.audience ? params.audience : "*"
def queryStr = "content-type:\"/page/article\" AND categories.item.key:\"${category}\" AND audience.item.key:\"${audience}\""

println queryStr

def query = searchService.createQuery()
	query.setQuery(queryStr)
    
return searchService.search(query)