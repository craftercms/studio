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
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException

import scripts.api.SecurityServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def body = slurper.parseText(requestBody)
    def username = body.username
    def password = body.password

    try {
        def context = SecurityServices.createContext(applicationContext, request)
        def ticket = SecurityServices.authenticate(context, username, password)

        if (ticket != null) {
            def profile = SecurityServices.getUserProfile(context, username)

            response.setStatus(200)
            result = ["username": username, "first_name": profile.first_name, "last_name": profile.last_name,
                      "email": profile.email, "authentication_type" : profile.authentication_type]
        } else {
            response.setStatus(500)
            result.message = "Internal server error"
        }
    } catch (BadCredentialsException e) {
        response.setStatus(401)
        result.message = "Unauthorized"
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}

return result
