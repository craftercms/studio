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
import scripts.api.SiteServices

def result = [:]
def status = [:]
def site = params.site_id
def path = params.path
def applyEnv = params.applyEnv

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
    def applyEnvironment = false
    if (applyEnv != null && applyEnv == 'true') {
        applyEnvironment = true
    }
    def context = SiteServices.createContext(applicationContext, request)
    result = SiteServices.getConfiguration(context, site, path, applyEnvironment)
}
return result
