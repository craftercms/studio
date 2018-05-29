/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

/**
 * @auhor Dejan Brkic
 */

import org.apache.commons.lang3.StringUtils
import scripts.api.ActivityServices

def result = [:]
def site = params.site_id
def user = params.user
def num = 10
def excludeLive = params.excludeLive
def valid = true
def filterType = params.filterType

/** Validate Parameters */
def invalidParams = false
def paramsList = []
// site_id
try {
    if (StringUtils.isEmpty(site)) {
        site = params.site
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("site_id")
}

// num
try {
    if (StringUtils.isNotEmpty(params.num)) {
        num = params.num.toInteger()
        if (num < 0) {
            invalidParams = true
            paramsList.add("num")
        }
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("num")
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = ActivityServices.createContext(applicationContext, request)
    def activities
    if (excludeLive != null && excludeLive != "undefined" && excludeLive == "true") {
        activities = ActivityServices.getActivities(context, site, user, num, "eventDate", false, true, filterType)

    } else {
        activities = ActivityServices.getActivities(context, site, user, num, "eventDate", false, false, filterType)
    }
    result.total = activities.size()
    result.sortedBy = "eventDate"
    result.ascending = "false"
    result.documents = activities
}
return result
