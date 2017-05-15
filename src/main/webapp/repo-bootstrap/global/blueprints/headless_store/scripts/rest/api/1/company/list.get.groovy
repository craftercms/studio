import org.craftercms.blueprints.headless.CompanySearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10

def companies = new CompanySearchHelper(searchService)
						.from(start)
						.to(rows)
						.getItems()

return companies
