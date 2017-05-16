import org.craftercms.blueprints.headless.TaxonomySearchHelper

def categories = new TaxonomySearchHelper("tags", searchService)
						.getItems()

return categories
