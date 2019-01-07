import org.craftercms.blueprints.headless.ProductSearchHelper

def id = pathVars.id

def products = new ProductSearchHelper(elasticSearch, siteItemService)
						.filter("objectId: $id")
						.getItems()

if(!products.items) {
	return []
}

return products.items[0]