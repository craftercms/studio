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

import org.craftercms.blueprints.headless.PostSearchHelper

def start = params.start?.toInteger() ?: 0
def rows = params.rows?.toInteger() ?: 10
def author = params.author
def categories = params.categories?.split(",")
def tags = params.tags?.split(",")
def q = params.q

def helper = new PostSearchHelper(elasticsearch, siteItemService)

if(q) {
	helper.query("body_html: $q")
}
if(author) {
	helper.filter("authors_o.item.key: \"$author\"")
}
if(categories) {
	helper.filter("categories_o.item.key: ( ${categories.join(' AND ')} )")
}
if(tags) {
	helper.filter("tags_o.item.key: ( ${tags.join(' AND ')} )")
}

def posts = helper.from(start).to(rows).getItems()

return posts
