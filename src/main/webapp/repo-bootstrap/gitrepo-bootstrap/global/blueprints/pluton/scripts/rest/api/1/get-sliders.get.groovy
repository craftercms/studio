def DEFAULT_SLIDE_SET = siteItemService.getSiteItem("/site/components/slider-sets/default.xml")
def slideSet = DEFAULT_SLIDE_SET

def profileSegment = (params.segment) ? params.segment : "all"
profileSegment = (profileSegment) ? profileSegment : "all"

def queryStatement = 'crafterSite:"pluton" AND content-type:"/component/slider-set" AND ((segment.item.key:"' + profileSegment  + '")^10 OR segment.item.key:"all")'

 
def query = searchService.createQuery()
query = query.setQuery(queryStatement)
//query = query.setRows(1)

def executedQuery = searchService.search(query)    
def itemsFound = executedQuery.response.numFound    
def items = executedQuery.response.documents

if(itemsFound > 0) {
	def targetedSlideId = items[0].localId
    def targetedItem = siteItemService.getSiteItem(targetedSlideId)
    
    if(targetedItem) {
	    slideSet = targetedItem
    }
}

return slideSet

