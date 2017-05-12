import org.craftercms.blueprints.headless.AuthorSearchHelper

def id = params.id

if(!id) {
	throw new Exception("Missing id parameter.")
}

def authors = new AuthorSearchHelper(searchService)
						.query("objectId: $id")
						.getItems()

if(!authors.items) {
	return []
}

return authors.items[0]
