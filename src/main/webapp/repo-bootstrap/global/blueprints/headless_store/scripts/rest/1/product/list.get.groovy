/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.craftercms.blueprints.headless.ProductSearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10
def company = params.company
def categories = params.categories?.split(",")
def tags = params.tags?.split(",")
def q = params.q

def helper = new ProductSearchHelper(searchService, siteItemService)

if(q) {
	helper.query("description_html: $q")
}
if(company) {
	helper.filter("company.item.key: $company")
}
if(categories) {
	helper.filter("categories.item.key: ( ${categories.join(' AND ')} )")
}
if(tags) {
	helper.filter("tags.item.key: ( ${tags.join(' AND ')} )")
}

def products = helper.from(start).to(rows).getItems()

return products
