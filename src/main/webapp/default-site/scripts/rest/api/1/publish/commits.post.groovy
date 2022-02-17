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
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.CommitNotFoundException
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.DeploymentServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def siteId = parsedReq.site_id
    def environment = parsedReq.environment
    def commitIds = parsedReq.commit_ids
    def comment = parsedReq.comment

/** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// site
    try {
        if (StringUtils.isEmpty(siteId)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

// environment
    try {
        if (StringUtils.isEmpty(environment)) {
            invalidParams = true
            paramsList.add("environment")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("environment")
    }

// commit_ids
    try {
        if (CollectionUtils.isEmpty(commitIds)) {
            invalidParams = true
            paramsList.add("commit_ids")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("commit_ids")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        def context = DeploymentServices.createContext(applicationContext, request)
        try {
            DeploymentServices.publishCommits(context, siteId, environment, commitIds, comment)
            result.message = "OK"
            response.setStatus(200)
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (EnvironmentNotFoundException e) {
            response.setStatus(404)
            result.message = "Environment not found"
        } catch (CommitNotFoundException e) {
            response.setStatus(404)
            result.message = "Commit(s) not found"
        } catch (Exception e) {
            response.setStatus(500)
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request\n" + e
}
return result