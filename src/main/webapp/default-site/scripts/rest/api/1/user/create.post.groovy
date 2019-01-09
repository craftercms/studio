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
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException
import scripts.api.SecurityServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def username = parsedReq.username
    def password = parsedReq.password
    def firstname = parsedReq.first_name
    def lastname = parsedReq.last_name
    def email = parsedReq.email

/** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// username
    try {
        if (StringUtils.isEmpty(username)) {
            invalidParams = true
            paramsList.add("username")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("username")
    }

// password
    try {
        if (StringUtils.isEmpty(password)) {
            invalidParams = true
            paramsList.add("password")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("password")
    }

// firstname
    try {
        if (StringUtils.isEmpty(firstname)) {
            invalidParams = true
            paramsList.add("firstname")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("firstname")
    }

// lastname
    try {
        if (StringUtils.isEmpty(lastname)) {
            invalidParams = true
            paramsList.add("lastname")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("lastname")
    }

// email
    try {
        if (StringUtils.isEmpty(email)) {
            invalidParams = true
            paramsList.add("email")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("email")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        def context = SecurityServices.createContext(applicationContext, request)
        try {
            SecurityServices.createUser(context, username, password, firstname, lastname, email);
            result.message = "OK"
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/user/get.json?user=" + username
            response.addHeader("Location", locationHeader)
            response.setStatus(201)
        } catch (UserAlreadyExistsException e) {
            response.setStatus(409)
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/user/get.json?user=" + username
            response.addHeader("Location", locationHeader)
            result.message = "User already exists"
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