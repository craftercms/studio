import org.craftercms.blueprints.headless.PostSearchHelper

def id = params.id

if(!id) {
	throw new Exception("Missing id parameter.")
}

def posts = new PostSearchHelper(searchService)
						.filter("objectId: $id")
						.getItems()

if(!posts.items) {
	return []
}

return posts.items[0]
