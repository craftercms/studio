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

def groupName = params.group_name
def siteId = params.site_id
def start = 0
if (params.start != null && params.start != '') {
    start = params.start.toInteger()
}
def end = 10
if (params.end != null && params.end != '') {
    end = params.end.toInteger()
}
def context = SecurityServices.createContext(applicationContext, request)
try {
    def usersPerGroup = SecurityServices.getUsersPerGroup(context, siteId, groupName, start, end);
    if (usersPerGroup != null && !usersPerGroup.isEmpty()) {
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/group/users?site_id=" + siteId + "&group_name=" + groupName + "&start="+ start + "&end=" + end
        response.addHeader("Location", locationHeader)
        result.users = usersPerGroup
        return result
    } else {
        response.setStatus(404)
        result.status = "Group not found"
        return result;
    }
} catch (Exception e) {
    response.setStatus(500)
    result.status = "Internal server error"
    return result;
}