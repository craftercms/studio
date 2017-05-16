import org.craftercms.blueprints.headless.TaxonomySearchHelper

def categories = new TaxonomySearchHelper("categories", searchService)
						.getItems()

return categories
