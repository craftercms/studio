/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.craftercms.sites.editorial.SearchHelper
import org.craftercms.sites.editorial.ProfileUtils

def segment = null

if (authToken) {
    segment = ProfileUtils.getSegment(authToken.principal, siteItemService)
}

def searchHelper = new SearchHelper(searchClient, urlTransformationService)
// articleCategories and articlePath should be provided as additionalModel of the component and
// should be the categories of the current article
def articles = searchHelper.searchArticles(false, articleCategories, segment, 0, 3, "-localId:\"${articlePath}\"")

templateModel.articles = articles
