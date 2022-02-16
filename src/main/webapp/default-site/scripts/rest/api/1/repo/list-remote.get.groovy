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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.SiteServices

def result = [:]
def siteId = params.site_id

/** Validate Parameters */
def invalidParams = false;

// site_id
try {
    if (StringUtils.isEmpty(siteId)) {
        invalidParams = true
    }
} catch (Exception exc) {
    invalidParams = true
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter: site_id"
} else {
    def context = SiteServices.createContext(applicationContext, request)

    try {
        result.remotes = SiteServices.listRemote(context, siteId)
        response.setStatus(200)
        result.message = "OK"
    } catch (SiteNotFoundException e) {
        response.setStatus(404)
        result.message = "Site not found"
    } catch (Exception e) {
        response.setStatus(500)
        result.message = "Internal server error: \n" + e
    }
}

return result
