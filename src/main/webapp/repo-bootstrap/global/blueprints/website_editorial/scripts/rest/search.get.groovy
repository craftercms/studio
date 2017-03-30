import org.craftercms.sites.editorial.SearchHelper

def userTerm = params.userTerm
def categories = params["categories[]"]
def start = params.start ? params.start as Integer : 0
def rows = params.rows ? params.rows as Integer : 10
def searchHelper = new SearchHelper(searchService, urlTransformationService)
def results = searchHelper.search(userTerm, categories, start, rows)

return results;
