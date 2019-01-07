import org.craftercms.blueprints.headless.TaxonomySearchHelper

def categories = new TaxonomySearchHelper("categories", elasticSearch, siteItemService)
						.getItems()

return categories
