def DEFAULT_SLIDE_SET = siteItemService.getSiteItem("/site/components/slider-sets/default.xml")
def lang = "en"

try {
lang =request.getRequestURI().substring(request.getContextPath().length()).substring(1,3)
}
catch(err){}

model.x ="" 
 
model.slideSet = DEFAULT_SLIDE_SET
profile = request.session.getAttribute("_cr_profile_state")

//def profileSegment = (profile) ? profile.getAttribute("segment") : "all"
def profileSegment = (profile) ? profile.get("segment") : "all"
profileSegment = (profileSegment) ? profileSegment : "all"

def queryStatement = 'crafterSite:"pluton" ' +
                     'AND content-type:"/component/slider-set" ' +
                     'AND ((segment.item.key:"' + profileSegment  + '")^10 OR segment.item.key:"all") '+
                     'AND localId:*/' + lang + '*'
                     

def query = searchService.createQuery()
query = query.setQuery(queryStatement)

def executedQuery = searchService.search(query)    
def itemsFound = executedQuery.response.numFound    
def items = executedQuery.response.documents

if(itemsFound > 0) {
	def targetedSlideId = items[0].localId
    def targetedItem = siteItemService.getSiteItem(targetedSlideId)
    
    if(targetedItem) {
	    model.slideSet = targetedItem
    }
}