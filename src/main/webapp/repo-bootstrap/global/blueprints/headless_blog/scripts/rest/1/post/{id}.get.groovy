import org.craftercms.blueprints.headless.PostSearchHelper

def id = pathVars.id

def posts = new PostSearchHelper(searchService, siteItemService)
						.filter("objectId: $id")
						.getItems()

if(!posts.items) {
	return []
}

return posts.items[0]
