import org.craftercms.blueprints.headless.PostSearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10
def author = params.author
def categories = params.categories?.split(",")
def tags = params.tags?.split(",")
def q = params.q

def helper = new PostSearchHelper(searchService, siteItemService)

if(q) {
	helper.query("body_html: $q")
}
if(author) {
	helper.filter("authors.item.key: \"$author\"")
}
if(categories) {
	helper.filter("categories.item.key: ( ${categories.join(' AND ')} )")
}
if(tags) {
	helper.filter("tags.item.key: ( ${tags.join(' AND ')} )")
}

def posts = helper.from(start).to(rows).getItems()

return posts
