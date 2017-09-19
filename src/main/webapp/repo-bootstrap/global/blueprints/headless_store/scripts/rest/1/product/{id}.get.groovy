import org.craftercms.blueprints.headless.ProductSearchHelper

def id = pathVars.id

def products = new ProductSearchHelper(searchService, siteItemService)
						.filter("objectId: $id")
						.getItems()

if(!products.items) {
	return []
}

return products.items[0]