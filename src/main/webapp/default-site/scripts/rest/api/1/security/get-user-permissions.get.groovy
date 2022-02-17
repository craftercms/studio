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


import org.apache.commons.lang3.StringUtils
import scripts.api.SecurityServices
/**
 * Created by dejanbrkic on 12/10/14.
 */

def result = [:];
// extract parameters
def groups = params.groups
def path = params.path
def site = params.site_id

if (groups != null) {
    /* set the authorityDisplayName as the group ID */
    def groupList = new java.util.ArrayList()
    for (i in groups) {
        //log("Getting permissions for group: " + groups[i]);
        groupList.add(groups[i])
    }
    groups = groupList
}

/** Validate Parameters */
def invalidParams = false
def paramsList = []

// site_id

if (StringUtils.isEmpty(site)) {
    site = params.site
}


if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = SecurityServices.createContext(applicationContext, request)
    result.permissions = SecurityServices.getUserPermissions(context, site, path, groups)
}

return result
