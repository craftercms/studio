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


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.craftercms.engine.exception.HttpStatusCodeException
import org.springframework.http.HttpStatus
import scripts.api.SiteServices

import static java.lang.String.format
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE

def studioConfiguration = applicationContext.get('studioConfiguration')

def result = [:]
try {
    def requestJson = request.reader.text
    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestJson)

    def siteId = parsedReq.site_id

    /** Validate Parameters */
    def invalidParams = false
    def paramsList = []

    // site_id
    try {
        if (StringUtils.isEmpty(siteId)) {
            siteId = parsedReq.siteId
            if (StringUtils.isEmpty(siteId)) {
                invalidParams = true
                paramsList.add("site_id")
            }
        }
    } catch (Exception e) {
        invalidParams = true
        paramsList.add("site_id")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        String systemSite = studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE)
        if (StringUtils.equalsIgnoreCase(siteId.trim(), systemSite)) {
            throw new HttpStatusCodeException(HttpStatus.BAD_REQUEST,format("Deleting system site %s is not allowed", systemSite))
        }
        def context = SiteServices.createContext(applicationContext, request)
        result = SiteServices.deleteSite(context, siteId)
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
