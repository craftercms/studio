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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import org.craftercms.studio.api.v1.exception.security.AuthenticationException
import scripts.api.DeploymentServices

def result = [:]
try {
    def siteId = params.site_id

    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def siteIdJson = parsedReq.site_id
/** Validate Parameters */
    def invalidParams = false;

    if (StringUtils.isEmpty(siteId)) {
        if (StringUtils.isEmpty(siteIdJson)) {
            invalidParams = true
        } else {
            siteId = siteIdJson;
        }
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter: site_id"
    } else {
        def context = DeploymentServices.createContext(applicationContext, request)
        try {
            def success = DeploymentServices.enablePublishing(context, siteId, true)
            if (success) {
                response.setStatus(200)
                result.message = "OK"
            } else {
                response.setStatus(500)
                result.message = "Internal server error"
            }
        } catch (AuthenticationException e) {
            response.setStatus(401)
            result.message = "Unauthorized"
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (Exception e) {
            response.setStatus(500)
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
