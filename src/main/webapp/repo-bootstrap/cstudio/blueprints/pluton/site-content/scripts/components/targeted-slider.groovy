def DEFAULT_SLIDE_SET = siteItemService.getSiteItem("/site/components/slider-sets/default.xml")
model.slideSet = DEFAULT_SLIDE_SET

def profileSegment = (profile) ? profile.getAttribute("segment") : "all"
profileSegment = (profileSegment) ? profileSegment : "all"

def queryStatement = 'content-type:"/component/slider-set" AND ((segment.item.key:"' + profileSegment  + '")^10 OR segment.item.key:"all")'
model.query = queryStatement
 
def query = searchService.createQuery()
query = query.setQuery(queryStatement)
//query = query.setRows(1)

def executedQuery = searchService.search(query)    
def itemsFound = executedQuery.response.numFound    
def items = executedQuery.response.documents

if(itemsFound > 0) {
	def targetedSlideId = items[0].localId
    model.slideSet = siteItemService.getSiteItem(targetedSlideId)
}
