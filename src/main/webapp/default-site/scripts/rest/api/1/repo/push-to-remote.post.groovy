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
import org.springframework.http.HttpStatus
import scripts.api.ContentServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def siteId = parsedReq.site_id
    def remoteName = parsedReq.remote_name
    def remoteBranch = parsedReq.remote_branch

/** Validate Parameters */
    def invalidParams = false
    def paramsList = []

// site_id
    try {
        if (StringUtils.isEmpty(siteId)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

    // remote_name
    try {
        if (StringUtils.isEmpty(remoteName)) {
            invalidParams = true
            paramsList.add("remote_name")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("remote_name")
    }

    // remote_branch
    try {
        if (StringUtils.isEmpty(remoteBranch)) {
            invalidParams = true
            paramsList.add("remote_branch")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("remote_branch")
    }


    if (invalidParams) {
        response.setStatus(HttpStatus.BAD_REQUEST.value())
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        def context = ContentServices.createContext(applicationContext, request)

        try {
            def success = ContentServices.pushToRemote(context, siteId, remoteName, remoteBranch)
            if (success) {
                response.setStatus(HttpStatus.OK.value())
                result.message = "OK"
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                result.message = "Push to remote failed"
            }
        } catch (SiteNotFoundException e) {
            response.setStatus(HttpStatus.NOT_FOUND.value())
            result.message = "Site not found"
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(HttpStatus.BAD_REQUEST.value())
    result.message = "Bad Request"
}
return result
