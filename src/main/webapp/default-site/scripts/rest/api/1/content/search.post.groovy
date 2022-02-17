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

import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import scripts.api.ContentServices

def results = [:]
try {
    results.resultCount = 0
    results.pageTotal = 1
    results.resultPerPage = 20
    results.searchFailed = false
    results.objectList = []

// get post body
    def reader = request.getReader()
    def body = ""

    def content = reader.readLine()
    while (content != null) {
        body += content
        content = reader.readLine()
    }

    def searchParams = new JsonSlurper().parseText(body)

// get search options
    def site = request.getParameter("site_id")
    def keywords = searchParams.keyword
    def page = ((searchParams.page.toInteger()) - 1) // UI is 1 based
    def pageSize = searchParams.pageSize.toInteger()
    def sort = searchParams.sortBy

    /** Validate Parameters */
    def invalidParams = false
    def paramsList = []

    // site_id
    try {
        if (StringUtils.isEmpty(site)) {
            site = request.getParameter("site")
            if (StringUtils.isEmpty(site)) {
                invalidParams = true
                paramsList.add("site_id")
            }
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        def context = ContentServices.createContext(applicationContext, request)

        results = ContentServices.search(site, keywords, searchParams, sort, page, pageSize, context)
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return results
