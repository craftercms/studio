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
import scripts.api.SiteServices
 
def result = [:];
def path = request.getParameter("path")
def site = request.getParameter("site_id")
def content = request.getInputStream()

/** Validate Parameters */
def invalidParams = false
def paramsList = []

if (StringUtils.isEmpty(site)) {
    site = request.getParameter("site")
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def context = SiteServices.createContext(applicationContext, request)
    if (site != null && site != "") {
        result.result = SiteServices.writeConfiguration(context, site, path, content)
    } else {
        result.result = SiteServices.writeConfiguration(context, path, content)
    }
}
return result
