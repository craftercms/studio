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
import org.craftercms.core.util.ExceptionUtils
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException
import org.craftercms.studio.model.rest.publish.PublishPackageResponse
import scripts.api.ContentServices
import org.craftercms.studio.api.v1.exception.ContentNotFoundException
import org.craftercms.commons.validation.ValidationException

def site = request.getParameter("site_id")
def path = request.getParameter("path")
def name = request.getParameter("name")

/** Validate Parameters */
def invalidParams = false
def paramsList = []

// site_id
try {
    if (StringUtils.isEmpty(site)) {
        site = request.getParameter("site")
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    }
} catch (Exception e) {
    invalidParams = true
    paramsList.add("site_id")
}

def result = [:]

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
    return result
}

try {
    def context = ContentServices.createContext(applicationContext, request)
    result.result = ContentServices.renameFolder(site, path, name, context)
} catch (ContentNotFoundException e) {
    response.setStatus(404)
    result.message = "Content does not exist at path '${path}' for site '${site}'".toString()
} catch (ValidationException e) {
    response.setStatus(400)
    result.message = "Invalid parameters"
} catch (Exception e) {
    Exception inQueueException = ExceptionUtils.getThrowableOfType(e, ContentInPublishQueueException.class);
    if (inQueueException == null) {
        response.setStatus(500)
        result.message = "Internal server error"
    } else {
        response.setStatus(409)
        result.message = inQueueException.message
        result.publishingPackages = inQueueException.getPublishPackages()
                .collect { new PublishPackageResponse(it) }
    }
}
return result
