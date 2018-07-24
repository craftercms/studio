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

import groovy.json.JsonException
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException
import org.craftercms.studio.api.v1.util.StudioConfiguration
import scripts.api.SiteServices;
import groovy.json.JsonSlurper

import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_PUSH;

def studioConfiguration = applicationContext.get("studioConfiguration")
def result = [:]
try {
    def requestJson = request.reader.text
    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestJson)

    def blueprint = parsedReq.blueprint
    def siteId = parsedReq.site_id
    def sandboxBranch = parsedReq.sandbox_branch
    def description = parsedReq.description
    /** Remote options */
    def useRemote = parsedReq.use_remote
    if (useRemote != null) {
        useRemote = useRemote.toBoolean();
    } else {
        useRemote = false
    }
    def remoteName = parsedReq.remote_name
    def remoteUrl = parsedReq.remote_url
    def remoteBranch = parsedReq.remote_branch
    def singleBranch = parsedReq.single_branch
    if (singleBranch != null) {
        singleBranch = singleBranch.toBoolean();
    } else {
        singleBranch = false
    }
    def authenticationType = parsedReq.authentication_type
    def remoteUsername = parsedReq.remote_username
    def remotePassword = parsedReq.remote_password
    def remoteToken = parsedReq.remote_token
    def remotePrivateKey = parsedReq.remote_private_key
    def createOption = parsedReq.create_option

/** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// blueprint
    try {
        if (!useRemote || (useRemote && StringUtils.equalsIgnoreCase(REMOTE_REPOSITORY_CREATE_OPTION_PUSH,
                createOption))) {
            if (StringUtils.isEmpty(blueprint)) {
                invalidParams = true
                paramsList.add("blueprint")
            }
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("blueprint")
    }

// site_id
    try {
        if (StringUtils.isEmpty(siteId) || StringUtils.length(siteId) > 50) {
            invalidParams = true
            paramsList.add("site_id")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

    // sandbox_branch
    if (StringUtils.isEmpty(sandboxBranch)) {
        sandboxBranch = studioConfiguration.getProperty(StudioConfiguration.REPO_SANDBOX_BRANCH);
    }

    if (useRemote) {
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

        // remote_url
        try {
            if (StringUtils.isEmpty(remoteUrl)) {
                invalidParams = true
                paramsList.add("remote_url")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("remote_url")
        }

        // authentication_type
        try {
            if (StringUtils.isEmpty(authenticationType)) {
                invalidParams = true
                paramsList.add("authentication_type")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("authentication_type")
        }

        // remote_username
        try {
            if (StringUtils.equalsIgnoreCase('basic', authenticationType) &&
                    (StringUtils.isEmpty(remoteUsername))) {
                invalidParams = true
                paramsList.add("remote_username")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("remote_username")
        }

        // remote_password
        try {
            if (StringUtils.equalsIgnoreCase('basic', authenticationType) &&
                    (StringUtils.isEmpty(remotePassword))) {
                invalidParams = true
                paramsList.add("remote_passsword")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("remote_password")
        }

        // remote_token
        try {
            if (StringUtils.equalsIgnoreCase('token', authenticationType) && (StringUtils.isEmpty(remoteToken))) {
                invalidParams = true
                paramsList.add("remote_token")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("remote_token")
        }

        // remote_private_key
        try {
            if (StringUtils.equalsIgnoreCase('key', authenticationType) &&
                    (StringUtils.isEmpty(remotePrivateKey))) {
                invalidParams = true
                paramsList.add("remote_private_key")
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("remote_private_key")
        }

        // create_option
        try {
            if (StringUtils.isEmpty(createOption)) {
                invalidParams = true
                paramsList.add("create_option")
            } else {
            }
        } catch (Exception exc) {
            invalidParams = true
            paramsList.add("create_option")
        }
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else if (useRemote && !StringUtils.equalsAnyIgnoreCase(createOption, REMOTE_REPOSITORY_CREATE_OPTION_CLONE,
            REMOTE_REPOSITORY_CREATE_OPTION_PUSH)) {
        response.setStatus(400)
        result.message = "Invalid create option for remote repository"
    } else {
        def context = SiteServices.createContext(applicationContext, request)
        try {
            if (!useRemote) {
                SiteServices.createSiteFromBlueprint(context, blueprint, siteId, siteId, sandboxBranch, description)
                result.message = "OK"
                def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") +
                        "/api/1/services/api/1/site/get.json?site_id=" + siteId
                response.addHeader("Location", locationHeader)
                response.setStatus(201)
            } else {
                SiteServices.createSiteWithRemoteOption(context, siteId, sandboxBranch, description, blueprint, remoteName,
                        remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
                        remoteToken, remotePrivateKey, createOption)
                result.message = "OK"
                def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") +
                        "/api/1/services/api/1/site/get.json?site_id=" + siteId
                response.addHeader("Location", locationHeader)
                response.setStatus(201)
            }
        } catch (InvalidRemoteRepositoryException e) {
            response.setStatus(400)
            result.message = "Remote repository URL invalid"
        } catch (InvalidRemoteRepositoryCredentialsException e) {
            response.setStatus(400)
            result.message = "Remote repository credentials invalid"
        } catch (BlueprintNotFoundException e) {
            response.setStatus(404)
            result.message = "Blueprint not found"
        } catch (RemoteRepositoryNotFoundException e) {
            response.setStatus(404)
            result.message = "Remote repository not found"
        } catch (RemoteRepositoryNotBareException e) {
            response.setStatus(409)
            result.message = "Remote repository not bare"
        } catch (SiteAlreadyExistsException e) {
            response.setStatus(409)
            def locationHeader = request.getRequestURL().toString().replace(request.getPathInfo().toString(), "") +
                    "/api/1/services/api/1/site/get.json?site_id=" + siteId
            response.addHeader("Location", locationHeader)
            result.message = "Site already exists"
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