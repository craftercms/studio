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


import org.craftercms.studio.api.v1.exception.security.UserNotFoundException
import scripts.api.SecurityServices

def result = [:]

def username = params.username

def context = SecurityServices.createContext(applicationContext, request)
try {
    def res = SecurityServices.forgotPassword(context, username)
    if (res.success) {
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/user/get.json?username=" + username
        response.addHeader("Location", locationHeader)
        result.message = res.message
        response.setStatus(200)
    } else {
        response.setStatus(500)
        result.message = "Internal server error"
    }
} catch (UserNotFoundException e) {
    response.setStatus(404)
    result.message = "User not found"
} catch (Exception e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}

return result