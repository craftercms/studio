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


import org.apache.commons.lang3.StringUtils
import scripts.api.ContentServices

def result = [:]
def path = params.path
def site = params.site_id

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
    def context = ContentServices.createContext(applicationContext, request)

    result.contentStream = ContentServices.getContentAtPath(context, site, path)
    result.contentPath = path

}
return result
