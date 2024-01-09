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

import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.commons.validation.validators.impl.SecurePathValidator
import org.craftercms.engine.exception.HttpStatusCodeException
import org.craftercms.studio.api.v1.exception.ServiceLayerException
import org.craftercms.studio.api.v2.exception.content.ContentExistException
import org.craftercms.studio.controller.rest.ValidationUtils
import scripts.api.ContentServices

def result = [:]
def site = ""
def path = ""
def oldPath = ""
def fileName = ""
def contentType = ""
def draft = "false"
def edit = "false"
def unlock = "true"
def content = null

def isImage = "false";
def allowedWidth = "";
def allowedHeight = "";
def allowLessSize = "";
def systemAsset = null;

def context = ContentServices.createContext(applicationContext, request)

site = request.getParameter("site_id")
if (StringUtils.isEmpty(site)) {
    site = request.getParameter("site")
}
path = request.getParameter("path")
if (!path || path == '') {
    result.code = 400
    result.message = "Path must be provided."
    return result
}
if (!site || site == '') {
    result.code = 400
    result.message = "Site must be provided."
    return result
}

ValidationUtils.validateValue(new SecurePathValidator(), path, 'path');
if (ServletFileUpload.isMultipartContent(request)) {
    def upload = new ServletFileUpload()
    def iterator = upload.getItemIterator(request)
    while(iterator.hasNext()) {
        def item = iterator.next()
        def name = item.getFieldName()
        def stream = item.openStream()
        if (item.isFormField()) {
            switch (name) {
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
            }
        } else {
            fileName = item.getName()
            if (fileName != null) {
                fileName = FilenameUtils.getName(fileName)
            }
            try {
                def writeAssetRes = ContentServices.writeContentAsset(context, site, path, fileName, stream,
                        isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
                if (!writeAssetRes.success) {
                    response.setStatus(500)
                    result.success = false
                    result.message = writeAssetRes.message ?: "Failed to write asset"
                } else if (writeAssetRes.success && writeAssetRes.message.isDeleted()) {
                    response.setStatus(500)
                    result.success = false
                    result.message = "Failed to write asset"
                } else {
                    result = writeAssetRes
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
    oldPath = request.getParameter("oldContentPath")
    fileName = (request.getParameter("fileName")) ? request.getParameter("fileName") : request.getParameter("filename")
    contentType = request.getParameter("contentType")
    edit = request.getParameter("edit")
    draft = request.getParameter("draft")
    unlock = request.getParameter("unlock")
    content = request.getInputStream()

    /** Validate Parameters */
    if (!fileName || fileName == '') {
        result.code = 400;
        result.message = "fileName must be provided."
        return result
    }

    if (oldPath != null && oldPath != "" && (draft==null || draft!=true)) {
        fileName = oldPath.substring(oldPath.lastIndexOf("/") + 1, oldPath.length())
        result.result = ContentServices.writeContentAndRename(context, site, oldPath, path, fileName, contentType, content, "true", edit, unlock, true)
    } else {
        if (path.startsWith("/site")) {
            try {
                result.result = ContentServices.writeContent(context, site, path, fileName, contentType, content, "true", edit, unlock)
            } catch (ContentExistException e) {
                response.setStatus(409)
                result.message = e.message
            }
        } else {
            result.result = ContentServices.writeContentAsset(context, site, path, fileName, content,
                isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)

        }
    }
}
return result
