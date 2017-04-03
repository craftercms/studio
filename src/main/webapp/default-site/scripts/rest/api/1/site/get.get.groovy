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
 */


import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.SiteServices;

def result = [:]
def siteId = params.site_id

def context = SiteServices.createContext(applicationContext, request)
try {
    def site = SiteServices.getSite(context, siteId)
    if (site != null) {
        def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") + "/api/1/services/api/1/site/get.json?site_id=" + siteId
        response.addHeader("Location", locationHeader)
        response.setStatus(200)
        result = site
    } else {
        response.setStatus(500)
        result.message = "Internal server error"
    }
} catch (SiteNotFoundException e) {
    response.setStatus(404)
    result.message = "Site not found"
} catch (Exception e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}
return result