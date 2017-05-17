import org.craftercms.blueprints.headless.CompanySearchHelper

def id = pathVars.id

def companies = new CompanySearchHelper(searchService)
						.filter("objectId: $id")
						.getItems()

if(!companies.items) {
	return []
}

return companies.items[0]