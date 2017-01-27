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

def context = SecurityServices.createContext(applicationContext, request)
try {
    def groupMap = SecurityServices.getGroup(context, siteId, groupName);
    if (groupMap != null && !groupMap.isEmpty()) {
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/get/get?site_id=" + siteId + "&group_name=" + groupName
        response.addHeader("Location", locationHeader)
        return groupMap;
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