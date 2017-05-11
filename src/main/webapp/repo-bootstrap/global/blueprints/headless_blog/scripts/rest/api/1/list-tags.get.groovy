import org.craftercms.blueprints.headless.TagsSearchHelper

def categories = new TagsSearchHelper(searchService)
						.getItems()

return categories
