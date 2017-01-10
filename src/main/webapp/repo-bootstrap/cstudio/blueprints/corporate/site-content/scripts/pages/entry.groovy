def profile = request.session.getAttribute("_cr_profile_state")
def profileSegment = (profile) ? profile.get("segment") : "all"
    profileSegment = (profileSegment) ? profileSegment : "all"

def queryStatement = 'crafterSite:"' + siteContext.siteName + '" ' +
                     'AND content-type:"/component/tout" ' +
                     'AND ((segment.item.key:"' + profileSegment  + '")^10 OR segment.item.key:"all") '

def query = searchService.createQuery()
query = query.setQuery(queryStatement)

def executedQuery = searchService.search(query)    
def itemsFound = executedQuery.response.numFound    
def touts = executedQuery.response.documents

model.touts = touts
