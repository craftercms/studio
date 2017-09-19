import org.craftercms.blueprints.headless.AuthorSearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10

def authors = new AuthorSearchHelper(searchService, siteItemService)
						.from(start)
						.to(rows)
						.getItems()

return authors
