/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.DependencyServices;

def result = [:]
try {
    def site = request.getParameter("site")
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def entities = parsedReq.entities

    def paths = entities.collect { it.item }
    /** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// site
    try {
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add("site")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site")
    }

// paths
    try {
        if (CollectionUtils.isEmpty(paths)) {
            invalidParams = true
            paramsList.add("entities")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("entities")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {

        def context = DependencyServices.createContext(applicationContext, request)
        try {
            result = DependencyServices.calculateDependencies(context, site, paths)
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (Exception e) {
            response.setStatus(500)
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
