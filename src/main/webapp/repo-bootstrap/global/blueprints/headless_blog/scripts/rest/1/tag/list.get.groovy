import org.craftercms.blueprints.headless.TaxonomySearchHelper

def categories = new TaxonomySearchHelper("tags", elasticSearch, siteItemService)
						.getItems()

return categories
