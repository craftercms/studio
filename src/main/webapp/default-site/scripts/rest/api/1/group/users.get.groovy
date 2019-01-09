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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.SecurityServices
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException

def result = [:]

def groupName = params.group_name
def siteId = params.site_id
def start = 0
def number = 25

/** Validate Parameters */
def invalidParams = false;
def paramsList = []

// group_name
try {
    if (StringUtils.isEmpty(groupName)) {
        invalidParams = true
        paramsList.add("group_name")
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("group_name")
}

// site_id
try {
    if (StringUtils.isEmpty(siteId)) {
        invalidParams = true
        paramsList.add("site_id")
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("site_id")
}

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
        def total = SecurityServices.getUsersPerGroupTotal(context, siteId, groupName);
        def usersPerGroup = SecurityServices.getUsersPerGroup(context, siteId, groupName, start, number);
        if (usersPerGroup != null) {
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/group/users.json?site_id=" + siteId + "&group_name=" + groupName + "&start=" + start + "&number=" + number
            response.addHeader("Location", locationHeader)
            response.setStatus(200)
            result.users = usersPerGroup
            result.total = total
        } else {
            response.setStatus(500)
            result.message = "Internal server error"
        }
    } catch (SiteNotFoundException e) {
        response.setStatus(404)
        result.message = "Site not found"
    } catch (GroupNotFoundException e) {
        response.setStatus(404)
        result.message = "Group not found"
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
}
return result