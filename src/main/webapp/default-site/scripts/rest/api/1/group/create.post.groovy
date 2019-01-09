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


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException
import scripts.api.SecurityServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def groupName = parsedReq.group_name
    def description = parsedReq.description
    def siteId = parsedReq.site_id

/** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// token
    try {
        if (StringUtils.isEmpty(groupName)) {
            invalidParams = true
            paramsList.add("group_name")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("group_name")
    }

// new password
    try {
        if (StringUtils.isEmpty(siteId)) {
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
        def context = SecurityServices.createContext(applicationContext, request)
        try {
            SecurityServices.createGroup(context, groupName, description, siteId)
            result.message = "OK"
            response.setStatus(201)
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/group/get.json?group_name=" + groupName
            response.addHeader("Location", locationHeader)
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (GroupAlreadyExistsException e) {
            response.setStatus(409)
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/group/get.json?group_name=" + groupName
            response.addHeader("Location", locationHeader)
            result.message = "Group already exists"
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
