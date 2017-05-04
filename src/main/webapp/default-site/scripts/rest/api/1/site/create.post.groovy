/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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


import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException
import scripts.api.SiteServices;
import groovy.json.JsonSlurper

def result = [:]
def requestJson = request.reader.text
def slurper = new JsonSlurper()
def parsedReq = slurper.parseText(requestJson)

def blueprint = parsedReq.blueprint
def siteId = parsedReq.site_id
def description = parsedReq.description

/** Validate Parameters */
def invalidParams = false;
def paramsList = []

// blueprint
try {
    if (StringUtils.isEmpty(blueprint)) {
        invalidParams = true
        paramsList.add("blueprint")
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("blueprint")
}

// site_id
try {
    if (StringUtils.isEmpty(siteId) || StringUtils.length(siteId) > 50) {
        invalidParams = true
        paramsList.add("site_id")
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("site_id")
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = SiteServices.createContext(applicationContext, request)
    try {
        SiteServices.createSiteFromBlueprint(context, blueprint, siteId, siteId, description)
        result.message = "OK"
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/site/get.json?site_id=" + siteId
        response.addHeader("Location", locationHeader)
        response.setStatus(201)
    } catch (SiteAlreadyExistsException e) {
        response.setStatus(409)
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/site/get.json?site_id=" + siteId
        response.addHeader("Location", locationHeader)
        result.message = "Site already exists"
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
}
return result