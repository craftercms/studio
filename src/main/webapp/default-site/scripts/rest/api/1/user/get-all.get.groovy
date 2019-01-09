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


import org.apache.commons.lang3.StringUtils
import scripts.api.SecurityServices

def result = [:]

def start = 0
def number = 25

/** Validate Parameters */
def invalidParams = false;
def paramsList = []

// start
try {
    if (StringUtils.isNotEmpty(params.start)) {
        start = params.start.toInteger()
        if (start < 0) {
            invalidParams = true
            paramsList.add("start")
        }
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("start")
}

// number
try {
    if (StringUtils.isNotEmpty(params.number)) {
        number = params.number.toInteger()
        if (number < 0) {
            invalidParams = true
            paramsList.add("number")
        }
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("number")
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = SecurityServices.createContext(applicationContext, request)
    try {
        def total = SecurityServices.getAllUsersTotal(context)
        def users = SecurityServices.getAllUsers(context, start, number)
        if (users != null) {
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/user/get-all.json?start=" + start + "&number=" + number
            response.addHeader("Location", locationHeader)
            result.users = users
            result.total = total
            response.setStatus(200)
        } else {
            response.setStatus(500)
            result.message = "Internal server error"
        }
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
}
return result