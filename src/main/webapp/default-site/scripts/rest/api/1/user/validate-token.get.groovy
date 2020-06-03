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


import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException
import scripts.api.SecurityServices

def result = [:]
def token = params.token

/** Validate Parameters */
def invalidParams = false;

// token
try {
    if (StringUtils.isEmpty(token)) {
        invalidParams = true
    }
} catch (Exception exc) {
    invalidParams = true
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter: token"
} else {
    def context = SecurityServices.createContext(applicationContext, request)
    try {
        def success = SecurityServices.validateToken(context, token)
        if (success) {
            result.message = "OK"
            response.setStatus(200)
        } else {
            result.message = "Unauthorized"
            response.setStatus(401)
        }
    } catch (UserExternallyManagedException e) {
        response.setStatus(403)
        result.message = "Externally managed user"
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
}
return result