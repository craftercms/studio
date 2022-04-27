/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.engine.exception.HttpStatusCodeException
import org.craftercms.studio.api.v1.exception.ServiceLayerException
import scripts.api.ContentServices

def result = [:]
def site = ""
def path = ""
def oldPath = ""
def fileName = ""
def contentType = ""
def draft = "false"
def createFolders = "true"
def edit = "false"
def unlock = "true"
def content = null

def isImage = "false";
def allowedWidth = "";
def allowedHeight = "";
def allowLessSize = "";
def changeCase = "";
def systemAsset = null;

def context = ContentServices.createContext(applicationContext, request)

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
                case "site":
                    site = Streams.asString(stream)
                    break
                case "path":
                    path = Streams.asString(stream)
                    break
                case "isImage":
                    isImage = Streams.asString(stream)
                    break
                case "allowedWidth":
                    allowedWidth = Streams.asString(stream)
                    break
                case "allowedHeight":
                    allowedHeight = Streams.asString(stream)
                    break
                case "allowLessSize":
                    allowLessSize = Streams.asString(stream)
                    break
                case "changeCase":
                    changeCase = Streams.asString(stream)
                    break
            }
        } else {
            fileName = item.getName()
            if (fileName != null) {
                fileName = FilenameUtils.getName(fileName)
            }
            contentType = item.getContentType()
            try {
                result = ContentServices.writeContentAsset(context, site, path, fileName, stream,
                        isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
                if (result.message.isDeleted()) {
                    response.setStatus(500)
                    result.success = false
                    result.message = "Failed to write asset"
                }
            } catch (ServiceLayerException e) {
                response.setStatus(500)
                result.setMessage = e.getMessage()
            }
        }
    }
    if (!fileName) {
        throw new HttpStatusCodeException(400, "multipart request is missing the file")
    }
} else {
    site = request.getParameter("site_id")
    path = request.getParameter("path")
    oldPath = request.getParameter("oldContentPath")
    fileName = (request.getParameter("fileName")) ? request.getParameter("fileName") : request.getParameter("filename")
    contentType = request.getParameter("contentType")
    createFolders = request.getParameter("createFolders")
    edit = request.getParameter("edit")
    draft = request.getParameter("draft")
    unlock = request.getParameter("unlock")
    content = request.getInputStream()

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
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

    if (!path || path == '') {
        result.code = 400
        result.message = "Path must be provided."
        return result
    }
    else if (!fileName || fileName == '') {
        result.code = 400;
        result.message = "fileName must be provided."
        return result
    }

    if (oldPath != null && oldPath != "" && (draft==null || draft!=true)) {
        fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length())
        result.result = ContentServices.writeContentAndRename(context, site, oldPath, path, fileName, contentType, content, "true", edit, unlock, true)

    } else {
        if(path.startsWith("/site")){
            result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content, "true", edit, unlock)
        }
        else {
            result.result = ContentServices.writeContentAsset(context, site, path, fileName, content,
                isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)

        }
    }
}
return result
