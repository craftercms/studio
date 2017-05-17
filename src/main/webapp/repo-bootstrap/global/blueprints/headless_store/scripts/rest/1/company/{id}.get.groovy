import org.craftercms.blueprints.headless.CompanySearchHelper

def id = pathVars.id

if(!id) {
	throw new Exception("Missing id parameter")
}

def companies = new CompanySearchHelper(searchService)
						.filter("objectId: $id")
						.getItems()

if(!companies.items) {
	return []
}

return companies.items[0]