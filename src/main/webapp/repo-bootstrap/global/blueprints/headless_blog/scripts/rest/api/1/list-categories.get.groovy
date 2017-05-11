import org.craftercms.blueprints.headless.CategorySearchHelper

def categories = new CategorySearchHelper(searchService)
						.getItems()

return categories
