/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

/**
 * @author Dejan Brkic
 */

import org.apache.commons.lang3.StringUtils
import scripts.api.WorkflowServices

// extract parameters
def result = [:]
def sort = params.sort
def site = params.site_id
def ascending = params.ascending
if (ascending != null) {
    ascending = ascending.toBoolean()
} else {
    ascending = false
}
def inProgressOnly = params.inProgressOnly
if (inProgressOnly != null) {
    inProgressOnly = inProgressOnly.toBoolean()
} else {
    inProgressOnly = false
}
def includeInProgress = params.includeInProgress
if (includeInProgress != null) {
    includeInProgress = includeInProgress.toBoolean()
} else {
    includeInProgress = false
}

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

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = WorkflowServices.createContext(applicationContext, request)
    if (inProgressOnly || includeInProgress) {
        result = WorkflowServices.getInProgressItems(context, site, sort, ascending, inProgressOnly)
    } else {
        result = WorkflowServices.getGoLiveItems(context, site, sort, ascending)
    }
}
return result
