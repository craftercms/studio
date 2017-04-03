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

import scripts.api.SecurityServices

def result = [:]

def start = 0
if (params.start != null) {
    start = params.start.toInteger()
}

// TODO: SJ: These should be constants
def number = 25
if (params.number != null) {
    number = params.number.toInteger()
}

def context = SecurityServices.createContext(applicationContext, request)
try {
    def groupList = SecurityServices.getAllGroups(context, start, number)
    if (groupList != null) {
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/group/get-all.json?start=" + start + "&number=" + number
        response.addHeader("Location", locationHeader)
        result.sites = groupList
    } else {
        response.setStatus(500)
        result.message = "Internal server error"
    }
} catch (Exception e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}

return result