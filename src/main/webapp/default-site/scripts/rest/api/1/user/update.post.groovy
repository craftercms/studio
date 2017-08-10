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
 *
 */


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException
import scripts.api.SecurityServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def username = parsedReq.username
    def firstname = parsedReq.first_name
    def lastname = parsedReq.last_name
    def email = parsedReq.email

/** Validate Parameters */
    def invalidParams = false;

// username
    try {
        if (StringUtils.isEmpty(username)) {
            invalidParams = true
        }
    } catch (Exception exc) {
        invalidParams = true
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter: username"
    } else {

        def context = SecurityServices.createContext(applicationContext, request)
        try {
            SecurityServices.updateUser(context, username, firstname, lastname, email)
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/user/get.json?username=" + username
            response.addHeader("Location", locationHeader)
            result.message = "OK"
            response.setStatus(200)
        } catch (UserExternallyManagedException e) {
            response.setStatus(403)
            result.message = "Externally managed user"
        } catch (UserNotFoundException e) {
            response.setStatus(404)
            result.message = "User not found"
        } catch (Exception e) {
            result.message = "Internal server error: \n" + e
            response.setStatus(500)
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result