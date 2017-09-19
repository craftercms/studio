import org.craftercms.blueprints.headless.AuthorSearchHelper

def id = pathVars.id

def authors = new AuthorSearchHelper(searchService, siteItemService)
						.query("objectId: $id")
						.getItems()

if(!authors.items) {
	return []
}

return authors.items[0]
