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
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException
import org.craftercms.studio.api.v1.exception.CmisTimeoutException
import org.craftercms.studio.api.v1.exception.CmisUnavailableException
import scripts.api.CmisServices

def result = [:]
def siteId
def cmisRepoId
def cmisPath
def filename
def content
try {

    if(ServletFileUpload.isMultipartContent(request)) {
        def upload = new ServletFileUpload()
        def iterator = upload.getItemIterator(request)
        while(iterator.hasNext()) {
            def item = iterator.next()
            def name = item.getFieldName()
            def stream = item.openStream()
            if (item.isFormField()) {
                switch (name) {
                    case "site_id":
                        siteId = Streams.asString(stream)
                        break
                    case "cmis_repo_id":
                        cmisRepoId = Streams.asString(stream)
                        break
                    case "cmis_path":
                        cmisPath = Streams.asString(stream)
                        break
                }
            } else {
                filename = item.getName()
                content = stream
                if (filename != null) {
                    filename = FilenameUtils.getName(filename)
                }
                break;
            }
        }

        /** Validate Parameters */
        def invalidParams = false;
        def paramsList = []

// site_id
        try {
            if (StringUtils.isEmpty(siteId)) {
                invalidParams = true
                paramsList.add("site_id")
            }
        } catch (Exception e) {
            invalidParams = true
            paramsList.add("site_id")
        }

// cmis_repo_id
        try {
            if (StringUtils.isEmpty(cmisRepoId)) {
                invalidParams = true
                paramsList.add("cmis_repo_id")
            }
        } catch (Exception e) {
            invalidParams = true
            paramsList.add("cmis_repo_id")
        }

// cmis_path
        try {
            if (StringUtils.isEmpty(cmisPath)) {
                invalidParams = true
                paramsList.add("cmis_path")
            }
        } catch (Exception e) {
            invalidParams = true
            paramsList.add("cmis_path")
        }

        if (invalidParams) {
            response.setStatus(400)
            result.message = "Invalid parameter(s): " + paramsList
        } else {
            def context = CmisServices.createContext(applicationContext, request)
            try {
                CmisServices.uploadContent(context, siteId, cmisRepoId, cmisPath, filename, content)
                result.message = "OK"
            } catch (CmisUnavailableException e) {
                response.setStatus(503)
                result.message = "CMIS Unavailable: \n" + e
            } catch (CmisPathNotFoundException e) {
                response.setStatus(404)
                result.message = "CMIS path not found: \n" + e
            } catch (CmisRepositoryNotFoundException e) {
                response.setStatus(404)
                result.message = "CMIS Repository " + cmisRepo + " not found"
            } catch (CmisObjectNotFoundException e) {
                response.setStatus(404)
                result.message = "CMIS Path Not Found.\n" + e
            } catch (CmisUnauthorizedException e) {
                response.setStatus(401)
                result.message = "CMIS Unauthorized: \nInvalid username or password in CMIS data source configuration, please contact your site administrator.\n" + e
            } catch (CmisConnectionException e) {
                response.setStatus(400)
                result.message = "Invalid CMIS parameter(s). Invalid CMIS data source configuration (hostname, port number, etc.), please contact your site administrator.\n" + e
            } catch (CmisTimeoutException e) {
                response.setStatus(408)
                result.message = "CMIS Timeout: \n" + e
            } catch (Exception e) {
                response.setStatus(500)
                result.message = "Internal server error: \n" + e
            }
        }

    } else {
        response.setStatus(400)
        result.message = "Bad Request"
    }

} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result