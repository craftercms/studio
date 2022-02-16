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
import scripts.api.ContentServices
import javax.imageio.ImageIO

def req = request
def site = params.site_id
def imgPath = params.path
def newName = params.newname
def t = params.t.toInteger()
def l = params.l.toInteger()
def w = params.w.toInteger()
def h = params.h.toInteger()

/** Validate Parameters */
def invalidParams = false
def paramsList = []
def result = [:]

// site_id
try {
    if (StringUtils.isEmpty(site)) {
        site = params.site
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    }
} catch (Exception exc) {
    invalidParams = true
    paramsList.add("site_id")
}

if (invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    def imgToCrop = null
    def imgCropped = null
    def imgCroppedOutStream = null
    def imgCroppedInStream = null

    def imgType = imgPath.substring(imgPath.indexOf(".") + 1)
    def imgPathOnly = imgPath.substring(0, imgPath.lastIndexOf("/"))
    def imgFilename = imgPath.substring(imgPath.lastIndexOf("/") + 1)
    if (newName) {
        imgFilename = newName;
    }

    def context = ContentServices.createContext(applicationContext, request)

    imgToCrop = ImageIO.read(ContentServices.getContentAsStream(site, imgPath, context))

    imgCropped = imgToCrop.getSubimage(l, t, w, h)
    imgCroppedOutStream = new ByteArrayOutputStream()

    ImageIO.write(imgCropped, imgType, imgCroppedOutStream)
    imgCroppedInStream = new ByteArrayInputStream(imgCroppedOutStream.toByteArray())

    result = ContentServices.writeContentAsset(context, site, imgPathOnly, imgFilename, imgCroppedInStream, "true", "", "", "", "false", "true", null)
}
return result
