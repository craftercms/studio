/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.lang3.StringUtils
import scripts.api.ClipboardServices
import groovy.json.JsonSlurper



def result = [:]
try {
    def site = request.getParameter("site_id")

    def requestBody = request.reader.text
    def context = ClipboardServices.createContext(applicationContext, request)
    def slurper = new JsonSlurper()
    def tree = slurper.parseText(requestBody)
    def paths = []

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

// parse the inbound request and compose an array of paths to put on the clipboard
        def rootItem = ClipboardServices.newClipboardItem(tree.item[0].uri, false)

        ClipboardServices.parseTree(rootItem, tree.item[0])

        ClipboardServices.copy(site, rootItem, context)

        result.success = true
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result



