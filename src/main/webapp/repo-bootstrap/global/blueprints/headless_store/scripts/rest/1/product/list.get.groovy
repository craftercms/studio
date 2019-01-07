import org.craftercms.blueprints.headless.ProductSearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10
def company = params.company
def categories = params.categories?.split(",")
def tags = params.tags?.split(",")
def q = params.q

def helper = new ProductSearchHelper(elasticSearch, siteItemService)

if(q) {
	helper.query("description_html: $q")
}
if(company) {
	helper.filter("company.item.component.objectId: $company")
}
if(categories) {
	helper.filter("categories.item.key: ( ${categories.join(' AND ')} )")
}
if(tags) {
	helper.filter("tags.item.key: ( ${tags.join(' AND ')} )")
}

def products = helper.from(start).to(rows).getItems()

return products
